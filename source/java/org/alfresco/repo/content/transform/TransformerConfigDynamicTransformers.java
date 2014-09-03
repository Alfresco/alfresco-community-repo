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

import static org.alfresco.repo.content.transform.TransformerConfig.AMP;
import static org.alfresco.repo.content.transform.TransformerConfig.AVAILABLE;
import static org.alfresco.repo.content.transform.TransformerConfig.EDITION;
import static org.alfresco.repo.content.transform.TransformerConfig.FAILOVER;
import static org.alfresco.repo.content.transform.TransformerConfig.PIPE;
import static org.alfresco.repo.content.transform.TransformerConfig.PIPELINE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.descriptor.DescriptorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Adds dynamic transformers defined in alfresco global properties to the ContentTransformerRegistry.
 * 
 * @author Alan Davis
 */
public class TransformerConfigDynamicTransformers extends TransformerPropertyNameExtractor
{
    private static final Log logger = LogFactory.getLog(TransformerConfigDynamicTransformers.class);
    int errorCount = 0;
    private final List<ContentTransformer> dynamicTransformers = new ArrayList<ContentTransformer>();

    public TransformerConfigDynamicTransformers(TransformerConfig transformerConfig, TransformerProperties transformerProperties,
            MimetypeService mimetypeService, ContentService contentService, ContentTransformerRegistry transformerRegistry,
            TransformerDebug transformerDebug, ModuleService moduleService, DescriptorService descriptorService)
    {
        createDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, contentService,
                transformerRegistry, transformerDebug, moduleService, descriptorService);
    }

    private void createDynamicTransformers(TransformerConfig transformerConfig, TransformerProperties transformerProperties,
            MimetypeService mimetypeService, ContentService contentService, ContentTransformerRegistry transformerRegistry,
            TransformerDebug transformerDebug, ModuleService moduleService, DescriptorService descriptorService)
    {
        Collection<String> SUFFIXES = Arrays.asList(new String [] {
                FAILOVER,
                PIPELINE,
                AVAILABLE,
                EDITION,
                AMP
        });

        Map<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue>
        transformerSourceTargetSuffixValues =
                getTransformerSourceTargetValuesMap(SUFFIXES, true, true, false, transformerProperties, mimetypeService);
        Collection<TransformerSourceTargetSuffixValue> properties =
                transformerSourceTargetSuffixValues.values();

        // Repeat until we cannot create any more transformers or all have been created
        Collection<TransformerSourceTargetSuffixValue> processed =
                new ArrayList<TransformerSourceTargetSuffixValue>();
        do
        {
            processed.clear();
            for (TransformerSourceTargetSuffixValue property: properties)
            {
                if (property.suffix.equals(PIPELINE) || property.suffix.equals(FAILOVER))
                {
                    try
                    {
                        String edition = getProperty(property.transformerName, null, null, EDITION,
                                null, transformerSourceTargetSuffixValues);
                        String moduleId = getProperty(property.transformerName, null, null, AMP,
                                null, transformerSourceTargetSuffixValues);
                        if (!supportedEdition(descriptorService, edition))
                        {
                            processed.add(property);
                            logger.debug(property.transformerName+" ignored. As it is an "+edition+" only transformer.");
                        }
                        else if (!supportedModule(moduleService, moduleId))
                        {
                            processed.add(property);
                            logger.debug(property.transformerName+" ignored. As the AMP "+moduleId+" is not installed.");
                        }
                        else
                        {
                            String availableStr = getProperty(property.transformerName, null, null, AVAILABLE,
                                    null, transformerSourceTargetSuffixValues);
                            boolean available = availableStr == null || "true".equalsIgnoreCase(availableStr);
                            
                            AbstractContentTransformer2 transformer = property.suffix.equals(PIPELINE)
                                    ? createComplexTransformer(property, transformerConfig, mimetypeService,
                                            contentService, transformerRegistry, transformerDebug, available)
                                    : createFailoverTransformer(property, transformerConfig, mimetypeService,
                                            contentService, transformerRegistry, transformerDebug, available);
                            transformer.register();
                            processed.add(property);
                            dynamicTransformers.add(transformer);
                            logger.debug(property.transformerName+" added");
                        }
                    }
                    catch (IllegalArgumentException e)
                    {
                        // Thrown if unknown sub transformer name - it might be dynamic
                    }
                    catch (AlfrescoRuntimeException e)
                    {
                        // Thrown if the mimetype is invalid or the transformer already exists
                        processed.add(property);
                        error(e.getMessage());
                    }
                }
            }
        } while (properties.removeAll(processed) && properties.size() > 0);
        
        for (TransformerSourceTargetSuffixValue property: properties)
        {
            if (property.suffix.equals(PIPELINE) || property.suffix.equals(FAILOVER))
            {
                error("Cannot create dynamic transformer "+property.transformerName+
                        " as sub transformers could not be found or created (\""+
                        property.value+"\").");
            }
        }
    }
    
    private boolean supportedEdition(DescriptorService descriptorService, String edition)
    {
        return descriptorService == null || edition == null ||
                descriptorService.getServerDescriptor().getEdition().equals(edition);
    }

    private boolean supportedModule(ModuleService moduleService, String moduleId)
    {
        return moduleService == null || moduleId == null ||
                moduleService.getModule(moduleId) != null;
    }

    private AbstractContentTransformer2 createComplexTransformer(TransformerSourceTargetSuffixValue property,
            TransformerConfig transformerConfig,
            MimetypeService mimetypeService, ContentService contentService,
            ContentTransformerRegistry transformerRegistry, TransformerDebug transformerDebug,
            boolean available)
    {
        List<ContentTransformer> transformers = new ArrayList<ContentTransformer>();
        List<String> intermediateMimetypes = new ArrayList<String>();

        extractTransformersAndMimetypes(property, transformers, intermediateMimetypes,
                mimetypeService, transformerRegistry);       
        
        ComplexContentTransformer transformer = new ComplexContentTransformer()
        {
            @Override
            public String getComments(boolean available)
            {
                return getCommentNameAndAvailable(true); // suppress the ...available=false line as it is reported anyway if set
            }
        };
        setupContentTransformer2(property, transformerConfig, mimetypeService, contentService,
                transformerRegistry, transformerDebug, available, transformer, transformers);
        
        // baseComplexContentTransformer
        transformer.setContentService(contentService);
        
        // ComplexContentTransformer
        transformer.setTransformers(transformers);
        transformer.setIntermediateMimetypes(intermediateMimetypes);
        
        return transformer;
    }

    private AbstractContentTransformer2 createFailoverTransformer(TransformerSourceTargetSuffixValue property,
            TransformerConfig transformerConfig,
            MimetypeService mimetypeService, ContentService contentService,
            ContentTransformerRegistry transformerRegistry, TransformerDebug transformerDebug,
            boolean available)
    {
        List<ContentTransformer> transformers = new ArrayList<ContentTransformer>();

        extractTransformersAndMimetypes(property, transformers, null,
                mimetypeService, transformerRegistry);       
        
        FailoverContentTransformer transformer = new FailoverContentTransformer()
        {
            @Override
            public String getComments(boolean available)
            {
                return getCommentNameAndAvailable(true); // suppress the ...available=false line as it is reported anyway if set
            }
        };
        setupContentTransformer2(property, transformerConfig, mimetypeService, contentService,
                transformerRegistry, transformerDebug, available, transformer, transformers);
        
        // FailoverContentTransformer
        transformer.setTransformers(transformers);
        
        return transformer;
    }

    /**
     * Populates transformers and intermediateMimetypes (optional) from the supplied property value.
     * @throws AlfrescoRuntimeException if the value is invalid
     * @throws IllegalArgumentException if sub-transformer does not exist
     */
    private void extractTransformersAndMimetypes(TransformerSourceTargetSuffixValue property,
            List<ContentTransformer> transformers, List<String> intermediateMimetypes,
            MimetypeService mimetypeService, ContentTransformerRegistry transformerRegistry)
    {
        String[] subTransformersAndMimetypes = property.value.split("\\"+PIPE);

        
        boolean hasMimetypes = intermediateMimetypes != null;
        if ((!hasMimetypes && subTransformersAndMimetypes.length < 2) ||
            (hasMimetypes && (subTransformersAndMimetypes.length < 3 || subTransformersAndMimetypes.length%2 == 0)))
        {
            throw new AlfrescoRuntimeException("Cannot create dynamic transformer "+
                    property.transformerName+" as the value "+property.value+" has the wrong number of components.");
        }
        
        boolean isTransformerName = true;
        for (String name: subTransformersAndMimetypes)
        {
            if (isTransformerName)
            {
                try
                {
                    ContentTransformer subTransformer = TransformerConfig.ANY.equals(name)
                            ? null
                              // throws IllegalArgumentException if sub-transformer does not exist
                            : transformerRegistry.getTransformer(TransformerConfig.TRANSFORMER+name);
                        transformers.add(subTransformer);
                }
                catch (IllegalArgumentException e)
                {
                    logger.trace(property.transformerName+" did not find "+TransformerConfig.TRANSFORMER+name);
                    throw e;
                }
            }
            else
            {
                String mimetype = mimetypeService.getMimetype(name);
                if (!MimetypeMap.EXTENSION_BINARY.equals(name) && MimetypeMap.MIMETYPE_BINARY.equals(mimetype))
                {
                    throw new AlfrescoRuntimeException("Cannot create dynamic transformer "+
                            property.transformerName+" as the extension "+name+" is unregistered.");
                }
                intermediateMimetypes.add(mimetype);
            }
            if (hasMimetypes)
            {
                isTransformerName = !isTransformerName;
            }
        }
    }

    // Set properties common to ComplexContentTransformer and FailoverContentTransformer.
    private void setupContentTransformer2(TransformerSourceTargetSuffixValue property,
            TransformerConfig transformerConfig, MimetypeService mimetypeService,
            ContentService contentService, ContentTransformerRegistry transformerRegistry,
            TransformerDebug transformerDebug, boolean available,
            AbstractContentTransformer2 transformer, List<ContentTransformer> transformers)
    {
        try
        {
            // Throws an exception if it does not exist
            transformerRegistry.getTransformer(property.transformerName);
            throw new AlfrescoRuntimeException("Cannot create dynamic transformer "+
                    property.transformerName+" as a transformer with that name already exists.");
        }
        catch (IllegalArgumentException e)
        {
            // good news it does not exist
        }
        
        // unregisteredBaseContentTransformer
        transformer.setMimetypeService(mimetypeService);
        transformer.setTransformerDebug(transformerDebug);
        transformer.setTransformerConfig(transformerConfig);
        transformer.setRegistry(transformerRegistry);

        // baseContentTransformer
        transformer.setRegisterTransformer(true);
        
        // AbstractContentTransformer2
        transformer.setBeanName(property.transformerName);
        transformer.setRegisterTransformer(available);
    }

    private void error(String msg)
    {
        errorCount++;
        logger.error(msg);
    }
    
    int getErrorCount()
    {
        return errorCount;
    }

    public void removeTransformers(ContentTransformerRegistry transformerRegistry)
    {
        for (ContentTransformer transformer: dynamicTransformers)
        {
            transformerRegistry.removeTransformer(transformer);
        }
    }
}
