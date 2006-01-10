/**
 * ActionItemType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.action;

public class ActionItemType  implements java.io.Serializable {
    private java.lang.String name;
    private java.lang.String title;
    private java.lang.String description;
    private boolean adHocPropertiesAllowed;
    private org.alfresco.repo.webservice.action.ParameterDefinition[] parameterDefinition;

    public ActionItemType() {
    }

    public ActionItemType(
           java.lang.String name,
           java.lang.String title,
           java.lang.String description,
           boolean adHocPropertiesAllowed,
           org.alfresco.repo.webservice.action.ParameterDefinition[] parameterDefinition) {
           this.name = name;
           this.title = title;
           this.description = description;
           this.adHocPropertiesAllowed = adHocPropertiesAllowed;
           this.parameterDefinition = parameterDefinition;
    }


    /**
     * Gets the name value for this ActionItemType.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this ActionItemType.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the title value for this ActionItemType.
     * 
     * @return title
     */
    public java.lang.String getTitle() {
        return title;
    }


    /**
     * Sets the title value for this ActionItemType.
     * 
     * @param title
     */
    public void setTitle(java.lang.String title) {
        this.title = title;
    }


    /**
     * Gets the description value for this ActionItemType.
     * 
     * @return description
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this ActionItemType.
     * 
     * @param description
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }


    /**
     * Gets the adHocPropertiesAllowed value for this ActionItemType.
     * 
     * @return adHocPropertiesAllowed
     */
    public boolean isAdHocPropertiesAllowed() {
        return adHocPropertiesAllowed;
    }


    /**
     * Sets the adHocPropertiesAllowed value for this ActionItemType.
     * 
     * @param adHocPropertiesAllowed
     */
    public void setAdHocPropertiesAllowed(boolean adHocPropertiesAllowed) {
        this.adHocPropertiesAllowed = adHocPropertiesAllowed;
    }


    /**
     * Gets the parameterDefinition value for this ActionItemType.
     * 
     * @return parameterDefinition
     */
    public org.alfresco.repo.webservice.action.ParameterDefinition[] getParameterDefinition() {
        return parameterDefinition;
    }


    /**
     * Sets the parameterDefinition value for this ActionItemType.
     * 
     * @param parameterDefinition
     */
    public void setParameterDefinition(org.alfresco.repo.webservice.action.ParameterDefinition[] parameterDefinition) {
        this.parameterDefinition = parameterDefinition;
    }

    public org.alfresco.repo.webservice.action.ParameterDefinition getParameterDefinition(int i) {
        return this.parameterDefinition[i];
    }

    public void setParameterDefinition(int i, org.alfresco.repo.webservice.action.ParameterDefinition _value) {
        this.parameterDefinition[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ActionItemType)) return false;
        ActionItemType other = (ActionItemType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.title==null && other.getTitle()==null) || 
             (this.title!=null &&
              this.title.equals(other.getTitle()))) &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
            this.adHocPropertiesAllowed == other.isAdHocPropertiesAllowed() &&
            ((this.parameterDefinition==null && other.getParameterDefinition()==null) || 
             (this.parameterDefinition!=null &&
              java.util.Arrays.equals(this.parameterDefinition, other.getParameterDefinition())));
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
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getTitle() != null) {
            _hashCode += getTitle().hashCode();
        }
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        _hashCode += (isAdHocPropertiesAllowed() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getParameterDefinition() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getParameterDefinition());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getParameterDefinition(), i);
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
        new org.apache.axis.description.TypeDesc(ActionItemType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "ActionItemType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
        elemField.setFieldName("adHocPropertiesAllowed");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "adHocPropertiesAllowed"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parameterDefinition");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "parameterDefinition"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "ParameterDefinition"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
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
