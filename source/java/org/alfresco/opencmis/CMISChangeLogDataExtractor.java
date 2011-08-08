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
package org.alfresco.opencmis;

import java.io.Serializable;
import java.util.HashMap;

import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.dictionary.CMISPropertyAccessor;
import org.alfresco.opencmis.dictionary.TypeDefinitionWrapper;
import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;

/**
 * An extractor that allows to filter data using the following rule: Audit
 * records should only be created for items in the CMIS domain model.
 * 
 * @author Stas Sokolovsky
 */
public class CMISChangeLogDataExtractor extends AbstractDataExtractor
{
    public static final String KEY_NODE_REF = "nodeRef";
    public static final String KEY_OBJECT_ID = "objectId";

    private NodeService nodeService;
    private CMISDictionaryService cmisDictionaryService;

    /**
     * Extracts relevant node refs and Ids from auditing data
     * 
     * @see org.alfresco.repo.audit.extractor.DataExtractor.extractData(java.io.
     *      Serializable)
     */
    public Serializable extractData(Serializable value) throws Throwable
    {
        NodeRef nodeRef = getNodeRef(value);

        QName typeQName = nodeService.getType(nodeRef);
        TypeDefinitionWrapper type = cmisDictionaryService.findNodeType(typeQName);

        HashMap<String, Serializable> result = new HashMap<String, Serializable>(5);
        result.put(KEY_NODE_REF, nodeRef);
        // Support version nodes by recording the object ID
        CMISPropertyAccessor accessor = type.getPropertyById(PropertyIds.OBJECT_ID).getPropertyAccessor();
        result.put(KEY_OBJECT_ID, accessor.getValue(accessor.createNodeInfo(nodeRef)));
        
        return result;
    }

    /**
     * @return Returns <tt>true</tt> if items in the CMIS domain model
     * @see org.alfresco.repo.audit.extractor.DataExtractor.isSupported(java.io.
     *      Serializable)
     */
    public boolean isSupported(Serializable data)
    {
        if (data != null)
        {
            NodeRef nodeRef = getNodeRef(data);
            if (nodeRef != null)
            {
                QName typeQName = nodeService.getType(nodeRef);
                TypeDefinitionWrapper type = cmisDictionaryService.findNodeType(typeQName);

                return (type != null)
                        && (type.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT || type.getBaseTypeId() == BaseTypeId.CMIS_FOLDER);
            }
        }
        return false;
    }

    /**
     * Gets the NodeRef from auditing data
     * 
     * @param data
     *            audit data
     * @return Node Reference
     */
    private NodeRef getNodeRef(Serializable data)
    {
        NodeRef nodeRef = null;
        if (data instanceof FileInfo)
        {
            nodeRef = ((FileInfo) data).getNodeRef();
        } else if (data instanceof NodeRef)
        {
            nodeRef = (NodeRef) data;
        }
        return nodeRef;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setOpenCMISDictionaryService(CMISDictionaryService cmisDictionaryService)
    {
        this.cmisDictionaryService = cmisDictionaryService;
    }
}
