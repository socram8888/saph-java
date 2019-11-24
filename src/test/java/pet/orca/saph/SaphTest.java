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

	@Test
	public void pythonTest2() throws Exception {
		byte[] hash = new Saph().add("salt").add("pass").hash();
		byte[] correct = new byte[] {
				(byte) 0xe1, (byte) 0x53, (byte) 0x0b, (byte) 0xa5, (byte) 0x99, (byte) 0xf8,
				(byte) 0x7e, (byte) 0x4e, (byte) 0x62, (byte) 0x56, (byte) 0x0e, (byte) 0x90,
				(byte) 0x8f, (byte) 0x3d, (byte) 0xb8, (byte) 0x33, (byte) 0xcb, (byte) 0xef,
				(byte) 0xa9, (byte) 0x7d, (byte) 0xc6, (byte) 0xcf, (byte) 0x91, (byte) 0x00,
				(byte) 0xd5, (byte) 0x5d, (byte) 0xf5, (byte) 0x7a, (byte) 0x3a, (byte) 0x9e,
				(byte) 0x29, (byte) 0xad
		};
		assertArrayEquals(correct, hash);
	}

	@Test
	public void pythonTest3() throws Exception {
		byte[] hash = new Saph().add("pepper").add("username").add("password").hash();
		byte[] correct = new byte[] {
				(byte) 0x38, (byte) 0xe4, (byte) 0x8e, (byte) 0x2b, (byte) 0x1d, (byte) 0x44,
				(byte) 0x18, (byte) 0x76, (byte) 0x65, (byte) 0x68, (byte) 0xe6, (byte) 0x21,
				(byte) 0x2e, (byte) 0x59, (byte) 0xab, (byte) 0xb9, (byte) 0x61, (byte) 0xb8,
				(byte) 0x76, (byte) 0xb2, (byte) 0xa1, (byte) 0xf7, (byte) 0xf2, (byte) 0x69,
				(byte) 0x75, (byte) 0x2e, (byte) 0xd8, (byte) 0x4a, (byte) 0xfe, (byte) 0x66,
				(byte) 0x37, (byte) 0xc0
		};
		assertArrayEquals(correct, hash);
	}

	@Test
	public void pythonTest4() throws Exception {
		byte[] hash = new Saph().add("qepper").add("username").add("password").hash();
		byte[] correct = new byte[] {
				(byte) 0xbb, (byte) 0x4a, (byte) 0x74, (byte) 0xeb, (byte) 0x50, (byte) 0xba,
				(byte) 0xb2, (byte) 0xe4, (byte) 0xcd, (byte) 0x33, (byte) 0x4d, (byte) 0x93,
				(byte) 0xee, (byte) 0x85, (byte) 0xd8, (byte) 0x4f, (byte) 0x9c, (byte) 0x91,
				(byte) 0xf4, (byte) 0x54, (byte) 0xef, (byte) 0x33, (byte) 0xa6, (byte) 0x8a,
				(byte) 0x48, (byte) 0x44, (byte) 0x08, (byte) 0x74, (byte) 0x7f, (byte) 0x0f,
				(byte) 0x39, (byte) 0x1a
		};
		assertArrayEquals(correct, hash);
	}
}
