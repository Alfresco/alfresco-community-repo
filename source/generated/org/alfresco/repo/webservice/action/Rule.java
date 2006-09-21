/**
 * Rule.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.action;

public class Rule  implements java.io.Serializable {
    private org.alfresco.repo.webservice.types.Reference ruleReference;

    private org.alfresco.repo.webservice.types.Reference owningReference;

    private java.lang.String[] ruleTypes;

    private java.lang.String title;

    private java.lang.String description;

    private boolean executeAsynchronously;

    private org.alfresco.repo.webservice.action.Action action;

    public Rule() {
    }

    public Rule(
           org.alfresco.repo.webservice.types.Reference ruleReference,
           org.alfresco.repo.webservice.types.Reference owningReference,
           java.lang.String[] ruleTypes,
           java.lang.String title,
           java.lang.String description,
           boolean executeAsynchronously,
           org.alfresco.repo.webservice.action.Action action) {
           this.ruleReference = ruleReference;
           this.owningReference = owningReference;
           this.ruleTypes = ruleTypes;
           this.title = title;
           this.description = description;
           this.executeAsynchronously = executeAsynchronously;
           this.action = action;
    }


    /**
     * Gets the ruleReference value for this Rule.
     * 
     * @return ruleReference
     */
    public org.alfresco.repo.webservice.types.Reference getRuleReference() {
        return ruleReference;
    }


    /**
     * Sets the ruleReference value for this Rule.
     * 
     * @param ruleReference
     */
    public void setRuleReference(org.alfresco.repo.webservice.types.Reference ruleReference) {
        this.ruleReference = ruleReference;
    }


    /**
     * Gets the owningReference value for this Rule.
     * 
     * @return owningReference
     */
    public org.alfresco.repo.webservice.types.Reference getOwningReference() {
        return owningReference;
    }


    /**
     * Sets the owningReference value for this Rule.
     * 
     * @param owningReference
     */
    public void setOwningReference(org.alfresco.repo.webservice.types.Reference owningReference) {
        this.owningReference = owningReference;
    }


    /**
     * Gets the ruleTypes value for this Rule.
     * 
     * @return ruleTypes
     */
    public java.lang.String[] getRuleTypes() {
        return ruleTypes;
    }


    /**
     * Sets the ruleTypes value for this Rule.
     * 
     * @param ruleTypes
     */
    public void setRuleTypes(java.lang.String[] ruleTypes) {
        this.ruleTypes = ruleTypes;
    }

    public java.lang.String getRuleTypes(int i) {
        return this.ruleTypes[i];
    }

    public void setRuleTypes(int i, java.lang.String _value) {
        this.ruleTypes[i] = _value;
    }


    /**
     * Gets the title value for this Rule.
     * 
     * @return title
     */
    public java.lang.String getTitle() {
        return title;
    }


    /**
     * Sets the title value for this Rule.
     * 
     * @param title
     */
    public void setTitle(java.lang.String title) {
        this.title = title;
    }


    /**
     * Gets the description value for this Rule.
     * 
     * @return description
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this Rule.
     * 
     * @param description
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }


    /**
     * Gets the executeAsynchronously value for this Rule.
     * 
     * @return executeAsynchronously
     */
    public boolean isExecuteAsynchronously() {
        return executeAsynchronously;
    }


    /**
     * Sets the executeAsynchronously value for this Rule.
     * 
     * @param executeAsynchronously
     */
    public void setExecuteAsynchronously(boolean executeAsynchronously) {
        this.executeAsynchronously = executeAsynchronously;
    }


    /**
     * Gets the action value for this Rule.
     * 
     * @return action
     */
    public org.alfresco.repo.webservice.action.Action getAction() {
        return action;
    }


    /**
     * Sets the action value for this Rule.
     * 
     * @param action
     */
    public void setAction(org.alfresco.repo.webservice.action.Action action) {
        this.action = action;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Rule)) return false;
        Rule other = (Rule) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.ruleReference==null && other.getRuleReference()==null) || 
             (this.ruleReference!=null &&
              this.ruleReference.equals(other.getRuleReference()))) &&
            ((this.owningReference==null && other.getOwningReference()==null) || 
             (this.owningReference!=null &&
              this.owningReference.equals(other.getOwningReference()))) &&
            ((this.ruleTypes==null && other.getRuleTypes()==null) || 
             (this.ruleTypes!=null &&
              java.util.Arrays.equals(this.ruleTypes, other.getRuleTypes()))) &&
            ((this.title==null && other.getTitle()==null) || 
             (this.title!=null &&
              this.title.equals(other.getTitle()))) &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
            this.executeAsynchronously == other.isExecuteAsynchronously() &&
            ((this.action==null && other.getAction()==null) || 
             (this.action!=null &&
              this.action.equals(other.getAction())));
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
        if (getRuleReference() != null) {
            _hashCode += getRuleReference().hashCode();
        }
        if (getOwningReference() != null) {
            _hashCode += getOwningReference().hashCode();
        }
        if (getRuleTypes() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRuleTypes());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRuleTypes(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getTitle() != null) {
            _hashCode += getTitle().hashCode();
        }
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        _hashCode += (isExecuteAsynchronously() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getAction() != null) {
            _hashCode += getAction().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Rule.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "Rule"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ruleReference");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "ruleReference"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("owningReference");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "owningReference"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ruleTypes");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "ruleTypes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("title");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "title"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("executeAsynchronously");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "executeAsynchronously"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("action");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "action"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "Action"));
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
