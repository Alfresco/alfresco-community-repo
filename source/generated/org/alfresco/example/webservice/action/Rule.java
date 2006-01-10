/**
 * Rule.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.action;

public class Rule  implements java.io.Serializable {
    private java.lang.String id;
    private org.alfresco.example.webservice.action.RuleType ruleType;
    private java.lang.String title;
    private java.lang.String description;
    private boolean executeAsynchronously;
    private org.alfresco.example.webservice.action.Condition[] conditions;
    private org.alfresco.example.webservice.action.Action[] actions;
    private java.lang.String runAsUserName;
    private org.alfresco.example.webservice.types.Reference reference;

    public Rule() {
    }

    public Rule(
           java.lang.String id,
           org.alfresco.example.webservice.action.RuleType ruleType,
           java.lang.String title,
           java.lang.String description,
           boolean executeAsynchronously,
           org.alfresco.example.webservice.action.Condition[] conditions,
           org.alfresco.example.webservice.action.Action[] actions,
           java.lang.String runAsUserName,
           org.alfresco.example.webservice.types.Reference reference) {
           this.id = id;
           this.ruleType = ruleType;
           this.title = title;
           this.description = description;
           this.executeAsynchronously = executeAsynchronously;
           this.conditions = conditions;
           this.actions = actions;
           this.runAsUserName = runAsUserName;
           this.reference = reference;
    }


    /**
     * Gets the id value for this Rule.
     * 
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this Rule.
     * 
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }


    /**
     * Gets the ruleType value for this Rule.
     * 
     * @return ruleType
     */
    public org.alfresco.example.webservice.action.RuleType getRuleType() {
        return ruleType;
    }


    /**
     * Sets the ruleType value for this Rule.
     * 
     * @param ruleType
     */
    public void setRuleType(org.alfresco.example.webservice.action.RuleType ruleType) {
        this.ruleType = ruleType;
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
     * Gets the conditions value for this Rule.
     * 
     * @return conditions
     */
    public org.alfresco.example.webservice.action.Condition[] getConditions() {
        return conditions;
    }


    /**
     * Sets the conditions value for this Rule.
     * 
     * @param conditions
     */
    public void setConditions(org.alfresco.example.webservice.action.Condition[] conditions) {
        this.conditions = conditions;
    }

    public org.alfresco.example.webservice.action.Condition getConditions(int i) {
        return this.conditions[i];
    }

    public void setConditions(int i, org.alfresco.example.webservice.action.Condition _value) {
        this.conditions[i] = _value;
    }


    /**
     * Gets the actions value for this Rule.
     * 
     * @return actions
     */
    public org.alfresco.example.webservice.action.Action[] getActions() {
        return actions;
    }


    /**
     * Sets the actions value for this Rule.
     * 
     * @param actions
     */
    public void setActions(org.alfresco.example.webservice.action.Action[] actions) {
        this.actions = actions;
    }

    public org.alfresco.example.webservice.action.Action getActions(int i) {
        return this.actions[i];
    }

    public void setActions(int i, org.alfresco.example.webservice.action.Action _value) {
        this.actions[i] = _value;
    }


    /**
     * Gets the runAsUserName value for this Rule.
     * 
     * @return runAsUserName
     */
    public java.lang.String getRunAsUserName() {
        return runAsUserName;
    }


    /**
     * Sets the runAsUserName value for this Rule.
     * 
     * @param runAsUserName
     */
    public void setRunAsUserName(java.lang.String runAsUserName) {
        this.runAsUserName = runAsUserName;
    }


    /**
     * Gets the reference value for this Rule.
     * 
     * @return reference
     */
    public org.alfresco.example.webservice.types.Reference getReference() {
        return reference;
    }


    /**
     * Sets the reference value for this Rule.
     * 
     * @param reference
     */
    public void setReference(org.alfresco.example.webservice.types.Reference reference) {
        this.reference = reference;
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
            ((this.id==null && other.getId()==null) || 
             (this.id!=null &&
              this.id.equals(other.getId()))) &&
            ((this.ruleType==null && other.getRuleType()==null) || 
             (this.ruleType!=null &&
              this.ruleType.equals(other.getRuleType()))) &&
            ((this.title==null && other.getTitle()==null) || 
             (this.title!=null &&
              this.title.equals(other.getTitle()))) &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
            this.executeAsynchronously == other.isExecuteAsynchronously() &&
            ((this.conditions==null && other.getConditions()==null) || 
             (this.conditions!=null &&
              java.util.Arrays.equals(this.conditions, other.getConditions()))) &&
            ((this.actions==null && other.getActions()==null) || 
             (this.actions!=null &&
              java.util.Arrays.equals(this.actions, other.getActions()))) &&
            ((this.runAsUserName==null && other.getRunAsUserName()==null) || 
             (this.runAsUserName!=null &&
              this.runAsUserName.equals(other.getRunAsUserName()))) &&
            ((this.reference==null && other.getReference()==null) || 
             (this.reference!=null &&
              this.reference.equals(other.getReference())));
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
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getRuleType() != null) {
            _hashCode += getRuleType().hashCode();
        }
        if (getTitle() != null) {
            _hashCode += getTitle().hashCode();
        }
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        _hashCode += (isExecuteAsynchronously() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getConditions() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getConditions());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getConditions(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getActions() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getActions());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getActions(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getRunAsUserName() != null) {
            _hashCode += getRunAsUserName().hashCode();
        }
        if (getReference() != null) {
            _hashCode += getReference().hashCode();
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
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ruleType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "ruleType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "RuleType"));
        elemField.setNillable(false);
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
        elemField.setFieldName("conditions");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "conditions"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "Condition"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("actions");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "actions"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "Action"));
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("runAsUserName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "runAsUserName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("reference");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "reference"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"));
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
