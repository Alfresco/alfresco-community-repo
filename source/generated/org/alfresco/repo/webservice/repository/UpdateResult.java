/**
 * UpdateResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.repository;

public class UpdateResult  implements java.io.Serializable {
    private java.lang.String statement;
    private org.alfresco.repo.webservice.types.ResultSet updateCount;
    private org.alfresco.repo.webservice.types.Reference source;
    private org.alfresco.repo.webservice.types.Reference destination;

    public UpdateResult() {
    }

    public UpdateResult(
           java.lang.String statement,
           org.alfresco.repo.webservice.types.ResultSet updateCount,
           org.alfresco.repo.webservice.types.Reference source,
           org.alfresco.repo.webservice.types.Reference destination) {
           this.statement = statement;
           this.updateCount = updateCount;
           this.source = source;
           this.destination = destination;
    }


    /**
     * Gets the statement value for this UpdateResult.
     * 
     * @return statement
     */
    public java.lang.String getStatement() {
        return statement;
    }


    /**
     * Sets the statement value for this UpdateResult.
     * 
     * @param statement
     */
    public void setStatement(java.lang.String statement) {
        this.statement = statement;
    }


    /**
     * Gets the updateCount value for this UpdateResult.
     * 
     * @return updateCount
     */
    public org.alfresco.repo.webservice.types.ResultSet getUpdateCount() {
        return updateCount;
    }


    /**
     * Sets the updateCount value for this UpdateResult.
     * 
     * @param updateCount
     */
    public void setUpdateCount(org.alfresco.repo.webservice.types.ResultSet updateCount) {
        this.updateCount = updateCount;
    }


    /**
     * Gets the source value for this UpdateResult.
     * 
     * @return source
     */
    public org.alfresco.repo.webservice.types.Reference getSource() {
        return source;
    }


    /**
     * Sets the source value for this UpdateResult.
     * 
     * @param source
     */
    public void setSource(org.alfresco.repo.webservice.types.Reference source) {
        this.source = source;
    }


    /**
     * Gets the destination value for this UpdateResult.
     * 
     * @return destination
     */
    public org.alfresco.repo.webservice.types.Reference getDestination() {
        return destination;
    }


    /**
     * Sets the destination value for this UpdateResult.
     * 
     * @param destination
     */
    public void setDestination(org.alfresco.repo.webservice.types.Reference destination) {
        this.destination = destination;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UpdateResult)) return false;
        UpdateResult other = (UpdateResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.statement==null && other.getStatement()==null) || 
             (this.statement!=null &&
              this.statement.equals(other.getStatement()))) &&
            ((this.updateCount==null && other.getUpdateCount()==null) || 
             (this.updateCount!=null &&
              this.updateCount.equals(other.getUpdateCount()))) &&
            ((this.source==null && other.getSource()==null) || 
             (this.source!=null &&
              this.source.equals(other.getSource()))) &&
            ((this.destination==null && other.getDestination()==null) || 
             (this.destination!=null &&
              this.destination.equals(other.getDestination())));
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
        if (getStatement() != null) {
            _hashCode += getStatement().hashCode();
        }
        if (getUpdateCount() != null) {
            _hashCode += getUpdateCount().hashCode();
        }
        if (getSource() != null) {
            _hashCode += getSource().hashCode();
        }
        if (getDestination() != null) {
            _hashCode += getDestination().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(UpdateResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "UpdateResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("statement");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "statement"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("updateCount");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "updateCount"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ResultSet"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("source");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "source"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("destination");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "destination"));
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
