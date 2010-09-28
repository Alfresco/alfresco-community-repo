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

import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.FieldGroup;
import org.alfresco.repo.forms.processor.FieldProcessor;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Default {@link FieldProcessor} implementation, used when an explicit FieldProcessor can not be located.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class DefaultFieldProcessor extends QNameFieldProcessor<ClassAttributeDefinition> implements InitializingBean
{
    private static final Log logger = LogFactory.getLog(DefaultFieldProcessor.class);

    private final AssociationFieldProcessor assocProcessor = new AssociationFieldProcessor();
    private final PropertyFieldProcessor propProcessor = new PropertyFieldProcessor();

    @Override
    protected Log getLogger() 
    {
        return logger;
    }

    @Override
    protected QName getFullName(String name) 
    {
        String[] parts = name.split(FormFieldConstants.FIELD_NAME_SEPARATOR);
        int position = parts.length - 1;
        String localName = parts[position];// local name is always the last
        // string in the arry
        position--;
        // prefix is always the penultimate string in the array.
        String prefix = parts[position];
        return QName.createQName(prefix, localName, namespaceService);
    }

    @Override
    public Field generateField(QName fullName, ContentModelItemData<?> itemData, boolean isForcedField) 
    {
        Field fieldInfo = propProcessor.generateField(fullName, itemData, isForcedField);
        if (fieldInfo == null) 
        {
            fieldInfo = assocProcessor.generateField(fullName, itemData, isForcedField);
        }
        return fieldInfo;
    }

    @Override
    protected String getRegistryKey()
    {
        return "";
    }

    public void afterPropertiesSet() throws Exception
    {
        ParameterCheck.mandatory("dictionaryService", dictionaryService);
        ParameterCheck.mandatory("namespaceService", namespaceService);
        assocProcessor.setDictionaryService(dictionaryService);
        assocProcessor.setNamespaceService(namespaceService);
        propProcessor.setDictionaryService(dictionaryService);
        propProcessor.setNamespaceService(namespaceService);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.node.QNameFieldProcessor#getGroup(org.alfresco.service.cmr.dictionary.ClassAttributeDefinition)
     */
    @Override
    protected FieldGroup getGroup(ClassAttributeDefinition typeDef)
    {
        throw new UnsupportedOperationException("This method should never be called!");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.node.QNameFieldProcessor#getTypeDefinition(org.alfresco.service.namespace.QName, org.alfresco.repo.forms.processor.node.ItemData, boolean)
     */
    @Override
    protected ClassAttributeDefinition getTypeDefinition(QName fullName, ContentModelItemData<?> itemData, boolean isForcedField)
    {
        throw new UnsupportedOperationException("This method should never be called!");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.node.QNameFieldProcessor#getValue(org.alfresco.service.namespace.QName, org.alfresco.repo.forms.processor.node.ItemData)
     */
    @Override
    protected Object getValue(QName fullName, ContentModelItemData<?> itemData)
    {
        throw new UnsupportedOperationException("This method should never be called!");
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.node.QNameFieldProcessor#makeField(org.alfresco.service.cmr.dictionary.ClassAttributeDefinition, java.lang.Object, org.alfresco.repo.forms.FieldGroup)
     */
    @Override
    protected Field makeField(ClassAttributeDefinition typeDef, Object value, FieldGroup group)
    {
        throw new UnsupportedOperationException("This method should never be called!");
    }
}