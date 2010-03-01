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
package org.alfresco.repo.content.transform;

import java.util.Map;

import org.alfresco.repo.content.ContentWorker;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Interface for class that allow content transformation from one mimetype to another.
 * 
 * @author Derek Hulley
 */
public interface ContentTransformer extends ContentWorker
{
    /**
     * Indicates whether the provided source mimetype can be transformed into the target mimetype with 
     * the options specified by this content transformer.
     * 
     * @param  sourceMimetype           the source mimetype
     * @param  destinationMimetype      the destination mimetype
     * @param  options                  the transformation options
     * @return boolean                  true if this content transformer can satify the mimetypes and options specified, false otherwise
     */
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options);
    
    /**
     * Indicates whether given the provided transformation parmaters this transformer can prvide an explict
     * transformation.
     * 
     * An explict transformation indicates that the transformation happens directly and not as a result of 
     * another transformation process.  Explict transformation always take presidence over normal transformations.
     * 
     * @param sourceMimetype    the source mimetype
     * @param targetMimetype    the target mimetype
     * @param options           the transformation options
     * @return boolean          true if it is an explicit transformation, flase otherwise         
     */
    public boolean isExplicitTransformation(String sourceMimetype, String targetMimetype, TransformationOptions options);
    
    /**
     * Provides an estimate, usually a worst case guess, of how long a transformation
     * will take.
     * <p>
     * This method is used to determine, up front, which of a set of
     * equally reliant transformers will be used for a specific transformation.
     * 
     * @return Returns the approximate number of milliseconds per transformation
     */
    public long getTransformationTime();
    
    /**
     * @see #transform(ContentReader, ContentWriter, TransformationOptions)
     */
    public void transform(ContentReader reader, ContentWriter writer) throws ContentIOException;
    
    /**
     * Transforms the content provided by the reader and source mimetype
     * to the writer and target mimetype.
     * <p>
     * The transformation viability can be determined by an up front call
     * to {@link #getReliability(String, String)}.
     * <p>
     * The source and target mimetypes <b>must</b> be available on the
     * {@link org.alfresco.service.cmr.repository.ContentAccessor#getMimetype()} methods of
     * both the reader and the writer.
     * <p>
     * Both reader and writer will be closed after the transformation completes.
     * 
     * @param reader the source of the content
     * @param writer the destination of the transformed content
     * @param options options to pass to the transformer.  These are transformer dependent
     *      and may be null.
     * @throws ContentIOException if an IO exception occurs
     * 
     * @deprecated 
     * Deprecated since 3.0.  Options should now be provided as a TransformationOptions object.
     */
    @Deprecated
    public void transform(
            ContentReader reader,
            ContentWriter writer,
            Map<String, Object> options) throws ContentIOException;
    
    /**
     * Transforms the content provided by the reader and source mimetype
     * to the writer and target mimetype with the provided transformation options.
     * <p>
     * The transformation viability can be determined by an up front call
     * to {@link #isTransformable(String, String, TransformationOptions)}.
     * <p>
     * The source and target mimetypes <b>must</b> be available on the
     * {@link org.alfresco.service.cmr.repository.ContentAccessor#getMimetype()} methods of
     * both the reader and the writer.
     * <p>
     * Both reader and writer will be closed after the transformation completes.
     * <p>
     * The provided options can be null.
     * 
     * @param  reader               the source of the content
     * @param  contentWriter        the destination of the transformed content    
     * @param  options              transformation options, these can be null
     * @throws ContentIOException   if an IO exception occurs
     */
    public void transform(ContentReader reader, ContentWriter contentWriter, TransformationOptions options) 
        throws ContentIOException;
}
