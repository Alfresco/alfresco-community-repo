/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import java.util.List;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;     
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
@AlfrescoPublicApi
public interface ContentTransformer extends ContentWorker
{
    /**
     * @deprecated use version with extra sourceSize parameter.
     */
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options);
    
    /**
     * Indicates whether the provided source mimetype can be transformed into the target mimetype with 
     * the options specified by this content transformer.
     * 
     * @param  sourceMimetype           the source mimetype
     * @param  sourceSize               the size (bytes) of the source. If negative it is unknown.
     * @param  targetMimetype           the target mimetype
     * @param  options                  the transformation options
     * @return boolean                  true if this content transformer can satify the mimetypes and options specified, false otherwise
     */
    public boolean isTransformable(String sourceMimetype, long sourceSize, String targetMimetype, TransformationOptions options);
    
    /**
     * Sub component of {@link #isTransformable(String, long, String, TransformationOptions)
     * that checks just the mimetypes.
     * @param  sourceMimetype           the source mimetype
     * @param  targetMimetype           the target mimetype
     * @param  options                  the transformation options
     * @return boolean                  true if this content transformer can satify the mimetypes, false otherwise
     */
    public boolean isTransformableMimetype(String sourceMimetype, String targetMimetype, TransformationOptions options);

    /**
     * Sub component of {@link #isTransformable(String, long, String, TransformationOptions)
     * that checks just the size limits.
     * @param  sourceMimetype           the source mimetype
     * @param  sourceSize               the size (bytes) of the source. If negative it is unknown.
     * @param  targetMimetype           the target mimetype
     * @param  options                  the transformation options
     * @return boolean                  true if this content transformer can satify the mimetypes, false otherwise
     */
    public boolean isTransformableSize(String sourceMimetype, long sourceSize, String targetMimetype, TransformationOptions options);

    /**
     * Overridden to supply a comment or String of commented out transformation properties
     * that specify any (hard coded or implied) supported transformations. Used
     * when providing a list of properties to an administrators who may be setting
     * other transformation properties, via JMX. Consider overriding if
     * {link {@link AbstractContentTransformerLimits#isTransformableMimetype(String, String, TransformationOptions)}
     * or {@link ContentTransformerWorker#isTransformable(String, String, TransformationOptions)}
     * have been overridden.
     * See {@link #getCommentsOnlySupports(List, List, boolean)} which may be used to help construct a comment.
     * @param available indicates if the transformer has been registered and is available to be selected.
     *                  {@code false} indicates that the transformer is only available as a component of a
     *                  complex transformer.
     * @return one line per property. The simple transformer name is returned by default as a comment.
     */
    public String getComments(boolean available);

    /**
     * Returns the maximum source size (in KBytes) allowed given the supplied values.
     * @return 0 if the the transformation is disabled, -1 if there is no limit, otherwise the size in KBytes.
     */
    public long getMaxSourceSizeKBytes(String sourceMimetype, String targetMimetype, TransformationOptions options);

    /**
     * @deprecated Use transformer priority and unsupported transformer properties.
     *  
     * Indicates whether given the provided transformation parameters this transformer can provide an explicit
     * transformation.
     * 
     * An explicit transformation indicates that the transformation happens directly and not as a result of 
     * another transformation process.  Explicit transformation always take presidency over normal transformations.
     * 
     * @param sourceMimetype    the source mimetype
     * @param targetMimetype    the target mimetype
     * @param options           the transformation options
     * @return boolean          true if it is an explicit transformation, false otherwise         
     */
    public boolean isExplicitTransformation(String sourceMimetype, String targetMimetype, TransformationOptions options);
    
    /**
     * @deprecated use mimetype specific version.
     */
    public long getTransformationTime();
    
    /**
     * Provides an estimate, usually a worst case guess, of how long a transformation
     * will take. Null mimetype values provide the overall value for the transformer.
     * <p>
     * This method is used to determine, up front, which of a set of
     * equally reliant transformers will be used for a specific transformation.
     * 
     * @param sourceMimetype    the source mimetype
     * @param targetMimetype    the target mimetype
     *
     * @return Returns the approximate number of milliseconds per transformation
     */
    public long getTransformationTime(String sourceMimetype, String targetMimetype);
    
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
    
    /**
     * Returns transformer's name used in configuration.
     */
    public String getName();
}
