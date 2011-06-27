/**
 * 
 */
package org.mobicents.protocols.asn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.BitSet;

/**
 * Represents external type, should be extended to allow getting real type of
 * data, once External ends decoding. Decoding should be done as follows in
 * subclass:<br>
 * 
 * <pre>
 * &lt;b&gt;decode(){
 *   super.decode();
 *   if(super.getType == requiredType)
 *       this.decode();
 *   }else
 *   {
 *   	//indicate error
 *   }
 *   &lt;/b&gt;
 * </pre>
 *  . Also encode/decode methods should be extended
 * 
 * @author baranowb
 * @author amit bhayani
 * 
 */
public class External {
	// FIXME: makes this proper, it should be kind of universal container....
	protected static final int _TAG_EXTERNAL_CLASS = Tag.CLASS_UNIVERSAL; // universal
	protected static final boolean _TAG_EXTERNAL_PC_PRIMITIVE = false; // isPrimitive

	// ENCODING TYPE
	protected static final int _TAG_ASN = 0x00;
	protected static final int _TAG_ASN_CLASS = Tag.CLASS_CONTEXT_SPECIFIC; // context
	// spec
	protected static final boolean _TAG_ASN_PC_PRIMITIVE = false; // isPrimitive

	// in case of Arbitrary and OctetAligned, we dont make decision if its
	// constructed or primitive, its done for us :)
	protected static final int _TAG_ARBITRARY = 0x02; // this is bit string
	protected static final int _TAG_ARBITRARY_CLASS = Tag.CLASS_CONTEXT_SPECIFIC; // context
	// spec

	protected static final int _TAG_OCTET_ALIGNED = 0x01; // this is bit
															// string
	protected static final int _TAG_OCTET_ALIGNED_CLASS = Tag.CLASS_CONTEXT_SPECIFIC; // context
	// spec

	protected static final int _TAG_IMPLICIT_SEQUENCE = 0x08;

	// some state vars
	// ENCODE TYPE - wtf, ASN is really mind blowing, cmon....
	// If Amit reads this, he will smile.

	// ENCODE AS.... boom
	protected boolean oid = false;
	protected boolean integer = false;
	protected boolean objDescriptor = false;

	// actual vals
	protected long[] oidValue = null;
	protected long indirectReference = 0;
	protected Object objDescriptorValue = null;

	// ENCoDING
	private boolean asn = false;
	private boolean octet = false;
	private boolean arbitrary = false;

	// data in binary form for ASN and octet string
	private byte[] data;
	private BitSet bitDataString;

	//FIXME: ensure structure from file and if it does not allow more than one type of data, enforce that!
	
	public void decode(AsnInputStream ais) throws AsnException {
		try {

			// The definition of EXTERNAL is
			//			
			// EXTERNAL ::= [UNIVERSAL 8] IMPLICIT SEQUENCE {
			// direct-reference OBJECT IDENTIFIER OPTIONAL,
			// indirect-reference INTEGER OPTIONAL,
			// data-value-descriptor ObjectDescriptor OPTIONAL,
			// encoding CHOICE {
			// single-ASN1-type [0] ANY,
			// octet-aligned [1] IMPLICIT OCTET STRING,
			// arbitrary [2] IMPLICIT BIT STRING }}
			//
			//			

			byte[] sequence = ais.readSequence();

			AsnInputStream localAsnIS = new AsnInputStream(
					new ByteArrayInputStream(sequence));
			int tag;
			int len;
			while (localAsnIS.available() > 0) {
				tag = localAsnIS.readTag();

				// we can have one of
				if (tag == Tag.OBJECT_IDENTIFIER) {

					this.oidValue = localAsnIS.readObjectIdentifier();
					this.setOid(true);

				} else if (tag == Tag.INTEGER) {
					this.indirectReference = localAsnIS.readInteger();
					this.setInteger(true);
				} else if (tag == Tag.OBJECT_DESCRIPTOR) {
					throw new AsnException();
				} else {
					throw new AsnException("Unrecognized tag value: " + tag);
				}

				// read encoding
				tag = localAsnIS.readTag();
				len = localAsnIS.readLength();

				if (tag == External._TAG_ASN) {
					setAsn(true);
					// this we dont decode...., we have no idea what is realy
					// there, might be simple type...
					// or app specific....
					data = new byte[len];
					localAsnIS.read(data);

				} else if (tag == External._TAG_OCTET_ALIGNED) {
					setOctet(true);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					localAsnIS.readOctetString(bos);
					setEncodeType(bos.toByteArray());
				} else if (tag == External._TAG_ARBITRARY) {
					setArbitrary(true);
					this.bitDataString = new BitSet();
					this.setEncodeBitStringType(this.bitDataString);
					tag = localAsnIS.readTag();
					if(tag != Tag.STRING_BIT)
					{
						throw new AsnException("Wrong tag value '"+tag+"' expected '"+Tag.STRING_BIT+"'");
					}
					BitSet bitSet = new BitSet();
					localAsnIS.readBitString(bitSet);
					this.setEncodeBitStringType(bitSet);
				} else {
					throw new AsnException();
				}

			}

		} catch (IOException e) {
			throw new AsnException(e);
		}
	}

