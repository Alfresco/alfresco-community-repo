/**
 * QueryResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.repository;

public class QueryResult  implements java.io.Serializable {
    private java.lang.String querySession;
    private org.alfresco.example.webservice.types.ResultSet resultSet;

    public QueryResult() {
    }

    public QueryResult(
           java.lang.String querySession,
           org.alfresco.example.webservice.types.ResultSet resultSet) {
           this.querySession = querySession;
           this.resultSet = resultSet;
    }


    /**
     * Gets the querySession value for this QueryResult.
     * 
     * @return querySession
     */
    public java.lang.String getQuerySession() {
        return querySession;
    }


    /**
     * Sets the querySession value for this QueryResult.
     * 
     * @param querySession
     */
    public void setQuerySession(java.lang.String querySession) {
        this.querySession = querySession;
    }


    /**
     * Gets the resultSet value for this QueryResult.
     * 
     * @return resultSet
     */
    public org.alfresco.example.webservice.types.ResultSet getResultSet() {
        return resultSet;
    }


    /**
     * Sets the resultSet value for this QueryResult.
     * 
     * @param resultSet
     */
    public void setResultSet(org.alfresco.example.webservice.types.ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof QueryResult)) return false;
        QueryResult other = (QueryResult) obj;
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
            ((this.resultSet==null && other.getResultSet()==null) || 
             (this.resultSet!=null &&
              this.resultSet.equals(other.getResultSet())));
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
        if (getResultSet() != null) {
            _hashCode += getResultSet().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(QueryResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "QueryResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("querySession");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "querySession"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("resultSet");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "resultSet"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ResultSet"));
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
