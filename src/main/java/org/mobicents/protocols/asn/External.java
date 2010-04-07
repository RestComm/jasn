/**
 * 
 */
package org.mobicents.protocols.asn;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Represents external type, should be extended to allow getting real type of data, once External ends decoding. Decoding should be done as follows in subclass:<br>
 * <pre><b>decode(){
 *   super.decode();
 *   if(super.getType == requiredType)
 *       this.decode();
 *   }else
 *   {
 *   	//indicate error
 *   }
 *   </b></pre>
 * . Also encode/decode methods should be extended
 * @author baranowb
 *
 */
public abstract class External {

	
	
	
	protected static final int _TAG_EXTERNAL_CLASS = Tag.CLASS_UNIVERSAL; //universal
	protected static final boolean _TAG_EXTERNAL_PC_PRIMITIVE = false; //isPrimitive
	
	//ENCODING TYPE
	protected static final int _TAG_ASN = 0x00; 
	protected static final int _TAG_ASN_CLASS = Tag.CLASS_CONTEXT_SPECIFIC; //context spec
	protected static final boolean _TAG_ASN_PC_PRIMITIVE = false; //isPrimitive
	
	//in case of Arbitrary and OctetAligned, we dont make decision if its constructed or primitive, its done for us :)
	protected static final int _TAG_ARBITRARY = 0x02;  // this is bit string
	protected static final int _TAG_ARBITRARY_CLASS = Tag.CLASS_CONTEXT_SPECIFIC; //context spec
	
	protected static final int _TAG_OCTET_ALIGNED = 0x01;  // this is bit string
	protected static final int _TAG_OCTET_ALIGNED_CLASS = Tag.CLASS_CONTEXT_SPECIFIC; //context spec
	
	//some state vars
	//ENCODE TYPE - wtf, ASN is really mind blowing, cmon....
	//If Amit reads this, he will smile.
	
	//ENCODE AS.... boom
	protected boolean oid = false;
	protected boolean integer = false;
	protected boolean objDescriptor = false;
	
	//actual vals
	protected long[] oidValue = null;
	protected long indirectReference= 0;
	protected Object objDescriptorValue = null;
	
	//ENCoDING
	private boolean asn = false;
	private boolean octet = false;
	private boolean arbitrary = false;
	
	
	
	public void decode(AsnInputStream ais) throws AsnException, IOException
	{
		//external tag has been read, lets read LEN
		int len = ais.readLength();
		if(ais.available()<len)
		{
			throw new AsnException("Wrong len, not enough data.");
		}
		
		//read encode type tag
		//FIXME: primitive tags...
		int tag = ais.readTag();
		//we can have one of
		if(tag == Tag.OBJECT_IDENTIFIER)
		{
			
		
			this.oidValue = ais.readObjectIdentifier();
			this.oid = true;
			
		}else if (tag == Tag.INTEGER)
		{
			this.indirectReference = ais.readInteger();
			this.integer = true;
		}else if (tag == Tag.OBJECT_DESCRIPTOR)
		{
			throw new AsnException();
		}else
		{
			throw new AsnException();
		}
			
	}
	
	public byte[] endecode() throws AsnException, IOException
	{
		//something to do encoding
		AsnOutputStream localOutput = new AsnOutputStream();
		if(oid)
		{
			localOutput.writeObjectIdentifier(this.oidValue);
		}else if(integer)
		{
			//AAAAMIT, why it read long, write int? cmon?
			localOutput.writeInteger(this.indirectReference);
		}else if(objDescriptor)
		{
			throw new AsnException();
		}else
		{
			throw new AsnException();
		}
		
		
		
		
		byte[] childData = encodeType();
		//told, you, mind blowing!
		
		if(asn)
		{
			localOutput.writeTag(_TAG_ASN_CLASS, _TAG_ASN_PC_PRIMITIVE, _TAG_ASN);
			localOutput.writeLength(childData.length);
			localOutput.write(childData);
			//childData = localOutput.toByteArray();
			//localOutput.reset();
		}else if (octet)
		{
			//get child class.... I think its done like that....
			boolean childConstructor = ((childData[0] & 0x20) >> 5) == Tag.PC_PRIMITIVITE;
			localOutput.writeTag(_TAG_OCTET_ALIGNED_CLASS, childConstructor, _TAG_OCTET_ALIGNED);
			localOutput.writeLength(childData.length);
			localOutput.write(childData);
			//childData = localOutput.toByteArray();
			//localOutput.reset();
		}else if(arbitrary)
		{
			boolean childConstructor = ((childData[0] & 0x20) >> 5) == Tag.PC_PRIMITIVITE;
			localOutput.writeTag(_TAG_ARBITRARY_CLASS, childConstructor, _TAG_ARBITRARY);
			localOutput.writeLength(childData.length);
			localOutput.write(childData);
			//childData = localOutput.toByteArray();
			//localOutput.reset();
		}else
		{
			throw new AsnException();
		}
	
		childData = localOutput.toByteArray();
		localOutput.reset();
		localOutput.writeTag(Tag.CLASS_UNIVERSAL, false, Tag.EXTERNAL);
		localOutput.writeLength(childData.length);
		localOutput.write(childData);
		return localOutput.toByteArray();
	}
	
	public abstract byte[] encodeType() throws AsnException;
	
	
	/**
	 * @return the oid
	 */
	public boolean isOid() {
		return oid;
	}
	/**
	 * @param oid the oid to set
	 */
	public void setOid(boolean oid) {
		this.oid = oid;
	}
	/**
	 * @return the integer
	 */
	public boolean isInteger() {
		return integer;
	}
	/**
	 * @param integer the integer to set
	 */
	public void setInteger(boolean integer) {
		this.integer = integer;
	}
	/**
	 * @return the objDescriptor
	 */
	public boolean isObjDescriptor() {
		return objDescriptor;
	}
	/**
	 * @param objDescriptor the objDescriptor to set
	 */
	public void setObjDescriptor(boolean objDescriptor) {
		this.objDescriptor = objDescriptor;
	}
	/**
	 * @return the oidValue
	 */
	public long[] getOidValue() {
		return oidValue;
	}
	/**
	 * @param oidValue the oidValue to set
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
	 * @param integerValue the integerValue to set
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
	 * @param objDescriptorValue the objDescriptorValue to set
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
	 * @param asn the asn to set
	 */
	public void setAsn(boolean asn) {
		this.asn = asn;
	}
	/**
	 * @return the octet
	 */
	public boolean isOctet() {
		return octet;
	}
	/**
	 * @param octet the octet to set
	 */
	public void setOctet(boolean octet) {
		this.octet = octet;
	}
	/**
	 * @return the arbitrary
	 */
	public boolean isArbitrary() {
		return arbitrary;
	}
	/**
	 * @param arbitrary the arbitrary to set
	 */
	public void setArbitrary(boolean arbitrary) {
		this.arbitrary = arbitrary;
	}

	
}
