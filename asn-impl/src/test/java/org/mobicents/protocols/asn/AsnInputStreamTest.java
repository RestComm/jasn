package org.mobicents.protocols.asn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.BitSet;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author amit bhayani
 * @author baranowb
 * @author sergey vetyutnev
 */
public class AsnInputStreamTest extends TestCase {

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() {
	}


	@Test
	public void testTag() throws Exception {
		byte[] data = new byte[] { (byte)0xBF, (byte)0x87, (byte)0x68 };
		AsnInputStream asnIs = new AsnInputStream(data);
		int tag = asnIs.readTag();
		
		assertEquals(1000, tag);
		assertEquals(Tag.CLASS_CONTEXT_SPECIFIC, asnIs.getTagClass());
		assertEquals(false, asnIs.isTagPrimitive());
	}
	
	@Test
	public void testReadSequence() throws Exception {
		
		AsnInputStream asnIs = new AsnInputStream(definiteSeqData());
		int tag = asnIs.readTag();
		AsnInputStream asnIs2 = asnIs.readSequenceStream();
		testSeqData(tag, asnIs2, asnIs);
		
		asnIs = new AsnInputStream(definiteSeqData());
		tag = asnIs.readTag();
		int length = asnIs.readLength();
		asnIs2 = asnIs.readSequenceStreamData(length);
		testSeqData(tag, asnIs2, asnIs);
		
		asnIs = new AsnInputStream(indefiniteSeqData());
		tag = asnIs.readTag();
		asnIs2 = asnIs.readSequenceStream();
		testSeqData(tag, asnIs2, asnIs);

		asnIs = new AsnInputStream(definiteSeqData());
		tag = asnIs.readTag();
		byte[] bfRes = asnIs.readSequence();
		testSeqData(tag, bfRes, asnIs);

		asnIs = new AsnInputStream(definiteSeqData());
		tag = asnIs.readTag();
		length = asnIs.readLength();
		bfRes = asnIs.readSequenceData(length);
		testSeqData(tag, bfRes, asnIs);

		asnIs = new AsnInputStream(indefiniteSeqData());
		tag = asnIs.readTag();
		bfRes = asnIs.readSequence();
		testSeqData(tag, bfRes, asnIs);

	}
	
	private void testSeqData(int tag, AsnInputStream asnIs2, AsnInputStream asnIs) throws IOException {

		assertEquals(0, asnIs.available());

		assertEquals(Tag.SEQUENCE, tag);

		assertTrue(5 == asnIs2.available());

		assertEquals(Tag.STRING_OCTET, asnIs2.read());
		assertEquals(3, asnIs2.read());
		assertEquals(1, asnIs2.read());
		assertEquals(2, asnIs2.read());
		assertEquals(3, asnIs2.read());
	}
	
	private void testSeqData(int tag, byte[] data, AsnInputStream asnIs) throws IOException {

		assertEquals(0, asnIs.available());

		assertEquals(Tag.SEQUENCE, tag);

		assertTrue(5 == data.length);

		assertEquals(Tag.STRING_OCTET, data[0]);
		assertEquals(3, data[1]);
		assertEquals(1, data[2]);
		assertEquals(2, data[3]);
		assertEquals(3, data[4]);
	}
	
	@Test
	public void testAdvanceElement() throws Exception {
		
		AsnInputStream asnIs = new AsnInputStream(definiteSeqData());
		int tag = asnIs.readTag();
		asnIs.advanceElement();
		assertEquals(16, tag);
		assertEquals(0, asnIs.available());

		asnIs = new AsnInputStream(definiteSeqData());
		tag = asnIs.readTag();
		int length = asnIs.readLength(); 
		asnIs.advanceElementData(length);
		assertEquals(0, asnIs.available());

		asnIs = new AsnInputStream(indefiniteSeqData());
		tag = asnIs.readTag();
		asnIs.advanceElement();
		assertEquals(0, asnIs.available());

		asnIs = new AsnInputStream(indefiniteSeqData());
		tag = asnIs.readTag();
		length = asnIs.readLength(); 
		asnIs.advanceElementData(length);
		assertEquals(0, asnIs.available());
	}
	
