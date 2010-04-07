package org.mobicents.protocols.asn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * Stream to handle BER endcoding of data. Provides primitives encoding. Takes
 * care of encoding
 * 
 * @author abhayani
 * @author baranowb
 */
public class AsnOutputStream extends ByteArrayOutputStream {
	// charset used to encode real data
	private static final String _REAL_BASE10_CHARSET = "US-ASCII";
	// out patterns for bool
	private static final byte _BOOLEAN_POSITIVE = (byte) 0xFF;
	private static final byte _BOOLEAN_NEGATIVE = 0x00;

	// FIXME: check how much we write after each tag?
	// some state
	// private byte length = 0;

	/**
	 * Method used to write tags for common types - be it complex or primitive.
	 * 
	 * @param tagClass
	 * @param primitive
	 * @param value
	 *            - less significant bits(4) are encoded as tag code
	 */
	public void writeTag(int tagClass, boolean primitive, int value) {

		int toEncode = (tagClass & 0x03) << 6;
		toEncode |= (primitive ? 0 : 1) << 5;
		toEncode |= value & 0x1F;
		this.write(toEncode);
	}

	/**
	 * Method used to write tags for common types - be it complex or primitive.
	 * 
	 * @param tagClass
	 * @param primitive
	 * @param value
	 *            - less significant bits(4) are encoded as tag code
	 */
	public void writeTag(int tagClass, boolean primitive, byte[] value) {

		int toEncode = (tagClass & 0x03) << 6;
		toEncode |= (primitive ? 0 : 1) << 5;
		// toEncode |= value & 0x0F;
		// FIXME: add hack here
		this.write(toEncode);
	}

	/**
	 * Writes length in simple or indefinite form
	 * 
	 * @param l
	 */
	public void writeLength(int l) {
		this.write(l & 0xFF);
	}

	public void writeBoolean(boolean value) {
		writeTag(Tag.CLASS_UNIVERSAL, true, Tag.BOOLEAN);
		writeLength(0x01);

		int V = value ? _BOOLEAN_POSITIVE : _BOOLEAN_NEGATIVE;
		this.write(V);
	}

	public void writeNULL() {
		writeTag(Tag.CLASS_UNIVERSAL, true, Tag.NULL);
		writeLength(0x00);
	}

	public void writeInteger(int v) throws IOException {
		// TAG
		this.writeTag(Tag.CLASS_UNIVERSAL, true, Tag.INTEGER);
		// if its positive, we need trailing 0x00
		boolean wasPositive = v > 0;
		if (!wasPositive) {
			v = -v;
		}
		// determine how much we should write :)
		int count = 0;
		if ((v & 0xFF000000) > 0) {
			count = 4;
		} else if ((v & 0x00FF0000) > 0) {
			count = 3;
		} else if ((v & 0x0000FF00) > 0) {
			count = 2;
		} else {
			count = 1;
		}

		// make the proper val;
		if (!wasPositive) {
			v = -v;
		}
		// now we know how much bytes we need from V, for positive with MSB set
		// on MSB-like octet, we need trailing 0x00, this L+1;
		// FIXME: change this, tmp hack.
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(v);
		bb.flip();
		for (int c = 4 - count; c > 0; c--) {
			bb.get();
		}
		byte[] dataToWrite = new byte[count];
		bb.get(dataToWrite);
		if (wasPositive && ((dataToWrite[0] & 0x80) > 0)) {
			this.writeLength(dataToWrite.length + 1);
			this.write(0x00);
		} else {
			this.writeLength(dataToWrite.length);
		}
		this.write(dataToWrite);
	}

