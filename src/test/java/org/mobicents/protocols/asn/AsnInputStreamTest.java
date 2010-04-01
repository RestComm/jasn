package org.mobicents.protocols.asn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author amit bhayani
 * 
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

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		int tagValue = asnIs.readTag();
		asnIs.readBitString(byteArrayOutputStream, tagValue);

		byte[] resultData = byteArrayOutputStream.toByteArray();

		for (int i = 0; i < resultData.length; i++) {
			assertTrue(resultData[i] == data[i + 3]);
		}

	}

	@Test
	public void testBitStringConstructed() throws Exception {
		byte[] data = new byte[] { 0x23, (byte) 0x80, 0x03, 0x02, (byte) 0xF0, (byte) 0xF0, 0x03, 0x02, 0x02, (byte)0xF4,
				0x00 };

		byte[] octetString = new byte[] { (byte) 0xF0, (byte) 0xF0, (byte)0xF4 };

		ByteArrayInputStream baIs = new ByteArrayInputStream(data);
		AsnInputStream asnIs = new AsnInputStream(baIs);

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		// here we have to explicitly read the Tag
		int tagValue = asnIs.readTag();
		asnIs.readBitString(byteArrayOutputStream, tagValue);

		byte[] resultData = byteArrayOutputStream.toByteArray();

		for (int i = 0; i < resultData.length; i++) {
			assertTrue(resultData[i] == octetString[i]);
		}
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

}
