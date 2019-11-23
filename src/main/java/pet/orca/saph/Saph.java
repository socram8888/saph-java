/*
 * ©2019 Marcos Del Sol Vives <marcos@orca.pet>
 * SPDX-License-Identifier: WTFPL
 */

package pet.orca.saph;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Saph hasher.
 *
 * <p>This class is a configurable implementation of the Saph hashing algorithm. Parts may be
 * added using the {@code add} methods (such as {@link #add(java.lang.String) } or
 * {@link #add(byte[]) }), and then finalized using the {@link #hash() } method.
 *
 * <p>An example for calculating the test vector 1 would be:
 * <pre>
 * // New instance with memory size = 4 and iterations = 2
 * Saph saph = new Saph(4, 2);
 * saph.add("just");
 * saph.add("a");
 * saph.add("test");
 * byte[] hash = saph.hash();
 * </pre>
 *
 * <p>For convenience, {@code add} methods support return the current instance, for call chaining,
 * so the above code may also be written as:
 * <pre>
 * byte[] hash = (new Saph(4, 2)).add("just").add("a").add("test").hash();
 * </pre>
 *
 * <p>Once the {@link #hash() } method has been called, no further additions are supported.
 */
public class Saph {

	/**
	 * Default memory size, in 64-byte blocks.
	 */
	public static final int DEFAULT_MEMORY_SIZE = 16384;

	/**
	 * Default number of iterations.
	 */
	public static final int DEFAULT_ITERATIONS = 8;

	/**
	 * Chunk length. This matches the chunk length of SHA-256.
	 */
	private static final int CHUNK_LEN = 64;

	/**
	 * Hash length in bytes. 32 bytes for SHA256.
	 */
	private static final int HASH_LEN = 32;

	/**
	 * Required memory size for calculating the password hash, in 64-byte blocks.
	 */
	private final int memorySize;

	/**
	 * Required number of iterations for calculating the password hash.
	 */
	private final int iterations;

	/**
	 * Message digest for hashing memory and initial status.
	 */
	private final MessageDigest currentMd;

	/**
	 * Message digest for hashing parts.
	 */
	private final MessageDigest partsMd;

	/**
	 * Buffer for holding a hash for a part.
	 */
	private final byte[] hashBuffer = new byte[HASH_LEN];

	/**
	 * True if the final hash has been calculated, and its value is in hash buffer.
	 */
	private boolean calculated;

	/**
	 * Creates a new instance with default memory and iteration count.
	 */
	public Saph() {
		this(DEFAULT_MEMORY_SIZE, DEFAULT_ITERATIONS);
	}

	/**
	 * Creates a new instance with the specified memory and iterations count.
	 *
	 * @param memorySize memory size, in 64-byte blocks
	 * @param iterations iteration count
	 * @throws IllegalArgumentException if memory or iterations are less than one
	 */
	public Saph(int memorySize, int iterations) {
		if (memorySize < 1) {
			throw new IllegalArgumentException("Memory size must be at least one block");
		}

		if (iterations < 1) {
			throw new IllegalArgumentException("Iterations must be at least one");
		}

		this.memorySize = memorySize;
		this.iterations = iterations;
		try {
			this.partsMd = MessageDigest.getInstance("SHA-256");
			this.currentMd = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException ex) {
			/*
			 * This should never happen, since SHA-256 is according to Oracle mandatory on all
			 * JVMs since Java 7.
			 */
			throw new RuntimeException("Mandatory SHA-256 not available (non-compliant JVM?)", ex);
		}
	}

	/**
	 * Encodes the given string to UTF-8 and adds it to the Saph hash.
	 *
	 * @param part part to add
	 * @return this instance, for chaining
	 * @throws NullPointerException if part is null
	 * @throws IllegalStateException if the hash has been already calculated
	 */
	public Saph add(String part) {
		if (part == null) {
			throw new IllegalArgumentException("Part may not be null");
		}

		byte[] encoded = part.getBytes(StandardCharsets.UTF_8);
		return realAdd(encoded, 0, encoded.length);
	}

	/**
	 * Adds the given part to the Saph hash.
	 *
	 * @param part part to add
	 * @return this instance, for chaining
	 * @throws NullPointerException if part is null
	 * @throws IllegalStateException if the hash has been already calculated
	 */
	public Saph add(byte[] part) {
		if (part == null) {
			throw new IllegalArgumentException("Part may not be null");
		}

		return realAdd(part, 0, part.length);
	}

	/**
	 * Adds a chunk of the given buffer as a part of the Saph hash.
	 *
	 * @param buffer buffer containing the part
	 * @param offset offset of data in given buffer
	 * @param length chunk length
	 * @return this instance, for chaining
	 * @throws NullPointerException if part is null
	 * @throws IllegalStateException if the hash has been already calculated
	 */
	public Saph add(byte[] buffer, int offset, int length) {
		if (buffer == null) {
			throw new IllegalArgumentException("Part may not be null");
		}
		return realAdd(buffer, offset, length);
	}

	/**
	 * Adds a chunk of the given buffer as a part of the Saph hash, without checking for nulls.
	 *
	 * @param buffer buffer containing the part
	 * @param offset offset of data in given buffer
	 * @param length chunk length
	 * @return this instance, for chaining
	 * @throws IllegalStateException if the hash has been already calculated
	 */
	private Saph realAdd(byte[] buffer, int offset, int length) {
		if (calculated) {
			throw new IllegalStateException("Parts can not be added to a calculated hash");
		}

		// Calculate hash for part
		this.partsMd.reset();
		this.partsMd.update(buffer, offset, length);
		this.partsMd.digest(hashBuffer);

		// Add to running digest
		this.currentMd.update(hashBuffer);

		return this;
	}

	/**
	 * Adds the given part to the Saph hash. 
	 *
	 * @param part the buffer containing the part
	 * @return this instance, for chaining
	 * @throws NullPointerException if part is null
	 * @throws IllegalStateException if the hash has been already calculated
	 */
	public Saph add(ByteBuffer part) {
		if (part == null) {
			throw new IllegalArgumentException("Part may not be null");
		}

		if (calculated) {
			throw new IllegalStateException("Parts can not be added to a calculated hash");
		}

		// Calculate hash for part
		this.partsMd.update(part);
		try {
			this.partsMd.digest(hashBuffer, 0, HASH_LEN);
		} catch (DigestException ex) {
			// I really don't see why or how could a SHA-256 digest fail
			throw new RuntimeException("Unexpected digest exception", ex);
		}

		// Add to running digest
		this.currentMd.update(hashBuffer);

		return this;
	}

	/**
	 * Calculates and returns the hash for the parts.
	 *
	 * <p>This call supports being called several times, but would be only calculated on the first
	 * call.
	 *
	 * @return the hash for the added parts
	 */
	public byte[] hash() {
		if (!calculated) {
			calculateHash();
			calculated = true;
		}

		return hashBuffer;
	}

	/**
	 * Performs the actual calculation of the Saph algorithm.
	 */
	private void calculateHash() {
		// Finalize parts hash
		try {
			this.currentMd.digest(hashBuffer, 0, HASH_LEN);
		} catch (DigestException ex) {
			throw new RuntimeException("Unexpected digest exception", ex);
		}

		// Allocate memory
		byte[] memory = new byte[memorySize * CHUNK_LEN];

		// Indexes for re-sorting
		int[] order = new int[memorySize];

		// Key buffer
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES/CBC/NoPadding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
			// AES-128-CBC is mandatory since Java 7, so this should never ever happen
			throw new RuntimeException(
					"Failed to initialize AES-128-CBC cipher (non-compliant JVM?)", ex);
		}
		
		// Repeat n iterations
		for (int iteration = 0; iteration < iterations; iteration++) {
			/*
			 * Initialize cipher.
			 *
			 * Half of the bytes of the current value are used as AES key, the other half is used
			 * as AES IV.
			 */
			SecretKeySpec key = new SecretKeySpec(hashBuffer, 0, 16, "AES");
			IvParameterSpec iv = new IvParameterSpec(hashBuffer, 16, 16);
			try {
				cipher.init(Cipher.ENCRYPT_MODE, key, iv);
			} catch (InvalidAlgorithmParameterException | InvalidKeyException ex) {
				throw new RuntimeException("Failed to initialized cipher, somehow", ex);
			}

			// Do the actual encryption
			try {
				cipher.update(memory, 0, memory.length, memory);
			} catch (ShortBufferException ex) {
				throw new RuntimeException("Cipher asked for a larger output than input (?)", ex);
			}

			// Initialize indexes
			for (int i = 0; i < order.length; i++) {
				order[i] = i;
			}

			// Calculate hashing order
			for (int i = 0, iPos = 0; i < order.length; i++, iPos += CHUNK_LEN) {
				// Parse low 32 bits of the block as the pair to swap with
				// We need it to be a long, because ints are signed
				long jLong =
						(long) memory[iPos] |
						(long) memory[iPos + 1] << 8 |
						(long) memory[iPos + 2] << 16 |
						(long) memory[iPos + 3] << 24;

				// Cap value to memory length
				int j = (int) (jLong % memory.length);

				// Swap now
				int x = order[i];
				order[i] = order[j];
				order[j] = x;
			}

			// Hash in order
			currentMd.reset();
			for (int i = 0; i < order.length; i++) {
				int pos = order[i] * CHUNK_LEN;
				currentMd.update(memory, pos, CHUNK_LEN);
			}

			// Finalize current iteration
			try {
				currentMd.digest(hashBuffer, 0, HASH_LEN);
			} catch (DigestException ex) {
				throw new RuntimeException("Unexpected digest exception", ex);
			}
		}
	}

	/**
	 * Returns the configured memory size, in 64-byte blocks.
	 * @return the configured memory size, in 64-byte blocks.
	 */
	public int getMemorySize() {
		return memorySize;
	}

	/**
	 * Returns the configured number of iterations.
	 * @return the configured number of iterations.
	 */
	public int getIterations() {
		return iterations;
	}

	/**
	 * Returns true if the hash has been already calculated.
	 * @return true if the hash has been already calculated.
	 */
	public boolean isCalculated() {
		return calculated;
	}

	/**
	 * Returns the calculated hash.
	 * @return the calculated hash.
	 * @throws IllegalStateException if the hash has not been yet calculated.
	 */
	public byte[] getHash() {
		if (!calculated) {
			throw new IllegalStateException("The hash has not been yet calculated");
		}
		return hashBuffer;
	}
}
