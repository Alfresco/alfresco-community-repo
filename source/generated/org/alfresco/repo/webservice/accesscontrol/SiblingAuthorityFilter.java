/**
 * SiblingAuthorityFilter.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.accesscontrol;

public class SiblingAuthorityFilter  implements java.io.Serializable {
    private java.lang.String authorityType;

    private boolean immediate;

    public SiblingAuthorityFilter() {
    }

    public SiblingAuthorityFilter(
           java.lang.String authorityType,
           boolean immediate) {
           this.authorityType = authorityType;
           this.immediate = immediate;
    }


    /**
     * Gets the authorityType value for this SiblingAuthorityFilter.
     * 
     * @return authorityType
     */
    public java.lang.String getAuthorityType() {
        return authorityType;
    }


    /**
     * Sets the authorityType value for this SiblingAuthorityFilter.
     * 
     * @param authorityType
     */
    public void setAuthorityType(java.lang.String authorityType) {
        this.authorityType = authorityType;
    }


    /**
     * Gets the immediate value for this SiblingAuthorityFilter.
     * 
     * @return immediate
     */
    public boolean isImmediate() {
        return immediate;
    }


    /**
     * Sets the immediate value for this SiblingAuthorityFilter.
     * 
     * @param immediate
     */
    public void setImmediate(boolean immediate) {
        this.immediate = immediate;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SiblingAuthorityFilter)) return false;
        SiblingAuthorityFilter other = (SiblingAuthorityFilter) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.authorityType==null && other.getAuthorityType()==null) || 
             (this.authorityType!=null &&
              this.authorityType.equals(other.getAuthorityType()))) &&
            this.immediate == other.isImmediate();
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getAuthorityType() != null) {
            _hashCode += getAuthorityType().hashCode();
        }
        _hashCode += (isImmediate() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SiblingAuthorityFilter.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/accesscontrol/1.0", "SiblingAuthorityFilter"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("authorityType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/accesscontrol/1.0", "authorityType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("immediate");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/accesscontrol/1.0", "immediate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
