/**
 * AssociationDefinition.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.types;

public class AssociationDefinition  implements java.io.Serializable {
    private java.lang.String name;

    private java.lang.String title;

    private java.lang.String description;

    private boolean isChild;

    private org.alfresco.repo.webservice.types.RoleDefinition sourceRole;

    private org.alfresco.repo.webservice.types.RoleDefinition targetRole;

    private java.lang.String targetClass;

    public AssociationDefinition() {
    }

    public AssociationDefinition(
           java.lang.String name,
           java.lang.String title,
           java.lang.String description,
           boolean isChild,
           org.alfresco.repo.webservice.types.RoleDefinition sourceRole,
           org.alfresco.repo.webservice.types.RoleDefinition targetRole,
           java.lang.String targetClass) {
           this.name = name;
           this.title = title;
           this.description = description;
           this.isChild = isChild;
           this.sourceRole = sourceRole;
           this.targetRole = targetRole;
           this.targetClass = targetClass;
    }


    /**
     * Gets the name value for this AssociationDefinition.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this AssociationDefinition.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the title value for this AssociationDefinition.
     * 
     * @return title
     */
    public java.lang.String getTitle() {
        return title;
    }


    /**
     * Sets the title value for this AssociationDefinition.
     * 
     * @param title
     */
    public void setTitle(java.lang.String title) {
        this.title = title;
    }


    /**
     * Gets the description value for this AssociationDefinition.
     * 
     * @return description
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this AssociationDefinition.
     * 
     * @param description
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }


    /**
     * Gets the isChild value for this AssociationDefinition.
     * 
     * @return isChild
     */
    public boolean isIsChild() {
        return isChild;
    }


    /**
     * Sets the isChild value for this AssociationDefinition.
     * 
     * @param isChild
     */
    public void setIsChild(boolean isChild) {
        this.isChild = isChild;
    }


    /**
     * Gets the sourceRole value for this AssociationDefinition.
     * 
     * @return sourceRole
     */
    public org.alfresco.repo.webservice.types.RoleDefinition getSourceRole() {
        return sourceRole;
    }


    /**
     * Sets the sourceRole value for this AssociationDefinition.
     * 
     * @param sourceRole
     */
    public void setSourceRole(org.alfresco.repo.webservice.types.RoleDefinition sourceRole) {
        this.sourceRole = sourceRole;
    }


    /**
     * Gets the targetRole value for this AssociationDefinition.
     * 
     * @return targetRole
     */
    public org.alfresco.repo.webservice.types.RoleDefinition getTargetRole() {
        return targetRole;
    }


    /**
     * Sets the targetRole value for this AssociationDefinition.
     * 
     * @param targetRole
     */
    public void setTargetRole(org.alfresco.repo.webservice.types.RoleDefinition targetRole) {
        this.targetRole = targetRole;
    }


    /**
     * Gets the targetClass value for this AssociationDefinition.
     * 
     * @return targetClass
     */
    public java.lang.String getTargetClass() {
        return targetClass;
    }


    /**
     * Sets the targetClass value for this AssociationDefinition.
     * 
     * @param targetClass
     */
    public void setTargetClass(java.lang.String targetClass) {
        this.targetClass = targetClass;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AssociationDefinition)) return false;
        AssociationDefinition other = (AssociationDefinition) obj;
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
            this.isChild == other.isIsChild() &&
            ((this.sourceRole==null && other.getSourceRole()==null) || 
             (this.sourceRole!=null &&
              this.sourceRole.equals(other.getSourceRole()))) &&
            ((this.targetRole==null && other.getTargetRole()==null) || 
             (this.targetRole!=null &&
              this.targetRole.equals(other.getTargetRole()))) &&
            ((this.targetClass==null && other.getTargetClass()==null) || 
             (this.targetClass!=null &&
              this.targetClass.equals(other.getTargetClass())));
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
        _hashCode += (isIsChild() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getSourceRole() != null) {
            _hashCode += getSourceRole().hashCode();
        }
        if (getTargetRole() != null) {
            _hashCode += getTargetRole().hashCode();
        }
        if (getTargetClass() != null) {
            _hashCode += getTargetClass().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AssociationDefinition.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "AssociationDefinition"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
        elemField.setFieldName("isChild");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "isChild"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sourceRole");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "sourceRole"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "RoleDefinition"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("targetRole");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "targetRole"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "RoleDefinition"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("targetClass");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "targetClass"));
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
