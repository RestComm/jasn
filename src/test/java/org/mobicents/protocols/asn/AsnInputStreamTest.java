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

		Tag tag = asnIs.readTag();
		assertNotNull(tag);
		assertEquals(Tag.CLASS_UNIVERSAL, tag.getTagClass());
		assertEquals(true, tag.isPrimitive);
		assertEquals(Tag.INTEGER, tag.getValue());

		int length = asnIs.readLength();
		long value = asnIs.readInteger(length);

		assertEquals(-128, value);

		// Test -ve integer -65536
		byte[] b = this.intToByteArray(-65536);

		data = new byte[] { 0x2, 0x4, b[0], b[1], b[2], b[3] };

		baIs = new ByteArrayInputStream(data);
		asnIs = new AsnInputStream(baIs);

		tag = asnIs.readTag();
		assertNotNull(tag);
		assertEquals(Tag.CLASS_UNIVERSAL, tag.getTagClass());
		assertEquals(true, tag.isPrimitive);
		assertEquals(Tag.INTEGER, tag.getValue());

		length = asnIs.readLength();
		assertEquals(4, length);
		value = asnIs.readInteger(length);

		assertEquals(-65536, value);

		// Test +ve integer 797979
		b = this.intToByteArray(797979);

		data = new byte[] { 0x2, 0x4, b[0], b[1], b[2], b[3] };

		baIs = new ByteArrayInputStream(data);
		asnIs = new AsnInputStream(baIs);

		tag = asnIs.readTag();
		assertNotNull(tag);
		assertEquals(Tag.CLASS_UNIVERSAL, tag.getTagClass());
		assertEquals(true, tag.isPrimitive);
		assertEquals(Tag.INTEGER, tag.getValue());

		length = asnIs.readLength();
		assertEquals(4, length);
		value = asnIs.readInteger(length);

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
	public void testOctetStringPrimitive() throws Exception {
		byte[] data = new byte[] { 0x4, 0x10, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99,
				(byte) 0xAA, (byte) 0xBB, (byte) 0XCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };

		ByteArrayInputStream baIs = new ByteArrayInputStream(data);
		AsnInputStream asnIs = new AsnInputStream(baIs);

		Tag tag = asnIs.readTag();
		assertNotNull(tag);
		assertEquals(Tag.CLASS_UNIVERSAL, tag.getTagClass());
		assertEquals(true, tag.isPrimitive);
		assertEquals(Tag.STRING_OCTET, tag.getValue());

		int length = asnIs.readLength();

		assertEquals(16, length);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		asnIs.readOctetString(length, tag.isPrimitive, byteArrayOutputStream);

		byte[] resultData = byteArrayOutputStream.toByteArray();

		for (int i = 0; i < resultData.length; i++) {
			assertTrue(resultData[i] == data[i + 2]);
		}
	}

}
