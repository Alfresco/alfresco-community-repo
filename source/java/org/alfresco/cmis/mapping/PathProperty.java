/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.cmis.mapping;

import java.io.Serializable;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISServices;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.ChildAssocElement;

/**
 * Get the CMIS path property.
 * 
 * @author davidc
 */
public class PathProperty extends AbstractProperty
{
    private CMISServices cmisService;
    
    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    public PathProperty(ServiceRegistry serviceRegistry, CMISServices cmisService)
    {
        super(serviceRegistry, CMISDictionaryModel.PROP_PATH);
        this.cmisService = cmisService;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.mapping.AbstractProperty#getValue(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Serializable getValue(NodeRef nodeRef)
    {
        Path path = getServiceRegistry().getNodeService().getPath(nodeRef);
        return toDisplayPath(path);
    }
    

    private String toDisplayPath(Path path)
    {
        StringBuilder displayPath = new StringBuilder(64);

        // skip to CMIS root path
        NodeRef rootNode = cmisService.getDefaultRootNodeRef();
        int i = 0;
        while (i < path.size())
        {
            Path.Element element = path.get(i);
            if (element instanceof ChildAssocElement)
            {
                ChildAssociationRef assocRef = ((ChildAssocElement)element).getRef();
                NodeRef node = assocRef.getChildRef();
                if (node.equals(rootNode))
                {
                    break;
                }
            }
            i++;
        }
        
        if (i == path.size())
        {
            // TODO:
            //throw new AlfrescoRuntimeException("Path " + path + " not in CMIS root node scope");
        }

        if (path.size() - i == 1)
        {
            // render root path
            displayPath.append("/");
        }
        else
        {
            // render CMIS scoped path
            i++;
            while (i < path.size())
            {
                Path.Element element = path.get(i);
                if (element instanceof ChildAssocElement)
                {
                    ChildAssociationRef assocRef = ((ChildAssocElement)element).getRef();
                    NodeRef node = assocRef.getChildRef();
                    displayPath.append("/");
                    displayPath.append(getServiceRegistry().getNodeService().getProperty(node, ContentModel.PROP_NAME));
                }
                i++;
            }
        }
        
        return displayPath.toString();
    }
    
}
