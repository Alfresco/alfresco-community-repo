/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.content.transform;

import java.util.Map;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;

/**
 * Interface for class that allow content transformation from one mimetype to another.
 * 
 * @author Derek Hulley
 */
public interface ContentTransformer
{
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
    public double getReliability(String sourceMimetype, String targetMimetype);
    
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
     * @see #transform(ContentReader, ContentWriter, Map)
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
     */
    public void transform(
            ContentReader reader,
            ContentWriter writer,
            Map<String, Object> options) throws ContentIOException;
}
