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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentAccessor;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides basic services for {@link org.alfresco.repo.content.transform.ContentTransformer}
 * implementations.
 * <p>
 * This class maintains the performance measures for the transformers as well, making sure that
 * there is an extra penalty for transformers that fail regularly.
 * 
 * @author Derek Hulley
 */
public abstract class AbstractContentTransformer implements ContentTransformer
{
    private static final Log logger = LogFactory.getLog(AbstractContentTransformer.class);
    
    private MimetypeService mimetypeService;
    private ContentTransformerRegistry registry;
    private List<ContentTransformerRegistry.TransformationKey> explicitTransformations;
    private double averageTime = 0.0;
    private long count = 0L;
    
    /**
     * All transformers start with an average transformation time of 0.0ms.
     */
    protected AbstractContentTransformer()
    {
        averageTime = 0.0;
        explicitTransformations = new ArrayList<ContentTransformerRegistry.TransformationKey>(0);
    }

    /**
     * The registry to auto-register with
     * 
     * @param registry the transformer registry
     */
    public void setRegistry(ContentTransformerRegistry registry)
    {
        this.registry = registry;
    }

    /**
     * Helper setter of the mimetype service.  This is not always required.
     * 
     * @param mimetypeService
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    /**
     * @return Returns the mimetype helper
     */
    protected MimetypeService getMimetypeService()
    {
        return mimetypeService;
    }

    /**
     * @return Returns the explicit transformations that were enabled for this transformer
     */
    protected List<ContentTransformerRegistry.TransformationKey> getExplicitTransformations()
    {
        return explicitTransformations;
    }

    /**
     * Set the transformations that this transformer can do regardless of what it returns
     * via the {@link ContentTransformer#getReliability(String, String) reliability check}.
     * 
     * @param explicitTransformations explicit key mappings
     */
    public void setExplicitTransformations(List<ContentTransformerRegistry.TransformationKey> explicitTransformations)
    {
        this.explicitTransformations = explicitTransformations;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
          .append("[ average=").append((long)averageTime).append("ms")
          .append("]");
        return sb.toString();
    }
    
    /**
     * Registers this instance with the {@link #setRegistry(ContentTransformerRegistry) registry}
     * if it is present.
     */
    public void register()
    {
        if (registry == null)
        {
            if (registry == null)
            {
                logger.warn("Property 'registry' has not been set.  Ignoring auto-registration: \n" +
                        "   transformer: " + this);
                return;
            }
            return;
        }
        // first register any explicit transformations
        if (explicitTransformations != null)
        {
            for (ContentTransformerRegistry.TransformationKey key : explicitTransformations)
            {
                registry.addExplicitTransformer(key, this);
            }
        }
        // register this instance for the fallback case
        registry.addTransformer(this);
    }
    
    /**
     * Convenience to fetch and check the mimetype for the given content
     * 
     * @param content the reader/writer for the content
     * @return Returns the mimetype for the content
     * @throws AlfrescoRuntimeException if the content doesn't have a mimetype
     */
    protected String getMimetype(ContentAccessor content)
    {
        String mimetype = content.getMimetype();
        if (mimetype == null)
        {
            throw new AlfrescoRuntimeException("Mimetype is mandatory for transformation: " + content);
        }
        // done
        return mimetype;
    }
    
    /**
     * Convenience method to check the reliability of a transformation
     * 
     * @param reader
     * @param writer
     * @throws AlfrescoRuntimeException if the reliability isn't > 0
     */
    protected void checkReliability(ContentReader reader, ContentWriter writer)
    {
        String sourceMimetype = getMimetype(reader);
        String targetMimetype = getMimetype(writer);
        double reliability = getReliability(sourceMimetype, targetMimetype);
        if (reliability <= 0.0)
        {
            throw new AlfrescoRuntimeException("Zero scoring transformation attempted: \n" +
                    "   reader: " + reader + "\n" +
                    "   writer: " + writer);
        }
        // it all checks out OK
    }

