/**
 * CMLMove.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.types;

public class CMLMove  implements java.io.Serializable {
    private org.alfresco.repo.webservice.types.ParentReference to;

    private java.lang.String to_id;

    private java.lang.String associationType;

    private java.lang.String childName;

    private org.alfresco.repo.webservice.types.Predicate where;

    private java.lang.String where_id;

    public CMLMove() {
    }

    public CMLMove(
           org.alfresco.repo.webservice.types.ParentReference to,
           java.lang.String to_id,
           java.lang.String associationType,
           java.lang.String childName,
           org.alfresco.repo.webservice.types.Predicate where,
           java.lang.String where_id) {
           this.to = to;
           this.to_id = to_id;
           this.associationType = associationType;
           this.childName = childName;
           this.where = where;
           this.where_id = where_id;
    }


    /**
     * Gets the to value for this CMLMove.
     * 
     * @return to
     */
    public org.alfresco.repo.webservice.types.ParentReference getTo() {
        return to;
    }


    /**
     * Sets the to value for this CMLMove.
     * 
     * @param to
     */
    public void setTo(org.alfresco.repo.webservice.types.ParentReference to) {
        this.to = to;
    }


    /**
     * Gets the to_id value for this CMLMove.
     * 
     * @return to_id
     */
    public java.lang.String getTo_id() {
        return to_id;
    }


    /**
     * Sets the to_id value for this CMLMove.
     * 
     * @param to_id
     */
    public void setTo_id(java.lang.String to_id) {
        this.to_id = to_id;
    }


    /**
     * Gets the associationType value for this CMLMove.
     * 
     * @return associationType
     */
    public java.lang.String getAssociationType() {
        return associationType;
    }


    /**
     * Sets the associationType value for this CMLMove.
     * 
     * @param associationType
     */
    public void setAssociationType(java.lang.String associationType) {
        this.associationType = associationType;
    }


    /**
     * Gets the childName value for this CMLMove.
     * 
     * @return childName
     */
    public java.lang.String getChildName() {
        return childName;
    }


    /**
     * Sets the childName value for this CMLMove.
     * 
     * @param childName
     */
    public void setChildName(java.lang.String childName) {
        this.childName = childName;
    }


    /**
     * Gets the where value for this CMLMove.
     * 
     * @return where
     */
    public org.alfresco.repo.webservice.types.Predicate getWhere() {
        return where;
    }


    /**
     * Sets the where value for this CMLMove.
     * 
     * @param where
     */
    public void setWhere(org.alfresco.repo.webservice.types.Predicate where) {
        this.where = where;
    }


    /**
     * Gets the where_id value for this CMLMove.
     * 
     * @return where_id
     */
    public java.lang.String getWhere_id() {
        return where_id;
    }


    /**
     * Sets the where_id value for this CMLMove.
     * 
     * @param where_id
     */
    public void setWhere_id(java.lang.String where_id) {
        this.where_id = where_id;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CMLMove)) return false;
        CMLMove other = (CMLMove) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.to==null && other.getTo()==null) || 
             (this.to!=null &&
              this.to.equals(other.getTo()))) &&
            ((this.to_id==null && other.getTo_id()==null) || 
             (this.to_id!=null &&
              this.to_id.equals(other.getTo_id()))) &&
            ((this.associationType==null && other.getAssociationType()==null) || 
             (this.associationType!=null &&
              this.associationType.equals(other.getAssociationType()))) &&
            ((this.childName==null && other.getChildName()==null) || 
             (this.childName!=null &&
              this.childName.equals(other.getChildName()))) &&
            ((this.where==null && other.getWhere()==null) || 
             (this.where!=null &&
              this.where.equals(other.getWhere()))) &&
            ((this.where_id==null && other.getWhere_id()==null) || 
             (this.where_id!=null &&
              this.where_id.equals(other.getWhere_id())));
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
        if (getTo() != null) {
            _hashCode += getTo().hashCode();
        }
        if (getTo_id() != null) {
            _hashCode += getTo_id().hashCode();
        }
        if (getAssociationType() != null) {
            _hashCode += getAssociationType().hashCode();
        }
        if (getChildName() != null) {
            _hashCode += getChildName().hashCode();
        }
        if (getWhere() != null) {
            _hashCode += getWhere().hashCode();
        }
        if (getWhere_id() != null) {
            _hashCode += getWhere_id().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CMLMove.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>move"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("to");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "to"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ParentReference"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("to_id");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "to_id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("associationType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "associationType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("childName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "childName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("where");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "where"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Predicate"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("where_id");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "where_id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
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
