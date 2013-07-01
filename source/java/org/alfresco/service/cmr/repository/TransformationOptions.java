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
package org.alfresco.service.cmr.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.datatype.TypeConverter;
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
public class TransformationOptions implements Cloneable
{
    /** Option map names to preserve backward compatibility */
    public static final String OPT_SOURCE_NODEREF = "contentReaderNodeRef";
    public static final String OPT_SOURCE_CONTENT_PROPERTY = "sourceContentProperty";
    public static final String OPT_TARGET_NODEREF = "contentWriterNodeRef";
    public static final String OPT_TARGET_CONTENT_PROPERTY = "targetContentProperty";
    public static final String OPT_INCLUDE_EMBEDDED = "includeEmbedded"; 
    public static final String OPT_USE = "use"; 
    
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
    
    /** The use to which the transform will be put. */
    private String use;
    
    /** Time, KBytes and page limits */
    private TransformationOptionLimits limits = new TransformationOptionLimits();
    
    /** Source options based on its mimetype */
    private Map<Class<? extends TransformationSourceOptions>, TransformationSourceOptions> sourceOptionsMap;

    /**
     * Default constructor
     */
    public TransformationOptions()
    {
    }
    
    /**
     * Deep clone constructor
     */
    public TransformationOptions(TransformationOptions options)
    {
        this(options.toMap());
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
        set(optionsMap);
    }
    
    @Override
    protected TransformationOptions clone() throws CloneNotSupportedException
    {
        TransformationOptions clone = (TransformationOptions) super.clone();
        clone.limits = new TransformationOptionLimits();
        clone.copyFrom(this);
        return clone;
    }    

    /**
     * Does the work of copying the given other TransformationOptions
     * values to this one
     * 
     * @param otherOptions   the options to copy
     */
    public void copyFrom(TransformationOptions otherOptions)
    {
        this.set(otherOptions.toMap());
        this.setSourceOptionsList(otherOptions.getSourceOptionsList());
    }
    
    /**
     * Creates a clone of the TransformationOptions
     * 
     * @return a copy of the options
     */
    public TransformationOptions deepCopy()
    {
        try
        {
            return clone();
        }
        catch (CloneNotSupportedException e)
        {
            // Not thrown
        }
        return null;
    }
    
