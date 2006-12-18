/**
 * ParentReference.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.types;

public class ParentReference  extends org.alfresco.repo.webservice.types.Reference  implements java.io.Serializable {
    private java.lang.String associationType;

    private java.lang.String childName;

    public ParentReference() {
    }

    public ParentReference(
           org.alfresco.repo.webservice.types.Store store,
           java.lang.String uuid,
           java.lang.String path,
           java.lang.String associationType,
           java.lang.String childName) {
        super(
            store,
            uuid,
            path);
        this.associationType = associationType;
        this.childName = childName;
    }


    /**
     * Gets the associationType value for this ParentReference.
     * 
     * @return associationType
     */
    public java.lang.String getAssociationType() {
        return associationType;
    }


    /**
     * Sets the associationType value for this ParentReference.
     * 
     * @param associationType
     */
    public void setAssociationType(java.lang.String associationType) {
        this.associationType = associationType;
    }


    /**
     * Gets the childName value for this ParentReference.
     * 
     * @return childName
     */
    public java.lang.String getChildName() {
        return childName;
    }


    /**
     * Sets the childName value for this ParentReference.
     * 
     * @param childName
     */
    public void setChildName(java.lang.String childName) {
        this.childName = childName;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ParentReference)) return false;
        ParentReference other = (ParentReference) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.associationType==null && other.getAssociationType()==null) || 
             (this.associationType!=null &&
              this.associationType.equals(other.getAssociationType()))) &&
            ((this.childName==null && other.getChildName()==null) || 
             (this.childName!=null &&
              this.childName.equals(other.getChildName())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getAssociationType() != null) {
            _hashCode += getAssociationType().hashCode();
        }
        if (getChildName() != null) {
            _hashCode += getChildName().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ParentReference.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ParentReference"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("associationType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "associationType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("childName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "childName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
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
