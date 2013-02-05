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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Base implementation of TransformationSourceOptions which holds applicable mimetypes
 * and handles merge of options.
 * 
 * @author Ray Gauss II
 */
public abstract class AbstractTransformationSourceOptions implements TransformationSourceOptions, Cloneable
{
    protected static final String MIMETYPE_VIDEO_PREFIX = "video/";
    protected static final String MIMETYPE_AUDIO_PREFIX = "audio/";
    protected static final String MIMETYPE_IMAGE_PREFIX = "image/";

    /** The list of applicable mimetypes */
    private List<String> applicabledMimetypes;

    /**
     * Gets the list of applicable mimetypes
     * 
     * @return the applicable mimetypes
     */
    public List<String> getApplicabledMimetypes()
    {
        return applicabledMimetypes;
    }

    /**
     * Sets the list of applicable mimetypes
     * 
     * @param applicableMimetypes the applicable mimetypes
     */
    public void setApplicableMimetypes(List<String> applicabledMimetypes)
    {
        this.applicabledMimetypes = applicabledMimetypes;
    }

    /**
     * Gets whether or not these transformation source options apply for the
     * given mimetype
     * 
     * @param mimetype the mimetype of the source
     * @return if these transformation source options apply
     */
    public boolean isApplicableForMimetype(String mimetype)
    {
        if (mimetype != null && applicabledMimetypes != null) { return applicabledMimetypes.contains(mimetype); }
        return false;
    }

    @Override
    protected AbstractTransformationSourceOptions clone() throws CloneNotSupportedException
    {
        return (AbstractTransformationSourceOptions) super.clone();
    }
    
    /**
     * Creates a new <code>TransformationSourceOptions</code> object from this
     * one, merging any non-null overriding fields in the given
     * <code>overridingOptions</code>
     * 
     * @param overridingOptions
     * @return a merged <code>TransformationSourceOptions</code> object
     */
    public TransformationSourceOptions mergedOptions(TransformationSourceOptions overridingOptions)
    {
        try
        {
            AbstractTransformationSourceOptions mergedOptions = this.clone();
            mergedOptions.setApplicableMimetypes(this.getApplicabledMimetypes());

            return mergedOptions;
        }
        catch (CloneNotSupportedException e)
        {
            // Not thrown
        }
        return null;
    }
    
    /**
     * Adds the given paramValue to the given params if it's not null.
     * 
     * @param paramName
     * @param paramValue
     * @param params
     */
    protected void putParameterIfNotNull(String paramName, Serializable paramValue, Map<String, Serializable> params)
    {
        if (paramValue != null)
        {
            params.put(paramName, paramValue);
        }
    }
    
}
