/**
 * CML.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.types;

public class CML  implements java.io.Serializable {
    private org.alfresco.repo.webservice.types.CMLCreate[] create;
    private org.alfresco.repo.webservice.types.CMLAddAspect[] addAspect;
    private org.alfresco.repo.webservice.types.CMLRemoveAspect[] removeAspect;
    private org.alfresco.repo.webservice.types.CMLUpdate[] update;
    private org.alfresco.repo.webservice.types.CMLDelete[] delete;
    private org.alfresco.repo.webservice.types.CMLMove[] move;
    private org.alfresco.repo.webservice.types.CMLCopy[] copy;
    private org.alfresco.repo.webservice.types.CMLAddChild[] addChild;
    private org.alfresco.repo.webservice.types.CMLRemoveChild[] removeChild;
    private org.alfresco.repo.webservice.types.CMLCreateAssociation[] createAssociation;
    private org.alfresco.repo.webservice.types.CMLRemoveAssociation[] removeAssociation;

    public CML() {
    }

    public CML(
           org.alfresco.repo.webservice.types.CMLCreate[] create,
           org.alfresco.repo.webservice.types.CMLAddAspect[] addAspect,
           org.alfresco.repo.webservice.types.CMLRemoveAspect[] removeAspect,
           org.alfresco.repo.webservice.types.CMLUpdate[] update,
           org.alfresco.repo.webservice.types.CMLDelete[] delete,
           org.alfresco.repo.webservice.types.CMLMove[] move,
           org.alfresco.repo.webservice.types.CMLCopy[] copy,
           org.alfresco.repo.webservice.types.CMLAddChild[] addChild,
           org.alfresco.repo.webservice.types.CMLRemoveChild[] removeChild,
           org.alfresco.repo.webservice.types.CMLCreateAssociation[] createAssociation,
           org.alfresco.repo.webservice.types.CMLRemoveAssociation[] removeAssociation) {
           this.create = create;
           this.addAspect = addAspect;
           this.removeAspect = removeAspect;
           this.update = update;
           this.delete = delete;
           this.move = move;
           this.copy = copy;
           this.addChild = addChild;
           this.removeChild = removeChild;
           this.createAssociation = createAssociation;
           this.removeAssociation = removeAssociation;
    }


    /**
     * Gets the create value for this CML.
     * 
     * @return create
     */
    public org.alfresco.repo.webservice.types.CMLCreate[] getCreate() {
        return create;
    }


    /**
     * Sets the create value for this CML.
     * 
     * @param create
     */
    public void setCreate(org.alfresco.repo.webservice.types.CMLCreate[] create) {
        this.create = create;
    }

    public org.alfresco.repo.webservice.types.CMLCreate getCreate(int i) {
        return this.create[i];
    }

    public void setCreate(int i, org.alfresco.repo.webservice.types.CMLCreate _value) {
        this.create[i] = _value;
    }


    /**
     * Gets the addAspect value for this CML.
     * 
     * @return addAspect
     */
    public org.alfresco.repo.webservice.types.CMLAddAspect[] getAddAspect() {
        return addAspect;
    }


    /**
     * Sets the addAspect value for this CML.
     * 
     * @param addAspect
     */
    public void setAddAspect(org.alfresco.repo.webservice.types.CMLAddAspect[] addAspect) {
        this.addAspect = addAspect;
    }

    public org.alfresco.repo.webservice.types.CMLAddAspect getAddAspect(int i) {
        return this.addAspect[i];
    }

    public void setAddAspect(int i, org.alfresco.repo.webservice.types.CMLAddAspect _value) {
        this.addAspect[i] = _value;
    }


    /**
     * Gets the removeAspect value for this CML.
     * 
     * @return removeAspect
     */
    public org.alfresco.repo.webservice.types.CMLRemoveAspect[] getRemoveAspect() {
        return removeAspect;
    }


    /**
     * Sets the removeAspect value for this CML.
     * 
     * @param removeAspect
     */
    public void setRemoveAspect(org.alfresco.repo.webservice.types.CMLRemoveAspect[] removeAspect) {
        this.removeAspect = removeAspect;
    }

    public org.alfresco.repo.webservice.types.CMLRemoveAspect getRemoveAspect(int i) {
        return this.removeAspect[i];
    }

    public void setRemoveAspect(int i, org.alfresco.repo.webservice.types.CMLRemoveAspect _value) {
        this.removeAspect[i] = _value;
    }


    /**
     * Gets the update value for this CML.
     * 
     * @return update
     */
    public org.alfresco.repo.webservice.types.CMLUpdate[] getUpdate() {
        return update;
    }


    /**
     * Sets the update value for this CML.
     * 
     * @param update
     */
    public void setUpdate(org.alfresco.repo.webservice.types.CMLUpdate[] update) {
        this.update = update;
    }

    public org.alfresco.repo.webservice.types.CMLUpdate getUpdate(int i) {
        return this.update[i];
    }

    public void setUpdate(int i, org.alfresco.repo.webservice.types.CMLUpdate _value) {
        this.update[i] = _value;
    }


    /**
     * Gets the delete value for this CML.
     * 
     * @return delete
     */
    public org.alfresco.repo.webservice.types.CMLDelete[] getDelete() {
        return delete;
    }


    /**
     * Sets the delete value for this CML.
     * 
     * @param delete
     */
    public void setDelete(org.alfresco.repo.webservice.types.CMLDelete[] delete) {
        this.delete = delete;
    }

    public org.alfresco.repo.webservice.types.CMLDelete getDelete(int i) {
        return this.delete[i];
    }

    public void setDelete(int i, org.alfresco.repo.webservice.types.CMLDelete _value) {
        this.delete[i] = _value;
    }


    /**
     * Gets the move value for this CML.
     * 
     * @return move
     */
    public org.alfresco.repo.webservice.types.CMLMove[] getMove() {
        return move;
    }


    /**
     * Sets the move value for this CML.
     * 
     * @param move
     */
    public void setMove(org.alfresco.repo.webservice.types.CMLMove[] move) {
        this.move = move;
    }

    public org.alfresco.repo.webservice.types.CMLMove getMove(int i) {
        return this.move[i];
    }

    public void setMove(int i, org.alfresco.repo.webservice.types.CMLMove _value) {
        this.move[i] = _value;
    }


    /**
     * Gets the copy value for this CML.
     * 
     * @return copy
     */
    public org.alfresco.repo.webservice.types.CMLCopy[] getCopy() {
        return copy;
    }


    /**
     * Sets the copy value for this CML.
     * 
     * @param copy
     */
    public void setCopy(org.alfresco.repo.webservice.types.CMLCopy[] copy) {
        this.copy = copy;
    }

    public org.alfresco.repo.webservice.types.CMLCopy getCopy(int i) {
        return this.copy[i];
    }

    public void setCopy(int i, org.alfresco.repo.webservice.types.CMLCopy _value) {
        this.copy[i] = _value;
    }


    /**
     * Gets the addChild value for this CML.
     * 
     * @return addChild
     */
    public org.alfresco.repo.webservice.types.CMLAddChild[] getAddChild() {
        return addChild;
    }


    /**
     * Sets the addChild value for this CML.
     * 
     * @param addChild
     */
    public void setAddChild(org.alfresco.repo.webservice.types.CMLAddChild[] addChild) {
        this.addChild = addChild;
    }

    public org.alfresco.repo.webservice.types.CMLAddChild getAddChild(int i) {
        return this.addChild[i];
    }

    public void setAddChild(int i, org.alfresco.repo.webservice.types.CMLAddChild _value) {
        this.addChild[i] = _value;
    }


    /**
     * Gets the removeChild value for this CML.
     * 
     * @return removeChild
     */
    public org.alfresco.repo.webservice.types.CMLRemoveChild[] getRemoveChild() {
        return removeChild;
    }


    /**
     * Sets the removeChild value for this CML.
     * 
     * @param removeChild
     */
    public void setRemoveChild(org.alfresco.repo.webservice.types.CMLRemoveChild[] removeChild) {
        this.removeChild = removeChild;
    }

    public org.alfresco.repo.webservice.types.CMLRemoveChild getRemoveChild(int i) {
        return this.removeChild[i];
    }

    public void setRemoveChild(int i, org.alfresco.repo.webservice.types.CMLRemoveChild _value) {
        this.removeChild[i] = _value;
    }


    /**
     * Gets the createAssociation value for this CML.
     * 
     * @return createAssociation
     */
    public org.alfresco.repo.webservice.types.CMLCreateAssociation[] getCreateAssociation() {
        return createAssociation;
    }


    /**
     * Sets the createAssociation value for this CML.
     * 
     * @param createAssociation
     */
    public void setCreateAssociation(org.alfresco.repo.webservice.types.CMLCreateAssociation[] createAssociation) {
        this.createAssociation = createAssociation;
    }

    public org.alfresco.repo.webservice.types.CMLCreateAssociation getCreateAssociation(int i) {
        return this.createAssociation[i];
    }

    public void setCreateAssociation(int i, org.alfresco.repo.webservice.types.CMLCreateAssociation _value) {
        this.createAssociation[i] = _value;
    }


    /**
     * Gets the removeAssociation value for this CML.
     * 
     * @return removeAssociation
     */
    public org.alfresco.repo.webservice.types.CMLRemoveAssociation[] getRemoveAssociation() {
        return removeAssociation;
    }


    /**
     * Sets the removeAssociation value for this CML.
     * 
     * @param removeAssociation
     */
    public void setRemoveAssociation(org.alfresco.repo.webservice.types.CMLRemoveAssociation[] removeAssociation) {
        this.removeAssociation = removeAssociation;
    }

    public org.alfresco.repo.webservice.types.CMLRemoveAssociation getRemoveAssociation(int i) {
        return this.removeAssociation[i];
    }

    public void setRemoveAssociation(int i, org.alfresco.repo.webservice.types.CMLRemoveAssociation _value) {
        this.removeAssociation[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CML)) return false;
        CML other = (CML) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.create==null && other.getCreate()==null) || 
             (this.create!=null &&
              java.util.Arrays.equals(this.create, other.getCreate()))) &&
            ((this.addAspect==null && other.getAddAspect()==null) || 
             (this.addAspect!=null &&
              java.util.Arrays.equals(this.addAspect, other.getAddAspect()))) &&
            ((this.removeAspect==null && other.getRemoveAspect()==null) || 
             (this.removeAspect!=null &&
              java.util.Arrays.equals(this.removeAspect, other.getRemoveAspect()))) &&
            ((this.update==null && other.getUpdate()==null) || 
             (this.update!=null &&
              java.util.Arrays.equals(this.update, other.getUpdate()))) &&
            ((this.delete==null && other.getDelete()==null) || 
             (this.delete!=null &&
              java.util.Arrays.equals(this.delete, other.getDelete()))) &&
            ((this.move==null && other.getMove()==null) || 
             (this.move!=null &&
              java.util.Arrays.equals(this.move, other.getMove()))) &&
            ((this.copy==null && other.getCopy()==null) || 
             (this.copy!=null &&
              java.util.Arrays.equals(this.copy, other.getCopy()))) &&
            ((this.addChild==null && other.getAddChild()==null) || 
             (this.addChild!=null &&
              java.util.Arrays.equals(this.addChild, other.getAddChild()))) &&
            ((this.removeChild==null && other.getRemoveChild()==null) || 
             (this.removeChild!=null &&
              java.util.Arrays.equals(this.removeChild, other.getRemoveChild()))) &&
            ((this.createAssociation==null && other.getCreateAssociation()==null) || 
             (this.createAssociation!=null &&
              java.util.Arrays.equals(this.createAssociation, other.getCreateAssociation()))) &&
            ((this.removeAssociation==null && other.getRemoveAssociation()==null) || 
             (this.removeAssociation!=null &&
              java.util.Arrays.equals(this.removeAssociation, other.getRemoveAssociation())));
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
        if (getCreate() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getCreate());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getCreate(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getAddAspect() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAddAspect());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAddAspect(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getRemoveAspect() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRemoveAspect());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRemoveAspect(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getUpdate() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getUpdate());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getUpdate(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getDelete() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getDelete());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDelete(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getMove() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getMove());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getMove(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getCopy() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getCopy());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getCopy(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getAddChild() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAddChild());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAddChild(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getRemoveChild() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRemoveChild());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRemoveChild(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getCreateAssociation() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getCreateAssociation());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getCreateAssociation(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getRemoveAssociation() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRemoveAssociation());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRemoveAssociation(), i);
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
        new org.apache.axis.description.TypeDesc(CML.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "CML"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("create");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "create"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>create"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("addAspect");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "addAspect"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>addAspect"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("removeAspect");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "removeAspect"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>removeAspect"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("update");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "update"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>update"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("delete");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "delete"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>delete"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("move");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "move"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>move"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("copy");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "copy"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>copy"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("addChild");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "addChild"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>addChild"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("removeChild");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "removeChild"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>removeChild"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("createAssociation");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "createAssociation"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>createAssociation"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("removeAssociation");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", "removeAssociation"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/cml/1.0", ">CML>removeAssociation"));
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