	private byte[] definiteSeqData() {
		return new byte[] { Tag.SEQUENCE, 5, Tag.STRING_OCTET, 3, 1, 2, 3 };
	}
	
	private byte[] indefiniteSeqData() {
		return new byte[] { Tag.SEQUENCE, (byte)0x80, Tag.STRING_OCTET, 3, 1, 2, 3, 0, 0 };
	}

	@Test
	public void testReadIndefinite() throws Exception {
		byte[] data = new byte[] { (byte)0x80, (byte)0x80, 0x09, (byte)0x96, 0x02, 0x24, (byte)0x80,
				0x03, 0x00, (byte)0x80, 0x00, (byte)0xf2, (byte)0x81, 0x07, (byte)0x91, 0x13, 0x26,
				(byte)0x98, (byte)0x86, 0x03, (byte)0xf0, 0x00, 0x00 };
		
		AsnInputStream asnIs = new AsnInputStream(data);
		int length = asnIs.readLength();
		assertTrue(Tag.Indefinite_Length == length);
		byte[] indefiniteData = asnIs.readIndefinite();
		assertTrue(20 == indefiniteData.length);

		asnIs = new AsnInputStream(data);
		AsnInputStream asnIs2 = asnIs.readSequenceStream();
		assertTrue(20 == asnIs2.available());

		asnIs = new AsnInputStream(data);
		byte[] bfRes = asnIs.readSequence();
		assertTrue(20 == bfRes.length);

	}

	@Test
	public void testNull() throws Exception {
		byte[] data = new byte[] { 5, 0 };
		AsnInputStream asnIs = new AsnInputStream(data);
		int tag = asnIs.readTag();
		asnIs.readNull();

		assertEquals(Tag.NULL, tag);
		assertEquals(Tag.CLASS_UNIVERSAL, asnIs.getTagClass());
		assertEquals(true, asnIs.isTagPrimitive());
		assertEquals(0, asnIs.available());
	}

	@Test
	public void testBool() throws Exception {
		byte[] data = new byte[] { 1, 1, (byte)0xFF };
		AsnInputStream asnIs = new AsnInputStream(data);
		int tag = asnIs.readTag();
		boolean res = asnIs.readBoolean();

		assertEquals(Tag.BOOLEAN, tag);
		assertEquals(Tag.CLASS_UNIVERSAL, asnIs.getTagClass());
		assertEquals(true, asnIs.isTagPrimitive());
		assertEquals(0, asnIs.available());
		assertEquals(true, res);
	}

	@Test
	public void testInteger() throws Exception {

		// Test -ve integer -128
		byte[] data = new byte[] { 0x2, 0x1, (byte) 0x80 };
		AsnInputStream asnIs = new AsnInputStream(data);
		int tag = asnIs.readTag();
		assertEquals(Tag.INTEGER, tag);
		long value = asnIs.readInteger();
		assertEquals(-128, value);

		// Test -ve integer 128
		data = new byte[] { 0x2, 0x2, 0x0, (byte) 0x80 };
		asnIs = new AsnInputStream(data);
		tag = asnIs.readTag();
		assertEquals(Tag.INTEGER, tag);
		int length = asnIs.readLength();
		value = asnIs.readIntegerData(length);
		assertEquals(128, value);

		// Test -ve integer 127
		data = new byte[] { 0x2, 0x1, (byte) 0x7F };
		asnIs = new AsnInputStream(data);
		tag = asnIs.readTag();
		assertEquals(Tag.INTEGER, tag);
		length = asnIs.readLength();
		value = asnIs.readIntegerData(length);
		assertEquals(127, value);

		// Test -ve integer -65536
		byte[] b = this.intToByteArray(-65536);

		System.err.println(Integer.toBinaryString(-65536));
		System.err.println("000000000000000" + Integer.toBinaryString(65536));

		data = new byte[] { 0x2, 0x3, b[1], b[2], b[3] };

		asnIs = new AsnInputStream(data);
		tag = asnIs.readTag();
		assertEquals(Tag.INTEGER, tag);
		value = asnIs.readInteger();

		assertEquals(-65536, value);

		// Test +ve integer 797979
		b = this.intToByteArray(797979);

		data = new byte[] { 0x2, 0x3, b[1], b[2], b[3] };

		asnIs = new AsnInputStream(data);
		tag = asnIs.readTag();
		assertEquals(Tag.INTEGER, tag);
		value = asnIs.readInteger();

		assertEquals(797979, value);

	}

