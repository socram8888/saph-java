#!/bin/bash -e

POMXLMNS="http://maven.apache.org/POM/4.0.0"

cd "$(dirname "$0")"

# Read version from pom
snapshot=$(xmlstarlet sel -N pom="$POMXLMNS" -t -m "//pom:project/pom:version[1]" -v . "pom.xml")

# Remove -SNAPSHOT
version="${snapshot%-SNAPSHOT}"

echo "Project version: $snapshot"

if [ "$version" == "$snapshot" ]; then
	echo "Project is not a SNAPSHOT. Nothing to do."
	exit 0
fi

iswineol=false
if [ $(cat "pom.xml" | tr -dc "\r" | wc -c) -gt 0 ]; then
	echo "WARNING: pom file uses Windows end of line"
	iswineol=true
fi

echo "Frozen version: $version"
xmlstarlet ed -O --pf --inplace -N pom="$POMXLMNS" -u "//pom:project/pom:version[1]" -v "$version" "pom.xml"

echo "Freezing dependencies:"

depPos=1
xmlstarlet sel -N pom="$POMXLMNS" -t \
		-m "//pom:project/pom:dependencies/pom:dependency" \
		-v "pom:groupId" -n \
		-v "pom:artifactId" -n \
		-v "pom:version" -n \
		"pom.xml" |
while read -r depGroup; do
	read -r depArtifact
	read -r depVersion

	frozenDepVersion="${depVersion%-SNAPSHOT}"
	if [ "$depVersion" != "$frozenDepVersion" ]; then
		echo " - $depGroup:$depArtifact: $depVersion -> $frozenDepVersion"
		xmlstarlet ed -O --pf --inplace -N pom="$POMXLMNS" -u "//pom:project/pom:dependencies/pom:dependency[$depPos]/pom:version" -v "$frozenDepVersion" "pom.xml"
	fi

	depPos=$((depPos+1))
done

echo "Done"

if $iswineol; then
	echo "Converting file back to Windows EOL"
	unix2dos "pom.xml"
fi

echo "Testing compilation"
mvn -P release clean test verify
echo "Done"

while true; do
	git diff

	echo -n "Do these changes look alright? (Yes/No/Again): "
	read reply

	case $reply in
		Y*|y*)
			break
			;;

		N*|n*)
			echo "Aborting"
			git checkout -- pom.xml
			exit 0
	esac
done

echo "Creating commit"
git commit -a -m "Version $version"

echo "Creating tag"
git tag -a -m "Version $version" "$version"

echo "Uploading"
git push --follow-tags
