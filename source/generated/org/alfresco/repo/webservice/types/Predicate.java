/**
 * Predicate.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.types;

public class Predicate  implements java.io.Serializable {
    private org.alfresco.repo.webservice.types.Reference[] nodes;

    private org.alfresco.repo.webservice.types.Store store;

    private org.alfresco.repo.webservice.types.Query query;

    public Predicate() {
    }

    public Predicate(
           org.alfresco.repo.webservice.types.Reference[] nodes,
           org.alfresco.repo.webservice.types.Store store,
           org.alfresco.repo.webservice.types.Query query) {
           this.nodes = nodes;
           this.store = store;
           this.query = query;
    }


    /**
     * Gets the nodes value for this Predicate.
     * 
     * @return nodes
     */
    public org.alfresco.repo.webservice.types.Reference[] getNodes() {
        return nodes;
    }


    /**
     * Sets the nodes value for this Predicate.
     * 
     * @param nodes
     */
    public void setNodes(org.alfresco.repo.webservice.types.Reference[] nodes) {
        this.nodes = nodes;
    }

    public org.alfresco.repo.webservice.types.Reference getNodes(int i) {
        return this.nodes[i];
    }

    public void setNodes(int i, org.alfresco.repo.webservice.types.Reference _value) {
        this.nodes[i] = _value;
    }


    /**
     * Gets the store value for this Predicate.
     * 
     * @return store
     */
    public org.alfresco.repo.webservice.types.Store getStore() {
        return store;
    }


    /**
     * Sets the store value for this Predicate.
     * 
     * @param store
     */
    public void setStore(org.alfresco.repo.webservice.types.Store store) {
        this.store = store;
    }


    /**
     * Gets the query value for this Predicate.
     * 
     * @return query
     */
    public org.alfresco.repo.webservice.types.Query getQuery() {
        return query;
    }


    /**
     * Sets the query value for this Predicate.
     * 
     * @param query
     */
    public void setQuery(org.alfresco.repo.webservice.types.Query query) {
        this.query = query;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Predicate)) return false;
        Predicate other = (Predicate) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.nodes==null && other.getNodes()==null) || 
             (this.nodes!=null &&
              java.util.Arrays.equals(this.nodes, other.getNodes()))) &&
            ((this.store==null && other.getStore()==null) || 
             (this.store!=null &&
              this.store.equals(other.getStore()))) &&
            ((this.query==null && other.getQuery()==null) || 
             (this.query!=null &&
              this.query.equals(other.getQuery())));
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
        if (getNodes() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getNodes());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getNodes(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getStore() != null) {
            _hashCode += getStore().hashCode();
        }
        if (getQuery() != null) {
            _hashCode += getQuery().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Predicate.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Predicate"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nodes");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "nodes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("store");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "store"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Store"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("query");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "query"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Query"));
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
