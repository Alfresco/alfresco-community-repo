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

import static org.alfresco.repo.content.transform.TransformerConfig.ANY;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentAccessor;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;

/**
 * A class providing basic functionality shared by both {@link ContentTransformer}s and {@link ContentTransformerWorker}s.
 * 
 * @author dward
 */
public class ContentTransformerHelper implements BeanNameAware
{
    private static final Log logger = LogFactory.getLog(ContentTransformerHelper.class);

    private MimetypeService mimetypeService;
    protected TransformerConfig transformerConfig;
    
    private List<String> deprecatedSetterMessages;
    private static boolean firstDeprecatedSetter = true;

    /** The bean name. */
    private String beanName;

    /**
     * Helper setter of the mimetype service. This is not always required.
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
     * @deprecated supported transformations are now set with global properties rather than spring configuration.
     */
    public void setExplicitTransformations(List<ExplictTransformationDetails> explicitTransformations)
    {
        deprecatedSupportedTransformations(explicitTransformations, null);
        // TODO Should suggest properties that indicate lower priority transformers should be unsupported.
        //      This is for completeness rather than needed as the priority will avoid the non explicit
        //      transformers from being used. Explicit transformers are given a priority of 5 rather than 10.
    }

    /**
     * @deprecated supported transformations are now set with global properties rather than spring configuration.
     */
    public void setSupportedTransformations(List<SupportedTransformation> supportedTransformations)
    {
        deprecatedSupportedTransformations(supportedTransformations, "true");
    }

    /**
     * @deprecated supported transformations are now set with global properties rather than spring configuration.
     */
    public void setUnsupportedTransformations(List<SupportedTransformation> unsupportedTransformations)
    {
        deprecatedSupportedTransformations(unsupportedTransformations, "false");
    }
    
    public void setTransformerConfig(TransformerConfig transformerConfig)
    {
        this.transformerConfig = transformerConfig;
    }

    /**
     * Convenience to fetch and check the mimetype for the given content
     * 
     * @param content
     *            the reader/writer for the content
     * @return Returns the mimetype for the content
     * @throws AlfrescoRuntimeException
     *             if the content doesn't have a mimetype
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
     * @deprecated Should now use priority and unsupported transformer properties.
     * 
     * @see org.alfresco.repo.content.transform.ContentTransformer#isExplicitTransformation(java.lang.String,
     *      java.lang.String, org.alfresco.service.cmr.repository.TransformationOptions)
     */
    public boolean isExplicitTransformation(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        return transformerConfig.getPriority(((ContentTransformer)this), sourceMimetype, targetMimetype) == TransformerConfig.PRIORITY_EXPLICIT;
    }

    public boolean isSupportedTransformation(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        return transformerConfig.isSupportedTransformation(((ContentTransformer)this), sourceMimetype, targetMimetype, options);
    }

    /**
     * Sets the Spring bean name.
     */
    @Override
    public void setBeanName(String beanName)
    {
        this.beanName = beanName;
    }

    /**
     * THIS IS A CUSTOM SPRING INIT METHOD 
     */
    public void register()
    {
        logDeprecatedSetter();
    }
    
    /**
     * Returns the Spring bean name.
     */
    public String getBeanName()
    {
        return beanName;
    }
    
    /**
     * Returns transformer name. Uses the Spring bean name, but if null uses the class name.
     */
    public String getName()
    {
        return (beanName == null) ? getClass().getSimpleName() : beanName;
    }

    /**
     * Called by deprecated property setter methods that should no longer be called
     * by Spring configuration as the values are now set using global properties.
     * @param suffixAndValue that should have been used. The first part of the
     *        property name "content.transformer.<name>." should not be included.
     *        The reason is that the setter methods might be called before the bean
     *        name is set.  
     */
    protected void deprecatedSetter(String suffixAndValue)
    {
        if (deprecatedSetterMessages == null)
        {
            deprecatedSetterMessages = new ArrayList<String>();
        }
        deprecatedSetterMessages.add(suffixAndValue);
    }
    
    /**
     * Called when the bean name is set after all the deprecated setters to log
     * INFO messages with the Alfresco global properties that should now be set
     * (if no set) to replace Spring configuration.
     */
    private void logDeprecatedSetter()
    {
        if (deprecatedSetterMessages != null)
        {
            for (String suffixAndValue: deprecatedSetterMessages)
            {
                String propertyNameAndValue = TransformerConfig.CONTENT+beanName+'.'+suffixAndValue;
                String propertyName = propertyNameAndValue.replaceAll("=.*", "");
                if (transformerConfig.getProperty(propertyName) == null)
                {
                    if (firstDeprecatedSetter)
                    {
                        firstDeprecatedSetter = false;
                        logger.error("In order to support dynamic setting of transformer options, Spring XML configuration");
                        logger.error("is no longer used to initialise these options.");
                        logger.error(" ");
                        logger.error("Your system appears to contains custom Spring configuration which should be replace by");
                        logger.error("the following Alfresco global properties. In the case of the Enterprise edition these");
                        logger.error("values may then be dynamically changed via JMX.");
                        logger.error(" ");
                        // Note: Cannot set these automatically because, an MBean reset would clear them.
                    }
                    logger.error(propertyNameAndValue);
                    
                    // Add them to the subsystem's properties anyway (even though an MBean reset would clear them),
                    // so that existing unit tests work.
                    transformerConfig.setProperty(propertyNameAndValue);
                }
                else
                {
                    logger.warn(propertyNameAndValue+" is set, but spring config still exists");
                }
            }
            deprecatedSetterMessages = null;
        }
    }

    private void deprecatedSupportedTransformations(List<? extends SupportedTransformation> transformations, String value)
    {
        if (transformations != null)
        {
            for (SupportedTransformation transformation: transformations)
            {
                String sourceMimetype = transformation.getSourceMimetype();
                String targetMimetype = transformation.getTargetMimetype();
                String sourceExt = getExtensionOrAny(sourceMimetype);
                String targetExt = getExtensionOrAny(targetMimetype);
                deprecatedSetter(TransformerConfig.EXTENSIONS.substring(1)+sourceExt+'.'+targetExt+
                        (value == null // same as: transformation instanceof ExplictTransformationDetails
                        ? TransformerConfig.PRIORITY+"="+TransformerConfig.PRIORITY_EXPLICIT
                        : TransformerConfig.SUPPORTED+"="+value));
            }
        }
    }
    
    protected String getExtensionOrAny(String mimetype)
    {
        return mimetype == null || ANY.equals(mimetype) ? ANY : mimetypeService.getExtension(mimetype);
    }
    
    public String toString()
    {
        return getName();
    }
}