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
package org.alfresco.repo.content.transform.magick;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Image transformation options
 * 
 * @author Roy Wetherall
 */
public class ImageTransformationOptions extends TransformationOptions
{
    /** imageTransformOptions aspect details */
    public static final QName ASPECT_IMAGE_TRANSFORATION_OPTIONS = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "imageTransformationOptions");
    public static final QName PROP_COMMAND_OPTIONS = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "commandOptions");
    public static final QName PROP_RESIZE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "resize");
    public static final QName PROP_RESIZE_WIDTH = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "resizeWidth");
    public static final QName PROP_RESIZE_HEIGHT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "resizeHeight");
    public static final QName PROP_RESIZE_MAINTAIN_ASPECT_RATIO = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "resizeMaintainAspectRatio");
    public static final QName PROP_RESIZE_PERCENT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "resizePercent");
    public static final QName PROP_RESIZE_TO_THUMBNAIL = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "resizeToThumbnail");
    		
    /** Command string options, provided for backward compatibility */
    private String commandOptions = "";
    
    /** Image resize options */
    private ImageResizeOptions resizeOptions;
    
    /**
     * Set the command string options
     * 
     * @param commandOptions    the command string options
     */
    public void setCommandOptions(String commandOptions)
    {
        this.commandOptions = commandOptions;
    }
    
    /**
     * Get the command string options
     * 
     * @return  String  the command string options
     */
    public String getCommandOptions()
    {
        return commandOptions;
    }
    
    /**
     * Set the image resize options
     * 
     * @param resizeOptions image resize options
     */
    public void setResizeOptions(ImageResizeOptions resizeOptions)
    {
        this.resizeOptions = resizeOptions;
    }
    
    /**
     * Get the image resize options
     * 
     * @return  ImageResizeOptions  image resize options
     */
    public ImageResizeOptions getResizeOptions()
    {
        return resizeOptions;
    }
    
    /**
     * Save the transformation options to the ImageTransformationOptions aspect.
     * 
     * @see org.alfresco.service.cmr.repository.TransformationOptions#saveToNode(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeService)
     */
    @Override
    public void saveToNode(NodeRef nodeRef, NodeService nodeService)
    {
        super.saveToNode(nodeRef, nodeService);
        
        // Create a list of the properties
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(7);
        properties.put(PROP_COMMAND_OPTIONS, this.commandOptions);
        if (this.resizeOptions != null)
        {
            properties.put(PROP_RESIZE, true);
            properties.put(PROP_RESIZE_HEIGHT, this.resizeOptions.getHeight());
            properties.put(PROP_RESIZE_WIDTH, this.resizeOptions.getWidth());
            properties.put(PROP_RESIZE_MAINTAIN_ASPECT_RATIO, this.resizeOptions.isMaintainAspectRatio());
            properties.put(PROP_RESIZE_PERCENT, this.resizeOptions.isPercentResize());
            properties.put(PROP_RESIZE_TO_THUMBNAIL, this.resizeOptions.isResizeToThumbnail());
        }
        else
        {
            properties.put(PROP_RESIZE, false);
        }
        
        // Add the aspect
        nodeService.addAspect(nodeRef, ASPECT_IMAGE_TRANSFORATION_OPTIONS, properties);
    }
    
    /**
     * Populate the image transformation options from the node provided.
     * 
     * @see org.alfresco.service.cmr.repository.TransformationOptions#populateFromNode(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeService)
     */
    @Override
    public void populateFromNode(NodeRef nodeRef, NodeService nodeService)
    {
        super.populateFromNode(nodeRef, nodeService);
        
        // Check whether the node has the image transformation options aspect
        if (nodeService.hasAspect(nodeRef, ASPECT_IMAGE_TRANSFORATION_OPTIONS) == true)
        {
            // Get the node's properties
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
            
            // Set the properties
            this.commandOptions = (String)properties.get(PROP_COMMAND_OPTIONS);
            
            // Set the resize properties
            Boolean isResize = (Boolean)properties.get(PROP_RESIZE);
            if (isResize.booleanValue() == true)
            {
                int height = ((Long)properties.get(PROP_RESIZE_HEIGHT)).intValue();
                int width = ((Long)properties.get(PROP_RESIZE_WIDTH)).intValue();
                boolean maintainAspectRatio = ((Boolean)properties.get(PROP_RESIZE_MAINTAIN_ASPECT_RATIO)).booleanValue();
                boolean percentResize = ((Boolean)properties.get(PROP_RESIZE_PERCENT)).booleanValue();
                boolean resizeToThumbnail = ((Boolean)properties.get(PROP_RESIZE_TO_THUMBNAIL)).booleanValue();
                
                this.resizeOptions = new ImageResizeOptions();
                this.resizeOptions.setHeight(height);
                this.resizeOptions.setWidth(width);
                this.resizeOptions.setMaintainAspectRatio(maintainAspectRatio);
                this.resizeOptions.setPercentResize(percentResize);
                this.resizeOptions.setResizeToThumbnail(resizeToThumbnail);
            }
        }
    }
}
