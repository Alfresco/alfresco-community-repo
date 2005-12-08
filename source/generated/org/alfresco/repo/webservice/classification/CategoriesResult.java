/**
 * CategoriesResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.classification;

public class CategoriesResult  implements java.io.Serializable {
    private org.alfresco.repo.webservice.types.Reference node;
    private org.alfresco.repo.webservice.classification.AppliedCategory[] categories;

    public CategoriesResult() {
    }

    public CategoriesResult(
           org.alfresco.repo.webservice.types.Reference node,
           org.alfresco.repo.webservice.classification.AppliedCategory[] categories) {
           this.node = node;
           this.categories = categories;
    }


    /**
     * Gets the node value for this CategoriesResult.
     * 
     * @return node
     */
    public org.alfresco.repo.webservice.types.Reference getNode() {
        return node;
    }


    /**
     * Sets the node value for this CategoriesResult.
     * 
     * @param node
     */
    public void setNode(org.alfresco.repo.webservice.types.Reference node) {
        this.node = node;
    }


    /**
     * Gets the categories value for this CategoriesResult.
     * 
     * @return categories
     */
    public org.alfresco.repo.webservice.classification.AppliedCategory[] getCategories() {
        return categories;
    }


    /**
     * Sets the categories value for this CategoriesResult.
     * 
     * @param categories
     */
    public void setCategories(org.alfresco.repo.webservice.classification.AppliedCategory[] categories) {
        this.categories = categories;
    }

    public org.alfresco.repo.webservice.classification.AppliedCategory getCategories(int i) {
        return this.categories[i];
    }

    public void setCategories(int i, org.alfresco.repo.webservice.classification.AppliedCategory _value) {
        this.categories[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CategoriesResult)) return false;
        CategoriesResult other = (CategoriesResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.node==null && other.getNode()==null) || 
             (this.node!=null &&
              this.node.equals(other.getNode()))) &&
            ((this.categories==null && other.getCategories()==null) || 
             (this.categories!=null &&
              java.util.Arrays.equals(this.categories, other.getCategories())));
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
        if (getNode() != null) {
            _hashCode += getNode().hashCode();
        }
        if (getCategories() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getCategories());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getCategories(), i);
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
        new org.apache.axis.description.TypeDesc(CategoriesResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "CategoriesResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("node");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "node"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("categories");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "categories"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/classification/1.0", "AppliedCategory"));
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
