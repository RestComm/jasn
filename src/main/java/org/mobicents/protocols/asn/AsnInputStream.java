package org.mobicents.protocols.asn;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * 
 * @author amit bhayani
 * @author baranowb
 */
@SuppressWarnings("unused")
public class AsnInputStream extends FilterInputStream {

	private static final int DATA_BUCKET_SIZE = 1024;


	// TODO : There should be getter / setter for these two?
	private int tagClass = -1;
	private int pCBit = -1;

	public AsnInputStream(InputStream in) {
		super(in);
	}

	@Override
	public int read() throws IOException {
		int i = super.read();
		if (i == -1) {
			throw new EOFException("AsnInputStream has reached the end");
		}
		return i;
	}

	public int readTag() throws IOException {
		// Tag tag = null;
		byte b = (byte) this.read();

		tagClass = (b & Tag.CLASS_MASK) >> 6;
		pCBit = (b & Tag.PC_MASK) >> 5;

		int value = b & Tag.NUMBER_MASK;

		// For larger tag values, the first octet has all ones in bits 5 to 1, and the tag value is then encoded in
		// as many following octets as are needed, using only the least significant seven bits of each octet,
		// and using the minimum number of octets for the encoding. The most significant bit (the "more"
		// bit) is set to 1 in the first following octet, and to zero in the last.
		if (value == Tag.NUMBER_MASK) {
			byte temp;
			value = 0;
			do {
				temp = (byte) this.read();
				value = (value << 7) | (0x7F & temp);
			} while (0 != (0x80 & temp));
		}

		// tag = new Tag(tagClass, (pCBit == 0 ? true : false), value);
		return value;
	}

	public int readLength() throws IOException {
		int length = -1;

		byte b = (byte) this.read();

		// This is short form. The short form can be used if the number of octets in the Value part is less than or
		// equal to 127, and can be used whether the Value part is primitive or constructed. This form is identified by
		// encoding bit 8 as zero, with the length count in bits 7 to 1 (as usual, with bit 7 the most significant bit
		// of the length).
		if ((b & 0x80) == 0) {
			return b;
		}

		// This is indefinite form. The indefinite form of length can only be used (but does not have to be) if the V
		// part is constructed, that
		// is to say, consists of a series of TLVs. In the indefinite form of length the first bit of the first octet is
		// set to 1, as for the long form, but the value N is set to zero.
		b = (byte) (b & 0x7F);
		if (b == 0) {
			return 0x80;
		}

		// If bit 8 of the first length octet is set to 1, then we have the long form of length. In long form, the first
		// octet encodes in its remaining seven bits a value N which is the length of a series of octets that themselves
		// encode the length of the Value part.
		byte temp;
		for (int i = 0; i < b; i++) {
			temp = (byte) this.read();
			length = (length << 8) | (0x00FF & temp);
		}

		return length;
	}

	/**
	 * Reads and converts for {@link Tag#BOOLEAN} primitive
	 * 
	 * @return
	 * @throws AsnException
	 * @throws IOException
	 */
	public boolean readBoolean() throws AsnException, IOException {
		int tagValue = this.readTag();

		if (tagValue != Tag.BOOLEAN) {
			throw new AsnException("Tag doesn't represent Boolean. Tag Class " + this.tagClass + " P/C falg "
					+ this.pCBit + " Tag Value " + tagValue);
		}
		byte temp;

		int length = readLength();
		if (length != 1)
			throw new AsnException("Boolean length should be 1 but is " + length);
		temp = (byte) this.read();

		// If temp is not zero stands for true irrespective of actual Value
		return (temp != 0);
	}

	/**
	 * Reads and converts for {@link Tag#INTEGER} primitive
	 * 
	 * @param length
	 * @return
	 * @throws AsnException
	 * @throws IOException
	 */
	public long readInteger() throws AsnException, IOException {

		int tagValue = this.readTag();

		if (tagValue != Tag.INTEGER) {
			throw new AsnException("Tag doesn't represent Integer. Tag Class " + this.tagClass + " P/C falg "
					+ this.pCBit + " Tag Value " + tagValue);
		}

		long value = 0;
		byte temp;

		int length = this.readLength();

		if (length == -1)
			throw new AsnException("Length for Integer is -1");

		if (length == 0)
			return value;

		temp = (byte) this.read();
		value = temp;

		for (int i = 0; i < length - 1; i++) {
			temp = (byte) this.read();
			value = (value << 8) | (0x00FF & temp);
		}

		return value;
	}

