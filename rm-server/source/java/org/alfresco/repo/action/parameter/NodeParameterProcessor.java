/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.action.parameter;

import java.io.Serializable;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.ArrayUtils;

/**
 * Node parameter processor.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class NodeParameterProcessor extends ParameterProcessor
{
    /** Supported data types */
    private QName[] supportedDataTypes =
    {
            DataTypeDefinition.TEXT,
            DataTypeDefinition.BOOLEAN,
            DataTypeDefinition.DATE,
            DataTypeDefinition.DATETIME,
            DataTypeDefinition.DOUBLE,
            DataTypeDefinition.FLOAT,
            DataTypeDefinition.INT,
            DataTypeDefinition.MLTEXT
    };

    /** Node service */
    private NodeService nodeService;

    /** Namespace service */
    private NamespaceService namespaceService;

    /** Dictionary service */
    private DictionaryService dictionaryService;

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @see org.alfresco.repo.action.parameter.ParameterProcessor#process(java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public String process(String value, NodeRef actionedUponNodeRef)
    {
        // the default position is to return the value un-changed
        String result = value;

        // strip the processor name from the value
        value = stripName(value);
        if (value.isEmpty() == false)
        {
            QName qname = QName.createQName(value, namespaceService);

            PropertyDefinition propertyDefinition = dictionaryService.getProperty(qname);
            if (propertyDefinition == null)
            {
                throw new AlfrescoRuntimeException("The property " + value + " does not have a property definition.");
            }

            QName type = propertyDefinition.getDataType().getName();
            if (ArrayUtils.contains(supportedDataTypes, type) == true)
            {
                Serializable propertyValue = nodeService.getProperty(actionedUponNodeRef, qname);   
                if (propertyValue != null)
                {
                    result = propertyValue.toString();
                }
                else
                {
                    // set the result to the empty string
                    result = "";
                }
            }
            else
            {
                throw new AlfrescoRuntimeException("The property " + value + " is of type " + type.toString() + " which is not supported by parameter substitution.");
            }
        }

        return result;
    }
}
