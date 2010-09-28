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

package org.alfresco.repo.forms.processor.workflow;

import java.io.Serializable;
import java.util.List;

import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.node.ContentModelItemData;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Utility class that assists in persisting content model related form data.
 * 
 * @since 3.4
 * @author Nick Smith
 *
 * @param <T>
 */
public abstract class ContentModelFormPersister<T> implements FormPersister<T>
{
    /**
     * Default Logger.
     */
    private static final Log LOGGER= LogFactory.getLog(ContentModelFormPersister.class);
 
    protected final static TypedPropertyValueGetter valueGetter = new TypedPropertyValueGetter();
    protected final DataKeyMatcher keyMatcher;
    protected final DictionaryService dictionaryService;
    protected final Log logger;
    protected final ContentModelItemData<?> itemData; 
    
    public ContentModelFormPersister(ContentModelItemData<?> itemData, NamespaceService namespaceService, DictionaryService dictionaryService, Log logger)
    {
        this.dictionaryService= dictionaryService;
        this.logger = logger==null ? LOGGER : logger;
        this.keyMatcher = new DataKeyMatcher(namespaceService);
        this.itemData = itemData; 
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.workflow.FormPersister#addField(java.lang.String, java.lang.Object)
     */
    public void addField(FieldData fieldData)
    {
        String dataKeyName = fieldData.getName();
        DataKeyInfo keyInfo = keyMatcher.match(dataKeyName);
        if (keyInfo == null)
        {
            logIgnore(fieldData);
        }
        else
        {
            boolean wasApplied = persistField(fieldData, keyInfo);
            if (wasApplied == false)
            {
                logIgnore(fieldData);
            }
        }
    }

    private boolean persistField(FieldData fieldData, DataKeyInfo keyInfo)
    {
        switch (keyInfo.getFieldType())
        {
            case PROPERTY:
                return addProperty(keyInfo.getQName(), fieldData);
            case TRANSIENT_PROPERTY:
                return updateTransientProperty(keyInfo.getFieldName(), fieldData);
            default: // Handle properties
                return changeAssociation(keyInfo, fieldData);
        }
    }

    protected boolean updateTransientProperty(String fieldName, FieldData fieldData)
    {
        return false;
    }

    protected boolean changeTransientAssociation(String fieldName, List<NodeRef> values, boolean add)
    {
        if (add)
        {
            return addTransientAssociation(fieldName, values);
        }
        else 
        {
            return removeTransientAssociation(fieldName, values);
        }
    }

    protected boolean removeTransientAssociation(String fieldName, List<NodeRef> values)
    {
        return false;
    }

    protected boolean addTransientAssociation(String fieldName, List<NodeRef> values)
    {
        return false;
    }

    protected boolean addProperty(QName qName, FieldData fieldData)
    {
        Object rawValue = fieldData.getValue();
        Serializable value = getPropertyValueToPersist(qName, rawValue);
        return updateProperty(qName, value);
    }
    
    protected Serializable getPropertyValueToPersist(QName qName, Object value)
    {
        PropertyDefinition propDef = itemData.getPropertyDefinition(qName);
        if (propDef == null)
        {
            propDef = dictionaryService.getProperty(qName);
        }
        if (propDef != null)
        {
            return valueGetter.getValue(value, propDef);
        }
        return (Serializable) value;
    }

    protected boolean changeAssociation(DataKeyInfo info, FieldData fieldData)
    {
        Object rawValue = fieldData.getValue();
        if (rawValue instanceof String)
        {
            List<NodeRef> values = NodeRef.getNodeRefs((String)rawValue, LOGGER);
            if (values.isEmpty()==false)
            {
                boolean add = info.isAdd();
                if (info.getFieldType() == FieldType.ASSOCIATION)
                {
                    return changeAssociation(info.getQName(), values, add);
                }
                else
                {
                    return changeTransientAssociation(info.getFieldName(), values, add);
                }
            }
        }
        return false;
    }

    private boolean changeAssociation(QName qName, List<NodeRef> values, boolean add)
    {
        if (add)
        {
            return addAssociation(qName, values);
        }
        else 
        {
            return removeAssociation(qName, values);
        }
    }

    protected void logIgnore(FieldData fieldData)
    {
        if (logger.isDebugEnabled())
            logger.debug("Ignoring unrecognized field: " + fieldData.getName());
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.workflow.FormPersister#persist()
     */
    public abstract T persist();
    
    protected abstract boolean removeAssociation(QName qName, List<NodeRef> values);

    protected abstract boolean addAssociation(QName qName, List<NodeRef> values);

    protected abstract boolean updateProperty(QName qName, Serializable value);

}