	/**
	 * Reads and converts for {@link Tag#REAL} primitive
	 * 
	 * @param base10 -
	 *            <ul>
	 *            <li><b>true</b> if real is encoded in base of 10 ( decimal )</li>
	 *            <li><b>false</b> if real is encoded in base of 2 ( binary )</li>
	 *            </ul>
	 * @return
	 * @throws AsnException
	 * @throws IOException
	 */
	public double readReal() throws AsnException, IOException {
		// see: http://en.wikipedia.org/wiki/Single_precision_floating-point_format
		// : http://en.wikipedia.org/wiki/Double_precision_floating-point_format
		int length = readLength();
		// universal part
		if (length == 0) {
			// yeah, nice
			return 0.0;
		}

		if (length == 1) {
			// +INF/-INF
			int b = this.read() & 0xFF;
			if (b == 0x40) {
				return Double.POSITIVE_INFINITY;
			} else if (b == 0x41) {
				return Double.NEGATIVE_INFINITY;
			} else {
				throw new AsnException("Real length indicates positive/negative infinity, but value is wrong: "
						+ Integer.toBinaryString(b));
			}
		}
		int infoBits = this.read();
		// substract on for info bits
		length--;

		// only binary has first bit of info set to 1;
		boolean base10 = (((infoBits >> 7) & 0x01) == 0x00);
		// now the tricky part, this takes into account base10
		if (base10) {
			// encoded as char string
			ByteArrayOutputStream bos = new ByteArrayOutputStream(length);
			this.readIA5String(length, true, bos);
			// IA5 == ASCII...?
			String nrRep = new String(bos.toByteArray(), "US-ASCII");
			// this will swallow NR(1-3) and give proper double :)
			return Double.parseDouble(nrRep);
		} else {
			// encoded binary - mantisa and all that funny digits.
			// the REAL type has been semantically equivalent to the
			// type:
			// [UNIVERSAL 9] IMPLICIT SEQUENCE {
			// mantissa INTEGER (ALL EXCEPT 0),
			// base INTEGER (2|10),
			// exponent INTEGER }
			// sign x N x (2 ^ scale) x (base ^ E); --> base ^ E == 2 ^(E+x) == where x
			int tmp = 0;

			int signBit = (infoBits & BERStatics.REAL_BB_SIGN_MASK) << 1;
			// now lets determine length of e(exponent) and n(positive integer)
			long e = 0;
			int s = (infoBits & BERStatics.REAL_BB_SCALE_MASK) >> 2;

			tmp = infoBits & BERStatics.REAL_BB_EE_MASK;
			if (tmp == 0x0) {
				e = this.read() & 0xFF;
				length--;
				// real representation
			} else if (tmp == 0x01) {
				e = (this.read() & 0xFF) << 8;
				length--;
				e |= this.read() & 0xFF;
				length--;
				if (e > 0x7FF) {

					// to many bits... Double
					throw new AsnException("Exponent part has to many bits lit, allowed are 11, present: "
							+ Long.toBinaryString(e));
				}
				// prepare E to become bits - this may cause loose of data,
				e &= 0x7FF;
			} else {
				// this is too big for java to handle.... we can have up to 11 bits..
				throw new AsnException(
						"Exponent part has to many bits lit, allowed are 11, but stream indicates 3 or more octets");
			}
			// now we may read up to 52bits
			// 7*8 == 56, we need up to 52
			if (length > 7) {
				throw new AsnException("Length exceeds JAVA double mantisa size");
			}

			long n = 0;
			while (length > 0) {
				--length;
				long readV = (((long) this.read() << 32) >>> 32) & 0xFF;

				readV = readV << (length * 8);

				n |= readV;
			}
			// check for possible overflow
			if (n > 4503599627370495L) {
				throw new AsnException("Overflow on mantissa");
			}
			// we have real part, now lets add that scale; this is M x (2^F), which essentialy is bit shift :)
			int shift = (int) Math.pow(2, s) - 1; // -1 for 2, where we dont shift
			n = n << (shift); // this might be bad code.

			// now lets take care of different base, we are base2: base8 == base2^3,base16== base2^4
			int base = (infoBits & BERStatics.REAL_BB_BASE_MASK) >> 4;
			// is this correct?
			if (base == 0x01) {
				e = e * 3; // (2^3)^e
			} else if (base == 0x10) {
				e = e * 4; // (2^4)^e
			}
			// do check again.
			if (e > 0x7FF) {
				// to many bits... Double
				throw new AsnException("Exponent part has to many bits lit, allowed are 11, present: "
						+ Long.toBinaryString(e));
			}

			// double is 8bytes
			byte[] doubleRep = new byte[8];
			// set sign, no need to shift
			doubleRep[0] = (byte) (signBit);
			// now get first 7 bits of e;
			doubleRep[0] |= ((e >> 4) & 0xFF);
			doubleRep[1] = (byte) ((e & 0x0F) << 4);
			// from back its easier
			doubleRep[7] = (byte) n;
			doubleRep[6] = (byte) (n >> 8);
			doubleRep[5] = (byte) (n >> 16);
			doubleRep[4] = (byte) (n >> 24);
			doubleRep[3] = (byte) (n >> 32);
			doubleRep[2] = (byte) (n >> 40);
			doubleRep[1] |= (byte) ((n >> 48) & 0x0F);
			ByteBuffer bb = ByteBuffer.wrap(doubleRep);
			return bb.getDouble();

		}

	}

