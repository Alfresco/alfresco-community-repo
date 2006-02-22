/**
 * ACE.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.accesscontrol;

public class ACE  implements java.io.Serializable {
    private java.lang.String authority;

    private java.lang.String permission;

    private org.alfresco.repo.webservice.accesscontrol.AccessStatus accessStatus;

    public ACE() {
    }

    public ACE(
           java.lang.String authority,
           java.lang.String permission,
           org.alfresco.repo.webservice.accesscontrol.AccessStatus accessStatus) {
           this.authority = authority;
           this.permission = permission;
           this.accessStatus = accessStatus;
    }


    /**
     * Gets the authority value for this ACE.
     * 
     * @return authority
     */
    public java.lang.String getAuthority() {
        return authority;
    }


    /**
     * Sets the authority value for this ACE.
     * 
     * @param authority
     */
    public void setAuthority(java.lang.String authority) {
        this.authority = authority;
    }


    /**
     * Gets the permission value for this ACE.
     * 
     * @return permission
     */
    public java.lang.String getPermission() {
        return permission;
    }


    /**
     * Sets the permission value for this ACE.
     * 
     * @param permission
     */
    public void setPermission(java.lang.String permission) {
        this.permission = permission;
    }


    /**
     * Gets the accessStatus value for this ACE.
     * 
     * @return accessStatus
     */
    public org.alfresco.repo.webservice.accesscontrol.AccessStatus getAccessStatus() {
        return accessStatus;
    }


    /**
     * Sets the accessStatus value for this ACE.
     * 
     * @param accessStatus
     */
    public void setAccessStatus(org.alfresco.repo.webservice.accesscontrol.AccessStatus accessStatus) {
        this.accessStatus = accessStatus;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ACE)) return false;
        ACE other = (ACE) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.authority==null && other.getAuthority()==null) || 
             (this.authority!=null &&
              this.authority.equals(other.getAuthority()))) &&
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
        if (getAuthority() != null) {
            _hashCode += getAuthority().hashCode();
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
        new org.apache.axis.description.TypeDesc(ACE.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/accesscontrol/1.0", "ACE"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("authority");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/accesscontrol/1.0", "authority"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
