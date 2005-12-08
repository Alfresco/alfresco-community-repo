/**
 * Association.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.repository;

public class Association  implements java.io.Serializable {
    private java.lang.String associationType;
    private org.alfresco.repo.webservice.repository.AssociationDirectionEnum direction;

    public Association() {
    }

    public Association(
           java.lang.String associationType,
           org.alfresco.repo.webservice.repository.AssociationDirectionEnum direction) {
           this.associationType = associationType;
           this.direction = direction;
    }


    /**
     * Gets the associationType value for this Association.
     * 
     * @return associationType
     */
    public java.lang.String getAssociationType() {
        return associationType;
    }


    /**
     * Sets the associationType value for this Association.
     * 
     * @param associationType
     */
    public void setAssociationType(java.lang.String associationType) {
        this.associationType = associationType;
    }


    /**
     * Gets the direction value for this Association.
     * 
     * @return direction
     */
    public org.alfresco.repo.webservice.repository.AssociationDirectionEnum getDirection() {
        return direction;
    }


    /**
     * Sets the direction value for this Association.
     * 
     * @param direction
     */
    public void setDirection(org.alfresco.repo.webservice.repository.AssociationDirectionEnum direction) {
        this.direction = direction;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Association)) return false;
        Association other = (Association) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.associationType==null && other.getAssociationType()==null) || 
             (this.associationType!=null &&
              this.associationType.equals(other.getAssociationType()))) &&
            ((this.direction==null && other.getDirection()==null) || 
             (this.direction!=null &&
              this.direction.equals(other.getDirection())));
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
        if (getAssociationType() != null) {
            _hashCode += getAssociationType().hashCode();
        }
        if (getDirection() != null) {
            _hashCode += getDirection().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Association.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "Association"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("associationType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "associationType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Name"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("direction");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "direction"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/repository/1.0", "AssociationDirectionEnum"));
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
