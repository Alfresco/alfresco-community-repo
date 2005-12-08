/**
 * ClassDefinition.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.example.webservice.types;

public class ClassDefinition  implements java.io.Serializable {
    private java.lang.String name;
    private java.lang.String title;
    private java.lang.String description;
    private java.lang.String superClass;
    private boolean isAspect;
    private org.alfresco.example.webservice.types.PropertyDefinition[] properties;
    private org.alfresco.example.webservice.types.AssociationDefinition[] associations;

    public ClassDefinition() {
    }

    public ClassDefinition(
           java.lang.String name,
           java.lang.String title,
           java.lang.String description,
           java.lang.String superClass,
           boolean isAspect,
           org.alfresco.example.webservice.types.PropertyDefinition[] properties,
           org.alfresco.example.webservice.types.AssociationDefinition[] associations) {
           this.name = name;
           this.title = title;
           this.description = description;
           this.superClass = superClass;
           this.isAspect = isAspect;
           this.properties = properties;
           this.associations = associations;
    }


    /**
     * Gets the name value for this ClassDefinition.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this ClassDefinition.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the title value for this ClassDefinition.
     * 
     * @return title
     */
    public java.lang.String getTitle() {
        return title;
    }


    /**
     * Sets the title value for this ClassDefinition.
     * 
     * @param title
     */
    public void setTitle(java.lang.String title) {
        this.title = title;
    }


    /**
     * Gets the description value for this ClassDefinition.
     * 
     * @return description
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this ClassDefinition.
     * 
     * @param description
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }


    /**
     * Gets the superClass value for this ClassDefinition.
     * 
     * @return superClass
     */
    public java.lang.String getSuperClass() {
        return superClass;
    }


    /**
     * Sets the superClass value for this ClassDefinition.
     * 
     * @param superClass
     */
    public void setSuperClass(java.lang.String superClass) {
        this.superClass = superClass;
    }


    /**
     * Gets the isAspect value for this ClassDefinition.
     * 
     * @return isAspect
     */
    public boolean isIsAspect() {
        return isAspect;
    }


    /**
     * Sets the isAspect value for this ClassDefinition.
     * 
     * @param isAspect
     */
    public void setIsAspect(boolean isAspect) {
        this.isAspect = isAspect;
    }


    /**
     * Gets the properties value for this ClassDefinition.
     * 
     * @return properties
     */
    public org.alfresco.example.webservice.types.PropertyDefinition[] getProperties() {
        return properties;
    }


    /**
     * Sets the properties value for this ClassDefinition.
     * 
     * @param properties
     */
    public void setProperties(org.alfresco.example.webservice.types.PropertyDefinition[] properties) {
        this.properties = properties;
    }

    public org.alfresco.example.webservice.types.PropertyDefinition getProperties(int i) {
        return this.properties[i];
    }

    public void setProperties(int i, org.alfresco.example.webservice.types.PropertyDefinition _value) {
        this.properties[i] = _value;
    }


    /**
     * Gets the associations value for this ClassDefinition.
     * 
     * @return associations
     */
    public org.alfresco.example.webservice.types.AssociationDefinition[] getAssociations() {
        return associations;
    }


    /**
     * Sets the associations value for this ClassDefinition.
     * 
     * @param associations
     */
    public void setAssociations(org.alfresco.example.webservice.types.AssociationDefinition[] associations) {
        this.associations = associations;
    }

    public org.alfresco.example.webservice.types.AssociationDefinition getAssociations(int i) {
        return this.associations[i];
    }

    public void setAssociations(int i, org.alfresco.example.webservice.types.AssociationDefinition _value) {
        this.associations[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ClassDefinition)) return false;
        ClassDefinition other = (ClassDefinition) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.title==null && other.getTitle()==null) || 
             (this.title!=null &&
              this.title.equals(other.getTitle()))) &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
            ((this.superClass==null && other.getSuperClass()==null) || 
             (this.superClass!=null &&
              this.superClass.equals(other.getSuperClass()))) &&
            this.isAspect == other.isIsAspect() &&
            ((this.properties==null && other.getProperties()==null) || 
             (this.properties!=null &&
              java.util.Arrays.equals(this.properties, other.getProperties()))) &&
            ((this.associations==null && other.getAssociations()==null) || 
             (this.associations!=null &&
              java.util.Arrays.equals(this.associations, other.getAssociations())));
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
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getTitle() != null) {
            _hashCode += getTitle().hashCode();
        }
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        if (getSuperClass() != null) {
            _hashCode += getSuperClass().hashCode();
        }
        _hashCode += (isIsAspect() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getProperties() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getProperties());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getProperties(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getAssociations() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAssociations());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAssociations(), i);
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
        new org.apache.axis.description.TypeDesc(ClassDefinition.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "ClassDefinition"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Name"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("title");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "title"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("superClass");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "superClass"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Name"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isAspect");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "isAspect"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("properties");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "properties"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "PropertyDefinition"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("associations");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "associations"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "AssociationDefinition"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
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
