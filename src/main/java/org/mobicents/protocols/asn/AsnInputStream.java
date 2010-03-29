package org.mobicents.protocols.asn;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author amit bhayani
 *
 */
public class AsnInputStream extends FilterInputStream {

	public AsnInputStream(InputStream in) {
		super(in);
	}

	@Override
	public int read() throws IOException {
		int i = super.read();
		if (i == -1) {
			throw new EOFException("input stream has reached the end");
		}
		return i;
	}

	public Tag readTag() throws IOException {
		Tag tag = null;
		byte b = (byte) this.read();

		int tagClass = b & Tag.CLASS_MASK;
		int pCBit = b & Tag.PC_MASK;

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

		tag = new Tag(tagClass, (pCBit == 0 ? true : false), value);
		return tag;
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
			return length;
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
	 * 
	 * @return
	 * @throws AsnException
	 * @throws IOException
	 */
	public boolean readBoolean() throws AsnException, IOException {
		byte temp;

		int length = readLength();
		if (length != 1)
			throw new AsnException("Boolean length should be 1 but is " + length);
		temp = (byte) this.read();

		// If temp is not zero stands for true irrespective of actual Value
		return (temp != 0);
	}

}