    /**
     * Sets options from the supplied map.
     * @param optionsMap
     */
    public void set(Map<String, Object> optionsMap)
    {
        this.sourceNodeRef = (NodeRef)optionsMap.get(OPT_SOURCE_NODEREF);
        this.sourceContentProperty = (QName)optionsMap.get(OPT_SOURCE_CONTENT_PROPERTY);
        this.targetNodeRef = (NodeRef)optionsMap.get(OPT_TARGET_NODEREF);
        this.targetContentProperty = (QName)optionsMap.get(OPT_TARGET_CONTENT_PROPERTY);
        this.includeEmbedded = (Boolean)optionsMap.get(OPT_INCLUDE_EMBEDDED);
        this.use = (String)optionsMap.get(OPT_USE);
        limits.set(optionsMap);
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
     * The use to which the transform will be put.
     * Initially used to select different transformation limits depending on the
     * use: "Index", "Preview"...
     *  
     * @param use to which the transform will be put.
     */
    public void setUse(String use) 
    {
       this.use = use;
    }

    /**
     * The use to which the transform will be put.
     * Initially used to select different transformation limits depending on the
     * use: "Index", "Preview"...
     * 
     * @return the use - may be null
     */
    public String getUse() 
    {
        return use;
    }

    // --------------- Time ---------------

    /**
     * Gets the timeout (ms) on the InputStream after which an IOExecption is thrown
     * to terminate very slow transformations or a subprocess is terminated (killed).
     * @return timeoutMs in milliseconds. If less than or equal to zero (the default)
     *         there is no timeout.
     */
    public long getTimeoutMs()
    {
        return limits.getTimeoutMs();
    }

    /**
     * Sets a timeout (ms) on the InputStream after which an IOExecption is thrown
     * to terminate very slow transformations or to terminate (kill) a subprocess.
     * @param timeoutMs in milliseconds. If less than or equal to zero (the default)
     *                  there is no timeout.
     *                  If greater than zero the {@code readLimitTimeMs} must not be set.
     */
    public void setTimeoutMs(long timeoutMs)
    {
        limits.setTimeoutMs(timeoutMs);
    }
    
    /**
     * Gets the limit in terms of the amount of data read (by time) to limit transformations where
     * only the start of the content is needed. After this limit is reached the InputStream reports
     * end of file.
     * @return readLimitBytes if less than or equal to zero (the default) there is no limit.
     */
    public long getReadLimitTimeMs()
    {
        return limits.getReadLimitTimeMs();
    }
    
    // --------------- KBytes ---------------

    /**
     * Sets a limit in terms of the amount of data read (by time) to limit transformations where
     * only the start of the content is needed. After this limit is reached the InputStream reports
     * end of file.
     * @param readLimitBytes if less than or equal to zero (the default) there is no limit.
     *                       If greater than zero the {@code timeoutMs} must not be set.
     */
    public void setReadLimitTimeMs(long readLimitTimeMs)
    {
        limits.setReadLimitTimeMs(readLimitTimeMs);
    }

    /**
     * Gets the maximum source content size, to skip transformations where
     * the source is just too large to expect it to perform. If the source is larger
     * the transformer indicates it is not available.
     * @return maxSourceSizeKBytes if less than or equal to zero (the default) there is no limit.
     */
    public long getMaxSourceSizeKBytes()
    {
        return limits.getMaxSourceSizeKBytes();
    }

    /**
     * Sets a maximum source content size, to skip transformations where
     * the source is just too large to expect it to perform. If the source is larger
     * the transformer indicates it is not available.
     * @param maxSourceSizeKBytes if less than or equal to zero (the default) there is no limit.
     *                       If greater than zero the {@code readLimitKBytes} must not be set.
     */
    public void setMaxSourceSizeKBytes(long maxSourceSizeKBytes)
    {
        limits.setMaxSourceSizeKBytes(maxSourceSizeKBytes);
    }
    
    /**
     * Gets the limit in terms of the about of data read to limit transformations where
     * only the start of the content is needed. After this limit is reached the InputStream reports
     * end of file.
     * @return readLimitKBytes if less than or equal to zero (the default) no limit should be applied.
     */
    public long getReadLimitKBytes()
    {
        return limits.getReadLimitKBytes();
    }

    /**
     * Sets a limit in terms of the about of data read to limit transformations where
     * only the start of the content is needed. After this limit is reached the InputStream reports
     * end of file.
     * @param readLimitKBytes if less than or equal to zero (the default) there is no limit.
     *                       If greater than zero the {@code maxSourceSizeKBytes} must not be set.
     */
    public void setReadLimitKBytes(long readLimitKBytes)
    {
        limits.setReadLimitKBytes(readLimitKBytes);
    }

    // --------------- Pages ---------------

    /**
     * Get the maximum number of pages read before an exception is thrown.
     * @return If less than or equal to zero (the default) no limit should be applied.
     */
    public int getMaxPages()
    {
        return limits.getMaxPages();
    }
    
    /**
     * Set the number of pages read from the source before an exception is thrown.
     * 
     * @param maxPages the number of pages to be read from the source. If less than or equal to zero
     *        (the default) no limit is applied.
     */
    public void setMaxPages(int maxPages)
    {
        limits.setMaxPages(maxPages);
    }

    /**
     * Get the page limit before returning EOF.
     * @return If less than or equal to zero (the default) no limit should be applied.
     */
    public int getPageLimit()
    {
        return limits.getPageLimit();
    }
    
    /**
     * Set the number of pages read from the source before returning EOF.
     * 
     * @param pageLimit the number of pages to be read from the source. If less 
     *        than or equal to zero (the default) no limit is applied.
     */
    public void setPageLimit(int pageLimit)
    {
        limits.setPageLimit(pageLimit);
    }
    
    /**
     * Returns max and limit values for time, size and pages in a single operation. 
     */
    public TransformationOptionLimits getLimits()
    {
        return limits; 
    }

    /**
     * Sets max and limit values for time, size and pages in a single operation. 
     */
    public void setLimits(TransformationOptionLimits limits)
    {
        this.limits = limits; 
    }
    
    /**
     * Gets the map of source options further describing how the source should
     * be transformed based on its mimetype
     * 
     * @return the source mimetype to source options map
     */
    protected Map<Class<? extends TransformationSourceOptions>, TransformationSourceOptions> getSourceOptionsMap()
    {
        return sourceOptionsMap;
    }
    
    /**
     * Gets the immutable list of source options further describing how the source should
     * be transformed based on its mimetype.
     * Use {@link TransformationOptions#addSourceOptions(TransformationSourceOptions)}
     * to add source options.
     * 
     * @return the source options list
     */
    public Collection<TransformationSourceOptions> getSourceOptionsList()
    {
        if (sourceOptionsMap == null)
            return null;
        return sourceOptionsMap.values();
    }
    
    /**
     * Sets the list of source options further describing how the source should
     * be transformed based on its mimetype.
     * 
     * @param sourceOptionsList the source options list
     */
    public void setSourceOptionsList(Collection<TransformationSourceOptions> sourceOptionsList)
    {
        if (sourceOptionsList != null)
        {
            for (TransformationSourceOptions sourceOptions : sourceOptionsList)
            {
                addSourceOptions(sourceOptions);
            }
        }
    }
    
    /**
     * Adds the given sourceOptions to the sourceOptionsMap.
     * <p>
     * Note that if source options of the same class already exists a new
     * merged source options object is added.
     * 
     * @param sourceOptions
     */
    public void addSourceOptions(TransformationSourceOptions sourceOptions)
    {
        if (sourceOptionsMap == null)
        {
            sourceOptionsMap = new HashMap<Class<? extends TransformationSourceOptions>, TransformationSourceOptions>(1);
        }
        TransformationSourceOptions newOptions = sourceOptions;
        TransformationSourceOptions existingOptions = sourceOptionsMap.get(sourceOptions.getClass());
        if (existingOptions != null)
        {
            newOptions = existingOptions.mergedOptions(sourceOptions);
        }
        sourceOptionsMap.put(sourceOptions.getClass(), newOptions);
    }
    
    /**
     * Gets the appropriate source options for the given mimetype if available.
     * 
     * @param sourceMimetype
     * @return the source options for the mimetype
     */
    @SuppressWarnings("unchecked")
    public <T extends TransformationSourceOptions> T getSourceOptions(Class<T> clazz)
    {
        if (sourceOptionsMap == null)
            return null;
        return (T) sourceOptionsMap.get(clazz);
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
     *   <li>{@link #OPT_USE}</li>
     *   <li>{@link TransformationOptionLimits#OPT_TIMEOUT_MS}</li>
     *   <li>{@link TransformationOptionLimits#OPT_READ_LIMIT_TIME_MS}</li>
     *   <li>{@link TransformationOptionLimits#OPT_MAX_SOURCE_SIZE_K_BYTES</li>
     *   <li>{@link TransformationOptionLimits#OPT_READ_LIMIT_K_BYTES}</li>
     *   <li>{@link TransformationOptionLimits#OPT_MAX_PAGES}</li>
     *   <li>{@link TransformationOptionLimits#OPT_PAGE_LIMIT}</li>
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
        optionsMap.put(OPT_USE, use);
        limits.toMap(optionsMap);
        return optionsMap;
    }
    
    public String toString(boolean includeLimits)
    {
        Map<String, Object> map = toMap();
        if (!includeLimits)
        {
            TransformationOptionLimits.removeFromMap(map);
        }
        return map.toString();
    }
    
    public String toString()
    {
        return toMap().toString();
    }
    
    public static TypeConverter.Converter<String, Boolean> relaxedBooleanTypeConverter = new TypeConverter.Converter<String, Boolean>()
    {
        public Boolean convert(String source)
        {
            if(source == null || source.length() == 0)
                return null;
            
            if(source.equalsIgnoreCase("true") ||
               source.equalsIgnoreCase("t") ||
               source.equalsIgnoreCase("yes") ||
               source.equalsIgnoreCase("y"))
            {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
    };

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.includeEmbedded == null) ? 0 : this.includeEmbedded.hashCode());
        result = prime * result + ((this.limits == null) ? 0 : this.limits.hashCode());
        result = prime * result + ((this.sourceContentProperty == null) ? 0 : this.sourceContentProperty.hashCode());
        result = prime * result + ((this.sourceNodeRef == null) ? 0 : this.sourceNodeRef.hashCode());
        result = prime * result + ((this.targetContentProperty == null) ? 0 : this.targetContentProperty.hashCode());
        result = prime * result + ((this.targetNodeRef == null) ? 0 : this.targetNodeRef.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TransformationOptions other = (TransformationOptions) obj;
        if (this.includeEmbedded == null)
        {
            if (other.includeEmbedded != null) return false;
        }
        else if (!this.includeEmbedded.equals(other.includeEmbedded)) return false;
        if (this.limits == null)
        {
            if (other.limits != null) return false;
        }
        else if (!this.limits.equals(other.limits)) return false;
        if (this.sourceContentProperty == null)
        {
            if (other.sourceContentProperty != null) return false;
        }
        else if (!this.sourceContentProperty.equals(other.sourceContentProperty)) return false;
        if (this.sourceNodeRef == null)
        {
            if (other.sourceNodeRef != null) return false;
        }
        else if (!this.sourceNodeRef.equals(other.sourceNodeRef)) return false;
        if (this.targetContentProperty == null)
        {
            if (other.targetContentProperty != null) return false;
        }
        else if (!this.targetContentProperty.equals(other.targetContentProperty)) return false;
        if (this.targetNodeRef == null)
        {
            if (other.targetNodeRef != null) return false;
        }
        else if (!this.targetNodeRef.equals(other.targetNodeRef)) return false;
        return true;
    }
}
