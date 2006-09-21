/**
 * NodeDefinition.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.types;

public class NodeDefinition  implements java.io.Serializable {
    private org.alfresco.repo.webservice.types.ClassDefinition type;

    private org.alfresco.repo.webservice.types.ClassDefinition[] aspects;

    public NodeDefinition() {
    }

    public NodeDefinition(
           org.alfresco.repo.webservice.types.ClassDefinition type,
           org.alfresco.repo.webservice.types.ClassDefinition[] aspects) {
           this.type = type;
           this.aspects = aspects;
    }


    /**
     * Gets the type value for this NodeDefinition.
     * 
     * @return type
     */
    public org.alfresco.repo.webservice.types.ClassDefinition getType() {
        return type;
    }


    /**
     * Sets the type value for this NodeDefinition.
     * 
     * @param type
     */
    public void setType(org.alfresco.repo.webservice.types.ClassDefinition type) {
        this.type = type;
    }


    /**
     * Gets the aspects value for this NodeDefinition.
     * 
     * @return aspects
     */
    public org.alfresco.repo.webservice.types.ClassDefinition[] getAspects() {
        return aspects;
    }


    /**
     * Sets the aspects value for this NodeDefinition.
     * 
     * @param aspects
     */
    public void setAspects(org.alfresco.repo.webservice.types.ClassDefinition[] aspects) {
        this.aspects = aspects;
    }

    public org.alfresco.repo.webservice.types.ClassDefinition getAspects(int i) {
        return this.aspects[i];
    }

    public void setAspects(int i, org.alfresco.repo.webservice.types.ClassDefinition _value) {
        this.aspects[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NodeDefinition)) return false;
        NodeDefinition other = (NodeDefinition) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.type==null && other.getType()==null) || 
             (this.type!=null &&
              this.type.equals(other.getType()))) &&
            ((this.aspects==null && other.getAspects()==null) || 
             (this.aspects!=null &&
              java.util.Arrays.equals(this.aspects, other.getAspects())));
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
        if (getType() != null) {
            _hashCode += getType().hashCode();
        }
        if (getAspects() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAspects());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAspects(), i);
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
        new org.apache.axis.description.TypeDesc(NodeDefinition.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "NodeDefinition"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("type");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "type"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ClassDefinition"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("aspects");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "aspects"));
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
