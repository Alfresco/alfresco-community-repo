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
package org.alfresco.repo.content.transform;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.el.MethodNotFoundException;

import org.alfresco.api.AlfrescoPublicApi;     
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Transformer that passes a document through several nested transformations
 * in order to accomplish its goal.
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public class ComplexContentTransformer extends AbstractContentTransformer2 implements InitializingBean
{
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(ComplexContentTransformer.class);
   
    /**
     *  Complex transformers contain lower level transformers. In order to find dynamic
     * (defined as null) child transformers to use, they recursively check available
     * transformers. It makes no sense to have a transformer that is its own child.
     */
    static final ThreadLocal<Deque<ContentTransformer>> parentTransformers = new ThreadLocal<Deque<ContentTransformer>>() {
        @Override
        protected Deque<ContentTransformer> initialValue() {
                return new ArrayDeque<ContentTransformer>();
        }
    };
    
    private List<ContentTransformer> transformers;
    private List<String> intermediateMimetypes;
    private Map<String,Serializable> transformationOptionOverrides;
    private ContentService contentService;

    public ComplexContentTransformer()
    {
    }

    /**
     * The list of transformers to use. If any element is null
     * all possible transformers will be considered. If any element
     * is null, the contentService property must be set.
     * <p>
     * If a single transformer is supplied, then it will still be used.
     * 
     * @param transformers list of <b>at least one</b> transformer
     * 
     * @see #setContentService(ContentService)
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
     * Sets the ContentService. Only required if {@code null} transformers
     * are provided to {@link #setTransformers(List).
     * @param contentService
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
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
        for (ContentTransformer transformer: transformers)
        {
            if (transformer == null)
            {
                if (contentService == null)
                {
                    throw new AlfrescoRuntimeException("'contentService' is a required property if " +
                                "there are any null (dynamic) transformers");
                }
                break;
            }
        }
    }
    
    @Override
    public boolean isTransformable(String sourceMimetype, long sourceSize, String targetMimetype,
            TransformationOptions options)
    {
        if (!isSupportedTransformation(sourceMimetype, targetMimetype, options))
        {
            return false;
        }

        // Don't allow transformer to be its own child.
        if (parentTransformers.get().contains(this))
        {
            return false;
        }

        overrideTransformationOptions(options);
        
        // Can use super isTransformableSize as it indirectly calls getLimits in this class
        // which combines the limits from the first transformer. Other transformer in the chain
        // are no checked as sizes are unknown.
        return
            isTransformableMimetype(sourceMimetype, targetMimetype, options) &&
            isTransformableSize(sourceMimetype, sourceSize, targetMimetype, options);
    }

    /**
     * Sets any transformation option overrides it can.
     */
    private void overrideTransformationOptions(TransformationOptions options)
    {
        // Set any transformation options overrides if we can
        if(options != null && transformationOptionOverrides != null)
        {
           for(String key : transformationOptionOverrides.keySet())
           {
              if(PropertyUtils.isWriteable(options, key))
              {
                 try 
                 {
                    PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(options, key);
                    Class<?> propertyClass = pd.getPropertyType();
                    
                    Object value = transformationOptionOverrides.get(key);
                    if(value != null)
                    {
                        if(propertyClass.isInstance(value))
                        {
                            // Nothing to do
                        }
                        else if(value instanceof String && propertyClass.isInstance(Boolean.TRUE))
                        {
                            // Use relaxed converter
                            value = TransformationOptions.relaxedBooleanTypeConverter.convert((String)value);
                        }
                        else
                        {
                            value = DefaultTypeConverter.INSTANCE.convert(propertyClass, value);
                        }
                    }
                    PropertyUtils.setProperty(options, key, value);
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
    }
    
    @Override
    public boolean isTransformableMimetype(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        boolean result = true;
        String currentSourceMimetype = sourceMimetype;
        Iterator<ContentTransformer> transformerIterator = transformers.iterator();

        // When using a wild card (null) intermediate transformer, don't
        // say we support a transformation that one of the none null intermediate
        // transformers can do on its own, to avoid double transformations.
        // Not done when there are no wild card transformers, as there are cases
        // where it makes sense to go via an intermediate format (quality/speed).
        while (transformerIterator.hasNext())
        {
            ContentTransformer transformer = transformerIterator.next();
            if (transformer == null)
            {
                transformerIterator = transformers.iterator();
                while (transformerIterator.hasNext())
                {
                    transformer = transformerIterator.next();
                    if (transformer != null)
                    {
                        if (transformer.isTransformable(sourceMimetype, -1, targetMimetype, options))
                        {
                            return false;
                        }
                    }
                }
                break;
            }
        }
        
        transformerIterator = transformers.iterator();
        Iterator<String> intermediateMimetypeIterator = intermediateMimetypes.iterator();
        while (transformerIterator.hasNext())
        {
            ContentTransformer transformer = transformerIterator.next();
            
            // determine the target mimetype. This is the final target if we are on the last transformation
            String currentTargetMimetype = transformerIterator.hasNext() ? intermediateMimetypeIterator.next() : targetMimetype;
            if (transformer == null)
            {
                try
                {
                    parentTransformers.get().push(this);
                    @SuppressWarnings("deprecation")
                    List<ContentTransformer> firstTansformers = contentService.getActiveTransformers(
                            currentSourceMimetype, -1, currentTargetMimetype, options);
                    if (firstTansformers.isEmpty())
                    {
                        result = false;
                        break;
                    }
                }
                finally
                {
                    parentTransformers.get().pop();
                }
            }
            else
            {
                if (transformer.isTransformable(currentSourceMimetype, -1, currentTargetMimetype, options) == false)
                {
                    result = false;
                    break;
                }
            }
            
            // move on
            currentSourceMimetype = currentTargetMimetype;
        }
        
        return result;
    }

    /**
     * Indicates if 'page' limits are supported by the first transformer in the chain.
     * If the first transformer is dynamic, all possible first transformers must support it.
     * @return true if the first transformer supports them.
     */
    @Override
    protected boolean isPageLimitSupported(String sourceMimetype, String targetMimetype,
            TransformationOptions options)
    {
        boolean pageLimitSupported;
        ContentTransformer firstTransformer = transformers.get(0);
        String firstTargetMimetype = intermediateMimetypes.get(0);
        if (firstTransformer == null)
        {
                try
                {
                    parentTransformers.get().push(this);
                    @SuppressWarnings("deprecation")
                    List<ContentTransformer> firstTansformers = contentService.getActiveTransformers(
                            sourceMimetype, -1, firstTargetMimetype, options);
                    pageLimitSupported = !firstTansformers.isEmpty();
                    if (pageLimitSupported)
                    {
                        for (ContentTransformer transformer: firstTansformers)
                        {
                            if (!isPageLimitSupported(transformer, sourceMimetype, targetMimetype, options))
                            {
                                pageLimitSupported = false;
                                break;
                            }
                        }
                    }
                }
                finally
                {
                    parentTransformers.get().pop();
                }
        }
        else
        {
            pageLimitSupported = isPageLimitSupported(firstTransformer, sourceMimetype, targetMimetype, options);
        }
        return pageLimitSupported;
    }

    private boolean isPageLimitSupported(ContentTransformer transformer, String sourceMimetype,
            String targetMimetype, TransformationOptions options)
    {
        return (transformer instanceof AbstractContentTransformerLimits)
            ? ((AbstractContentTransformerLimits)transformer).isPageLimitSupported(sourceMimetype, targetMimetype, options)
            : false;
    }
    
    /**
     * Returns the limits from this transformer combined with those of the first transformer in the chain.
     * If the first transformer is dynamic, the lowest common denominator between all possible first transformers
     * are combined.
     */
    protected TransformationOptionLimits getLimits(String sourceMimetype, String targetMimetype,
            TransformationOptions options)
    {
        TransformationOptionLimits firstTransformerLimits = null;
        TransformationOptionLimits limits = super.getLimits(sourceMimetype, targetMimetype, options);
        ContentTransformer firstTransformer = transformers.get(0);
        String firstTargetMimetype = intermediateMimetypes.get(0);
        if (firstTransformer == null)
        {
            try
            {
                parentTransformers.get().push(this);
                @SuppressWarnings("deprecation")
                List<ContentTransformer> firstTansformers = contentService.getActiveTransformers(
                        sourceMimetype, -1, firstTargetMimetype, options);
                if (!firstTansformers.isEmpty())
                {
                    for (ContentTransformer transformer: firstTansformers)
                    {
                        if (transformer instanceof AbstractContentTransformerLimits)
                        {
                            TransformationOptionLimits transformerLimits = ((AbstractContentTransformerLimits)transformer).
                                    getLimits(sourceMimetype, firstTargetMimetype, options);
                            firstTransformerLimits = (firstTransformerLimits == null)
                                ? transformerLimits
                                : firstTransformerLimits.combineUpper(transformerLimits);
                        }
                    }
                }
            }
            finally
            {
                parentTransformers.get().pop();
            }
        }
        else
        {
            if (firstTransformer instanceof AbstractContentTransformerLimits)
            {
                firstTransformerLimits = ((AbstractContentTransformerLimits)firstTransformer).
                        getLimits(sourceMimetype, firstTargetMimetype, options);
            }
        }

        if (firstTransformerLimits != null)
        {
            limits = limits.combine(firstTransformerLimits);
        }
        
        return limits;
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
        NodeRef origSourceNodeRef = options.getSourceNodeRef();
        try
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
                if (transformer == null)
                {
                    try
                    {
                        parentTransformers.get().push(this);
                        contentService.transform(currentReader, currentWriter, options);
                    }
                    finally
                    {
                        parentTransformers.get().pop();
                    }
                }
                else
                {
                    transformer.transform(currentReader, currentWriter, options);
                }

                // Must clear the sourceNodeRef after the first transformation to avoid later 
                // transformers thinking the intermediate file is the original node. However as
                // we put the original sourceNodeRef back at the end of this try block (so that we are
                // not changing any data), we must setting the value to null just after the
                // transformation. Really only needs to be done after the first transformation
                // but doing it every time is simpler and faster.
                options.setSourceNodeRef(null);
                
                // move the source on
                currentReader = currentWriter.getReader();
            }
            // done
        }
        finally
        {
            options.setSourceNodeRef(origSourceNodeRef);
        }
    }  
    
    public List<String> getIntermediateMimetypes()
    {
       return Collections.unmodifiableList(intermediateMimetypes);
    }
    
    public List<ContentTransformer> getIntermediateTransformers()
    {
       return Collections.unmodifiableList(transformers);
    }
    
    /**
     * Returns the transformer properties predefined (hard coded or implied) by this transformer.
     */
    @Override
    public String getComments(boolean available)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getComments(available));
        sb.append("# ");
        sb.append(TransformerConfig.CONTENT);
        sb.append(getName());
        sb.append(TransformerConfig.PIPELINE);
        sb.append('=');
        Iterator<String> iterator = intermediateMimetypes.iterator();
        for (ContentTransformer transformer: transformers)
        {
            sb.append(transformer != null ? getSimpleName(transformer) : TransformerConfig.ANY);
            if (iterator.hasNext())
            {
                sb.append(TransformerConfig.PIPE);
                String mimetype = iterator.next();
                if (mimetype != null && mimetype.length() != 0)
                {
                    String extension = getMimetypeService().getExtension(mimetype);
                    sb.append(extension);
                }
                sb.append(TransformerConfig.PIPE);
            }
        }
        sb.append('\n');
        return sb.toString();
    }
}
