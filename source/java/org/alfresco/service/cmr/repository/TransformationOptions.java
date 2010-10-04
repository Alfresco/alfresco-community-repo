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
package org.alfresco.service.cmr.repository;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.namespace.QName;

/**
 * Class containing values of options that are passed to content transformers.  These options 
 * are used to determine the applicability of a content transformer and also during the
 * transformation process to provide context or parameter values.
 * <p>
 * This base class provides some common, optional contextual information about the source and
 * target nodes and properties used by the transformation. 
 * 
 * @author Roy Wetherall
 * @since 3.0.0
 */
public class TransformationOptions
{
    /** Option map names to preserve backward compatibility */
    public static final String OPT_SOURCE_NODEREF = "contentReaderNodeRef";
    public static final String OPT_SOURCE_CONTENT_PROPERTY = "sourceContentProperty";
    public static final String OPT_TARGET_NODEREF = "contentWriterNodeRef";
    public static final String OPT_TARGET_CONTENT_PROPERTY = "targetContentProperty";
    public static final String OPT_INCLUDE_EMBEDDED = "includeEmbedded"; 
    
    /** The source node reference */
    private NodeRef sourceNodeRef;
    
    /** The source content property */    
    private QName sourceContentProperty;
    
    /** The target node reference */
    private NodeRef targetNodeRef;
    
    /** The target content property */
    private QName targetContentProperty;
    
    /** The include embedded resources yes/no */
    private Boolean includeEmbedded;

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
    public TransformationOptions(
            NodeRef sourceNodeRef, QName sourceContentProperty, NodeRef targetNodeRef, QName targetContentProperty)
    {
        this.sourceNodeRef = sourceNodeRef;
        this.sourceContentProperty = sourceContentProperty;
        this.targetNodeRef = targetNodeRef;
        this.targetContentProperty = targetContentProperty;
        this.includeEmbedded = null;
    }
    
    /**
     * Constructor.  Creates a transformation options object from a map.  
     * Provided for back ward compatibility.
     * 
     * @param optionsMap    options map
     */
    public TransformationOptions(Map<String, Object> optionsMap)
    {
        this.sourceNodeRef = (NodeRef)optionsMap.get(OPT_SOURCE_NODEREF);
        this.sourceContentProperty = (QName)optionsMap.get(OPT_SOURCE_CONTENT_PROPERTY);
        this.targetNodeRef = (NodeRef)optionsMap.get(OPT_TARGET_NODEREF);
        this.targetContentProperty = (QName)optionsMap.get(OPT_TARGET_CONTENT_PROPERTY);
        this.includeEmbedded = (Boolean)optionsMap.get(OPT_INCLUDE_EMBEDDED);
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
     * If the source content includes embedded resources,
     *  should the transformer attempt to transform these
     *  as well?
     * Not many transformers do support embedded resources,
     *  so this option will only affect those that can.
     *  
     * @param includeEmbedded the include embedded flag.
     */
    public void setIncludeEmbedded(Boolean includeEmbedded) 
    {
       this.includeEmbedded = includeEmbedded;
    }

    /**
     * If the source content includes embedded resources,
     *  should the transformer attempt to transform these
     *  as well?
     * Not many transformers do support embedded resources,
     *  so this option will only affect those that can.
     *  
     * @return true, false, or null for the default for the transformer
     */
    public Boolean getIncludeEmbedded() 
    {
        return includeEmbedded;
    }

   /**
     * Convert the transformation options into a map.
     * <p>
     * Basic options (optional) are:
     * <ul>
     *   <li>{@link #OPT_SOURCE_NODEREF}</li>
     *   <li>{@link #OPT_SOURCE_CONTENT_PROPERTY}</li>
     *   <li>{@link #OPT_TARGET_NODEREF}</li>
     *   <li>{@link #OPT_TARGET_CONTENT_PROPERTY}</li>
     *   <li>{@link #OPT_INCLUDE_EMBEDDED}</li>
     * </ul>
     * <p>
     * Override this method to append option values to the map.  Derived classes should call
     * the base class before appending further values and returning the result.
     */
    public Map<String, Object> toMap()
    {
        Map<String, Object> optionsMap = new HashMap<String, Object>(7);
        optionsMap.put(OPT_SOURCE_NODEREF, sourceNodeRef);
        optionsMap.put(OPT_SOURCE_CONTENT_PROPERTY, sourceContentProperty);
        optionsMap.put(OPT_TARGET_NODEREF, targetNodeRef);
        optionsMap.put(OPT_TARGET_CONTENT_PROPERTY, targetContentProperty);
        optionsMap.put(OPT_INCLUDE_EMBEDDED, includeEmbedded);
        return optionsMap;
    }
}