	public void encode(AsnOutputStream aos) throws AsnException {
		try {
			// something to do encoding
			AsnOutputStream localOutput = new AsnOutputStream();
			if (oid) {
				localOutput.writeObjectIdentifier(this.oidValue);
			} else if (integer) {
				// FIXME: remove cast, now it takes only int, should take long
				localOutput.writeInteger((int) this.indirectReference);
			} else if (objDescriptor) {
				throw new AsnException();
			} else {
				throw new AsnException();
			}

			// told, you, mind blowing!

			if (asn) {
				byte[] childData = getEncodeType();
				localOutput.writeTag(_TAG_ASN_CLASS, _TAG_ASN_PC_PRIMITIVE,
						_TAG_ASN);
				localOutput.writeLength(childData.length);
				localOutput.write(childData);
				// childData = localOutput.toByteArray();
				// localOutput.reset();
			} else if (octet) {
				byte[] childData = getEncodeType();
				// get child class.... I think its done like that....
				boolean childConstructor = ((childData[0] & 0x20) >> 5) == Tag.PC_PRIMITIVITE;
				localOutput.writeTag(_TAG_OCTET_ALIGNED_CLASS,
						childConstructor, _TAG_OCTET_ALIGNED);
				localOutput.writeLength(childData.length);
				localOutput.write(childData);
				// childData = localOutput.toByteArray();
				// localOutput.reset();
			} else if (arbitrary) {

				AsnOutputStream _bitStrinAos = new AsnOutputStream();
				_bitStrinAos.writeStringBinary(this.bitDataString);
				byte[] childData = _bitStrinAos.toByteArray();
				boolean childConstructor = ((childData[0] & 0x20) >> 5) == Tag.PC_PRIMITIVITE;
				localOutput.writeTag(_TAG_ARBITRARY_CLASS, childConstructor,
						_TAG_ARBITRARY);
				localOutput.writeLength(childData.length);
				localOutput.write(childData);
				// childData = localOutput.toByteArray();
				// localOutput.reset();
			} else {
				throw new AsnException();
			}

			byte[] externalChildData = localOutput.toByteArray();

			// Write the UserInformation Tag and length
			aos.writeTag(Tag.CLASS_UNIVERSAL, false, Tag.EXTERNAL);
			aos.writeLength(externalChildData.length);

			// Write the Sequence Tag and length
			// aos.writeTag(Tag.CLASS_UNIVERSAL, true, Tag.SEQUENCE);
			// aos.writeLength(externalChildData.length);

			// Write actual Data now
			aos.write(externalChildData);
			return;
		} catch (IOException e) {
			throw new AsnException(e);
		}
	}

	public byte[] getEncodeType() throws AsnException {
		return data;
	}

	public void setEncodeType(byte[] data) {
		this.data = data;
	}

	public BitSet getEncodeBitStringType() throws AsnException {
		return (BitSet) bitDataString.clone();
	}

	public void setEncodeBitStringType(BitSet data) {
		this.bitDataString = data;
		this.setArbitrary(true);
	}

	/**
	 * @return the oid
	 */
	public boolean isOid() {
		return oid;
	}

	/**
	 * @param oid
	 *            the oid to set
	 */
	public void setOid(boolean oid) {
		this.oid = oid;
		if (oid) {
			setInteger(false);
			setObjDescriptor(false);
		}
	}

	/**
	 * @return the integer
	 */
	public boolean isInteger() {
		return integer;
	}

	/**
	 * @param integer
	 *            the integer to set
	 */
	public void setInteger(boolean integer) {
		this.integer = integer;
		if (integer) {
			setOid(false);
			setObjDescriptor(false);
		}
	}

	/**
	 * @return the objDescriptor
	 */
	public boolean isObjDescriptor() {
		return objDescriptor;
	}

	/**
	 * @param objDescriptor
	 *            the objDescriptor to set
	 */
	public void setObjDescriptor(boolean objDescriptor) {
		this.objDescriptor = objDescriptor;
		if (objDescriptor) {
			setOid(false);
			setInteger(false);
		}
	}

	/**
	 * @return the oidValue
	 */
	public long[] getOidValue() {
		return oidValue;
	}

	/**
	 * @param oidValue
	 *            the oidValue to set
	 */
	public void setOidValue(long[] oidValue) {
		this.oidValue = oidValue;
	}

	/**
	 * @return the integerValue
	 */
	public long getIndirectReference() {
		return indirectReference;
	}

	/**
	 * @param integerValue
	 *            the integerValue to set
	 */
	public void setIndirectReference(long indirectReference) {
		this.indirectReference = indirectReference;
	}

	/**
	 * @return the objDescriptorValue
	 */
	public Object getObjDescriptorValue() {
		return objDescriptorValue;
	}

	/**
	 * @param objDescriptorValue
	 *            the objDescriptorValue to set
	 */
	public void setObjDescriptorValue(Object objDescriptorValue) {
		this.objDescriptorValue = objDescriptorValue;
	}

	/**
	 * @return the asn
	 */
	public boolean isAsn() {
		return asn;
	}

	/**
	 * @param asn
	 *            the asn to set
	 */
	public void setAsn(boolean asn) {
		this.asn = asn;
		if (asn) {
			setArbitrary(false);
			setOctet(false);
		}
	}

	/**
	 * @return the octet
	 */
	public boolean isOctet() {
		return octet;
	}

	/**
	 * @param octet
	 *            the octet to set
	 */
	public void setOctet(boolean octet) {
		this.octet = octet;
		if (octet) {
			setArbitrary(false);
			setAsn(false);
		}
	}

	/**
	 * @return the arbitrary
	 */
	public boolean isArbitrary() {
		return arbitrary;
	}

	/**
	 * @param arbitrary
	 *            the arbitrary to set
	 */
	public void setArbitrary(boolean arbitrary) {
		this.arbitrary = arbitrary;
		if (arbitrary) {
			setObjDescriptor(false);
			setAsn(false);
		}
	}

}
