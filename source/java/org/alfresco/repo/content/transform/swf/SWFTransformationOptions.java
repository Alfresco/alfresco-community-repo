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
package org.alfresco.repo.content.transform.swf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * SFW transformation options
 * 
 * @author Roy Wetherall
 */
public class SWFTransformationOptions extends TransformationOptions
{
    private final static QName ASPECT_SWF_TRANSFORMATION_OPTIONS = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "swfTransformationOptions");
    private final static QName PROP_FLASH_VERSION = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "flashVerison");
    
    /** The version of the flash to convert to */
    private String flashVersion = "9";
    
    public void setFlashVersion(String flashVersion)
    {
        this.flashVersion = flashVersion;
    }
    
    public String getFlashVersion()
    {
        return flashVersion;
    }
    
    @Override
    public void saveToNode(NodeRef nodeRef, NodeService nodeService)
    {
        super.saveToNode(nodeRef, nodeService);
        
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(7);
        properties.put(PROP_FLASH_VERSION, this.flashVersion);
        nodeService.addAspect(nodeRef, ASPECT_SWF_TRANSFORMATION_OPTIONS, properties);
    }
    
    @Override
    public void populateFromNode(NodeRef nodeRef, NodeService nodeService)
    {
        super.populateFromNode(nodeRef, nodeService);
        
        // Check whether the node has the image transformation options aspect
        if (nodeService.hasAspect(nodeRef, ASPECT_SWF_TRANSFORMATION_OPTIONS) == true)
        {
            // Get the node's properties
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
            
            // Set the properties
            this.flashVersion = (String)properties.get(PROP_FLASH_VERSION);
        }
    }
}
