/**
 * CheckinResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.authoring;

public class CheckinResult  implements java.io.Serializable {
    private org.alfresco.repo.webservice.types.Reference[] workingCopies;

    private org.alfresco.repo.webservice.types.Reference[] checkedIn;

    public CheckinResult() {
    }

    public CheckinResult(
           org.alfresco.repo.webservice.types.Reference[] workingCopies,
           org.alfresco.repo.webservice.types.Reference[] checkedIn) {
           this.workingCopies = workingCopies;
           this.checkedIn = checkedIn;
    }


    /**
     * Gets the workingCopies value for this CheckinResult.
     * 
     * @return workingCopies
     */
    public org.alfresco.repo.webservice.types.Reference[] getWorkingCopies() {
        return workingCopies;
    }


    /**
     * Sets the workingCopies value for this CheckinResult.
     * 
     * @param workingCopies
     */
    public void setWorkingCopies(org.alfresco.repo.webservice.types.Reference[] workingCopies) {
        this.workingCopies = workingCopies;
    }

    public org.alfresco.repo.webservice.types.Reference getWorkingCopies(int i) {
        return this.workingCopies[i];
    }

    public void setWorkingCopies(int i, org.alfresco.repo.webservice.types.Reference _value) {
        this.workingCopies[i] = _value;
    }


    /**
     * Gets the checkedIn value for this CheckinResult.
     * 
     * @return checkedIn
     */
    public org.alfresco.repo.webservice.types.Reference[] getCheckedIn() {
        return checkedIn;
    }


    /**
     * Sets the checkedIn value for this CheckinResult.
     * 
     * @param checkedIn
     */
    public void setCheckedIn(org.alfresco.repo.webservice.types.Reference[] checkedIn) {
        this.checkedIn = checkedIn;
    }

    public org.alfresco.repo.webservice.types.Reference getCheckedIn(int i) {
        return this.checkedIn[i];
    }

    public void setCheckedIn(int i, org.alfresco.repo.webservice.types.Reference _value) {
        this.checkedIn[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CheckinResult)) return false;
        CheckinResult other = (CheckinResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.workingCopies==null && other.getWorkingCopies()==null) || 
             (this.workingCopies!=null &&
              java.util.Arrays.equals(this.workingCopies, other.getWorkingCopies()))) &&
            ((this.checkedIn==null && other.getCheckedIn()==null) || 
             (this.checkedIn!=null &&
              java.util.Arrays.equals(this.checkedIn, other.getCheckedIn())));
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
        if (getWorkingCopies() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getWorkingCopies());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getWorkingCopies(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getCheckedIn() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getCheckedIn());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getCheckedIn(), i);
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
        new org.apache.axis.description.TypeDesc(CheckinResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "CheckinResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workingCopies");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "workingCopies"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("checkedIn");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/authoring/1.0", "checkedIn"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"));
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
