Saph for Java
=============

Saph is the Stupid Algorithm for Password Hashing. This is the Java implementation.

For more information about Saph, go to [its specification](https://github.com/socram8888/saph/blob/master/README.md).

Usage
-----

The class `pet.orca.saph.Saph` contains the implementation. It may be used as follows:

```java
Saph saph = new Saph();
saph.add("pepper");
saph.add("username");
saph.add("password");
byte[] hash = saph.hash();
```

The package [is available in Maven Central](https://mvnrepository.com/artifact/pet.orca/saph) and may be included as:

```xml
<dependencies>
	...
	<dependency>
		<groupId>pet.orca</groupId>
		<artifactId>saph</artifactId>
		<version>1.0</version>
	</dependency>
	...
</dependencies>
```
