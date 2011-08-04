/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.protocols.asn;

/**
 * 
 * @author amit bhayani
 * @author baranowb
 */
public class Tag {
	
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
	public static final int TAG_MASK = 0x1F;

	// Universal class tag assignments as per X.680-0207, Section 8.4
	public static final int BOOLEAN = 0x01;
	public static final int INTEGER = 0x02;
	public static final int STRING_BIT = 0x03;
	public static final int STRING_OCTET = 0x04;
	public static final int NULL = 0x05;
    public static final int OBJECT_IDENTIFIER = 0x06;
    public static final int EXTERNAL = 0x8;
	public static final int REAL = 0x09;
	public static final int ENUMERATED = 0x0A;
	public static final int STRING_UTF8 = 0x0C;
	public static final int SEQUENCE = 0x10; 
	public static final int STRING_IA5 = 0x16;
	//UNKNOWN, add this ....
	public static final int OBJECT_DESCRIPTOR = 0xFF;
	
	//values for ending stream of string for constructed form, see 18.2.6 in ASN.1 Communication between Heterogeneous Systems
	public static final int NULL_TAG = 0x00;
	public static final int NULL_VALUE = 0x00;

	// value of indefinite length for readLength()
	public static final int Indefinite_Length  = -1;


	private Tag() {
		super();
	
	}


	public static boolean isPrimitive(int tagValue)
	{
		//no shift needed, since for primitive its '0'
		return (tagValue & PC_MASK) == PC_PRIMITIVITE;
	}

	public static int getSimpleTagValue(int tagValue)
	{
		return tagValue & TAG_MASK;
	}
	public static boolean isUniversal(int tagValue)
	{
		return (tagValue & CLASS_MASK) == CLASS_UNIVERSAL;
	}
}