	/**
	 * 
	 * @param d
	 *            - string representing double
	 * @param NR
	 *            - NR to be written, must match string content.
	 * @throws AsnException
	 * @throws IOException
	 */
	public void writeReal(String d, int NR) throws AsnException,NumberFormatException, IOException {
		// check?
		Double.parseDouble(d);
		// This is weird, BER does not allow L = 0 for zero on integer, but for
		// real it does.... cmon
		this.writeTag(Tag.CLASS_UNIVERSAL, true, Tag.REAL);
		byte[] encoded = null;
		try {
			encoded = d.getBytes(_REAL_BASE10_CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new AsnException(e);
		}

		// FIXME: add check on length exceeding simple boundry!
		if (encoded.length + 1 > 127) {
			throw new AsnException("Not supported yet, is it even in specs?");
		}

		this.writeLength(encoded.length + 1);

		if (NR > 3 || NR < 1) {
			throw new AsnException("NR is out of range: <0,3>");
		}
		this.write(NR);

		this.write(encoded);

	}

	public void writeReal(double d) throws AsnException, IOException {
		// This is weird, BER does not allow L = 0 for zero on integer, but for
		// real it does.... cmon
		this.writeTag(Tag.CLASS_UNIVERSAL, true, Tag.REAL);
		if (d == 0) {
			this.writeLength(0x00);
			return;
		}

		if (d == Double.POSITIVE_INFINITY) {
			this.writeLength(0x01);
			this.write(0x40);
			return;
		}

		if (d == Double.NEGATIVE_INFINITY) {
			this.writeLength(0x01);
			this.write(0x41);
			return;
		}

		// now that sucky stuff with FF,BB,EE ....
		// see:
		// http://en.wikipedia.org/wiki/Single_precision_floating-point_format
		// : http://en.wikipedia.org/wiki/Double_precision_floating-point_format

		// L: 8 for bits(however we have more +1 since exp and mantisa dont end
		// on octet boundry), 1 for info bits
		this.writeLength(10);
		// get sign;
		long bits = Double.doubleToLongBits(d);
		// get sign bit
		int info = ((int) (bits >> 57)) & 0x40;
		// 10 00 00 01
		// binary+0 sign BB[2] FF[0] EE[2]
		info |= 0x81;

		this.write(info);

		// get 11 bits of exp
		byte[] exp = new byte[2];
		byte[] mantisa = new byte[7];

		// 3 bits
		exp[0] = (byte) (((int) (bits >> 60)) & 0x07);
		exp[1] = (byte) (bits >> 52);
		for (int index = 0; index < 7; index++) {
			mantisa[6 - index] = (byte) (bits >> (index * 8));

		}

		mantisa[0] &= 0x0F;

		this.write(exp);
		this.write(mantisa);

	}
	
	
	public void writeStringOctet( InputStream io ) throws AsnException, IOException
	{
		if (io.available() <= 127) {
			// its simple :
			this.writeTag(Tag.CLASS_UNIVERSAL, true, Tag.STRING_OCTET);
			this.writeLength(io.available());
			byte[] data = new byte[io.available()];
			io.read(data);
			this.write(data);
		}else
		{
			this.writeTag(Tag.CLASS_UNIVERSAL, false, Tag.STRING_OCTET);
			//indefinite
			this.writeLength(0x80);
			//now lets write fractions, 127 octet chunks
			
			
			int count = io.available();
			while( count>0)
			{
				
				byte[] dataChunk = new byte[count > 127?127: count];
				io.read(dataChunk);
				this.writeString(dataChunk, Tag.STRING_OCTET);
				count -= dataChunk.length;
			}
			//terminate complex
			this.write(Tag.NULL_TAG);
			this.write(Tag.NULL_VALUE);
			
			
		}
		
		
	}

	public void writeStringBinary(BitSet bitString) throws AsnException, IOException
	{
		//DONT USE BitSet.size();
		writeStringBinary(bitString,bitString.length(),0);
	}
	/**
	 * 
	 * @param bitString
	 * @param bitNumber
	 * @param startIndex
	 * @throws AsnException
	 * @throws IOException
	 */
	private void writeStringBinary(BitSet bitString, int bitNumber, int startIndex) throws AsnException, IOException {
		

		
		//check if we can write it in simple form
		int octetCount = bitNumber/8;
		int rest = bitNumber%8;
		if(rest!=0)
		{
			octetCount++;
		}
		//126 - cause bit string has one extra octet.
		if(octetCount<=126)
		{
			this.writeTag(Tag.CLASS_UNIVERSAL, true, Tag.STRING_BIT);
			this.writeLength(octetCount+1);
			//the extra octet from bit string
			if(rest == 0)
			{
				this.write(0);
			}else
			{
				this.write(8-rest);
			}

	
			//this will padd unused bits with zeros
			for(int i = 0;i<octetCount;i++)
			{
				byte byteRead = _getByte(startIndex+i*8, bitString);
				this.write(byteRead);
			}
			
			
			
		}else
		{
			
			this.writeTag(Tag.CLASS_UNIVERSAL, false, Tag.STRING_BIT);
			//indefinite
			this.writeLength(0x80);
			int count = octetCount;
			int lastBitIndex = startIndex;
			while( count>0 )
			{
				
				int dataChunkSize = count > 126?126: count;
				
				int localBitNum = dataChunkSize*8;
				if(dataChunkSize != 126)
				{
					localBitNum+=rest;
					if(rest!=0)
					{
						//we need to remove this, since its fake full octet, and we pass bit num.
						localBitNum-=8;
					}
				}
				this.writeStringBinary(bitString,localBitNum,lastBitIndex);
				lastBitIndex+=dataChunkSize*8;
				count -= dataChunkSize;
			}
			//terminate complex
			this.write(Tag.NULL_TAG);
			this.write(Tag.NULL_VALUE);
		}
	}
	/**
	 * Attepts to read up to 8 bits and store into byte. If less is found, only those are returned
	 * @param startIndex
	 * @param set
	 * @return
	 * @throws AsnException 
	 */
	private static byte _getByte(int startIndex, BitSet set) throws AsnException
	{
		
		int count = 8;
		byte data = 0;
		
		if(set.length()-1<startIndex)
		{
			throw new AsnException();
		}
		
		while(count>0)
		{
			if(set.length()-1<startIndex)
			{
				break;
			}else
			{
				boolean lit = set.get(startIndex);
				if(lit)
				{
					data|=(0x01 << (count-1));
				}
				
				startIndex++;
				count--;
			}
			
		}
		return data;
	}
	public void writeStringUTF8(String data) throws AsnException, IOException {
		byte[] dataEncoded = null;
		try {
			dataEncoded = data.getBytes(BERStatics.STRING_UTF8_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new AsnException(e);
		}
		writeString(dataEncoded, Tag.STRING_UTF8);
	}

	public void writeStringIA5(String data) throws AsnException, IOException {
		byte[] dataEncoded = null;
		try {
			dataEncoded = data.getBytes(BERStatics.STRING_IA5_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new AsnException(e);
		}
		writeString(dataEncoded, Tag.STRING_IA5);
	}

	private void writeString(byte[] dataEncoded, int stringTag) throws AsnException, IOException {
		
		if (dataEncoded.length <= 127) {
			// its simple :
			this.writeTag(Tag.CLASS_UNIVERSAL, true, stringTag);
			this.writeLength(dataEncoded.length);
			this.write(dataEncoded);
		}else
		{
			this.writeTag(Tag.CLASS_UNIVERSAL, false, stringTag);
			//indefinite
			this.writeLength(0x80);
			//now lets write fractions, 127 octet chunks
			
			ByteArrayInputStream bis = new ByteArrayInputStream(dataEncoded);
			int count = bis.available();
			while( count>0)
			{
				
				byte[] dataChunk = new byte[count > 127?127: count];
				bis.read(dataChunk);
				this.writeString(dataChunk, stringTag);
				count -= dataChunk.length;
			}
			//terminate complex
			this.write(Tag.NULL_TAG);
			this.write(Tag.NULL_VALUE);
			
			
		}

	}

	// FIXME: we need this also?
	// public void writeReal(float d) {
	//
	// }

}
