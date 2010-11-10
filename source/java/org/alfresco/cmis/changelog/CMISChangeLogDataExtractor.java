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
package org.alfresco.cmis.changelog;

import java.io.Serializable;
import java.util.HashMap;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISInvalidArgumentException;
import org.alfresco.cmis.CMISServices;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.CMISTypeId;
import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * An extractor that allows to filter data using the following rule: 
 * Audit records should only be created for items in the CMIS domain model.
 * 
 * @author Stas Sokolovsky
 */
public class CMISChangeLogDataExtractor extends AbstractDataExtractor
{
    private CMISServices cmisService;
    
    public static final String KEY_NODE_REF = "nodeRef";
    public static final String KEY_OBJECT_ID = "objectId";

    /**
     * Extracts relevant node refs and Ids from auditing data
     * 
     * @see org.alfresco.repo.audit.extractor.DataExtractor.extractData(java.io.Serializable)
     */
    public Serializable extractData(Serializable value) throws Throwable
    {
        NodeRef nodeRef = getNodeRef(value);
        HashMap <String, Serializable> result = new HashMap<String, Serializable>(5);
        result.put(KEY_NODE_REF, nodeRef);
        // Support version nodes by recording the object ID
        result.put(KEY_OBJECT_ID, cmisService.getProperty(nodeRef, CMISDictionaryModel.PROP_OBJECT_ID));
        return result;
    }

    /**
     * @return Returns <tt>true</tt> if items in the CMIS domain model
     * @see org.alfresco.repo.audit.extractor.DataExtractor.isSupported(java.io.Serializable)
     */
    public boolean isSupported(Serializable data)
    {
        if (data != null)
        {
            NodeRef nodeRef = getNodeRef(data);
            if (nodeRef != null)
            {
                try
                {
                    CMISTypeDefinition typeDef = cmisService.getTypeDefinition(nodeRef);
                    if (typeDef != null)
                    {
                        CMISTypeId typeId = typeDef.getBaseType().getTypeId(); 
                        return typeId.equals(CMISDictionaryModel.DOCUMENT_TYPE_ID) || typeId.equals(CMISDictionaryModel.FOLDER_TYPE_ID);
                    }
                }
                catch (CMISInvalidArgumentException e)
                {
                    // Ignore and return false
                }
            }
        }
        return false;
    }

    /**
     * Gets the NodeRef from auditing data
     * 
     * @param data audit data
     * @return Node Reference
     */
    private NodeRef getNodeRef(Serializable data)
    {
        NodeRef nodeRef = null;
        if (data instanceof ChildAssociationRef)
        {
            nodeRef = ((ChildAssociationRef) data).getChildRef();
        }
        if (data instanceof FileInfo)
        {
            nodeRef = ((FileInfo) data).getNodeRef();
        }
        else if (data instanceof NodeRef)
        {
            nodeRef = (NodeRef) data;
        }
        return nodeRef;
    }

    /**
     * Set the CMIS service
     * 
     * @param cmisService CMIS service
     */
    public void setCmisService(CMISServices cmisService)
    {
        this.cmisService = cmisService;
    }
}
