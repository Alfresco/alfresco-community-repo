/**
 * ResultSet.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.types;

public class ResultSet  implements java.io.Serializable {
    private org.alfresco.example.webservice.types.ResultSetRow[] rows;
    private long totalRowCount;
    private org.alfresco.example.webservice.types.ResultSetMetaData metaData;

    public ResultSet() {
    }

    public ResultSet(
           org.alfresco.example.webservice.types.ResultSetRow[] rows,
           long totalRowCount,
           org.alfresco.example.webservice.types.ResultSetMetaData metaData) {
           this.rows = rows;
           this.totalRowCount = totalRowCount;
           this.metaData = metaData;
    }


    /**
     * Gets the rows value for this ResultSet.
     * 
     * @return rows
     */
    public org.alfresco.example.webservice.types.ResultSetRow[] getRows() {
        return rows;
    }


    /**
     * Sets the rows value for this ResultSet.
     * 
     * @param rows
     */
    public void setRows(org.alfresco.example.webservice.types.ResultSetRow[] rows) {
        this.rows = rows;
    }

    public org.alfresco.example.webservice.types.ResultSetRow getRows(int i) {
        return this.rows[i];
    }

    public void setRows(int i, org.alfresco.example.webservice.types.ResultSetRow _value) {
        this.rows[i] = _value;
    }


    /**
     * Gets the totalRowCount value for this ResultSet.
     * 
     * @return totalRowCount
     */
    public long getTotalRowCount() {
        return totalRowCount;
    }


    /**
     * Sets the totalRowCount value for this ResultSet.
     * 
     * @param totalRowCount
     */
    public void setTotalRowCount(long totalRowCount) {
        this.totalRowCount = totalRowCount;
    }


    /**
     * Gets the metaData value for this ResultSet.
     * 
     * @return metaData
     */
    public org.alfresco.example.webservice.types.ResultSetMetaData getMetaData() {
        return metaData;
    }


    /**
     * Sets the metaData value for this ResultSet.
     * 
     * @param metaData
     */
    public void setMetaData(org.alfresco.example.webservice.types.ResultSetMetaData metaData) {
        this.metaData = metaData;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ResultSet)) return false;
        ResultSet other = (ResultSet) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.rows==null && other.getRows()==null) || 
             (this.rows!=null &&
              java.util.Arrays.equals(this.rows, other.getRows()))) &&
            this.totalRowCount == other.getTotalRowCount() &&
            ((this.metaData==null && other.getMetaData()==null) || 
             (this.metaData!=null &&
              this.metaData.equals(other.getMetaData())));
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
        if (getRows() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRows());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRows(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        _hashCode += new Long(getTotalRowCount()).hashCode();
        if (getMetaData() != null) {
            _hashCode += getMetaData().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ResultSet.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ResultSet"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("rows");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "rows"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ResultSetRow"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("totalRowCount");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "totalRowCount"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("metaData");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "metaData"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ResultSetMetaData"));
        elemField.setMinOccurs(0);
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
