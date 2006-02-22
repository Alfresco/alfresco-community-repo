/**
 * VersionHistory.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.types;

public class VersionHistory  implements java.io.Serializable {
    private java.lang.String marker;

    private org.alfresco.repo.webservice.types.Version[] versions;

    public VersionHistory() {
    }

    public VersionHistory(
           java.lang.String marker,
           org.alfresco.repo.webservice.types.Version[] versions) {
           this.marker = marker;
           this.versions = versions;
    }


    /**
     * Gets the marker value for this VersionHistory.
     * 
     * @return marker
     */
    public java.lang.String getMarker() {
        return marker;
    }


    /**
     * Sets the marker value for this VersionHistory.
     * 
     * @param marker
     */
    public void setMarker(java.lang.String marker) {
        this.marker = marker;
    }


    /**
     * Gets the versions value for this VersionHistory.
     * 
     * @return versions
     */
    public org.alfresco.repo.webservice.types.Version[] getVersions() {
        return versions;
    }


    /**
     * Sets the versions value for this VersionHistory.
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
        if (!(obj instanceof VersionHistory)) return false;
        VersionHistory other = (VersionHistory) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.marker==null && other.getMarker()==null) || 
             (this.marker!=null &&
              this.marker.equals(other.getMarker()))) &&
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
        if (getMarker() != null) {
            _hashCode += getMarker().hashCode();
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
        new org.apache.axis.description.TypeDesc(VersionHistory.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "VersionHistory"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("marker");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "marker"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("versions");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "versions"));
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
