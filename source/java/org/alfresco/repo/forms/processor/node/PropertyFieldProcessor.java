/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.forms.processor.node;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.PROP_DATA_PREFIX;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.FieldGroup;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.repo.forms.PropertyFieldDefinition.FieldConstraint;
import org.alfresco.repo.forms.processor.FieldProcessor;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

/**
 * {@link FieldProcessor} implementation that handles properties.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class PropertyFieldProcessor extends QNameFieldProcessor<PropertyDefinition>
{
    private static final Log logger = LogFactory.getLog(PropertyFieldProcessor.class);

    public PropertyFieldProcessor()
    {
        // Constructor for Spring.
    }

    public PropertyFieldProcessor(NamespaceService namespaceService, DictionaryService dictionaryService)
    {
        super(namespaceService, dictionaryService);
    }

    @Override
    protected Log getLogger() 
    {
        return logger;
    }

    @Override
    protected PropertyDefinition getTypeDefinition(QName fullName, ContentModelItemData<?> itemData, boolean isForcedField)
    {
        PropertyDefinition propDef = itemData.getPropertyDefinition(fullName);
        if (propDef == null)
        {
            if (isForcedField)
            {
                propDef = dictionaryService.getProperty(fullName);
            }
        }
        return propDef;
    }

    @Override
    public Field makeField(PropertyDefinition propDef, Object value, FieldGroup group)
    {
        PropertyFieldDefinition fieldDef = makePropertyFieldDefinition(propDef, group);
        return new ContentModelField(propDef, fieldDef, value);
    }

    @Override
    protected FieldGroup getGroup(PropertyDefinition propDef)
    {
        // TODO Need to Implement this once Composite Content is implementd.
        return null;
    }

    @Override
    public Object getValue(QName name, ContentModelItemData<?> data)
    {
        Serializable value = data.getPropertyValue(name);
        if (value == null)
        {
            return getDefaultValue(name, data);
        }
        
        if (value instanceof Collection<?>)
        {
            // temporarily add repeating field data as a comma
            // separated list, this will be changed to using
            // a separate field for each value once we have full
            // UI support in place.
            Collection<?> values = (Collection<?>) value;
            
            // if the non empty collection is a List of Date objects 
            // we need to convert each date to a ISO8601 format 
            if (value instanceof List<?> && !values.isEmpty())
            {
                List<?> list = (List<?>)values;
                if (list.get(0) instanceof Date)
                {
                    List<String> isoDates = new ArrayList<String>(list.size());
                    for (Object date : list)
                    {
                        isoDates.add(ISO8601DateFormat.format((Date)date));
                    }
                    
                    // return the ISO formatted dates as a comma delimited string 
                    return StringUtils.collectionToCommaDelimitedString(isoDates);
                }
            }
            
            // return everything else using toString()
            return StringUtils.collectionToCommaDelimitedString(values);
        }
        else if (value instanceof ContentData)
        {
            // for content properties retrieve the info URL rather than the
            // the object value itself
            ContentData contentData = (ContentData)value;
            return contentData.getInfoUrl();
        }
        else if (value instanceof NodeRef)
        {
            return ((NodeRef)value).toString();
        }
        
        return value;
    }

    private Object getDefaultValue(QName name, ContentModelItemData<?> data)
    {
        PropertyDefinition propDef = data.getPropertyDefinition(name);
        if (propDef != null)
        {
            return propDef.getDefaultValue();
        }
        return null;
    }

    private PropertyFieldDefinition makePropertyFieldDefinition(final PropertyDefinition propDef, FieldGroup group)
    {
        String name = getPrefixedName(propDef);
        QName dataType = propDef.getDataType().getName();
        PropertyFieldDefinition fieldDef = new PropertyFieldDefinition(name, dataType.getLocalName());
        
        populateFieldDefinition(propDef, fieldDef, group, PROP_DATA_PREFIX);

        fieldDef.setDefaultValue(propDef.getDefaultValue());
        fieldDef.setMandatory(propDef.isMandatory());
        fieldDef.setRepeating(propDef.isMultiValued());

        // any property from the system model (sys prefix) should be protected
        // the model doesn't
        // currently enforce this so make sure they are not editable
        if (NamespaceService.SYSTEM_MODEL_1_0_URI.equals(propDef.getName().getNamespaceURI()))
        {
            fieldDef.setProtectedField(true);
        }

        // If the property data type is d:period we need to setup a data
        // type parameters object to represent the options and rules
        if (dataType.equals(DataTypeDefinition.PERIOD))
        {
            PeriodDataTypeParameters periodOptions = getPeriodOptions();
            fieldDef.setDataTypeParameters(periodOptions);
        }

        // setup constraints for the property
        List<FieldConstraint> fieldConstraints = makeFieldConstraints(propDef);
        fieldDef.setConstraints(fieldConstraints);
        return fieldDef;
    }

    private List<FieldConstraint> makeFieldConstraints(PropertyDefinition propDef)
    {
        List<FieldConstraint> fieldConstraints = null;
        List<ConstraintDefinition> constraints = propDef.getConstraints();
        if (constraints != null && constraints.size() > 0)
        {
            fieldConstraints = new ArrayList<FieldConstraint>(constraints.size());
            for (ConstraintDefinition constraintDef : constraints)
            {
                Constraint constraint = constraintDef.getConstraint();
                String type = constraint.getType();
                Map<String, Object> params = constraint.getParameters();
                
                // ListOfValuesConstraints have special handling for localising their allowedValues.
                if (ListOfValuesConstraint.CONSTRAINT_TYPE.equals(constraint.getType()))
                {
                    ListOfValuesConstraint lovConstraint = (ListOfValuesConstraint) constraint;
                    List<String> allowedValues = lovConstraint.getAllowedValues();
                    List<String> localisedValues = new ArrayList<String>(allowedValues.size());
                    
                    // Look up each localised display-label in turn.
                    for (String value : allowedValues)
                    {
                        String displayLabel = lovConstraint.getDisplayLabel(value);
                        // Change the allowedValue entry to the format the FormsService expects for localised strings: "value|label"
                        // If there is no localisation defined for any value, then this will give us "value|value".
                        localisedValues.add(value + "|" + displayLabel);
                    }
                    
                    // Now replace the allowedValues param with our localised version.
                    params.put(ListOfValuesConstraint.ALLOWED_VALUES_PARAM, localisedValues);
                }
                FieldConstraint fieldConstraint = new FieldConstraint(type, params);
                fieldConstraints.add(fieldConstraint);
            }
        }
        return fieldConstraints;
    }
    
    private PeriodDataTypeParameters getPeriodOptions()
    {
        PeriodDataTypeParameters periodOptions = new PeriodDataTypeParameters();
        Set<String> providers = Period.getProviderNames();
        for (String provider : providers)
        {
            periodOptions.addPeriodProvider(Period.getProvider(provider));
        }
        return periodOptions;
    }
    
    @Override
    protected String getRegistryKey() 
    {
        return FormFieldConstants.PROP;
    }
}
