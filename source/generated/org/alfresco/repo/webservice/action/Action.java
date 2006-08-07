/**
 * Action.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.alfresco.repo.webservice.action;

public class Action  implements java.io.Serializable {
    private org.alfresco.repo.webservice.types.Reference actionReference;

    private java.lang.String id;

    private java.lang.String actionName;

    private java.lang.String title;

    private java.lang.String description;

    private org.alfresco.repo.webservice.types.NamedValue[] parameters;

    private org.alfresco.repo.webservice.action.Condition[] conditions;

    private org.alfresco.repo.webservice.action.Action compensatingAction;

    private org.alfresco.repo.webservice.action.Action[] actions;

    public Action() {
    }

    public Action(
           org.alfresco.repo.webservice.types.Reference actionReference,
           java.lang.String id,
           java.lang.String actionName,
           java.lang.String title,
           java.lang.String description,
           org.alfresco.repo.webservice.types.NamedValue[] parameters,
           org.alfresco.repo.webservice.action.Condition[] conditions,
           org.alfresco.repo.webservice.action.Action compensatingAction,
           org.alfresco.repo.webservice.action.Action[] actions) {
           this.actionReference = actionReference;
           this.id = id;
           this.actionName = actionName;
           this.title = title;
           this.description = description;
           this.parameters = parameters;
           this.conditions = conditions;
           this.compensatingAction = compensatingAction;
           this.actions = actions;
    }


    /**
     * Gets the actionReference value for this Action.
     * 
     * @return actionReference
     */
    public org.alfresco.repo.webservice.types.Reference getActionReference() {
        return actionReference;
    }


    /**
     * Sets the actionReference value for this Action.
     * 
     * @param actionReference
     */
    public void setActionReference(org.alfresco.repo.webservice.types.Reference actionReference) {
        this.actionReference = actionReference;
    }


    /**
     * Gets the id value for this Action.
     * 
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this Action.
     * 
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }


    /**
     * Gets the actionName value for this Action.
     * 
     * @return actionName
     */
    public java.lang.String getActionName() {
        return actionName;
    }


    /**
     * Sets the actionName value for this Action.
     * 
     * @param actionName
     */
    public void setActionName(java.lang.String actionName) {
        this.actionName = actionName;
    }


    /**
     * Gets the title value for this Action.
     * 
     * @return title
     */
    public java.lang.String getTitle() {
        return title;
    }


    /**
     * Sets the title value for this Action.
     * 
     * @param title
     */
    public void setTitle(java.lang.String title) {
        this.title = title;
    }


    /**
     * Gets the description value for this Action.
     * 
     * @return description
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this Action.
     * 
     * @param description
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }


    /**
     * Gets the parameters value for this Action.
     * 
     * @return parameters
     */
    public org.alfresco.repo.webservice.types.NamedValue[] getParameters() {
        return parameters;
    }


    /**
     * Sets the parameters value for this Action.
     * 
     * @param parameters
     */
    public void setParameters(org.alfresco.repo.webservice.types.NamedValue[] parameters) {
        this.parameters = parameters;
    }

    public org.alfresco.repo.webservice.types.NamedValue getParameters(int i) {
        return this.parameters[i];
    }

    public void setParameters(int i, org.alfresco.repo.webservice.types.NamedValue _value) {
        this.parameters[i] = _value;
    }


    /**
     * Gets the conditions value for this Action.
     * 
     * @return conditions
     */
    public org.alfresco.repo.webservice.action.Condition[] getConditions() {
        return conditions;
    }


    /**
     * Sets the conditions value for this Action.
     * 
     * @param conditions
     */
    public void setConditions(org.alfresco.repo.webservice.action.Condition[] conditions) {
        this.conditions = conditions;
    }

    public org.alfresco.repo.webservice.action.Condition getConditions(int i) {
        return this.conditions[i];
    }

    public void setConditions(int i, org.alfresco.repo.webservice.action.Condition _value) {
        this.conditions[i] = _value;
    }


    /**
     * Gets the compensatingAction value for this Action.
     * 
     * @return compensatingAction
     */
    public org.alfresco.repo.webservice.action.Action getCompensatingAction() {
        return compensatingAction;
    }


    /**
     * Sets the compensatingAction value for this Action.
     * 
     * @param compensatingAction
     */
    public void setCompensatingAction(org.alfresco.repo.webservice.action.Action compensatingAction) {
        this.compensatingAction = compensatingAction;
    }


    /**
     * Gets the actions value for this Action.
     * 
     * @return actions
     */
    public org.alfresco.repo.webservice.action.Action[] getActions() {
        return actions;
    }


    /**
     * Sets the actions value for this Action.
     * 
     * @param actions
     */
    public void setActions(org.alfresco.repo.webservice.action.Action[] actions) {
        this.actions = actions;
    }

    public org.alfresco.repo.webservice.action.Action getActions(int i) {
        return this.actions[i];
    }

    public void setActions(int i, org.alfresco.repo.webservice.action.Action _value) {
        this.actions[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Action)) return false;
        Action other = (Action) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.actionReference==null && other.getActionReference()==null) || 
             (this.actionReference!=null &&
              this.actionReference.equals(other.getActionReference()))) &&
            ((this.id==null && other.getId()==null) || 
             (this.id!=null &&
              this.id.equals(other.getId()))) &&
            ((this.actionName==null && other.getActionName()==null) || 
             (this.actionName!=null &&
              this.actionName.equals(other.getActionName()))) &&
            ((this.title==null && other.getTitle()==null) || 
             (this.title!=null &&
              this.title.equals(other.getTitle()))) &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
            ((this.parameters==null && other.getParameters()==null) || 
             (this.parameters!=null &&
              java.util.Arrays.equals(this.parameters, other.getParameters()))) &&
            ((this.conditions==null && other.getConditions()==null) || 
             (this.conditions!=null &&
              java.util.Arrays.equals(this.conditions, other.getConditions()))) &&
            ((this.compensatingAction==null && other.getCompensatingAction()==null) || 
             (this.compensatingAction!=null &&
              this.compensatingAction.equals(other.getCompensatingAction()))) &&
            ((this.actions==null && other.getActions()==null) || 
             (this.actions!=null &&
              java.util.Arrays.equals(this.actions, other.getActions())));
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
        if (getActionReference() != null) {
            _hashCode += getActionReference().hashCode();
        }
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getActionName() != null) {
            _hashCode += getActionName().hashCode();
        }
        if (getTitle() != null) {
            _hashCode += getTitle().hashCode();
        }
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        if (getParameters() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getParameters());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getParameters(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getConditions() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getConditions());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getConditions(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getCompensatingAction() != null) {
            _hashCode += getCompensatingAction().hashCode();
        }
        if (getActions() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getActions());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getActions(), i);
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
        new org.apache.axis.description.TypeDesc(Action.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "Action"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("actionReference");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "actionReference"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "Reference"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("actionName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "actionName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("title");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "title"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parameters");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "parameters"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/model/content/1.0", "NamedValue"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("conditions");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "conditions"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "Condition"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("compensatingAction");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "compensatingAction"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "Action"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("actions");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "actions"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.alfresco.org/ws/service/action/1.0", "Action"));
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
