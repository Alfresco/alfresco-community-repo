/**
 * CMLCreateAssociation.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.types;

public class CMLCreateAssociation  implements java.io.Serializable {
    private org.alfresco.repo.webservice.types.Predicate from;

    private java.lang.String from_id;

    private org.alfresco.repo.webservice.types.Predicate to;

    private java.lang.String to_id;

    private java.lang.String association;

    public CMLCreateAssociation() {
    }

    public CMLCreateAssociation(
           org.alfresco.repo.webservice.types.Predicate from,
           java.lang.String from_id,
           org.alfresco.repo.webservice.types.Predicate to,
           java.lang.String to_id,
           java.lang.String association) {
           this.from = from;
           this.from_id = from_id;
           this.to = to;
           this.to_id = to_id;
           this.association = association;
    }


    /**
     * Gets the from value for this CMLCreateAssociation.
     * 
     * @return from
     */
    public org.alfresco.repo.webservice.types.Predicate getFrom() {
        return from;
    }


    /**
     * Sets the from value for this CMLCreateAssociation.
     * 
     * @param from
     */
    public void setFrom(org.alfresco.repo.webservice.types.Predicate from) {
        this.from = from;
    }


    /**
     * Gets the from_id value for this CMLCreateAssociation.
     * 
     * @return from_id
     */
    public java.lang.String getFrom_id() {
        return from_id;
    }


    /**
     * Sets the from_id value for this CMLCreateAssociation.
     * 
     * @param from_id
     */
    public void setFrom_id(java.lang.String from_id) {
        this.from_id = from_id;
    }


    /**
     * Gets the to value for this CMLCreateAssociation.
     * 
     * @return to
     */
    public org.alfresco.repo.webservice.types.Predicate getTo() {
        return to;
    }


    /**
     * Sets the to value for this CMLCreateAssociation.
     * 
     * @param to
     */
    public void setTo(org.alfresco.repo.webservice.types.Predicate to) {
        this.to = to;
    }


    /**
     * Gets the to_id value for this CMLCreateAssociation.
     * 
     * @return to_id
     */
    public java.lang.String getTo_id() {
        return to_id;
    }


    /**
     * Sets the to_id value for this CMLCreateAssociation.
     * 
     * @param to_id
     */
    public void setTo_id(java.lang.String to_id) {
        this.to_id = to_id;
    }


    /**
     * Gets the association value for this CMLCreateAssociation.
     * 
     * @return association
     */
    public java.lang.String getAssociation() {
        return association;
    }


    /**
     * Sets the association value for this CMLCreateAssociation.
     * 
     * @param association
     */
    public void setAssociation(java.lang.String association) {
        this.association = association;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CMLCreateAssociation)) return false;
        CMLCreateAssociation other = (CMLCreateAssociation) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.from==null && other.getFrom()==null) || 
             (this.from!=null &&
              this.from.equals(other.getFrom()))) &&
            ((this.from_id==null && other.getFrom_id()==null) || 
             (this.from_id!=null &&
              this.from_id.equals(other.getFrom_id()))) &&
            ((this.to==null && other.getTo()==null) || 
             (this.to!=null &&
              this.to.equals(other.getTo()))) &&
            ((this.to_id==null && other.getTo_id()==null) || 
             (this.to_id!=null &&
              this.to_id.equals(other.getTo_id()))) &&
            ((this.association==null && other.getAssociation()==null) || 
             (this.association!=null &&
              this.association.equals(other.getAssociation())));
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
        if (getFrom() != null) {
            _hashCode += getFrom().hashCode();
        }
        if (getFrom_id() != null) {
            _hashCode += getFrom_id().hashCode();
        }
        if (getTo() != null) {
            _hashCode += getTo().hashCode();
        }
        if (getTo_id() != null) {
            _hashCode += getTo_id().hashCode();
        }
        if (getAssociation() != null) {
            _hashCode += getAssociation().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CMLCreateAssociation.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>createAssociation"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("from");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "from"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Predicate"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("from_id");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "from_id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("to");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "to"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Predicate"));
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
        elemField.setFieldName("association");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "association"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
