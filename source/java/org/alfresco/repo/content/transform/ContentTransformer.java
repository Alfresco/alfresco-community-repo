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
     * Transform option constants
     * 
     * It is up to the transformation implementation whether these 
     * options are used, but they should be considered optional and their absence 
     * should not interfere with the execution of the transformer.
     */
    //public static final String OPT_SOURCE_NODEREF = "sourceNodeRef";
    //public static final String OPT_DESTINATION_NODEREF = "destinationNodeRef";
    
    /**
     * Provides the approximate accuracy with which this transformer can
     * transform from one mimetype to another.
     * <p>
     * This method is used to determine, up front, which of a set of
     * transformers will be used to perform a specific transformation.
     * 
     * @param sourceMimetype the source mimetype
     * @param targetMimetype the target mimetype 
     * @return Returns a score 0.0 to 1.0.  0.0 indicates that the
     *      transformation cannot be performed at all.  1.0 indicates that
     *      the transformation can be performed perfectly.
     */
    //public double getReliability(String sourceMimetype, String targetMimetype);
    
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
     * 
     * @param sourceMimetype
     * @param targetMimetype
     * @param options
     * @return
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
