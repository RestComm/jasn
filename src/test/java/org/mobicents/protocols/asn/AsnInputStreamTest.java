package org.mobicents.protocols.asn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
	public void testInteger() throws Exception {

		// Test -ve integer -128
		byte[] data = new byte[] { 0x2, 0x1, (byte) 0x80 };
		ByteArrayInputStream baIs = new ByteArrayInputStream(data);
		AsnInputStream asnIs = new AsnInputStream(baIs);

		long value = asnIs.readInteger();

		assertEquals(-128, value);

		// Test -ve integer -65536
		byte[] b = this.intToByteArray(-65536);

		data = new byte[] { 0x2, 0x4, b[0], b[1], b[2], b[3] };

		baIs = new ByteArrayInputStream(data);
		asnIs = new AsnInputStream(baIs);

		value = asnIs.readInteger();

		assertEquals(-65536, value);

		// Test +ve integer 797979
		b = this.intToByteArray(797979);

		data = new byte[] { 0x2, 0x4, b[0], b[1], b[2], b[3] };

		baIs = new ByteArrayInputStream(data);
		asnIs = new AsnInputStream(baIs);

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
		byte[] data = new byte[] { 0x03, 0x04, 0x02, (byte) 0xF0, (byte) 0xF0, (byte) 0xF4 };

		ByteArrayInputStream baIs = new ByteArrayInputStream(data);
		AsnInputStream asnIs = new AsnInputStream(baIs);

		BitSet bitSet = new BitSet();
		
		int tagValue = asnIs.readTag();
		asnIs.readBitString(bitSet, 0);
		
		//f0f0f4 is 111100001111000011110100 reduce 02 bits so total length is 22
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
		byte[] data = new byte[] { 0x23, (byte) 0x80, 0x03, 0x03, 0x00, (byte) 0xF0, (byte) 0xF0, 0x03, 0x02, 0x02, (byte)0xF4,
				0x00 };

		byte[] octetString = new byte[] { (byte) 0xF0, (byte) 0xF0, (byte)0xF4 };

		ByteArrayInputStream baIs = new ByteArrayInputStream(data);
		AsnInputStream asnIs = new AsnInputStream(baIs);

		BitSet bitSet = new BitSet();
		// here we have to explicitly read the Tag
		int tagValue = asnIs.readTag();
		asnIs.readBitString(bitSet, 0);
		
		//f0f0f4 is 111100001111000011110100 reduce 02 bits so total length is 22
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
		byte[] data = new byte[] { 0x4, 0x10, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99,
				(byte) 0xAA, (byte) 0xBB, (byte) 0XCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };

		ByteArrayInputStream baIs = new ByteArrayInputStream(data);
		AsnInputStream asnIs = new AsnInputStream(baIs);

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		// here we have to explicitly read the Tag
		int tagValue = asnIs.readTag();
		asnIs.readOctetString(byteArrayOutputStream, tagValue);

		byte[] resultData = byteArrayOutputStream.toByteArray();

		for (int i = 0; i < resultData.length; i++) {
			assertTrue(resultData[i] == data[i + 2]);
		}
	}

	@Test
	public void testOctetStringConstructed() throws Exception {
		byte[] data = new byte[] { 0x24, (byte) 0x80, 0x04, 0x08, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x04,
				0x08, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0XCC, (byte) 0xDD, (byte) 0xEE,
				(byte) 0xFF, 0x00 };

		byte[] octetString = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99,
				(byte) 0xAA, (byte) 0xBB, (byte) 0XCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };

		ByteArrayInputStream baIs = new ByteArrayInputStream(data);
		AsnInputStream asnIs = new AsnInputStream(baIs);

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		// here we have to explicitly read the Tag
		int tagValue = asnIs.readTag();
		asnIs.readOctetString(byteArrayOutputStream, tagValue);

		byte[] resultData = byteArrayOutputStream.toByteArray();

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
				(Tag.CLASS_UNIVERSAL << 6) | (Tag.PC_PRIMITIVITE << 5) | Tag.REAL,
				// Length - this is definite - we dont handle more? do we?
				0x0A,// 1(info bits) 2(exponent 7(mantisa)
				// info bits (binary,sign,BB,FF,EE)
				(byte) (0x80 | (0x0 << 6) | 0x00 << 4 | 0x01), // 1 0 00(base2) 00(scale = 0) 01 ( two octets for
				// exponent
				// exponent, two octets
				// 100 00000101
				0x04, 0x05,
				// mantisa
				// 1101 10101000 00000000 00000000 00000000 00000000 00000000

				0x0D, (byte) 0xA8, 0x00, 0x00, 0x00, 0x00, 0x00 };

		ByteArrayInputStream baIs = new ByteArrayInputStream(binary1);
		AsnInputStream asnIs = new AsnInputStream(baIs);
		int tagValue = asnIs.readTag();
		
		//assertEquals(Tag.CLASS_UNIVERSAL, Tag.getTagClass(tagValue));
		//assertTrue(Tag.isPrimitive(tagValue);
		//assertEquals(Tag.REAL, Tag.getType(tagValue));
		double d = asnIs.readReal();
		assertEquals("Decoded value is not proper!!",118.625d, d);
		//-118.625
		byte[] binary2 = new byte[]
		        {
				//TAG;
				(Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.REAL,
				//Length - this is definite - we dont handle more? do we?
				0x0A,//1(info bits) 2(exponent 7(mantisa)
				 //info bits  (binary,sign,BB,FF,EE)
				(byte)(0x80 | (0x1<<6) | 0x00 <<4 | 0x01)  , //1 0 00(base2) 00(scale = 0) 01 ( two octets for exponent
				//exponent, two octets
				//100 00000101
				0x04,
				0x05,
				//mantisa
				//1101  10101000  00000000  00000000  00000000  00000000  00000000
		
				0x0D,
				(byte)0xA8,
				0x00,
				0x00,
				0x00,
				0x00,
				0x00
		         };

		baIs = new ByteArrayInputStream(binary2);
		asnIs = new AsnInputStream(baIs);
		tagValue = asnIs.readTag();
		
		//assertEquals(Tag.CLASS_UNIVERSAL, Tag.getTagClass(tagValue));
		//assertTrue(Tag.isPrimitive(tagValue);
		//assertEquals(Tag.REAL, Tag.getType(tagValue));
		d = asnIs.readReal();
		assertEquals("Decoded value is not proper!!",-118.625d, d);
		
	}

	@Test
	public void testRealBase10() throws Exception
	{
		//TODO get real data trace?
		String[] digs= new String[]{"   0004902"
		        ,"  +0004902"
		        ," -4902"
		        ,"4902.00"
		        ,"4902."
		        ,".5"
		        ," 0.3E-04"
		        ,"-2.8E+000000"
		        ,"   000004.50000E123456789"
		        ,"+5.6e+03"
		        ,"+0.56E+4"};
		
		
		for(int index = 0;index<digs.length;index++)
		{
			double d = Double.parseDouble(digs[index]);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(20);
			//write tag
			bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.REAL);
			//length is unknown for a bit, lets do the math
			byte[] data = digs[index].getBytes("US-ASCII");
			bos.write(1+data.length); // 1 for 2 bits for base10 indicator and 6 bits for NR
			int NR = 0; // for now it is ignored
			
			if(index<=2)
			{
				//NR1
				NR = BERStatics.REAL_NR1;
			}else if(index<=5)
			{
				NR = BERStatics.REAL_NR2;
			}else
			{
				NR = BERStatics.REAL_NR3;
			}
			bos.write( ((0x00<<6))| (NR));
			bos.write(data);
			byte[] bb = bos.toByteArray(); ByteArrayInputStream baIs = new ByteArrayInputStream(bb);
			AsnInputStream asnIs = new AsnInputStream(baIs);
			int tagValue = asnIs.readTag();
			
			//assertEquals(Tag.CLASS_UNIVERSAL, Tag.getTagClass(tagValue));
			//assertTrue(Tag.isPrimitive(tagValue);
			//assertEquals(Tag.REAL, Tag.getType(tagValue));
			double dd = asnIs.readReal();
			assertEquals("Decoded value is not proper!!",d, dd);
		}
		
		
		
	}
	
	//IA5 data taken from table on page:   http://www.zytrax.com/tech/ia5.html
	@Test
	public void testIA5StringDefiniteShort() throws Exception
	{
		//ACEace$}
		String dataString = "ACEace$}";
		byte[] data = new byte[]
		{
				0x41,
				0x43,
				0x45,
				0x61,
				0x63,
				0x65,
				0x24,
				0x7D
				
		};
		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		//write tag
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_IA5);
		bos.write(data.length);
		bos.write(data);
		
		byte[] bb = bos.toByteArray(); ByteArrayInputStream baIs = new ByteArrayInputStream(bb);
		AsnInputStream asnIs = new AsnInputStream(baIs);
		int tagValue = asnIs.readTag();
		
		//assertEquals(Tag.CLASS_UNIVERSAL, Tag.getTagClass(tagValue));
		//assertTrue(Tag.isPrimitive(tagValue);
		//assertEquals(Tag.REAL, Tag.getType(tagValue));
		String readData = asnIs.readIA5String();
		assertEquals(dataString, readData);
	}
	
	@Test
	public void testIA5StringIndefinite_1() throws Exception
	{
		//ACEace$}
		String dataString = "ACEace$}";
		String resultString = dataString+dataString+dataString+dataString;
		byte[] data = new byte[]
		{
				0x41,
				0x43,
				0x45,
				0x61,
				0x63,
				0x65,
				0x24,
				0x7D
				
		};
	
		//we want 
		// TL [TL[TLV TLV 0 0] TLV TLV 0 0]
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		//write tag
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_CONSTRUCTED<<5) | Tag.STRING_IA5);
		bos.write(0x80); // idefinite length
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_CONSTRUCTED<<5) | Tag.STRING_IA5);
		bos.write(0x80); // idefinite length
		
		//now first two data
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);
		
		//add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);
		
		
		//add second set of data
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);
		
		//add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);
		
		byte[] bb = bos.toByteArray(); ByteArrayInputStream baIs = new ByteArrayInputStream(bb);
		AsnInputStream asnIs = new AsnInputStream(baIs);
		int tagValue = asnIs.readTag();
		
		//assertEquals(Tag.CLASS_UNIVERSAL, Tag.getTagClass(tagValue));
		//assertTrue(Tag.isPrimitive(tagValue);
		//assertEquals(Tag.REAL, Tag.getType(tagValue));
		String readData = asnIs.readIA5String();
		assertEquals(resultString, readData);
	}
	
	public void testIA5StringIndefinite_2() throws Exception
	{
		//ACEace$}
		String dataString = "ACEace$}";
		String resultString = dataString+dataString+dataString+dataString;
		byte[] data = new byte[]
		{
				0x41,
				0x43,
				0x45,
				0x61,
				0x63,
				0x65,
				0x24,
				0x7D
				
		};
	
		//we want 
		// TL [TLV TL[TLV TLV 0 0]  TLV 0 0]
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		//write tag
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_CONSTRUCTED<<5) | Tag.STRING_IA5);
		bos.write(0x80); // idefinite length
		//now first data
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);
		
		//add middle complex
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_CONSTRUCTED<<5) | Tag.STRING_IA5);
		bos.write(0x80); // idefinite length
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);
		//add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);
		
		
		//add second set of data
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);		
		//add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);
		
		byte[] bb = bos.toByteArray(); ByteArrayInputStream baIs = new ByteArrayInputStream(bb);
		AsnInputStream asnIs = new AsnInputStream(baIs);
		int tagValue = asnIs.readTag();
		
		//assertEquals(Tag.CLASS_UNIVERSAL, Tag.getTagClass(tagValue));
		//assertTrue(Tag.isPrimitive(tagValue);
		//assertEquals(Tag.REAL, Tag.getType(tagValue));
		String readData = asnIs.readIA5String();
		assertEquals(resultString, readData);
	}
	
	@Test
	public void testIA5StringIndefinite_3() throws Exception
	{
		//ACEace$}
		String dataString = "ACEace$}";
		String resultString = dataString+dataString+dataString+dataString;
		byte[] data = new byte[]
		{
				0x41,
				0x43,
				0x45,
				0x61,
				0x63,
				0x65,
				0x24,
				0x7D
				
		};
	
		//we want 
		// TL [TLV TLV TL[TLV TLV 0 0] 0 0]
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		//write tag
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_CONSTRUCTED<<5) | Tag.STRING_IA5);
		bos.write(0x80); // idefinite length
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_CONSTRUCTED<<5) | Tag.STRING_IA5);
		bos.write(0x80); // idefinite length
		
		
		
		
		//now first two data
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);
		
		//add second set of data
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_IA5);
		bos.write(data.length); // definite length
		bos.write(data);
		//add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);
		
		//add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);
		
		byte[] bb = bos.toByteArray(); ByteArrayInputStream baIs = new ByteArrayInputStream(bb);
		AsnInputStream asnIs = new AsnInputStream(baIs);
		int tagValue = asnIs.readTag();
		
		//assertEquals(Tag.CLASS_UNIVERSAL, Tag.getTagClass(tagValue));
		//assertTrue(Tag.isPrimitive(tagValue);
		//assertEquals(Tag.REAL, Tag.getType(tagValue));
		String readData = asnIs.readIA5String();
		assertEquals(resultString, readData);
	}
	@Test
	public void testUTF8StringDefiniteShort() throws Exception
	{
		//ACEace$}
		String dataString = "ACEace$} - S�u�by wiedz�, kto zorganizowa� zamachy w metrze.";
		byte[] data = dataString.getBytes(BERStatics.STRING_UTF8_ENCODING);
		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		//write tag
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_UTF8);
		bos.write(data.length);
		bos.write(data);
		
		byte[] bb = bos.toByteArray(); ByteArrayInputStream baIs = new ByteArrayInputStream(bb);
		AsnInputStream asnIs = new AsnInputStream(baIs);
		int tagValue = asnIs.readTag();
		
		//assertEquals(Tag.CLASS_UNIVERSAL, Tag.getTagClass(tagValue));
		//assertTrue(Tag.isPrimitive(tagValue);
		//assertEquals(Tag.REAL, Tag.getType(tagValue));
		String readData = asnIs.readUTF8String();
		assertEquals(dataString, readData);
	}
	
	@Test
	public void testUTF8StringIndefinite_1() throws Exception
	{
		//ACEace$}
		String dataString = "ACEace$} S�u�by wiedz�.";
		String resultString = dataString+dataString+dataString+dataString;
		byte[] data = dataString.getBytes(BERStatics.STRING_UTF8_ENCODING);
	
		//we want 
		// TL [TL[TLV TLV 0 0] TLV TLV 0 0]
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		//write tag
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_CONSTRUCTED<<5) | Tag.STRING_UTF8);
		bos.write(0x80); // idefinite length
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_CONSTRUCTED<<5) | Tag.STRING_UTF8);
		bos.write(0x80); // idefinite length
		
		//now first two data
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);
		
		//add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);
		
		
		//add second set of data
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);
		
		//add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);
		
		byte[] bb = bos.toByteArray(); ByteArrayInputStream baIs = new ByteArrayInputStream(bb);
		AsnInputStream asnIs = new AsnInputStream(baIs);
		int tagValue = asnIs.readTag();
		
		//assertEquals(Tag.CLASS_UNIVERSAL, Tag.getTagClass(tagValue));
		//assertTrue(Tag.isPrimitive(tagValue);
		//assertEquals(Tag.REAL, Tag.getType(tagValue));
		String readData = asnIs.readUTF8String();
		assertEquals(resultString, readData);
	}
	
	@Test
	public void testUTF8StringIndefinite_2() throws Exception
	{
		//ACEace$}
		String dataString = "ACEace$} S�u�by wiedz�.";
		String resultString = dataString+dataString+dataString+dataString;
		byte[] data = dataString.getBytes(BERStatics.STRING_UTF8_ENCODING);
	
		//we want 
		// TL [TLV TL[TLV TLV 0 0]  TLV 0 0]
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		//write tag
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_CONSTRUCTED<<5) | Tag.STRING_UTF8);
		bos.write(0x80); // idefinite length
		//now first data
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);
		
		//add middle complex
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_CONSTRUCTED<<5) | Tag.STRING_UTF8);
		bos.write(0x80); // idefinite length
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);
		//add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);
		
		
		//add second set of data
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);		
		//add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);
		
		byte[] bb = bos.toByteArray(); ByteArrayInputStream baIs = new ByteArrayInputStream(bb);
		AsnInputStream asnIs = new AsnInputStream(baIs);
		int tagValue = asnIs.readTag();
		
		//assertEquals(Tag.CLASS_UNIVERSAL, Tag.getTagClass(tagValue));
		//assertTrue(Tag.isPrimitive(tagValue);
		//assertEquals(Tag.REAL, Tag.getType(tagValue));
		String readData = asnIs.readUTF8String();
		assertEquals(resultString, readData);
	}
	
	@Test
	public void testUTF8StringIndefinite_3() throws Exception
	{
		//ACEace$}
		String dataString = "ACEace$} S�u�by wiedz�.";
		String resultString = dataString+dataString+dataString+dataString;
		byte[] data = dataString.getBytes(BERStatics.STRING_UTF8_ENCODING);
	
		//we want 
		// TL [TLV TLV TL[TLV TLV 0 0] 0 0]
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		//write tag
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_CONSTRUCTED<<5) | Tag.STRING_UTF8);
		bos.write(0x80); // idefinite length
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_CONSTRUCTED<<5) | Tag.STRING_UTF8);
		bos.write(0x80); // idefinite length
		
		
		
		
		//now first two data
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);
		
		//add second set of data
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_UTF8);
		bos.write(data.length); // definite length
		bos.write(data);
		//add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);
		
		//add null
		bos.write(Tag.NULL_TAG);
		bos.write(Tag.NULL_VALUE);
		
		byte[] bb = bos.toByteArray(); ByteArrayInputStream baIs = new ByteArrayInputStream(bb);
		AsnInputStream asnIs = new AsnInputStream(baIs);
		int tagValue = asnIs.readTag();
		
		//assertEquals(Tag.CLASS_UNIVERSAL, Tag.getTagClass(tagValue));
		//assertTrue(Tag.isPrimitive(tagValue);
		//assertEquals(Tag.REAL, Tag.getType(tagValue));
		String readData = asnIs.readUTF8String();
		assertEquals(resultString, readData);
	}
	
}
