/**
 * ResultSetMetaData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.types;

public class ResultSetMetaData  implements java.io.Serializable {
    private org.alfresco.repo.webservice.types.ValueDefinition[] valueDefs;
    private org.alfresco.repo.webservice.types.ClassDefinition[] classDefs;

    public ResultSetMetaData() {
    }

    public ResultSetMetaData(
           org.alfresco.repo.webservice.types.ValueDefinition[] valueDefs,
           org.alfresco.repo.webservice.types.ClassDefinition[] classDefs) {
           this.valueDefs = valueDefs;
           this.classDefs = classDefs;
    }


    /**
     * Gets the valueDefs value for this ResultSetMetaData.
     * 
     * @return valueDefs
     */
    public org.alfresco.repo.webservice.types.ValueDefinition[] getValueDefs() {
        return valueDefs;
    }


    /**
     * Sets the valueDefs value for this ResultSetMetaData.
     * 
     * @param valueDefs
     */
    public void setValueDefs(org.alfresco.repo.webservice.types.ValueDefinition[] valueDefs) {
        this.valueDefs = valueDefs;
    }

    public org.alfresco.repo.webservice.types.ValueDefinition getValueDefs(int i) {
        return this.valueDefs[i];
    }

    public void setValueDefs(int i, org.alfresco.repo.webservice.types.ValueDefinition _value) {
        this.valueDefs[i] = _value;
    }


    /**
     * Gets the classDefs value for this ResultSetMetaData.
     * 
     * @return classDefs
     */
    public org.alfresco.repo.webservice.types.ClassDefinition[] getClassDefs() {
        return classDefs;
    }


    /**
     * Sets the classDefs value for this ResultSetMetaData.
     * 
     * @param classDefs
     */
    public void setClassDefs(org.alfresco.repo.webservice.types.ClassDefinition[] classDefs) {
        this.classDefs = classDefs;
    }

    public org.alfresco.repo.webservice.types.ClassDefinition getClassDefs(int i) {
        return this.classDefs[i];
    }

    public void setClassDefs(int i, org.alfresco.repo.webservice.types.ClassDefinition _value) {
        this.classDefs[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ResultSetMetaData)) return false;
        ResultSetMetaData other = (ResultSetMetaData) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.valueDefs==null && other.getValueDefs()==null) || 
             (this.valueDefs!=null &&
              java.util.Arrays.equals(this.valueDefs, other.getValueDefs()))) &&
            ((this.classDefs==null && other.getClassDefs()==null) || 
             (this.classDefs!=null &&
              java.util.Arrays.equals(this.classDefs, other.getClassDefs())));
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
        if (getValueDefs() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getValueDefs());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getValueDefs(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getClassDefs() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getClassDefs());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getClassDefs(), i);
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
        new org.apache.axis.description.TypeDesc(ResultSetMetaData.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ResultSetMetaData"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("valueDefs");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "valueDefs"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ValueDefinition"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("classDefs");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "classDefs"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ClassDefinition"));
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
