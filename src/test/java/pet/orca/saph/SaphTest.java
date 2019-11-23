/*
 * ©2019 Marcos Del Sol Vives <marcos@orca.pet>
 * SPDX-License-Identifier: WTFPL
 */

package pet.orca.saph;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Test for Saph.
 */
public class SaphTest {
	private static final byte[] TEST_VECTOR_1 = new byte[] {
			(byte) 0x8a, (byte) 0x6d, (byte) 0x4f, (byte) 0x4a,
			(byte) 0x17, (byte) 0x09, (byte) 0x29, (byte) 0xf2,
			(byte) 0x64, (byte) 0xda, (byte) 0xe9, (byte) 0x67,
			(byte) 0x74, (byte) 0x8b, (byte) 0xf9, (byte) 0xf8,
			(byte) 0xf6, (byte) 0x3a, (byte) 0xc7, (byte) 0x32,
			(byte) 0x09, (byte) 0x3e, (byte) 0xd4, (byte) 0x39,
			(byte) 0xc4, (byte) 0x44, (byte) 0xb0, (byte) 0x44,
			(byte) 0x73, (byte) 0x01, (byte) 0x09, (byte) 0xff
	};

	@Test
	public void negativeMemoryTest() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> {
			new Saph(-1, 8);
		});
	}

	@Test
	public void negativeSpaceTest() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> {
			new Saph(8, -1);
		});
	}

	@Test
	public void addAfterHashTest() throws Exception {
		assertThrows(IllegalStateException.class, () -> {
			Saph saph = new Saph();
			saph.add("ok");
			saph.hash();
			saph.add("fail");
		});
	}

	@Test
	public void testVector1Str() throws Exception {
		Saph saph = new Saph(4, 2);
		saph.add("just");
		saph.add("a");
		saph.add("test");
		assertArrayEquals(TEST_VECTOR_1, saph.hash());
	}

	@Test
	public void testVector1Bytes() throws Exception {
		Saph saph = new Saph(4, 2);
		saph.add(new byte[] { (byte) 0x6A, (byte) 0x75, (byte) 0x73, (byte) 0x74 }); // Add "just".
		saph.add(new byte[] { (byte) 0x61 }); // Add "a".
		saph.add(new byte[] { (byte) 0x74, (byte) 0x65, (byte) 0x73, (byte) 0x74 }); // Add "test".
		assertArrayEquals(TEST_VECTOR_1, saph.hash());
	}

	@Test
	public void testVector1ByteChunk() throws Exception {
		byte[] justATest = new byte[] {
				(byte) 0x6A, (byte) 0x75, (byte) 0x73, (byte) 0x74, (byte) 0x61, (byte) 0x74,
				(byte) 0x65, (byte) 0x73, (byte) 0x74
		};
		Saph saph = new Saph(4, 2);
		saph.add(justATest, 0, 4); // Add "just".
		saph.add(justATest, 4, 1); // Add "a".
		saph.add(justATest, 5, 4); // Add "test".
		assertArrayEquals(TEST_VECTOR_1, saph.hash());
	}
}
