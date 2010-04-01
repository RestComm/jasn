package org.mobicents.protocols.asn;

/**
 * 
 * @author amit bhayani
 * @author baranowb
 */
public interface Tag {
	
	/**
	 * Class of tag used with primitives
	 */
	public static final int CLASS_UNIVERSAL = 0x0;
	public static final int CLASS_APPLICATION = 0x1;
	public static final int CLASS_CONTEXT_SPECIFIC = 0x2;
	public static final int CLASS_PRIVATE = 0x3;

	// first two bits encode the class
	public static final int CLASS_MASK = 0xC0;

	// The next bit (bit six) is called the primitive/constructed (P/C) bit
	public static final int PC_MASK = 0x20;
	public static final int PC_PRIMITIVITE = 0x0;
	public static final int PC_CONSTRUCTED = 0x1;
	// The last five bits (bits 5 to 1) encode the number of the tag in tag octet
	public static final int NUMBER_MASK = 0x1F;

	// Universal class tag assignments as per X.680-0207, Section 8.4
	public static final int BOOLEAN = 0x01;
	public static final int INTEGER = 0x02;
	public static final int STRING_BIT = 0x03;
	public static final int STRING_OCTET = 0x04;
	public static final int NULL = 0x05;
	public static final int REAL = 0x09;
	public static final int ENUMERATED = 0x0A;



}
