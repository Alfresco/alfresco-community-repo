/**
 * UserQueryResults.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.administration;

public class UserQueryResults  implements java.io.Serializable {
    private java.lang.String querySession;
    private org.alfresco.repo.webservice.administration.UserDetails[] userDetails;

    public UserQueryResults() {
    }

    public UserQueryResults(
           java.lang.String querySession,
           org.alfresco.repo.webservice.administration.UserDetails[] userDetails) {
           this.querySession = querySession;
           this.userDetails = userDetails;
    }


    /**
     * Gets the querySession value for this UserQueryResults.
     * 
     * @return querySession
     */
    public java.lang.String getQuerySession() {
        return querySession;
    }


    /**
     * Sets the querySession value for this UserQueryResults.
     * 
     * @param querySession
     */
    public void setQuerySession(java.lang.String querySession) {
        this.querySession = querySession;
    }


    /**
     * Gets the userDetails value for this UserQueryResults.
     * 
     * @return userDetails
     */
    public org.alfresco.repo.webservice.administration.UserDetails[] getUserDetails() {
        return userDetails;
    }


    /**
     * Sets the userDetails value for this UserQueryResults.
     * 
     * @param userDetails
     */
    public void setUserDetails(org.alfresco.repo.webservice.administration.UserDetails[] userDetails) {
        this.userDetails = userDetails;
    }

    public org.alfresco.repo.webservice.administration.UserDetails getUserDetails(int i) {
        return this.userDetails[i];
    }

    public void setUserDetails(int i, org.alfresco.repo.webservice.administration.UserDetails _value) {
        this.userDetails[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UserQueryResults)) return false;
        UserQueryResults other = (UserQueryResults) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.querySession==null && other.getQuerySession()==null) || 
             (this.querySession!=null &&
              this.querySession.equals(other.getQuerySession()))) &&
            ((this.userDetails==null && other.getUserDetails()==null) || 
             (this.userDetails!=null &&
              java.util.Arrays.equals(this.userDetails, other.getUserDetails())));
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
        if (getQuerySession() != null) {
            _hashCode += getQuerySession().hashCode();
        }
        if (getUserDetails() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getUserDetails());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getUserDetails(), i);
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
        new org.apache.axis.description.TypeDesc(UserQueryResults.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/administration/1.0", "UserQueryResults"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("querySession");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/administration/1.0", "querySession"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userDetails");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/administration/1.0", "userDetails"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/administration/1.0", "UserDetails"));
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
