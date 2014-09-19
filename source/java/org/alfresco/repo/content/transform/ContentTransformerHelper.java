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
import java.util.Arrays;
import java.util.List;

import org.alfresco.api.AlfrescoPublicApi; 
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
@AlfrescoPublicApi
public class ContentTransformerHelper implements BeanNameAware
{
    private static final Log logger = LogFactory.getLog(ContentTransformerHelper.class);

    private MimetypeService mimetypeService;
    protected TransformerConfig transformerConfig;
    
    List<DeprecatedSetter> deprecatedSetterMessages;
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
        logDeprecatedSetter(deprecatedSetterMessages);
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
     * Returns the simple form of the transformer name, which has had the normal
     * "transformer." prefix to the Spring bean name removed.
     */
    public static String getSimpleName(ContentTransformer transformer)
    {
        String transformerName = transformer.getName();
        return transformerName.startsWith(TransformerConfig.TRANSFORMER)
                ? transformerName.substring(TransformerConfig.TRANSFORMER.length())
                : transformerName;
    }
    
    /**
     * Called by deprecated property setter methods that should no longer be called
     * by Spring configuration as the values are now set using global properties.
     * @param sourceMimetype so that the source extension can be worked out once the mimetypeService
     *        has been set. 
     * @param targetMimetype so that the target extension can be worked out once the mimetypeService
     *        has been set. 
     * @param suffixAndValue that should be used.
     */
    protected void deprecatedSetter(String sourceMimetype, String targetMimetype, String suffixAndValue)
    {
        if (deprecatedSetterMessages == null)
        {
            deprecatedSetterMessages = new ArrayList<DeprecatedSetter>();
        }
        deprecatedSetterMessages.add(new DeprecatedSetter(sourceMimetype, targetMimetype, suffixAndValue));
    }
    
    /**
     * Called when the bean name is set after all the deprecated setters to log
     * INFO messages with the Alfresco global properties that should now be set
     * (if not set) to replace Spring configuration.
     */
    void logDeprecatedSetter(List<DeprecatedSetter> deprecatedSetterMessages)
    {
        if (deprecatedSetterMessages != null)
        {
            StringBuilder sb = new StringBuilder();
            for (DeprecatedSetter deprecatedSetter: deprecatedSetterMessages)
            {
                String propertyNameAndValue = deprecatedSetter.getPropertyNameAndValue(beanName);
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
                    
                    sb.append(propertyNameAndValue);
                    sb.append('\n');
                }
                else
                {
                    logger.warn(propertyNameAndValue+" is set, but spring config still exists");
                }
            }
            deprecatedSetterMessages = null;
            if (sb.length() > 0)
            {
                // Add subsystem's properties anyway (even though an MBean reset would clear them),
                // so that existing unit tests work.
                transformerConfig.setProperties(sb.toString());
            }
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
                
                if (value == null)
                {
                    deprecatedSetter(sourceMimetype, targetMimetype, TransformerConfig.PRIORITY+"="+TransformerConfig.PRIORITY_EXPLICIT);
                    deprecatedSetter(sourceMimetype, targetMimetype, TransformerConfig.SUPPORTED+"=true");
                }
                else
                {
                    deprecatedSetter(sourceMimetype, targetMimetype, TransformerConfig.SUPPORTED+"="+value);
                }
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
    public String getComments(boolean available)
    {
        return getCommentNameAndAvailable(available);
    }

    /**
     * Helper method for {@link #getComments(boolean) to
     * create a line that indicates which source and target mimetypes
     * it supports.
     * @param sourceMimetypes
     * @param targetMimetypes
     * @param available TODO
     * @return a String of the form "# only supports xxx, yyy or zzz to aaa or bb\n".
     */
    protected String getCommentsOnlySupports(List<String> sourceMimetypes, List<String> targetMimetypes, boolean available)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getCommentNameAndAvailable(available));
        sb.append("# Only supports ");
        sb.append(getExtensions(sourceMimetypes));
        sb.append(" to ");
        sb.append(getExtensions(targetMimetypes));
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Returns the transformer's simple name and an indication if the transformer is not
     * available for selection.
     */
    String getCommentNameAndAvailable(boolean available)
    {
        String name = this instanceof ContentTransformer ? getSimpleName((ContentTransformer)this) : getName();
        StringBuilder sb = new StringBuilder();
        sb.append(getCommentName(name));
        if (!available)
        {
            sb.append("# ");
            sb.append(TransformerConfig.CONTENT);
            sb.append(getName());
            sb.append(TransformerConfig.AVAILABLE);
            sb.append("=false\n");
        }
        return sb.toString();
    }

    static String getCommentName(String name)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("# ");
        sb.append(name);
        sb.append('\n');
        sb.append("# ");
        for (int i = name.length(); i > 0; i--)
        {
            sb.append('-');
        }
        sb.append('\n');
        return sb.toString();
    }

    /**
     * Helper method for {@link #getComments(boolean) to
     * create a line that indicates which source and target mimetypes
     * it supports.
     * @param sourceMimetype
     * @param targetMimetype
     * @param available TODO
     * @return a String of the form "# only supports xxx to aaa\n".
     */
    protected String onlySupports(String sourceMimetype, String targetMimetype, boolean available)
    {
        return getCommentsOnlySupports(
                Arrays.asList(new String[] {sourceMimetype}),
                Arrays.asList(new String[] {targetMimetype}), available);
    }
    
    /**
     * Returns a comma separated String of mimetype file extensions. 
     */
    private String getExtensions(List<String> origMimetypes)
    {
        // Only use the mimetypes we have registered
        List<String> mimetypes = new ArrayList<String>(origMimetypes);
        mimetypes.retainAll(getMimetypeService().getMimetypes());
        
        StringBuilder sb = new StringBuilder();
        int j = mimetypes.size();
        int i=1;
        for (String mimetype: mimetypes)
        {
            sb.append(getMimetypeService().getExtension(mimetype));
            if (i < j)
            {
                sb.append(++i < j ? ", " : " or ");
            }
        }
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((beanName == null) ? 0 : beanName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ContentTransformerHelper other = (ContentTransformerHelper) obj;
        if (beanName == null)
        {
            if (other.beanName != null)
                return false;
        }
        else if (!beanName.equals(other.beanName))
            return false;
        return true;
    }
    
    private class DeprecatedSetter
    {
        private final String sourceMimetype;
        private final String targetMimetype;
        private final String suffixAndValue;
        
        DeprecatedSetter(String sourceMimetype, String targetMimetype, String suffixAndValue)
        {
            this.sourceMimetype = sourceMimetype;
            this.targetMimetype = targetMimetype;
            this.suffixAndValue = suffixAndValue;
        }

        public String getPropertyNameAndValue(String beanName)
        {
            return TransformerConfig.CONTENT+beanName+
                    (sourceMimetype != null
                    ? TransformerConfig.EXTENSIONS+getExtensionOrAny(sourceMimetype)+'.'+getExtensionOrAny(targetMimetype)
                    : ".")+
                    suffixAndValue;
        }
    }
}