	private byte[] intToByteArray(int value) {

		System.out.println("binary value = " + Integer.toBinaryString(value));

		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++) {
			int offset = (b.length - 1 - i) * 8;
			b[i] = (byte) ((value >>> offset) & 0xFF);
			System.out.println("byte for " + i + " is " + b[i]);
		}
		return b;
	}

	@Test
	public void testBitStringPrimitive() throws Exception {
		byte[] data = new byte[] { 0x03, 0x04, 0x02, (byte) 0xF0, (byte) 0xF0,
				(byte) 0xF4 };

		AsnInputStream asnIs = new AsnInputStream(data);

		BitSet bitSet = new BitSet();

		int tagValue = asnIs.readTag();
		assertEquals(Tag.STRING_BIT, tagValue);
		bitSet = asnIs.readBitString();

		// f0f0f4 is 111100001111000011110100 reduce 02 bits so total length is
		// 22
		assertEquals(22, bitSet.length());
		assertTrue(bitSet.get(0));
		assertTrue(bitSet.get(1));
		assertTrue(bitSet.get(2));
		assertTrue(bitSet.get(3));

		assertFalse(bitSet.get(4));
		assertFalse(bitSet.get(5));
		assertFalse(bitSet.get(6));
		assertFalse(bitSet.get(7));

		assertTrue(bitSet.get(8));
		assertTrue(bitSet.get(9));
		assertTrue(bitSet.get(10));
		assertTrue(bitSet.get(11));

		assertFalse(bitSet.get(12));
		assertFalse(bitSet.get(13));
		assertFalse(bitSet.get(14));
		assertFalse(bitSet.get(15));

		assertTrue(bitSet.get(16));
		assertTrue(bitSet.get(17));
		assertTrue(bitSet.get(18));
		assertTrue(bitSet.get(19));

		assertFalse(bitSet.get(20));

		assertTrue(bitSet.get(21));

	}

	@Test
	public void testBitStringPrimitiveData() throws Exception {
		byte[] data = new byte[] { 0x03, 0x04, 0x02, (byte) 0xF0, (byte) 0xF0,
				(byte) 0xF4 };

		AsnInputStream asnIs = new AsnInputStream(data);

		int tagValue = asnIs.readTag();
		int length = asnIs.readLength();
		BitSet bitSet = asnIs.readBitStringData(length);
		assertEquals(Tag.STRING_BIT, tagValue);

		// f0f0f4 is 111100001111000011110100 reduce 02 bits so total length is
		// 22
		assertEquals(22, bitSet.length());
		assertTrue(bitSet.get(0));
		assertTrue(bitSet.get(1));
		assertTrue(bitSet.get(2));
		assertTrue(bitSet.get(3));

		assertFalse(bitSet.get(4));
		assertFalse(bitSet.get(5));
		assertFalse(bitSet.get(6));
		assertFalse(bitSet.get(7));

		assertTrue(bitSet.get(8));
		assertTrue(bitSet.get(9));
		assertTrue(bitSet.get(10));
		assertTrue(bitSet.get(11));

		assertFalse(bitSet.get(12));
		assertFalse(bitSet.get(13));
		assertFalse(bitSet.get(14));
		assertFalse(bitSet.get(15));

		assertTrue(bitSet.get(16));
		assertTrue(bitSet.get(17));
		assertTrue(bitSet.get(18));
		assertTrue(bitSet.get(19));

		assertFalse(bitSet.get(20));

		assertTrue(bitSet.get(21));

	}

	@Test
	public void testBitStringConstructed() throws Exception {

		byte[] data = new byte[] { 0x23, (byte) 0x80, 0x03, 0x03, 0x00,
				(byte) 0xF0, (byte) 0xF0, 0x03, 0x02, 0x02, (byte) 0xF4, 0x00, 0x00 };

		_testBitStringConstructed(data);

		data = new byte[] { 0x23, 0x09, 0x03, 0x03, 0x00,
				(byte) 0xF0, (byte) 0xF0, 0x03, 0x02, 0x02, (byte) 0xF4 };

		_testBitStringConstructed(data);

	}

	private void _testBitStringConstructed(byte[] data) throws IOException, AsnException {
		AsnInputStream asnIs = new AsnInputStream(data);

		// here we have to explicitly read the Tag
		int tagValue = asnIs.readTag();
		assertEquals(Tag.STRING_BIT, tagValue);
		BitSet bitSet = asnIs.readBitString();

		// f0f0f4 is 111100001111000011110100 reduce 02 bits so total length is
		// 22
		assertEquals(22, bitSet.length());
		assertTrue(bitSet.get(0));
		assertTrue(bitSet.get(1));
		assertTrue(bitSet.get(2));
		assertTrue(bitSet.get(3));

		assertFalse(bitSet.get(4));
		assertFalse(bitSet.get(5));
		assertFalse(bitSet.get(6));
		assertFalse(bitSet.get(7));

		assertTrue(bitSet.get(8));
		assertTrue(bitSet.get(9));
		assertTrue(bitSet.get(10));
		assertTrue(bitSet.get(11));

		assertFalse(bitSet.get(12));
		assertFalse(bitSet.get(13));
		assertFalse(bitSet.get(14));
		assertFalse(bitSet.get(15));

		assertTrue(bitSet.get(16));
		assertTrue(bitSet.get(17));
		assertTrue(bitSet.get(18));
		assertTrue(bitSet.get(19));

		assertFalse(bitSet.get(20));

		assertTrue(bitSet.get(21));
	}

	@Test
	public void testOctetStringPrimitive() throws Exception {
		byte[] data = new byte[] { 0x4, 0x10, 0x00, 0x11, 0x22, 0x33, 0x44,
				0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA,
				(byte) 0xBB, (byte) 0XCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };

		AsnInputStream asnIs = new AsnInputStream(data);
		// here we have to explicitly read the Tag
		int tagValue = asnIs.readTag();
		assertEquals(Tag.STRING_OCTET, tagValue);
		byte[] resultData = asnIs.readOctetString();
		for (int i = 0; i < resultData.length; i++) {
			assertTrue(resultData[i] == data[i + 2]);
		}

		asnIs = new AsnInputStream(data);
		// here we have to explicitly read the Tag
		tagValue = asnIs.readTag();
		assertEquals(Tag.STRING_OCTET, tagValue);
		int length = asnIs.readLength();
		resultData = asnIs.readOctetStringData(length);
		for (int i = 0; i < resultData.length; i++) {
			assertTrue(resultData[i] == data[i + 2]);
		}
	}

	@Test
	public void testOctetStringConstructed() throws Exception {
		
		// indefinite length
		byte[] data = new byte[] { 0x24, (byte) 0x80, 0x04, 0x08, 0x00, 0x11,
				0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x04, 0x08, (byte) 0x88,
				(byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0XCC,
				(byte) 0xDD, (byte) 0xEE, (byte) 0xFF, 0x00, 0x00 };

		byte[] octetString = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55,
				0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB,
				(byte) 0XCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };

		AsnInputStream asnIs = new AsnInputStream(data);
		int tagValue = asnIs.readTag();
		assertEquals(Tag.STRING_OCTET, tagValue);
		byte[] resultData = asnIs.readOctetString();
		for (int i = 0; i < resultData.length; i++) {
			assertTrue(resultData[i] == octetString[i]);
		}
		
		// definite length
		byte[] data2 = new byte[] { 0x24, 20, 0x04, 0x08, 0x00, 0x11,
				0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x04, 0x08, (byte) 0x88,
				(byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0XCC,
				(byte) 0xDD, (byte) 0xEE, (byte) 0xFF };
		asnIs = new AsnInputStream(data2);
		tagValue = asnIs.readTag();
		assertEquals(Tag.STRING_OCTET, tagValue);
		resultData = asnIs.readOctetString();
		for (int i = 0; i < resultData.length; i++) {
			assertTrue(resultData[i] == octetString[i]);
		}
	}

	// those two are completly made up, couldnt find trace
	@Test
	public void testRealBinary() throws Exception {

		// 118.625
		byte[] binary1 = new byte[] {
		// TAG;
				(Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
						| Tag.REAL,
				// Length - this is definite - we dont handle more? do we?
				0x0A,// 1(info bits) 2(exponent 7(mantisa)
				// info bits (binary,sign,BB,FF,EE)
				(byte) (0x80 | (0x0 << 6) | 0x00 << 4 | 0x01), // 1 0 00(base2)
																// 00(scale = 0)
																// 01 ( two
																// octets for
				// exponent
				// exponent, two octets
				// 100 00000101
				0x04, 0x05,
				// mantisa
				// 1101 10101000 00000000 00000000 00000000 00000000 00000000

				0x0D, (byte) 0xA8, 0x00, 0x00, 0x00, 0x00, 0x00 };

		AsnInputStream asnIs = new AsnInputStream(binary1);
		int tagValue = asnIs.readTag();
		assertEquals(Tag.REAL, tagValue);
		
		// assertEquals(Tag.CLASS_UNIVERSAL, Tag.getTagClass(tagValue));
		// assertTrue(Tag.isPrimitive(tagValue);
		// assertEquals(Tag.REAL, Tag.getType(tagValue));
		double d = asnIs.readReal();
		assertEquals("Decoded value is not proper!!", 118.625d, d);
		// -118.625
		byte[] binary2 = new byte[] {
		// TAG;
				(Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
						| Tag.REAL,
				// Length - this is definite - we dont handle more? do we?
				0x0A,// 1(info bits) 2(exponent 7(mantisa)
				// info bits (binary,sign,BB,FF,EE)
				(byte) (0x80 | (0x1 << 6) | 0x00 << 4 | 0x01), // 1 0 00(base2)
																// 00(scale = 0)
																// 01 ( two
																// octets for
				// exponent
				// exponent, two octets
				// 100 00000101
				0x04, 0x05,
				// mantisa
				// 1101 10101000 00000000 00000000 00000000 00000000 00000000

				0x0D, (byte) 0xA8, 0x00, 0x00, 0x00, 0x00, 0x00 };

		asnIs = new AsnInputStream(binary2);
		tagValue = asnIs.readTag();

		// assertEquals(Tag.CLASS_UNIVERSAL, Tag.getTagClass(tagValue));
		// assertTrue(Tag.isPrimitive(tagValue);
		// assertEquals(Tag.REAL, Tag.getType(tagValue));
		d = asnIs.readReal();
		assertEquals("Decoded value is not proper!!", -118.625d, d);

	}

	@Test
	public void testRealBase10() throws Exception {
		// TODO get real data trace?
		String[] digs = new String[] { "   0004902", "  +0004902", " -4902",
				"4902.00", "4902.", ".5", " 0.3E-04", "-2.8E+000000",
				"   000004.50000E123456789", "+5.6e+03", "+0.56E+4" };

		for (int index = 0; index < digs.length; index++) {
			double d = Double.parseDouble(digs[index]);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(20);
			// write tag
			bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
					| Tag.REAL);
			// length is unknown for a bit, lets do the math
			byte[] data = digs[index].getBytes("US-ASCII");
			bos.write(1 + data.length); // 1 for 2 bits for base10 indicator and
										// 6 bits for NR
			int NR = 0; // for now it is ignored

			if (index <= 2) {
				// NR1
				NR = BERStatics.REAL_NR1;
			} else if (index <= 5) {
				NR = BERStatics.REAL_NR2;
			} else {
				NR = BERStatics.REAL_NR3;
			}
			bos.write(((0x00 << 6)) | (NR));
			bos.write(data);
			byte[] bb = bos.toByteArray();
			AsnInputStream asnIs = new AsnInputStream(bb);
			int tagValue = asnIs.readTag();
			assertEquals(Tag.REAL, tagValue);

			// assertEquals(Tag.CLASS_UNIVERSAL, Tag.getTagClass(tagValue));
			// assertTrue(Tag.isPrimitive(tagValue);
			// assertEquals(Tag.REAL, Tag.getType(tagValue));
			double dd = asnIs.readReal();
			assertEquals("Decoded value is not proper!!", d, dd);
		}

	}

	// IA5 data taken from table on page: http://www.zytrax.com/tech/ia5.html
	@Test
	public void testIA5StringDefiniteShort() throws Exception {
		// ACEace$}
		String dataString = "ACEace$}";
		byte[] data = new byte[] { 0x41, 0x43, 0x45, 0x61, 0x63, 0x65, 0x24,
				0x7D

		};
		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		// write tag
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_IA5);
		bos.write(data.length);
		bos.write(data);

		byte[] bb = bos.toByteArray();
		AsnInputStream asnIs = new AsnInputStream(bb);
		int tag = asnIs.readTag();
		assertEquals(Tag.CLASS_UNIVERSAL, asnIs.getTagClass());
		assertEquals(true, asnIs.isTagPrimitive());
		assertEquals(Tag.STRING_IA5, tag);
		String readData = asnIs.readIA5String();
		assertEquals(dataString, readData);
	}

	@Test
	public void testIA5StringIndefinite_1() throws Exception {
		// ACEace$}
		String dataString = "ACEace$}";
		String resultString = dataString + dataString + dataString + dataString;
		byte[] data = new byte[] { 0x41, 0x43, 0x45, 0x61, 0x63, 0x65, 0x24,
				0x7D

		};

		// we want
		// TL [TL[TLV TLV 0 0] TLV TLV 0 0]

		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		// write tag
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_CONSTRUCTED << 5)
				| Tag.STRING_IA5);
		bos.write(0x80); // idefinite length

		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_CONSTRUCTED << 5)
				| Tag.STRING_IA5);
		bos.write(0x80); // idefinite length

		// now first two data
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);

		// add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);

		// add second set of data
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);

		// add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);

		byte[] bb = bos.toByteArray();
		AsnInputStream asnIs = new AsnInputStream(bb);
		int tag = asnIs.readTag();

		assertEquals(Tag.CLASS_UNIVERSAL, asnIs.getTagClass());
		assertEquals(false, asnIs.isTagPrimitive());
		assertEquals(Tag.STRING_IA5, tag);
		String readData = asnIs.readIA5String();
		assertEquals(resultString, readData);
	}

	public void testIA5StringIndefinite_2() throws Exception {
		// ACEace$}
		String dataString = "ACEace$}";
		String resultString = dataString + dataString + dataString + dataString;
		byte[] data = new byte[] { 0x41, 0x43, 0x45, 0x61, 0x63, 0x65, 0x24,
				0x7D

		};

		// we want
		// TL [TLV TL[TLV TLV 0 0] TLV 0 0]

		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		// write tag
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_CONSTRUCTED << 5)
				| Tag.STRING_IA5);
		bos.write(0x80); // idefinite length
		// now first data
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);

		// add middle complex
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_CONSTRUCTED << 5)
				| Tag.STRING_IA5);
		bos.write(0x80); // idefinite length
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);
		// add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);

		// add second set of data
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);
		// add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);

		byte[] bb = bos.toByteArray();
		AsnInputStream asnIs = new AsnInputStream(bb);
		int tag = asnIs.readTag();

		assertEquals(Tag.CLASS_UNIVERSAL, asnIs.getTagClass());
		assertEquals(false, asnIs.isTagPrimitive());
		assertEquals(Tag.STRING_IA5, tag);
		String readData = asnIs.readIA5String();
		assertEquals(resultString, readData);
	}

	@Test
	public void testIA5StringIndefinite_3() throws Exception {
		// ACEace$}
		String dataString = "ACEace$}";
		String resultString = dataString + dataString + dataString + dataString;
		byte[] data = new byte[] { 0x41, 0x43, 0x45, 0x61, 0x63, 0x65, 0x24,
				0x7D

		};

		// we want
		// TL [TLV TLV TL[TLV TLV 0 0] 0 0]

		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		// write tag
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_CONSTRUCTED << 5)
				| Tag.STRING_IA5);
		bos.write(0x80); // idefinite length
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_CONSTRUCTED << 5)
				| Tag.STRING_IA5);
		bos.write(0x80); // idefinite length

		// now first two data
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);

		// add second set of data
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);
		// add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);

		// add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);

		byte[] bb = bos.toByteArray();
		AsnInputStream asnIs = new AsnInputStream(bb);
		int tag = asnIs.readTag();

		assertEquals(Tag.CLASS_UNIVERSAL, asnIs.getTagClass());
		assertEquals(false, asnIs.isTagPrimitive());
		assertEquals(Tag.STRING_IA5, tag);
		String readData = asnIs.readIA5String();
		assertEquals(resultString, readData);
	}

	@Test
	public void testUTF8StringDefiniteShort() throws Exception {
		// ACEace$}
		String dataString = "ACEace$} - S�u�by wiedz�, kto zorganizowa� zamachy w metrze.";
		byte[] data = dataString.getBytes(BERStatics.STRING_UTF8_ENCODING);
		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		// write tag
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_UTF8);
		bos.write(data.length);
		bos.write(data);

		byte[] bb = bos.toByteArray();
		AsnInputStream asnIs = new AsnInputStream(bb);
		int tag = asnIs.readTag();

		assertEquals(Tag.CLASS_UNIVERSAL, asnIs.getTagClass());
		assertEquals(true, asnIs.isTagPrimitive());
		assertEquals(Tag.STRING_UTF8, tag);
		String readData = asnIs.readUTF8String();
		assertEquals(dataString, readData);
	}

	@Test
	public void testUTF8StringIndefinite_1() throws Exception {
		// ACEace$}
		String dataString = "ACEace$} S�u�by wiedz�.";
		String resultString = dataString + dataString + dataString + dataString;
		byte[] data = dataString.getBytes(BERStatics.STRING_UTF8_ENCODING);

		// we want
		// TL [TL[TLV TLV 0 0] TLV TLV 0 0]

		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		// write tag
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_CONSTRUCTED << 5)
				| Tag.STRING_UTF8);
		bos.write(0x80); // idefinite length
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_CONSTRUCTED << 5)
				| Tag.STRING_UTF8);
		bos.write(0x80); // idefinite length

		// now first two data
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);

		// add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);

		// add second set of data
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);

		// add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);

		byte[] bb = bos.toByteArray();
		AsnInputStream asnIs = new AsnInputStream(bb);
		int tag = asnIs.readTag();

		assertEquals(Tag.CLASS_UNIVERSAL, asnIs.getTagClass());
		assertEquals(false, asnIs.isTagPrimitive());
		assertEquals(Tag.STRING_UTF8, tag);
		String readData = asnIs.readUTF8String();
		assertEquals(resultString, readData);
	}

	@Test
	public void testUTF8StringIndefinite_2() throws Exception {
		// ACEace$}
		String dataString = "ACEace$} S�u�by wiedz�.";
		String resultString = dataString + dataString + dataString + dataString;
		byte[] data = dataString.getBytes(BERStatics.STRING_UTF8_ENCODING);

		// we want
		// TL [TLV TL[TLV TLV 0 0] TLV 0 0]

		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		// write tag
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_CONSTRUCTED << 5)
				| Tag.STRING_UTF8);
		bos.write(0x80); // idefinite length
		// now first data
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);

		// add middle complex
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_CONSTRUCTED << 5)
				| Tag.STRING_UTF8);
		bos.write(0x80); // idefinite length
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);
		// add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);

		// add second set of data
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);
		// add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);

		byte[] bb = bos.toByteArray();
		AsnInputStream asnIs = new AsnInputStream(bb);
		int tag = asnIs.readTag();

		assertEquals(Tag.CLASS_UNIVERSAL, asnIs.getTagClass());
		assertEquals(false, asnIs.isTagPrimitive());
		assertEquals(Tag.STRING_UTF8, tag);
		String readData = asnIs.readUTF8String();
		assertEquals(resultString, readData);
	}

	@Test
	public void testUTF8StringIndefinite_3() throws Exception {
		// ACEace$}
		String dataString = "ACEace$} S�u�by wiedz�.";
		String resultString = dataString + dataString + dataString + dataString;
		byte[] data = dataString.getBytes(BERStatics.STRING_UTF8_ENCODING);

		// we want
		// TL [TLV TLV TL[TLV TLV 0 0] 0 0]

		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		// write tag
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_CONSTRUCTED << 5)
				| Tag.STRING_UTF8);
		bos.write(0x80); // idefinite length
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_CONSTRUCTED << 5)
				| Tag.STRING_UTF8);
		bos.write(0x80); // idefinite length

		// now first two data
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);

		// add second set of data
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5)
				| Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);
		// add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);

		// add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);

		byte[] bb = bos.toByteArray();
		AsnInputStream asnIs = new AsnInputStream(bb);
		int tag = asnIs.readTag();

		assertEquals(Tag.CLASS_UNIVERSAL, asnIs.getTagClass());
		assertEquals(false, asnIs.isTagPrimitive());
		assertEquals(Tag.STRING_UTF8, tag);
		String readData = asnIs.readUTF8String();
		assertEquals(resultString, readData);
	}

	@Test
	public void testObjectIdentifiers() throws Exception {
		// Test {iso(1) standard(0) 8571 abstract-syntax(2)}
		byte[] data = new byte[] { 0x6, 0x4, 0x28, (byte) 0xC2, (byte) 0x7B,
				0x02 };

		long[] actualOID = new long[] { 1, 0, 8571, 2 };
		AsnInputStream asnIs = new AsnInputStream(data);
		assertEquals(Tag.OBJECT_IDENTIFIER, asnIs.readTag());
		long[] decodedOID = asnIs.readObjectIdentifier();

		assertEquals(actualOID.length, decodedOID.length);
		for (int i = 0; i < decodedOID.length; i++) {
			assertEquals(actualOID[i], decodedOID[i]);
		}

		// Test { iso(1) member-body(2) 840 113549 }
		data = new byte[] { 0x6, 0x6, 0x2A, (byte) 0x86, (byte) 0x48,
				(byte) 0x86, (byte) 0xF7, 0x0D };

		actualOID = new long[] { 1, 2, 840, 113549 };
		asnIs = new AsnInputStream(data);
		assertEquals(Tag.OBJECT_IDENTIFIER, asnIs.readTag());
		decodedOID = asnIs.readObjectIdentifier();

		assertEquals(actualOID.length, decodedOID.length);
		for (int i = 0; i < decodedOID.length; i++) {
			assertEquals(actualOID[i], decodedOID[i]);
		}

	}

	@Test
	public void testObjectIdentifiers2() throws Exception {
		// Test {iso(1) standard(0) 8571 abstract-syntax(2)}
		byte[] data = new byte[] { 0x6, 0x7, 0x00, 0x11, (byte) 0x86, 0x05,
				0x01, 0x02, 0x01 };
		long[] actualOID = new long[] { 0, 0, 17, 773, 1, 2, 1 };

		AsnInputStream asnIs = new AsnInputStream(data);
		assertEquals(Tag.OBJECT_IDENTIFIER, asnIs.readTag());
		int length = asnIs.readLength();
		long[] decodedOID = asnIs.readObjectIdentifierData(length);

		assertEquals(actualOID.length, decodedOID.length);
		for (int i = 0; i < decodedOID.length; i++) {
			assertEquals(actualOID[i], decodedOID[i]);
		}

		data = new byte[] { Tag.OBJECT_IDENTIFIER, 0x2, (byte)180, 1 };
		actualOID = new long[] { 2, 100, 1 };
		asnIs = new AsnInputStream(data);
		assertEquals(Tag.OBJECT_IDENTIFIER, asnIs.readTag());
		decodedOID = asnIs.readObjectIdentifier();

		assertEquals(actualOID.length, decodedOID.length);
		for (int i = 0; i < decodedOID.length; i++) {
			assertEquals(actualOID[i], decodedOID[i]);
		}
	}

}
