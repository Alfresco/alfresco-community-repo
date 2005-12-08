/**
 * CheckoutResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.authoring;

public class CheckoutResult  implements java.io.Serializable {
    private org.alfresco.example.webservice.types.Reference[] originals;
    private org.alfresco.example.webservice.types.Reference[] workingCopies;

    public CheckoutResult() {
    }

    public CheckoutResult(
           org.alfresco.example.webservice.types.Reference[] originals,
           org.alfresco.example.webservice.types.Reference[] workingCopies) {
           this.originals = originals;
           this.workingCopies = workingCopies;
    }


    /**
     * Gets the originals value for this CheckoutResult.
     * 
     * @return originals
     */
    public org.alfresco.example.webservice.types.Reference[] getOriginals() {
        return originals;
    }


    /**
     * Sets the originals value for this CheckoutResult.
     * 
     * @param originals
     */
    public void setOriginals(org.alfresco.example.webservice.types.Reference[] originals) {
        this.originals = originals;
    }

    public org.alfresco.example.webservice.types.Reference getOriginals(int i) {
        return this.originals[i];
    }

    public void setOriginals(int i, org.alfresco.example.webservice.types.Reference _value) {
        this.originals[i] = _value;
    }


    /**
     * Gets the workingCopies value for this CheckoutResult.
     * 
     * @return workingCopies
     */
    public org.alfresco.example.webservice.types.Reference[] getWorkingCopies() {
        return workingCopies;
    }


    /**
     * Sets the workingCopies value for this CheckoutResult.
     * 
     * @param workingCopies
     */
    public void setWorkingCopies(org.alfresco.example.webservice.types.Reference[] workingCopies) {
        this.workingCopies = workingCopies;
    }

    public org.alfresco.example.webservice.types.Reference getWorkingCopies(int i) {
        return this.workingCopies[i];
    }

    public void setWorkingCopies(int i, org.alfresco.example.webservice.types.Reference _value) {
        this.workingCopies[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CheckoutResult)) return false;
        CheckoutResult other = (CheckoutResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.originals==null && other.getOriginals()==null) || 
             (this.originals!=null &&
              java.util.Arrays.equals(this.originals, other.getOriginals()))) &&
            ((this.workingCopies==null && other.getWorkingCopies()==null) || 
             (this.workingCopies!=null &&
              java.util.Arrays.equals(this.workingCopies, other.getWorkingCopies())));
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
        if (getOriginals() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getOriginals());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getOriginals(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getWorkingCopies() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getWorkingCopies());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getWorkingCopies(), i);
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
        new org.apache.axis.description.TypeDesc(CheckoutResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "CheckoutResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("originals");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "originals"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workingCopies");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "workingCopies"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"));
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
