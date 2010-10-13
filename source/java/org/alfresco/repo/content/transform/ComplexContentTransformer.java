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

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.el.MethodNotFoundException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;

/**
 * Transformer that passes a document through several nested transformations
 * in order to accomplish its goal.
 * 
 * @author Derek Hulley
 */
public class ComplexContentTransformer extends AbstractContentTransformer2 implements InitializingBean
{
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(ComplexContentTransformer.class);
   
    private List<ContentTransformer> transformers;
    private List<String> intermediateMimetypes;
    private Map<String,Serializable> transformationOptionOverrides;
    
    public ComplexContentTransformer()
    {
    }

    /**
     * The list of transformers to use.
     * <p>
     * If a single transformer is supplied, then it will still be used.
     * 
     * @param transformers list of <b>at least one</b> transformer
     */
    public void setTransformers(List<ContentTransformer> transformers)
    {
        this.transformers = transformers;
    }

    /**
     * Set the intermediate mimetypes that the transformer must take the content
     * through.  If the transformation <b>A..B..C</b> is performed in order to
     * simulate <b>A..C</b>, then <b>B</b> is the intermediate mimetype.  There
     * must always be <b>n-1</b> intermediate mimetypes, where <b>n</b> is the
     * number of {@link #setTransformers(List) transformers} taking part in the
     * transformation.
     * 
     * @param intermediateMimetypes intermediate mimetypes to transition the content
     *      through.
     */
    public void setIntermediateMimetypes(List<String> intermediateMimetypes)
    {
        this.intermediateMimetypes = intermediateMimetypes;
    }

    /**
     * Sets any properties to be set on the TransformationOption as passed in.
     * This allows you to force certain properties to always be set on it, 
     *  to control the transformers in a different way to their default.
     * Note that only properties that are supported by the passed-in 
     *  {@link TransformationOptions} are changed, others are ignored.
     * @param transformationOptionOverrides
     */
    public void setTransformationOptionOverrides(
          Map<String, Serializable> transformationOptionOverrides) 
    {
        this.transformationOptionOverrides = transformationOptionOverrides;
    }

   /**
     * Ensures that required properties have been set
     */
    public void afterPropertiesSet() throws Exception
    {
        if (transformers == null || transformers.size() == 0)
        {
            throw new AlfrescoRuntimeException("At least one inner transformer must be supplied: " + this);
        }
        if (intermediateMimetypes == null || intermediateMimetypes.size() != transformers.size() - 1)
        {
            throw new AlfrescoRuntimeException(
                    "There must be n-1 intermediate mimetypes, where n is the number of transformers");
        }
        if (getMimetypeService() == null)
        {
            throw new AlfrescoRuntimeException("'mimetypeService' is a required property");
        }
    }
    
    /**
     * Check we can transform all the way along the chain of mimetypes
     * 
     * @see org.alfresco.repo.content.transform.ContentTransformer#isTransformable(java.lang.String, java.lang.String, org.alfresco.service.cmr.repository.TransformationOptions)
     */
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        boolean result = true;
        String currentSourceMimetype = sourceMimetype;
        
        // Set any transformation options overrides if we can
        if(options != null && transformationOptionOverrides != null)
        {
           for(String key : transformationOptionOverrides.keySet())
           {
              if(PropertyUtils.isWriteable(options, key))
              {
                 try 
                 {
                    PropertyUtils.setProperty(options, key, transformationOptionOverrides.get(key));
                 } 
                 catch(MethodNotFoundException mnfe) {}
                 catch(NoSuchMethodException nsme) {}
                 catch(InvocationTargetException ite) {}
                 catch(IllegalAccessException iae) {}
              }
              else
              {
                 logger.warn("Unable to set override Transformation Option " + key + " on " + options);
              }
           }
        }
        
        Iterator<ContentTransformer> transformerIterator = transformers.iterator();
        Iterator<String> intermediateMimetypeIterator = intermediateMimetypes.iterator();
        while (transformerIterator.hasNext())
        {
            ContentTransformer transformer = transformerIterator.next();
            // determine the target mimetype.  This is the final target if we are on the last transformation
            String currentTargetMimetype = null;
            if (!transformerIterator.hasNext())
            {
                currentTargetMimetype = targetMimetype;
            }
            else
            {
                // use an intermediate transformation mimetype
                currentTargetMimetype = intermediateMimetypeIterator.next();
            }
            
            // check we can tranform the current stage
            if (transformer.isTransformable(currentSourceMimetype, currentTargetMimetype, options) == false)
            {
                result = false;
                break;
            }
            
            // move on
            currentSourceMimetype = currentTargetMimetype;
        }
        
        return result;
    }

    /**
     * @see org.alfresco.repo.content.transform.AbstractContentTransformer2#transformInternal(org.alfresco.service.cmr.repository.ContentReader, org.alfresco.service.cmr.repository.ContentWriter, org.alfresco.service.cmr.repository.TransformationOptions)
     */
    @Override
    public void transformInternal(
            ContentReader reader,
            ContentWriter writer,
            TransformationOptions options) throws Exception
    {
        ContentReader currentReader = reader;
        
        Iterator<ContentTransformer> transformerIterator = transformers.iterator();
        Iterator<String> intermediateMimetypeIterator = intermediateMimetypes.iterator();
        while (transformerIterator.hasNext())
        {
            ContentTransformer transformer = transformerIterator.next();
            // determine the target mimetype.  This is the final target if we are on the last transformation
            ContentWriter currentWriter = null;
            if (!transformerIterator.hasNext())
            {
                currentWriter = writer;
            }
            else
            {
                String nextMimetype = intermediateMimetypeIterator.next();
                // make a temp file writer with the correct extension
                String sourceExt = getMimetypeService().getExtension(currentReader.getMimetype());
                String targetExt = getMimetypeService().getExtension(nextMimetype);
                File tempFile = TempFileProvider.createTempFile(
                        "ComplextTransformer_intermediate_" + sourceExt + "_",
                        "." + targetExt);
                currentWriter = new FileContentWriter(tempFile);
                currentWriter.setMimetype(nextMimetype);
            }
            // transform
            transformer.transform(currentReader, currentWriter, options);
            // move the source on
            currentReader = currentWriter.getReader();
        }
        // done
    }  
    
    public List<String> getIntermediateMimetypes()
    {
       return Collections.unmodifiableList(intermediateMimetypes);
    }
}
