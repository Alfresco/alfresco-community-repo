/**
 * VersionResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.authoring;

public class VersionResult  implements java.io.Serializable {
    private org.alfresco.repo.webservice.types.Reference[] nodes;

    private org.alfresco.repo.webservice.types.Version[] versions;

    public VersionResult() {
    }

    public VersionResult(
           org.alfresco.repo.webservice.types.Reference[] nodes,
           org.alfresco.repo.webservice.types.Version[] versions) {
           this.nodes = nodes;
           this.versions = versions;
    }


    /**
     * Gets the nodes value for this VersionResult.
     * 
     * @return nodes
     */
    public org.alfresco.repo.webservice.types.Reference[] getNodes() {
        return nodes;
    }


    /**
     * Sets the nodes value for this VersionResult.
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
     * Gets the versions value for this VersionResult.
     * 
     * @return versions
     */
    public org.alfresco.repo.webservice.types.Version[] getVersions() {
        return versions;
    }


    /**
     * Sets the versions value for this VersionResult.
     * 
     * @param versions
     */
    public void setVersions(org.alfresco.repo.webservice.types.Version[] versions) {
        this.versions = versions;
    }

    public org.alfresco.repo.webservice.types.Version getVersions(int i) {
        return this.versions[i];
    }

    public void setVersions(int i, org.alfresco.repo.webservice.types.Version _value) {
        this.versions[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof VersionResult)) return false;
        VersionResult other = (VersionResult) obj;
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
            ((this.versions==null && other.getVersions()==null) || 
             (this.versions!=null &&
              java.util.Arrays.equals(this.versions, other.getVersions())));
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
        if (getVersions() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getVersions());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getVersions(), i);
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
        new org.apache.axis.description.TypeDesc(VersionResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "VersionResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nodes");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "nodes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("versions");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "versions"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Version"));
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
