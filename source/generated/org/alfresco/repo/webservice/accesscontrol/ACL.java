/**
 * ACL.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.accesscontrol;

public class ACL  implements java.io.Serializable {
    private org.alfresco.repo.webservice.types.Reference reference;
    private boolean inheritPermissions;
    private org.alfresco.repo.webservice.accesscontrol.ACE[] aces;

    public ACL() {
    }

    public ACL(
           org.alfresco.repo.webservice.types.Reference reference,
           boolean inheritPermissions,
           org.alfresco.repo.webservice.accesscontrol.ACE[] aces) {
           this.reference = reference;
           this.inheritPermissions = inheritPermissions;
           this.aces = aces;
    }


    /**
     * Gets the reference value for this ACL.
     * 
     * @return reference
     */
    public org.alfresco.repo.webservice.types.Reference getReference() {
        return reference;
    }


    /**
     * Sets the reference value for this ACL.
     * 
     * @param reference
     */
    public void setReference(org.alfresco.repo.webservice.types.Reference reference) {
        this.reference = reference;
    }


    /**
     * Gets the inheritPermissions value for this ACL.
     * 
     * @return inheritPermissions
     */
    public boolean isInheritPermissions() {
        return inheritPermissions;
    }


    /**
     * Sets the inheritPermissions value for this ACL.
     * 
     * @param inheritPermissions
     */
    public void setInheritPermissions(boolean inheritPermissions) {
        this.inheritPermissions = inheritPermissions;
    }


    /**
     * Gets the aces value for this ACL.
     * 
     * @return aces
     */
    public org.alfresco.repo.webservice.accesscontrol.ACE[] getAces() {
        return aces;
    }


    /**
     * Sets the aces value for this ACL.
     * 
     * @param aces
     */
    public void setAces(org.alfresco.repo.webservice.accesscontrol.ACE[] aces) {
        this.aces = aces;
    }

    public org.alfresco.repo.webservice.accesscontrol.ACE getAces(int i) {
        return this.aces[i];
    }

    public void setAces(int i, org.alfresco.repo.webservice.accesscontrol.ACE _value) {
        this.aces[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ACL)) return false;
        ACL other = (ACL) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.reference==null && other.getReference()==null) || 
             (this.reference!=null &&
              this.reference.equals(other.getReference()))) &&
            this.inheritPermissions == other.isInheritPermissions() &&
            ((this.aces==null && other.getAces()==null) || 
             (this.aces!=null &&
              java.util.Arrays.equals(this.aces, other.getAces())));
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
        if (getReference() != null) {
            _hashCode += getReference().hashCode();
        }
        _hashCode += (isInheritPermissions() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getAces() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAces());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAces(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ACL.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/accesscontrol/1.0", "ACL"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("reference");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/accesscontrol/1.0", "reference"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("inheritPermissions");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/accesscontrol/1.0", "inheritPermissions"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("aces");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/accesscontrol/1.0", "aces"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/accesscontrol/1.0", "ACE"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
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
