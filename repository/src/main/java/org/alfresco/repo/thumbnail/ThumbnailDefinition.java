/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.thumbnail;

import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * This class provides the thumbnail details to the thumbnail service.
 * 
 * @author Roy Wetherall
 *
 * @deprecated The thumbnails code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public class ThumbnailDefinition
{

    private static final Log logger = LogFactory.getLog(ThumbnailDefinition.class);

    /** Name of the thumbnail */
    private String name;

    /** The destination mimetype */
    private String mimetype;

    /** Transformation options */
    private TransformationOptions options;

    /** Failure options */
    private FailureHandlingOptions failureOptions;

    /**
     * Path to placeholder thumbnail
     */
    private String placeHolderResourcePath;

    /**
     * Path to mime aware placeholder thumbnail
     */
    private String mimeAwarePlaceHolderResourcePath;

    /** Username to run the thumbnailrendition as */
    private String runAs;

    /** The thumbnail registry */
    private ThumbnailRegistry thumbnailRegistry;

    /**
     * Default constructor
     */
    public ThumbnailDefinition()
    {}

    /**
     * Constructor
     * 
     * @param destinationMimetype
     *            String
     * @param options
     *            TransformationOptions
     */
    public ThumbnailDefinition(String destinationMimetype, TransformationOptions options)
    {
        this.mimetype = destinationMimetype;
        this.options = options;
    }

    /**
     * Constructor. Specify the name of the thumbnail.
     * 
     * @param thumbnailName
     *            the name of the thumbnail, can be null
     */
    public ThumbnailDefinition(String mimetype, TransformationOptions options, String thumbnailName)
    {
        this(mimetype, options);
        this.name = thumbnailName;
        options.setUse(thumbnailName);
    }

    /**
     * Constructor. Specify the place holder thumbnail path.
     * 
     * @param mimetype
     *            String
     * @param options
     *            TransformationOptions
     * @param thumbnailName
     *            String
     * @param placeHolderResourcePath
     *            String
     */
    public ThumbnailDefinition(String mimetype, TransformationOptions options, String thumbnailName, String placeHolderResourcePath)
    {
        this(mimetype, options, thumbnailName);
        this.placeHolderResourcePath = placeHolderResourcePath;
        options.setUse(thumbnailName);
    }

    /**
     * Set the destination mimetype
     * 
     * @param mimetype
     *            the destination minetype
     */
    public void setMimetype(String mimetype)
    {
        this.mimetype = mimetype;
    }

    /**
     * Get the destination mimetype
     * 
     * @return the destination mimetype
     */
    public String getMimetype()
    {
        return mimetype;
    }

    /**
     * Set the transformation options
     * 
     * @param options
     *            the transformation options
     */
    public void setTransformationOptions(TransformationOptions options)
    {
        this.options = options;
    }

    /**
     * Get the transformation options
     * 
     * @return the transformation options
     */
    public TransformationOptions getTransformationOptions()
    {
        return options;
    }

    /**
     * Set the {@link FailureHandlingOptions failure options}.
     * 
     * @param failureOptions
     *            the failure options.
     * @since 3.5.0
     */
    public void setFailureHandlingOptions(FailureHandlingOptions failureOptions)
    {
        this.failureOptions = failureOptions;
    }

    /**
     * Get the {@link FailureHandlingOptions failure options}.
     * 
     * @return the failure options
     * @since 3.5.0
     */
    public FailureHandlingOptions getFailureHandlingOptions()
    {
        return failureOptions;
    }

    /**
     * Sets the name of the thumbnail
     * 
     * @param thumbnailName
     *            the thumbnail name
     */
    public void setName(String thumbnailName)
    {
        this.name = thumbnailName;
    }

    /**
     * Gets the name of the thumbnail
     * 
     * @return String the name of the thumbnail, null if non specified
     */
    public String getName()
    {
        return name;
    }

    public void setRunAs(String runAs)
    {
        this.runAs = runAs;
    }

    public String getRunAs()
    {
        return this.runAs;
    }

    /**
     * This method sets the placeholder resource path.
     * 
     * @param placeHolderResourcePath
     *            String
     */
    public void setPlaceHolderResourcePath(String placeHolderResourcePath)
    {
        this.placeHolderResourcePath = placeHolderResourcePath;
    }

    /**
     * 
     * @return String
     */
    public String getPlaceHolderResourcePath()
    {
        return placeHolderResourcePath;
    }

    /**
     * This method sets the mime-aware placeholder resource path template.
     * 
     * @param mimeAwarePlaceHolderResourcePath
     *            String
     * @since 3.4.1 (Team)
     */
    public void setMimeAwarePlaceHolderResourcePath(String mimeAwarePlaceHolderResourcePath)
    {
        this.mimeAwarePlaceHolderResourcePath = mimeAwarePlaceHolderResourcePath;
    }

    /**
     * 
     * @return String
     * @since 3.4.1 (Team)
     */
    public String getMimeAwarePlaceHolderResourcePath()
    {
        return mimeAwarePlaceHolderResourcePath;
    }

    /**
     * Gets the thumbnail registry
     * 
     * @return the thumbnail registry
     */
    public ThumbnailRegistry getThumbnailRegistry()
    {
        return thumbnailRegistry;
    }

    /**
     * Sets the thumbnail registry
     * 
     * @param thumbnailRegistry
     *            ThumbnailRegistry
     */
    public void setThumbnailRegistry(ThumbnailRegistry thumbnailRegistry)
    {
        this.thumbnailRegistry = thumbnailRegistry;
    }

    /**
     * Registers the thumbnail definition with the thumbnail registry.
     * 
     * @see #setThumbnailRegistry(ThumbnailRegistry)
     */
    public void register()
    {
        if (thumbnailRegistry == null)
        {
            logger.warn("Property 'thumbnailRegistry' has not been set.  Ignoring auto-registration: \n" +
                    "   extracter: " + this);
            return;
        }
        thumbnailRegistry.addThumbnailDefinition(this);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        ThumbnailDefinition that = (ThumbnailDefinition) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }
}
