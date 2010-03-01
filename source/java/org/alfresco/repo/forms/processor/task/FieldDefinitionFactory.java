/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.forms.processor.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.*;
import org.alfresco.repo.forms.AssociationFieldDefinition;
import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.FieldGroup;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.repo.forms.AssociationFieldDefinition.Direction;
import org.alfresco.repo.forms.PropertyFieldDefinition.FieldConstraint;
import org.alfresco.repo.forms.processor.node.PeriodDataTypeParameters;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 */
public class FieldDefinitionFactory
{
    private final NamespaceService namespaceService;

    public FieldDefinitionFactory(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public AssociationFieldDefinition makeAssociationFieldDefinition(
                final AssociationDefinition assocDef, FieldGroup group)
    {
        // Callback used to construct the AssociationFieldDefinition.
        FieldDefinitionCreator<AssociationFieldDefinition> factory = new FieldDefinitionCreator<AssociationFieldDefinition>()
        {
            public AssociationFieldDefinition create(String name)
            {
                String endpointType = assocDef.getTargetClass().getName().toPrefixString(
                            namespaceService);
                return new AssociationFieldDefinition(name, endpointType, Direction.TARGET);
            }
        };
        AssociationFieldDefinition fieldDef = makeFieldDefinition(assocDef, group,
                    ASSOC_DATA_PREFIX, factory);

        fieldDef.setEndpointMandatory(assocDef.isTargetMandatory());
        fieldDef.setEndpointMany(assocDef.isTargetMany());
        return fieldDef;
    }

    public PropertyFieldDefinition makePropertyFieldDefinition(final PropertyDefinition propDef,
                FieldGroup group)
    {
        // Callback used to construct the PropertyFieldDefinition.
        FieldDefinitionCreator<PropertyFieldDefinition> factory = new FieldDefinitionCreator<PropertyFieldDefinition>()
        {
            public PropertyFieldDefinition create(String name)
            {
                QName dataType = propDef.getDataType().getName();
                return new PropertyFieldDefinition(name, dataType.getLocalName());
            }
        };
        PropertyFieldDefinition fieldDef = makeFieldDefinition(propDef, group, PROP_DATA_PREFIX,
                    factory);

        fieldDef.setDefaultValue(propDef.getDefaultValue());
        fieldDef.setMandatory(propDef.isMandatory());
        fieldDef.setRepeating(propDef.isMultiValued());

        // any property from the system model (sys prefix) should be protected
        // the model doesn't
        // cu rrently enforce this so make sure they are not editable
        if (NamespaceService.SYSTEM_MODEL_1_0_URI.equals(propDef.getName().getNamespaceURI()))
        {
            fieldDef.setProtectedField(true);
        }

        // If the property data type is d:period we need to setup a data
        // type parameters object to represent the options and rules
        if (fieldDef.getDataType().equals(DataTypeDefinition.PERIOD))
        {
            PeriodDataTypeParameters periodOptions = getPeriodOptions();
            fieldDef.setDataTypeParameters(periodOptions);
        }

        // setup constraints for the property
        List<FieldConstraint> fieldConstraints = makeFieldConstraints(propDef);
        fieldDef.setConstraints(fieldConstraints);
        return fieldDef;
    }

    /**
     * Creates either a PropertyFieldDefinition or an
     * AssociationFieldDefinition, as defined by the factory class being passed
     * in. Sets several properties on this FieldDefinition, including name,
     * label, description, dataKeyName and whether the field is protected. These
     * values are derived from the <code>attribDef</code> parameter.
     * 
     * @param attribDef Used to set the values of name, description, label,
     *            dataKeyName and isProtected properties on the returned object.
     * @param group Used to set the group on the returned FieldDefinition.
     * @param factory A factory object used to create the FieldDefinition to be
     *            returned.
     * @return An object of type <code>T</code> which extends
     *         <code>FieldDefinition</code>.
     */
    private <T extends FieldDefinition> T makeFieldDefinition(ClassAttributeDefinition attribDef,
                FieldGroup group, String dataKeyPrefix, FieldDefinitionCreator<T> factory)
    {
        String attribName = attribDef.getName().toPrefixString(namespaceService);
        T fieldDef = factory.create(attribName);
        fieldDef.setGroup(group);
        String title = attribDef.getTitle();
        title = title == null ? attribName : title;
        fieldDef.setLabel(title);
        fieldDef.setDescription(attribDef.getDescription());
        fieldDef.setProtectedField(attribDef.isProtected());

        // define the data key name and set
        String dataKeyName = makeDataKeyForName(attribName, dataKeyPrefix);
        fieldDef.setDataKeyName(dataKeyName);
        return fieldDef;
    }

    private static List<FieldConstraint> makeFieldConstraints(PropertyDefinition propDef)
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

    private static String makeDataKeyForName(String propName, String prefix)
    {
        String[] nameParts = QName.splitPrefixedQName(propName);
        return prefix + nameParts[0] + DATA_KEY_SEPARATOR + nameParts[1];
    }

    private interface FieldDefinitionCreator<T extends FieldDefinition>
    {
        T create(String fieldDefinitionName);
    }

}
