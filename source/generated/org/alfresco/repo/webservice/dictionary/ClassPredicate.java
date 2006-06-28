/**
 * ClassPredicate.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.dictionary;

public class ClassPredicate  implements java.io.Serializable {
    private java.lang.String[] names;

    private boolean followSubClass;

    private boolean followSuperClass;

    public ClassPredicate() {
    }

    public ClassPredicate(
           java.lang.String[] names,
           boolean followSubClass,
           boolean followSuperClass) {
           this.names = names;
           this.followSubClass = followSubClass;
           this.followSuperClass = followSuperClass;
    }


    /**
     * Gets the names value for this ClassPredicate.
     * 
     * @return names
     */
    public java.lang.String[] getNames() {
        return names;
    }


    /**
     * Sets the names value for this ClassPredicate.
     * 
     * @param names
     */
    public void setNames(java.lang.String[] names) {
        this.names = names;
    }

    public java.lang.String getNames(int i) {
        return this.names[i];
    }

    public void setNames(int i, java.lang.String _value) {
        this.names[i] = _value;
    }


    /**
     * Gets the followSubClass value for this ClassPredicate.
     * 
     * @return followSubClass
     */
    public boolean isFollowSubClass() {
        return followSubClass;
    }


    /**
     * Sets the followSubClass value for this ClassPredicate.
     * 
     * @param followSubClass
     */
    public void setFollowSubClass(boolean followSubClass) {
        this.followSubClass = followSubClass;
    }


    /**
     * Gets the followSuperClass value for this ClassPredicate.
     * 
     * @return followSuperClass
     */
    public boolean isFollowSuperClass() {
        return followSuperClass;
    }


    /**
     * Sets the followSuperClass value for this ClassPredicate.
     * 
     * @param followSuperClass
     */
    public void setFollowSuperClass(boolean followSuperClass) {
        this.followSuperClass = followSuperClass;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ClassPredicate)) return false;
        ClassPredicate other = (ClassPredicate) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.names==null && other.getNames()==null) || 
             (this.names!=null &&
              java.util.Arrays.equals(this.names, other.getNames()))) &&
            this.followSubClass == other.isFollowSubClass() &&
            this.followSuperClass == other.isFollowSuperClass();
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
        if (getNames() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getNames());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getNames(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        _hashCode += (isFollowSubClass() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (isFollowSuperClass() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ClassPredicate.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/dictionary/1.0", "ClassPredicate"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("names");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/dictionary/1.0", "names"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("followSubClass");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/dictionary/1.0", "followSubClass"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("followSuperClass");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/dictionary/1.0", "followSuperClass"));
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
