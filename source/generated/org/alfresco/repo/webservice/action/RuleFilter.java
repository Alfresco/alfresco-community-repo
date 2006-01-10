/**
 * RuleFilter.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.action;

public class RuleFilter  implements java.io.Serializable {
    private java.lang.String[] ids;
    private java.lang.String[] ruleTypeNames;
    private boolean includeInherited;

    public RuleFilter() {
    }

    public RuleFilter(
           java.lang.String[] ids,
           java.lang.String[] ruleTypeNames,
           boolean includeInherited) {
           this.ids = ids;
           this.ruleTypeNames = ruleTypeNames;
           this.includeInherited = includeInherited;
    }


    /**
     * Gets the ids value for this RuleFilter.
     * 
     * @return ids
     */
    public java.lang.String[] getIds() {
        return ids;
    }


    /**
     * Sets the ids value for this RuleFilter.
     * 
     * @param ids
     */
    public void setIds(java.lang.String[] ids) {
        this.ids = ids;
    }

    public java.lang.String getIds(int i) {
        return this.ids[i];
    }

    public void setIds(int i, java.lang.String _value) {
        this.ids[i] = _value;
    }


    /**
     * Gets the ruleTypeNames value for this RuleFilter.
     * 
     * @return ruleTypeNames
     */
    public java.lang.String[] getRuleTypeNames() {
        return ruleTypeNames;
    }


    /**
     * Sets the ruleTypeNames value for this RuleFilter.
     * 
     * @param ruleTypeNames
     */
    public void setRuleTypeNames(java.lang.String[] ruleTypeNames) {
        this.ruleTypeNames = ruleTypeNames;
    }

    public java.lang.String getRuleTypeNames(int i) {
        return this.ruleTypeNames[i];
    }

    public void setRuleTypeNames(int i, java.lang.String _value) {
        this.ruleTypeNames[i] = _value;
    }


    /**
     * Gets the includeInherited value for this RuleFilter.
     * 
     * @return includeInherited
     */
    public boolean isIncludeInherited() {
        return includeInherited;
    }


    /**
     * Sets the includeInherited value for this RuleFilter.
     * 
     * @param includeInherited
     */
    public void setIncludeInherited(boolean includeInherited) {
        this.includeInherited = includeInherited;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RuleFilter)) return false;
        RuleFilter other = (RuleFilter) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.ids==null && other.getIds()==null) || 
             (this.ids!=null &&
              java.util.Arrays.equals(this.ids, other.getIds()))) &&
            ((this.ruleTypeNames==null && other.getRuleTypeNames()==null) || 
             (this.ruleTypeNames!=null &&
              java.util.Arrays.equals(this.ruleTypeNames, other.getRuleTypeNames()))) &&
            this.includeInherited == other.isIncludeInherited();
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
        if (getIds() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getIds());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getIds(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getRuleTypeNames() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRuleTypeNames());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRuleTypeNames(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        _hashCode += (isIncludeInherited() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RuleFilter.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "RuleFilter"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ids");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "ids"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ruleTypeNames");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "ruleTypeNames"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("includeInherited");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "includeInherited"));
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
