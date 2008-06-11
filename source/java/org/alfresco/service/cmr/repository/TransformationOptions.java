/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.service.cmr.repository;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.namespace.QName;

/**
 * Tansformation options.
 * <p>
 * Class containing values of options that are passed to content transformers.  These options 
 * are used to determine the applicability of a content transformer and also during the
 * transformation process to provide context or parameter values.
 * 
 * @author Roy Wetherall
 */
public class TransformationOptions
{
    /** Option map names to preserve backward compatibility */
    public static final String OPT_SOURCE_NODEREF = "contentReaderNodeRef";
    public static final String OPT_SOURCE_CONTENT_PROPERTY = "sourceContentProperty";
    public static final String OPT_TARGET_NODEREF = "contentWriterNodeRef";
    public static final String OPT_TARGET_CONTENT_PROPERTY = "targetContentProperty";
    
    /** The source node reference */
    private NodeRef sourceNodeRef;
    
    /** The source content property */    
    private QName sourceContentProperty;
    
    /** The target node reference */
    private NodeRef targetNodeRef;
    
    /** The target content property */
    private QName targetContentProperty;

    /**
     * Default construtor
     */
    public TransformationOptions()
    {
    }
    
    /**
     * Constructor 
     * 
     * @param sourceNodeRef             the source node reference
     * @param sourceContentProperty     the source content property
     * @param targetNodeRef             the target node reference
     * @param targetContentProperty     the target content property
     */
    public TransformationOptions(NodeRef sourceNodeRef, QName sourceContentProperty, NodeRef targetNodeRef, QName targetContentProperty)
    {
        this.sourceNodeRef = sourceNodeRef;
        this.sourceContentProperty = sourceContentProperty;
        this.targetNodeRef = targetNodeRef;
        this.targetContentProperty = targetContentProperty;
    }
    
    /**
     * Constrcutor.  Creates a transformation options object from a map.  
     * Provided for back ward compatibility.
     * 
     * @param optionsMap    options map
     */
    public TransformationOptions(Map<String, Object> optionsMap)
    {
        fromMapImpl(optionsMap);
    }
    
    /**
     * Set the source node reference
     * 
     * @param sourceNodeRef     the source node reference
     */
    public void setSourceNodeRef(NodeRef sourceNodeRef)
    {
        this.sourceNodeRef = sourceNodeRef;
    }
    
    /**
     * Gets the source node reference
     * 
     * @return NodeRef  the source node reference
     */
    public NodeRef getSourceNodeRef()
    {
        return sourceNodeRef;
    }
    
    /**
     * Set the source content property
     * 
     * @param sourceContentProperty     the source content property
     */
    public void setSourceContentProperty(QName sourceContentProperty)
    {
        this.sourceContentProperty = sourceContentProperty;
    }
    
    /**
     * Get the source content property
     * 
     * @return  the source content property
     */
    public QName getSourceContentProperty()
    {
        return sourceContentProperty;
    }
    
    /**
     * Set the taget node reference
     * 
     * @param targetNodeRef     the target node reference
     */
    public void setTargetNodeRef(NodeRef targetNodeRef)
    {
        this.targetNodeRef = targetNodeRef;
    }
    
    /**
     * Get the target node reference
     * 
     * @return  the target node reference
     */
    public NodeRef getTargetNodeRef()
    {
        return targetNodeRef;
    }
    
    /**
     * Set the target content property
     * 
     * @param targetContentProperty     the target content property
     */
    public void setTargetContentProperty(QName targetContentProperty)
    {
        this.targetContentProperty = targetContentProperty;
    }
    
    /**
     * Get the target content property
     * 
     * @return  the target property
     */
    public QName getTargetContentProperty()
    {
        return targetContentProperty;
    }
    
    /**
     * Converts the transformation options object into an equivalent map
     * 
     * @return  
     */
    public Map<String, Object> toMap()
    {
        return new HashMap<String, Object>(10);
    }
    
    /**
     * Places the values of the transformation options into a Map
     * 
     * @param optionsMap    the options map
     */
    protected void toMapImpl(Map<String, Object> optionsMap)
    {
        optionsMap.put(OPT_SOURCE_NODEREF, getSourceNodeRef());
        optionsMap.put(OPT_SOURCE_CONTENT_PROPERTY, getSourceContentProperty());
        optionsMap.put(OPT_TARGET_NODEREF, getTargetNodeRef());
        optionsMap.put(OPT_TARGET_CONTENT_PROPERTY, getTargetContentProperty());
    }
    
    /**
     * Populates the transformation options from a given Map of value.
     * 
     * @param optionsMap    the options map
     */
    protected void fromMapImpl(Map<String, Object> optionsMap)
    {
        this.sourceNodeRef = (NodeRef)optionsMap.get(OPT_SOURCE_NODEREF);
        this.sourceContentProperty = (QName)optionsMap.get(OPT_SOURCE_CONTENT_PROPERTY);
        this.targetNodeRef = (NodeRef)optionsMap.get(OPT_TARGET_NODEREF);
        this.targetContentProperty = (QName)optionsMap.get(OPT_TARGET_CONTENT_PROPERTY);
    } 
}
