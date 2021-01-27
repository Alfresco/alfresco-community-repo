/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.api.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.rest.api.ClassDefinitionMapper;
import org.alfresco.rest.api.model.ClassDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
/**
 * Maps representations from TypeDefinition to NodeDefinition
 *
 * @author gfertuso
 */
public class ClassDefinitionMapperImpl implements ClassDefinitionMapper
{

    private final List<String> EXCLUDED_NS = Arrays.asList(NamespaceService.SYSTEM_MODEL_1_0_URI);
    private static final List<QName> EXCLUDED_PROPS = Arrays.asList(ContentModel.PROP_CONTENT);


    @Override
    public ClassDefinition fromDictionaryClassDefinition(org.alfresco.service.cmr.dictionary.ClassDefinition classDefinition, MessageLookup messageLookup) {
        if (classDefinition == null)
        {
            throw new AlfrescoRuntimeException("Undefined ClassDefinition for the node");
        }

        ClassDefinition _classDefinition = new ClassDefinition();
        _classDefinition.setProperties(getProperties(classDefinition.getProperties(), messageLookup));

        return _classDefinition;
    }

    private boolean isPropertyExcluded(QName propertyName)
    {
        return EXCLUDED_NS.contains(propertyName.getNamespaceURI()) || EXCLUDED_PROPS.contains(propertyName);
    }

    private List <org.alfresco.rest.api.model.PropertyDefinition> getProperties(Map<QName, org.alfresco.service.cmr.dictionary.PropertyDefinition> propertiesMap, MessageLookup messageLookup)
    {
        return propertiesMap.values().stream()
                .filter(p -> !isPropertyExcluded(p.getName()))
                .map(p -> fromPropertyDefinitionToProperty(p , messageLookup))
                .collect(Collectors.toList());
    }
    
    private org.alfresco.rest.api.model.PropertyDefinition fromPropertyDefinitionToProperty(org.alfresco.service.cmr.dictionary.PropertyDefinition propertyDefinition,
                                                                MessageLookup messageLookup)
    {
        org.alfresco.rest.api.model.PropertyDefinition property = new org.alfresco.rest.api.model.PropertyDefinition();
        property.setId(propertyDefinition.getName().toPrefixString());
        property.setTitle(propertyDefinition.getTitle(messageLookup));
        property.setDescription(propertyDefinition.getDescription(messageLookup));
        property.setDefaultValue(propertyDefinition.getDefaultValue());
        property.setDataType(propertyDefinition.getDataType().getName().toPrefixString());
        property.setIsMultiValued(propertyDefinition.isMultiValued());
        property.setIsMandatory(propertyDefinition.isMandatory());
        property.setIsMandatoryEnforced(propertyDefinition.isMandatoryEnforced());
        property.setIsProtected(propertyDefinition.isProtected());
        property.setConstraints(getConstraints(propertyDefinition.getConstraints(), messageLookup));
       
        return property;
    }
    
    private List<org.alfresco.rest.api.model.ConstraintDefinition> getConstraints(Collection<org.alfresco.service.cmr.dictionary.ConstraintDefinition> constraintDefinitions,
                                                      MessageLookup messageLookup)
    {

        return constraintDefinitions.stream()
                .filter(constraint -> constraint.getConstraint() != null)
                .map(constraint -> fromConstraintDefinitionToConstraint(constraint, messageLookup))
                .collect(Collectors.toList());
    }

    private org.alfresco.rest.api.model.ConstraintDefinition fromConstraintDefinitionToConstraint(org.alfresco.service.cmr.dictionary.ConstraintDefinition constraintDefinition,
                                                                      MessageLookup messageLookup)
    {
        org.alfresco.rest.api.model.ConstraintDefinition constraint = new org.alfresco.rest.api.model.ConstraintDefinition();
        constraint.setId(constraintDefinition.getConstraint().getShortName());
        constraint.setType(constraintDefinition.getConstraint().getType());
        constraint.setTitle(constraintDefinition.getTitle(messageLookup));
        constraint.setDescription(constraintDefinition.getDescription(messageLookup));
        constraint.setParameters(constraintDefinition.getConstraint().getParameters());
        return constraint;
    }
}
