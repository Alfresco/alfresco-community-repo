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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

/**
 * Provides basic services for {@link org.alfresco.repo.content.transform.ContentTransformer}
 * implementations.
 * <p>
 * This class maintains the performance measures for the transformers as well, making sure that
 * there is an extra penalty for transformers that fail regularly.
 * 
 * @author Derek Hulley
 * @author Roy Wetherall
 */
public abstract class AbstractContentTransformer2 extends ContentTransformerHelper implements ContentTransformer
{
    private static final Log logger = LogFactory.getLog(AbstractContentTransformer2.class);
    
    private ContentTransformerRegistry registry;
    private double averageTime = 0.0;
    private long count = 0L;
    
    private TikaConfig tikaConfig;
    
    /**
     * All transformers start with an average transformation time of 0.0ms.
     */
    protected AbstractContentTransformer2()
    {
        averageTime = 0.0;
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
            logger.warn("Property 'registry' has not been set.  Ignoring auto-registration: \n" +
                    "   transformer: " + this);
            return;
        }

        // register this instance for the fallback case
        registry.addTransformer(this);
    }
    
    /**
     * Convenience method to check the transformability of a transformation
     * 
     * @param reader    content reader
     * @param writer    content writer
     * @param options   transformation options
     * @throws AlfrescoRuntimeException if the the transformation isn't supported
     */
    protected void checkTransformable(ContentReader reader, ContentWriter writer, TransformationOptions options)
    {
        String sourceMimetype = getMimetype(reader);
        String targetMimetype = getMimetype(writer);
        boolean transformable = isTransformable(sourceMimetype, targetMimetype, options);
        if (transformable == false)
        {
            throw new AlfrescoRuntimeException("Unsuported transformation attempted: \n" +
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
            TransformationOptions options) throws Exception;
    
    /**
     * @see #transform(ContentReader, ContentWriter, Map)
     * @see #transformInternal(ContentReader, ContentWriter, Map)
     */
    public final void transform(ContentReader reader, ContentWriter writer) throws ContentIOException
    {
        transform(reader, writer, new TransformationOptions());
    }
    
    /**
     * @see org.alfresco.repo.content.transform.ContentTransformer#transform(org.alfresco.service.cmr.repository.ContentReader, org.alfresco.service.cmr.repository.ContentWriter, org.alfresco.service.cmr.repository.TransformationOptions)
     */
    public final void transform(ContentReader reader, ContentWriter writer, TransformationOptions options)
        throws ContentIOException
    {
        // begin timing
        long before = System.currentTimeMillis();
        
        // check options map
        if (options == null)
        {
            options = new TransformationOptions();
        }
        
        try
        {
            // Check the transformability
            checkTransformable(reader, writer, options);

            // Transform
            transformInternal(reader, writer, options);
        }
        catch (Throwable e)
        {
            // Make sure that this transformation gets set back i.t.o. time taken.
            // This will ensure that transformers that compete for the same transformation
            // will be prejudiced against transformers that tend to fail
            recordTime(60 * 1000);   // 1 minute, i.e. rubbish
            
            // Ask Tika to detect the document, and report back on if
            //  the current mime type is plausible
            String differentType = checkMimeTypeMatches(reader.getReader());
    
            // Report the error
            if(differentType == null)
            {
               throw new ContentIOException("Content conversion failed: \n" +
                       "   reader: " + reader + "\n" +
                       "   writer: " + writer + "\n" +
                       "   options: " + options,
                       e);
            }
            else
            {
               throw new ContentIOException("Content conversion failed: \n" +
                     "   reader: " + reader + "\n" +
                     "   writer: " + writer + "\n" +
                     "   options: " + options + "\n" +
                     "   claimed mime type: " + reader.getMimetype() + "\n" +
                     "   detected mime type: " + differentType,
                     e);
            }
        }
        finally
        {
            // check that the reader and writer are both closed
            if (reader.isChannelOpen())
            {
                logger.error("Content reader not closed by transformer: \n" +
                        "   reader: " + reader + "\n" +
                        "   transformer: " + this);
            }
            if (writer.isChannelOpen())
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

    public final void transform(
            ContentReader reader,
            ContentWriter writer,
            Map<String, Object> options) throws ContentIOException
    {
        this.transform(reader, writer, new TransformationOptions(options));
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
    
    /**
     * Use Apache Tika to check if the mime type of the document really matches
     *  what it claims to be.
     * This is typically used when a transformation fails, and you want to know
     *  if someone has renamed a file and consequently it has the wrong mime type. 
     * @return Null if the mime type seems ok, otherwise the mime type it probably is
     */
    protected String checkMimeTypeMatches(ContentReader reader)
    {
       if(tikaConfig == null)
       {
          try {
             tikaConfig = TikaConfig.getDefaultConfig();
          } catch(Exception e) {
             logger.warn("Error creating Tika detector", e);
             return null;
          }
       }
       
       Metadata metadata = new Metadata();
       MediaType type;
       try {
          type = tikaConfig.getMimeRepository().detect(
                reader.getContentInputStream(), metadata
          );
          logger.debug(reader + " detected by Tika as being " + type.toString());
       } catch(Exception e) {
          logger.warn("Error identifying content type of problem document", e);
          return null;
       }
       
       // Is it a good match?
       if(type.toString().equals(reader.getMimetype())) 
       {
          return null;
       }
       
       // Is it close?
       MediaType claimed = MediaType.parse(reader.getMimetype());
       if(tikaConfig.getMediaTypeRegistry().isSpecializationOf(claimed, type) ||
          tikaConfig.getMediaTypeRegistry().isSpecializationOf(type, claimed))
       {
          // Probably close enough
          return null;
       }
       
       // If we get here, then most likely the type is wrong
       return type.toString();
    }
}
