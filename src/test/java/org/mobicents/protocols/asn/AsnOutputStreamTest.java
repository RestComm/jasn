package org.mobicents.protocols.asn;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

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
public class AsnOutputStreamTest extends TestCase {

	private AsnOutputStream output;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		this.output = new AsnOutputStream();
	}

	@After
	public void tearDown() {
		this.output  = null;
	}
	private void compareArrays(byte[] expected, byte[] encoded)
	{
		boolean same = Arrays.equals(expected, encoded);
		assertTrue("byte[] dont match, expected|encoded \n"+Arrays.toString(expected)+"\n"+Arrays.toString(encoded),same);
	}

	@Test
	public void testNULL() throws Exception {
		byte[] expected = new byte[]{0x05,0};
		this.output.writeNULL();
		byte[] encodedData = this.output.toByteArray();
		
		compareArrays(expected,encodedData);
	}
	@Test
	public void testBooleanPos() throws Exception {
		//								T   L   V
		byte[] expected = new byte[]{0x01,0x01,(byte) 0xFF};
		this.output.writeBoolean(true);
		byte[] encodedData = this.output.toByteArray();
		
		compareArrays(expected,encodedData);
		
	}
	@Test
	public void testBooleanNeg() throws Exception {
		
								//		T   L   V
		byte[] expected = new byte[]{0x01,0x01,0x00};
		this.output.writeBoolean(false);
		byte[] encodedData = this.output.toByteArray();
		
		compareArrays(expected,encodedData);
	}
	

	@Test
	public void testInteger72() throws Exception {
		
		byte[] expected = new byte[]{0x02,0x01,0x48};
		this.output.writeInteger(72);
		byte[] encodedData = this.output.toByteArray();
		
		compareArrays(expected,encodedData);
	}

	@Test
	public void testInteger127() throws Exception {
		
		byte[] expected = new byte[]{0x02,0x01,0x7F};
		this.output.writeInteger(127);
		byte[] encodedData = this.output.toByteArray();
		
		compareArrays(expected,encodedData);
	}
	@Test
	public void testInteger_128() throws Exception {
		//							  T    L         V
		byte[] expected = new byte[]{0x02,0x01,(byte) 0x80};
		this.output.writeInteger(-128);
		byte[] encodedData = this.output.toByteArray();
		
		compareArrays(expected,encodedData);
	}
	@Test
	public void testInteger128() throws Exception {
									// T    L   V -------------
		byte[] expected = new byte[]{0x02,0x02,0x00,(byte) 0x80};
		this.output.writeInteger(128);
		byte[] encodedData = this.output.toByteArray();
		
		compareArrays(expected,encodedData);
	}
	

	@Test
	public void testUTF8StringShort() throws Exception
	{
		String dataString = "ACEace$} - S�u�by wiedz�, kto zorganizowa� zamachy w metrze.";
		byte[] data = dataString.getBytes(BERStatics.STRING_UTF8_ENCODING);
		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		//write tag
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_UTF8);
		bos.write(data.length);
		bos.write(data);
		
		byte[] expected = bos.toByteArray(); 
		
		
		
		this.output.writeStringUTF8(dataString);
		byte[] encoded = this.output.toByteArray();
		compareArrays(expected, encoded);
	}
	
	@Test
	public void testUTF8StringComplex() throws Exception
	{
		//actual encoding of this is 80bytes, double == 160
		String dataString = "ACEace$} - S�u�by wiedz�, kto zorganizowa� zamachy w metrze.";
		dataString+=dataString;
		
		byte[] data = dataString.getBytes(BERStatics.STRING_UTF8_ENCODING);
		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		//write tag
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_CONSTRUCTED<<5) | Tag.STRING_UTF8);
		bos.write(0x80);
		
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_UTF8);
		bos.write(127);
		bos.write(data,0,127);
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_UTF8);
		bos.write(160-127);
		bos.write(data,127,160-127);
		
		bos.write(0);
		bos.write(0);
		
		
		byte[] expected = bos.toByteArray(); 
		
		
		
		this.output.writeStringUTF8(dataString);
		byte[] encoded = this.output.toByteArray();
		compareArrays(expected, encoded);
	}
	
	
	@Test
	public void testIA5StringShort() throws Exception
	{
		String dataString = "ACEace$}";
		byte[] data = dataString.getBytes(BERStatics.STRING_IA5_ENCODING);
		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		//write tag
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_IA5);
		bos.write(data.length);
		bos.write(data);
		
		byte[] expected = bos.toByteArray(); 
		
		
		
		this.output.writeStringIA5(dataString);
		byte[] encoded = this.output.toByteArray();
		compareArrays(expected, encoded);
	}
	
	@Test
	public void testIA5StringComplex() throws Exception
	{
		//actual encoding of this is 80bytes, double == 160
		String dataString = "ACEace$}ACEace$}ACEace$}ACEace$}ACEace$}ACEace$}ACEace$}ACEace$}ACEace$}ACEace$}ACEace$}";
		dataString+=dataString;

		byte[] data = dataString.getBytes(BERStatics.STRING_IA5_ENCODING);
	
		ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
		//write tag
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_CONSTRUCTED<<5) | Tag.STRING_IA5);
		bos.write(0x80);
		
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_IA5);
		bos.write(127);
		bos.write(data,0,127);
		bos.write((Tag.CLASS_UNIVERSAL<<6)|(Tag.PC_PRIMITIVITE<<5) | Tag.STRING_IA5);
		bos.write(176-127);
		bos.write(data,127,176-127);
		
		bos.write(0);
		bos.write(0);
		
		
		byte[] expected = bos.toByteArray(); 
		
		
		
		this.output.writeStringIA5(dataString);
		byte[] encoded = this.output.toByteArray();
		compareArrays(expected, encoded);
	}
	
	
	
	
}
