/**
 * HasPermissionsResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.accesscontrol;

public class HasPermissionsResult  implements java.io.Serializable {
    private org.alfresco.repo.webservice.types.Reference reference;

    private java.lang.String permission;

    private org.alfresco.repo.webservice.accesscontrol.AccessStatus accessStatus;

    public HasPermissionsResult() {
    }

    public HasPermissionsResult(
           org.alfresco.repo.webservice.types.Reference reference,
           java.lang.String permission,
           org.alfresco.repo.webservice.accesscontrol.AccessStatus accessStatus) {
           this.reference = reference;
           this.permission = permission;
           this.accessStatus = accessStatus;
    }


    /**
     * Gets the reference value for this HasPermissionsResult.
     * 
     * @return reference
     */
    public org.alfresco.repo.webservice.types.Reference getReference() {
        return reference;
    }


    /**
     * Sets the reference value for this HasPermissionsResult.
     * 
     * @param reference
     */
    public void setReference(org.alfresco.repo.webservice.types.Reference reference) {
        this.reference = reference;
    }


    /**
     * Gets the permission value for this HasPermissionsResult.
     * 
     * @return permission
     */
    public java.lang.String getPermission() {
        return permission;
    }


    /**
     * Sets the permission value for this HasPermissionsResult.
     * 
     * @param permission
     */
    public void setPermission(java.lang.String permission) {
        this.permission = permission;
    }


    /**
     * Gets the accessStatus value for this HasPermissionsResult.
     * 
     * @return accessStatus
     */
    public org.alfresco.repo.webservice.accesscontrol.AccessStatus getAccessStatus() {
        return accessStatus;
    }


    /**
     * Sets the accessStatus value for this HasPermissionsResult.
     * 
     * @param accessStatus
     */
    public void setAccessStatus(org.alfresco.repo.webservice.accesscontrol.AccessStatus accessStatus) {
        this.accessStatus = accessStatus;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof HasPermissionsResult)) return false;
        HasPermissionsResult other = (HasPermissionsResult) obj;
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
            ((this.permission==null && other.getPermission()==null) || 
             (this.permission!=null &&
              this.permission.equals(other.getPermission()))) &&
            ((this.accessStatus==null && other.getAccessStatus()==null) || 
             (this.accessStatus!=null &&
              this.accessStatus.equals(other.getAccessStatus())));
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
        if (getPermission() != null) {
            _hashCode += getPermission().hashCode();
        }
        if (getAccessStatus() != null) {
            _hashCode += getAccessStatus().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(HasPermissionsResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/accesscontrol/1.0", "HasPermissionsResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("reference");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/accesscontrol/1.0", "reference"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("permission");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/accesscontrol/1.0", "permission"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("accessStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/accesscontrol/1.0", "accessStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/accesscontrol/1.0", "AccessStatus"));
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
