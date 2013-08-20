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

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.DATA_KEY_SEPARATOR;

import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.FieldGroup;
import org.alfresco.repo.forms.processor.AbstractFieldProcessor;
import org.alfresco.repo.forms.processor.FieldProcessor;
import org.alfresco.repo.forms.processor.FormCreationData;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * {@link FieldProcessor} implementation that handles QName fields.
 * 
 * @since 3.4
 * @author Nick Smith
 *
 * @param <Type>
 */
public abstract class QNameFieldProcessor<Type extends ClassAttributeDefinition> extends AbstractFieldProcessor<ContentModelItemData<?>>
{
    protected NamespaceService namespaceService;
    protected DictionaryService dictionaryService;

    public QNameFieldProcessor()
    {
        // Constructor for Spring.
    }

    public QNameFieldProcessor(NamespaceService namespaceService, DictionaryService dictionaryService)
    {
        this.namespaceService = namespaceService;
        this.dictionaryService = dictionaryService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.field.processor.AbstractFieldProcessor#generateTypedField(java.lang.String, java.lang.Object)
     */
    @Override
    protected Field generateTypedField(String fieldName, FormCreationData formData, ContentModelItemData<?> typedData)
    {
        Field field = null;
        try
        {
            QName fullName = getFullName(fieldName);
            boolean isForcedField = formData.isForcedField(fieldName);
            field = generateField(fullName, typedData, isForcedField);
        }
        catch (NamespaceException ne)
        {
            // ignore fields with an invalid namespace - the model may no longer be present in the repository
        }
        return field;
    }
    
    protected QName getFullName(String name)
    {
        String[] parts = name.split(FormFieldConstants.FIELD_NAME_SEPARATOR);
        if(parts.length == 2)
        {
            String prefix = parts[0];
            String localName = parts[1];
            return QName.createQName(prefix, localName, namespaceService);
        }
        else
        {
            String prefix = parts[1];
            String localName = parts[2];
            return QName.createQName(prefix, localName, namespaceService);
        }
    }
    
    protected String getPrefixedName(ClassAttributeDefinition attribDef) 
    {
        return attribDef.getName().toPrefixString(namespaceService);
    }

    public Field generateField(QName fullName, ContentModelItemData<?> itemData, boolean isForcedField) 
    {
        Type propDef = getTypeDefinition(fullName, itemData, isForcedField);
        Field field = null;
        if (propDef != null)
        {
            Object value = getValue(fullName, itemData);
            FieldGroup group = getGroup(propDef);
            field = makeField(propDef, value, group);
        }
        return field;
    }

    /**
     * Sets several properties on the {@link FieldDefinition}, including name,
     * label, description, dataKeyName and whether the field is protected. These
     * values are derived from the <code>attribDef</code> parameter.
     * 
     * @param attribDef Used to set the values of name, description, label,
     *            dataKeyName and isProtected properties on the returned object.
     * @param fieldDef A factory object used to create the FieldDefinition to be
     *            returned.
     * @param group Used to set the group on the returned FieldDefinition.
     */
    protected void populateFieldDefinition(Type attribDef, FieldDefinition fieldDef,
                FieldGroup group, String dataKeyPrefix)
    {
        String attribName = fieldDef.getName();
        fieldDef.setGroup(group);
        String title = attribDef.getTitle(dictionaryService);
        title = title == null ? attribName : title;
        fieldDef.setLabel(title);
        fieldDef.setDescription(attribDef.getDescription(dictionaryService));
        fieldDef.setProtectedField(attribDef.isProtected());

        // define the data key name and set
        String dataKeyName = makeDataKeyForName(attribName, dataKeyPrefix);
        fieldDef.setDataKeyName(dataKeyName);
    }
    
    protected String makeDataKeyForName(String propName, String prefix)
    {
        String[] nameParts = QName.splitPrefixedQName(propName);
        String firstPart = nameParts[0];
        StringBuilder builder = new StringBuilder(prefix);
        if (firstPart.length() > 0)
        {
            builder.append(firstPart);
            builder.append(DATA_KEY_SEPARATOR);
        }
        builder.append(nameParts[1]);
        return builder.toString();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.field.processor.AbstractFieldProcessor#getExpectedDataType()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Class<ContentModelItemData<?>> getExpectedDataType()
    {
        // This is nasty but unavoidable because of generics.
        Object clazz = ContentModelItemData.class;
        return (Class<ContentModelItemData<?>>)clazz;
    }
    
    /**
     * @param namespaceService the namespaceService to set
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * @param dictionaryService the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    protected abstract Field makeField(Type typeDef, Object value, FieldGroup group);

    protected abstract FieldGroup getGroup(Type typeDef);

    protected abstract Object getValue(QName fullName, ContentModelItemData<?> itemData);

    protected abstract Type getTypeDefinition(QName fullName, ContentModelItemData<?> itemData, boolean isForcedField);
}