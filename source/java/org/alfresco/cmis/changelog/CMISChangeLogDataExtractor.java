/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.cmis.changelog;

import java.io.Serializable;
import java.util.HashMap;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISServices;
import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
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
    private FileFolderService fileFolderService;
    
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
        boolean result = false;
        if (data != null)
        {
            NodeRef nodeRef = getNodeRef(data);
            if (nodeRef != null)
            {
                if (!fileFolderService.exists(nodeRef))
                {
                    result = true;
                }
                // Does the node represent a file or folder
                else if (fileFolderService.getFileInfo(nodeRef) != null)
                {
                    // Is the node located within CMIS defaultRootStore
                    if (cmisService.getDefaultRootStoreRef().equals(nodeRef.getStoreRef()))
                    {
                        result = true;
                    }
                }
            }
        }
        return result;
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

    /**
     * Set the FileFolder service
     * 
     * @param fileFolderService FileFolder service
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

}
