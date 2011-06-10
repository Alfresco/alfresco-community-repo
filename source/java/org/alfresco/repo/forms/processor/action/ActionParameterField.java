/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.forms.processor.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.forms.AssociationFieldDefinition;
import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.FormException;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.repo.forms.PropertyFieldDefinition.FieldConstraint;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterConstraint;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * This {@link Field} implementation is a form field which represents a defined
 * {@link ActionDefinition#getParameterDefinitions() action parameter}.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 * @see ActionDefinition#getParameterDefintion(String)
 */
public class ActionParameterField implements Field
{
    private FieldDefinition fieldDef;
    private String name;

    public ActionParameterField(ParameterDefinition parameterDef, ActionService actionService)
    {
        //TODO i18n
        
        this.name = parameterDef.getName();
        
        QName type = parameterDef.getType();
        final List<FieldConstraint> fieldConstraints = processActionConstraints(parameterDef, actionService);
        
        if (DataTypeDefinition.NODE_REF.equals(type) && fieldConstraints.isEmpty())
        {
            // Parameters of type NodeRef need to be AssociationPickers so that a NodeRef can be selected in the form
            // using the association picker for navigation.
            // However this is only true for NodeRef parameters without constraints.
            // NodeRef parameters which are constrained (to a list of particular NodeRefs) will
            // be handled below.
            this.fieldDef = new AssociationFieldDefinition(this.name, "cm:cmobject", AssociationFieldDefinition.Direction.TARGET);
            AssociationFieldDefinition assocFieldDef = (AssociationFieldDefinition) this.fieldDef;
            assocFieldDef.setEndpointMandatory(parameterDef.isMandatory());
            assocFieldDef.setEndpointMany(parameterDef.isMultiValued());
        }
        else
        {
            if (DataTypeDefinition.BOOLEAN.equals(type))
            {
                this.fieldDef = new PropertyFieldDefinition(this.name, DataTypeDefinition.BOOLEAN.getLocalName());
            }
            else
            {    
                this.fieldDef = new PropertyFieldDefinition(this.name, DataTypeDefinition.TEXT.getLocalName());
            }
            PropertyFieldDefinition propFieldDef = (PropertyFieldDefinition)this.fieldDef;
            
            
            propFieldDef.setMandatory(parameterDef.isMandatory());
            propFieldDef.setRepeating(parameterDef.isMultiValued());
            
            if (!fieldConstraints.isEmpty())
            {
                propFieldDef.setConstraints(fieldConstraints);
            }
        }
        
        // Properties common to PropertyFieldDefinitions and AssociationFieldDefinitions.
        this.fieldDef.setDescription(parameterDef.getName());
        this.fieldDef.setLabel(parameterDef.getDisplayLabel());
        this.fieldDef.setDataKeyName(this.name);
    }
    
    /**
     * This method creates a list of {@link FieldConstraint field constraints}, if there are any.
     * 
     * @return a List<FieldConstraint> if there are any, else Collections.emptyList()
     */
    private List<FieldConstraint> processActionConstraints(ParameterDefinition parameterDef,
            ActionService actionService)
    {
        List<FieldConstraint> fieldConstraints = Collections.emptyList();
        String paramConstraintName = parameterDef.getParameterConstraintName();
        if (paramConstraintName != null)
        {
            ParameterConstraint paramConstraint = actionService.getParameterConstraint(paramConstraintName);
            if (paramConstraint == null)
            {
                throw new FormException("ParameterConstraint name '" + paramConstraintName + "' not recognised.");
            }
            else
            {
                // This map is of allowedValue : display label for that value.
                Map<String, String> allowableValuesMap = paramConstraint.getAllowableValues();
                
                // We need to concatenate each key-value entry into a String like "value|displaylabel"
                // Then the FormService can display the labels but deal with the values as the underlying data.
                List<String> pipeSeparatedAllowedValues = new ArrayList<String>(allowableValuesMap.size());
                for (Map.Entry<String, String> entry : allowableValuesMap.entrySet())
                {
                    pipeSeparatedAllowedValues.add(entry.getKey() + "|" + entry.getValue());
                }
                
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("allowedValues", pipeSeparatedAllowedValues);
                
                // Finally wrap it up in a parameter map.
                fieldConstraints = new ArrayList<FieldConstraint>(allowableValuesMap.size());
                fieldConstraints.add(new FieldConstraint("LIST", params));
                
            }
        }
        return fieldConstraints;
    }
    
    @Override
    public FieldDefinition getFieldDefinition()
    {
        return this.fieldDef;
    }

    @Override
    public String getFieldName()
    {
        return this.name;
    }

    @Override
    public Object getValue()
    {
        // Action forms always have every value set to null as there are no default values for action parameters.
        return null;
    }
}
