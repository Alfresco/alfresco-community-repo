/**
 * ActionExecutionResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.action;

public class ActionExecutionResult  implements java.io.Serializable {
    private org.alfresco.repo.webservice.types.Reference reference;

    private org.alfresco.repo.webservice.action.Action[] actions;

    public ActionExecutionResult() {
    }

    public ActionExecutionResult(
           org.alfresco.repo.webservice.types.Reference reference,
           org.alfresco.repo.webservice.action.Action[] actions) {
           this.reference = reference;
           this.actions = actions;
    }


    /**
     * Gets the reference value for this ActionExecutionResult.
     * 
     * @return reference
     */
    public org.alfresco.repo.webservice.types.Reference getReference() {
        return reference;
    }


    /**
     * Sets the reference value for this ActionExecutionResult.
     * 
     * @param reference
     */
    public void setReference(org.alfresco.repo.webservice.types.Reference reference) {
        this.reference = reference;
    }


    /**
     * Gets the actions value for this ActionExecutionResult.
     * 
     * @return actions
     */
    public org.alfresco.repo.webservice.action.Action[] getActions() {
        return actions;
    }


    /**
     * Sets the actions value for this ActionExecutionResult.
     * 
     * @param actions
     */
    public void setActions(org.alfresco.repo.webservice.action.Action[] actions) {
        this.actions = actions;
    }

    public org.alfresco.repo.webservice.action.Action getActions(int i) {
        return this.actions[i];
    }

    public void setActions(int i, org.alfresco.repo.webservice.action.Action _value) {
        this.actions[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ActionExecutionResult)) return false;
        ActionExecutionResult other = (ActionExecutionResult) obj;
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
            ((this.actions==null && other.getActions()==null) || 
             (this.actions!=null &&
              java.util.Arrays.equals(this.actions, other.getActions())));
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
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ActionExecutionResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "ActionExecutionResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("reference");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "reference"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("actions");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "actions"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "Action"));
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
