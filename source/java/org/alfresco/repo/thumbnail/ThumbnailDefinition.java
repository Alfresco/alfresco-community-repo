/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.thumbnail;

import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * This class provides the thumbnail details to the thumbnail service.
 * 
 * @author Roy Wetherall
 */
public class ThumbnailDefinition
{
    /** Name of the thumbnail */
    private String name;
    
    /** The destination mimetype */
    private String mimetype;
    
    /** Transformation options */
    private TransformationOptions options;    
    
    /** Failure options */
    private FailureHandlingOptions failureOptions;
    
    /** Path to placeholder thumbnail
     */
    private String placeHolderResourcePath;
    
    /** Path to mime aware placeholder thumbnail
     * */
    private String mimeAwarePlaceHolderResourcePath;
    
    /** Username to run the thumbnailrendition as */
    private String runAs;
    
    /**
     * Default constructor
     */
    public ThumbnailDefinition()
    {
    }
    
    /**
     * Constructor
     * 
     * @param destinationMimetype
     * @param options
     */
    public ThumbnailDefinition(String destinationMimetype, TransformationOptions options)
    {       
        this.mimetype = destinationMimetype;
        this.options = options;
    }    
    
    /**
     * Constructor.  Specify the name of the thumbnail.
     * 
     * @param thumbnailName the name of the thumbnail, can be null
     */
    public ThumbnailDefinition(String mimetype, TransformationOptions options, String thumbnailName)
    {
        this(mimetype, options);
        this.name= thumbnailName;
    }
    
    /**
     * Constructor.  Specify the place holder thumbnail path.
     * 
     * @param mimetype          
     * @param options
     * @param thumbnailName
     * @param placeHolderResourcePath
     */
    public ThumbnailDefinition(String mimetype, TransformationOptions options, String thumbnailName, String placeHolderResourcePath)
    {
        this(mimetype, options, thumbnailName);
        this.placeHolderResourcePath = placeHolderResourcePath;
    }
    
    /**
     * Set the destination mimetype
     * 
     * @param mimetype   the destination minetype
     */
    public void setMimetype(String mimetype)
    {
        this.mimetype = mimetype;
    }
    
    /**
     * Get the destination mimetype
     * 
     * @return  the destination mimetype
     */
    public String getMimetype()
    {
        return mimetype;
    }
    
    /**
     * Set the transformation options
     * 
     * @param options   the transformation options
     */
    public void setTransformationOptions(TransformationOptions options)
    {
        this.options = options;
    }

    /**
     * Get the transformation options
     * 
     * @return  the transformation options
     */
    public TransformationOptions getTransformationOptions()
    {
        return options;
    }

    /**
     * Set the {@link FailureHandlingOptions failure options}.
     * 
     * @param failureOptions the failure options.
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
     * @param thumbnailName     the thumbnail name
     */
    public void setName(String thumbnailName)
    {
        this.name = thumbnailName;
    }
    
    /**
     * Gets the name of the thumbnail
     * 
     * @return String   the name of the thumbnail, null if non specified
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
     */
    public void setPlaceHolderResourcePath(String placeHolderResourcePath)
    {
        this.placeHolderResourcePath = placeHolderResourcePath;
    }
    
    /**
     * 
     * @return
     */
    public String getPlaceHolderResourcePath()
    {   
        return placeHolderResourcePath;
    }

    /**
     * This method sets the mime-aware placeholder resource path template.
     * 
     * @param mimeAwarePlaceHolderResourcePath
     * @since 3.4.1 (Team)
     */
    public void setMimeAwarePlaceHolderResourcePath(String mimeAwarePlaceHolderResourcePath)
    {
        this.mimeAwarePlaceHolderResourcePath = mimeAwarePlaceHolderResourcePath;
    }
    
    /**
     * 
     * @return
     * @since 3.4.1 (Team)
     */
    public String getMimeAwarePlaceHolderResourcePath()
    {   
        return mimeAwarePlaceHolderResourcePath;
    }
}
