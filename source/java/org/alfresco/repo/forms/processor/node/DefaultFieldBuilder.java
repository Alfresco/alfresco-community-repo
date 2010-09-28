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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.processor.FieldProcessor;
import org.alfresco.repo.forms.processor.FieldProcessorRegistry;
import org.alfresco.repo.forms.processor.FormCreationData;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class for building the default fields for a form where an explicit
 * set of fields was not provided.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class DefaultFieldBuilder
{
    private static final String ASSOC_WARN = "Could not build Association Field as no valid FieldProcessor was specified";
    private static final String PROP_WARNING = "Could not build Property Field as no valid FieldProcessor was specified";

    private static final Log MY_LOGGER = LogFactory.getLog(DefaultFieldBuilder.class);
    
    private final FormCreationData formData;
    private final ContentModelItemData<?> ItemData;
    private final FieldProcessorRegistry registry;
    private final NamespaceService namespaceService;
    private final List<String> ignoredFields;
    
    private final Log logger;

    public DefaultFieldBuilder(FormCreationData data,
            FieldProcessorRegistry registry,
            NamespaceService namespaceService,
            List<String> ignoredFields)
    {
        this(data, registry, namespaceService, ignoredFields, MY_LOGGER);
    }

    public DefaultFieldBuilder(FormCreationData formData,
                FieldProcessorRegistry registry,
                NamespaceService namespaceService,
                List<String> ignoredFields,
                Log logger)
    {
        this.logger = logger;
        this.formData = formData;
        this.registry = registry;
        this.namespaceService = namespaceService;
        this.ignoredFields = getNonNullList(ignoredFields );
        this.ItemData = (ContentModelItemData<?>) formData.getItemData();
    }

    private <T> List<T> getNonNullList(List<T> list)
    {
        return list == null ? Collections.<T>emptyList() : list;
    }

    public List<Field> buildDefaultFields()
    {
        List<Field> assocFields = buildDefaultAssociationFields();
        List<Field> propFields = buildDefaultPropertyFields();
        List<Field> transFields = buildDefaultTransientFields();
        int size = assocFields.size() + propFields.size() + transFields.size();
        ArrayList<Field> fields = new ArrayList<Field>(size);
        fields.addAll(assocFields);
        fields.addAll(propFields);
        fields.addAll(transFields);
        return fields;
    }
    
    public List<Field> buildDefaultPropertyFields()
    {
        Collection<QName> names = ItemData.getAllPropertyDefinitionNames();
        List<Field> fields = new ArrayList<Field>(names.size());
        for (QName name : names)
        {
            if (ignoreQName(name) == false)
            {
                fields.add(buildPropertyField(name));
            }
        }
        return fields;
    }
    
    private boolean ignoreQName(QName qname)
    {
        String name = qname.toPrefixString(namespaceService);
        return ignoredFields.contains(name);
    }

    public List<Field> buildDefaultAssociationFields()
    {
        Collection<QName> names = ItemData.getAllAssociationDefinitionNames();
        List<Field> fields = new ArrayList<Field>(names.size());
        for (QName name : names)
        {
            if(ignoreQName(name)==false)
            {
                fields.add(buildAssociationField(name));
            }
        }
        return fields;
    }
    
    public List<Field> buildDefaultTransientFields()
    {
        Collection<String> names = ItemData.getAllTransientFieldNames();
        List<Field> fields = new ArrayList<Field>(names.size());
        for (String name : names)
        {
            if(ignoredFields.contains(name)==false)
            {
                fields.add(buildTransientField(name));
            }
        }
        return fields;
    }
    
    public Field buildAssociationField(QName assocName)
    {
        return buildQNameField(assocName, FormFieldConstants.ASSOC, ASSOC_WARN);
    }

    public Field buildPropertyField(QName propName)
    {
        return buildQNameField(propName, FormFieldConstants.PROP, PROP_WARNING);
    }

    private Field buildQNameField(QName assocName, String key, String warningMsg)
    {
        FieldProcessor fieldProcessor = registry.get(key);
        if (fieldProcessor != null && fieldProcessor instanceof QNameFieldProcessor<?>)
        {
            QNameFieldProcessor<?> qnameProcessor = (QNameFieldProcessor<?>) fieldProcessor;
            return qnameProcessor.generateField(assocName, ItemData, false);
        }
        
        if (logger.isWarnEnabled())
            logger.warn(warningMsg);
        
        return null;
    }

    public Field buildTransientField(String name)
    {
        FieldProcessor fieldProcessor = registry.get(name);
        if (fieldProcessor != null)
        {
            return fieldProcessor.generateField(name, formData);
        }
        
        if (logger.isWarnEnabled())
            logger.warn("Could not build Transient Field: "+ name +" as no FieldProcessor specified");
        
        return null;
    }
}