	// FIXME: this should be "read string" or something, but lest leave it for now
	public void readIA5String(int length, boolean primitive, OutputStream outputStream) throws AsnException,
			IOException {
		if (primitive) {
			this.fillOutputStream(outputStream, length);
		} else {
			if (length != 0x80) {
				throw new AsnException("The length field of Constructed OctetString is not 0x80");
			}
		}
	}

	private int getPadMask(int pad) throws AsnException {
		switch (pad) {
		case 0:
			return 0xFF;
		case 1:
			return 0xFE;
		case 2:
			return 0xFC;
		case 3:
			return 0xF8;
		case 4:
			return 0xF0;
		case 5:
			return 0xE0;
		case 6:
			return 0xC0;
		case 7:
			return 0x80;
		default:
			throw new AsnException("Pading asked for " + pad);
		}

	}

	public void readBitString(OutputStream outputStream) throws AsnException, IOException {

		int length = this.readLength();

		if (this.pCBit == 0) {
			int pad = this.read();

			//TODO We are assuming that there is always pad, even if it is 00. This may not be true for some Constructed
			// BitString where padding is only applied to last TLV. In which case this algo is incorrect
			for (int count = 1; count < (length - 1); count++) {
				int dataByte = this.read();
				outputStream.write(dataByte);
			}

			int lastByte = this.read();

			lastByte &= this.getPadMask(pad);

			outputStream.write(lastByte);

		} else {
			if (length != 0x80) {
				throw new AsnException("The length field of Constructed OctetString is not 0x80");
			}

			int tagValue = -1;
			while ((tagValue = this.readTag()) != 0x0) {
				readBitString(outputStream);
			}
		}

	}

	/**
	 * Reads and converts for {@link Tag#STRING_OCTET} primitive
	 * 
	 * @param length
	 * @param primitive
	 * @param outputStream
	 * @throws AsnException
	 * @throws IOException
	 */
	public void readOctetString(OutputStream outputStream, int tagValue) throws AsnException, IOException {

		// if(tagValue != Tag.STRING_OCTET){
		// throw new AsnException("Tag doesn't represent Octet String. Tag Class "+ this.tagClass + " P/C falg "+
		// this.pCBit +"
		// Tag Value "+tagValue);
		// }

		int length = this.readLength();

		if (this.pCBit == 0) {
			this.fillOutputStream(outputStream, length);
		} else {
			if (length != 0x80) {
				throw new AsnException("The length field of Constructed OctetString is not 0x80");
			}

			while ((tagValue = this.readTag()) != 0x0) {
				readOctetString(outputStream, tagValue);
			}
		}
	}

	/**
	 * Read and converts(actually does not since its null) for {@link Tag#NULL} primitive
	 */
	public void readNull() throws AsnException, IOException {
		int tagValue = this.readTag();

		int length = readLength();
		if (length != 0)
			throw new AsnException("Null length should be 0 but is " + length);

		// and thats it. Null has no V part. Its encoded as follows:
		// T[0000 0101] L[0000 0000] V[]
	}

	// private helper methods -------------------------------------------------
	private void fillOutputStream(OutputStream stream, int length) throws AsnException, IOException {
		byte[] dataBucket = new byte[DATA_BUCKET_SIZE];
		int readCount;

		while (length != 0) {
			readCount = read(dataBucket, 0, length < DATA_BUCKET_SIZE ? length : DATA_BUCKET_SIZE);
			if (readCount == -1)
				throw new AsnException("input stream has reached the end");
			stream.write(dataBucket, 0, readCount);
			length -= readCount;
		}
	}

}