    /**
     * Method to be implemented by subclasses wishing to make use of the common infrastructural code
     * provided by this class.
     * 
     * @param reader the source of the content to transform
     * @param writer the target to which to write the transformed content
     * @param options a map of options to use when performing the transformation.  The map
     *      will never be null.
     * @throws Exception exceptions will be handled by this class - subclasses can throw anything
     */
    protected abstract void transformInternal(
            ContentReader reader,
            ContentWriter writer,
            Map<String, Object> options) throws Exception;
    
    /**
     * @see #transform(ContentReader, ContentWriter, Map)
     * @see #transformInternal(ContentReader, ContentWriter, Map)
     */
    public final void transform(ContentReader reader, ContentWriter writer) throws ContentIOException
    {
        transform(reader, writer, null);
    }

    /**
     * Performs the following:
     * <ul>
     *   <li>Times the transformation</li>
     *   <li>Ensures that the transformation is allowed</li>
     *   <li>Calls the subclass implementation of {@link #transformInternal(ContentReader, ContentWriter)}</li>
     *   <li>Transforms any exceptions generated</li>
     *   <li>Logs a successful transformation</li>
     * </ul>
     * Subclass need only be concerned with performing the transformation.
     * <p>
     * If the options provided are null, then an empty map will be created.
     */
    public final void transform(
            ContentReader reader,
            ContentWriter writer,
            Map<String, Object> options) throws ContentIOException
    {
        // begin timing
        long before = System.currentTimeMillis();
        
        // check the reliability
        checkReliability(reader, writer);
        
        // check options map
        if (options == null)
        {
            options = Collections.emptyMap();
        }
        
        try
        {
            transformInternal(reader, writer, options);
        }
        catch (Throwable e)
        {
            // Make sure that this transformation gets set back i.t.o. time taken.
            // This will ensure that transformers that compete for the same transformation
            // will be prejudiced against transformers that tend to fail
            recordTime(10000);   // 10 seconds, i.e. rubbish
            
            throw new ContentIOException("Content conversion failed: \n" +
                    "   reader: " + reader + "\n" +
                    "   writer: " + writer + "\n" +
                    "   options: " + options,
                    e);
        }
        finally
        {
            // check that the reader and writer are both closed
            if (!reader.isClosed())
            {
                logger.error("Content reader not closed by transformer: \n" +
                        "   reader: " + reader + "\n" +
                        "   transformer: " + this);
            }
            if (!writer.isClosed())
            {
                logger.error("Content writer not closed by transformer: \n" +
                        "   writer: " + writer + "\n" +
                        "   transformer: " + this);
            }
        }
        
        // record time
        long after = System.currentTimeMillis();
        recordTime(after - before);
        
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Completed transformation: \n" +
                    "   reader: " + reader + "\n" +
                    "   writer: " + writer + "\n" +
                    "   options: " + options + "\n" +
                    "   transformer: " + this);
        }
    }

    /**
     * @return Returns the calculated running average of the current transformations
     */
    public synchronized long getTransformationTime()
    {
        return (long) averageTime;
    }

    /**
     * Records and updates the average transformation time for this transformer.
     * <p>
     * Subclasses should call this after every transformation in order to keep
     * the running average of the transformation times up to date.
     * <p>
     * This method is thread-safe.  The time spent in this method is negligible
     * so the impact will be minor.
     * 
     * @param transformationTime the time it took to perform the transformation.
     *      The value may be 0.
     */
    protected final synchronized void recordTime(long transformationTime)
    {
        if (count == Long.MAX_VALUE)
        {
            // we have reached the max count - reduce it by half
            // the average fluctuation won't be extreme
            count /= 2L;
        }
        // adjust the average
        count++;
        double diffTime = ((double) transformationTime) - averageTime;
        averageTime += diffTime / (double) count;
    }
}
