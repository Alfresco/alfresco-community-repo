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
import static org.alfresco.repo.content.transform.TransformerConfig.CONTENT;
import static org.alfresco.repo.content.transform.TransformerConfig.MIMETYPES_SEPARATOR;
import static org.alfresco.repo.content.transform.TransformerConfig.SUPPORTED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.Triple;

/**
 * Provides access to the lists of supported and unsupported mimetype transformations
 * defined via properties for all transformers.
 * 
 * @author Alan Davis
 */
public class TransformerConfigSupported extends TransformerPropertyNameExtractor
{
    private static final String IS_MIMETYPE_REGEX = 
            "(application|audio|image|message|model|multipart|text|video)/.+";
    private static final String IS_MIMETYPE_SUBTYPE_WILDCARD_REGEX = 
            "(application|audio|image|message|model|multipart|text|video)/.*\\" + ANY + ".*";
    
    // Holds configured (entries only exist if configured rather than for all possible combinations)
    // of supported and unsupported mimetypes transformations for a transformer.
    // SourceMimetype and targetMimetype may be 'ANY' values to act as wild cards.
    private Map<String, SupportedAndUnsupportedTransformations> supported;

    public TransformerConfigSupported(ChildApplicationContextFactory subsystem, MimetypeService mimetypeService)
    {
        setSupported(subsystem, mimetypeService);
    }
    
    /**
     * Determines if the given string is a mime type expression.
     * 
     * @param extension
     * @return whether or not the string is a mime type
     */
    private boolean isMimetype(String extension)
    {
        return (extension != null && extension.matches(IS_MIMETYPE_REGEX));
    }
    
    /**
     * Determines if the given string is a mime type expression containing a wildcard
     * in the subtype.
     * 
     * @param mimetype
     * @return whether or not the mime type contains a wildcard subtype
     */
    private boolean isMimetypeSubtypeWildcard(String mimetype)
    {
        return (mimetype != null && mimetype.matches(IS_MIMETYPE_SUBTYPE_WILDCARD_REGEX));
    }
    
    /**
     * Gets the mimetypes which match the given <code>configMimetype</code> from the given
     * <code>mimetypeService</code>.
     * <p>
     * If the given mime type string contains one or more wildcards (*) in the subtype, the string
     * is converted to a regular expression and any mimetypes which match are returned.
     * <p>
     * If the given mime type string has no wildcards a list with only the given
     * mime type is returned.
     * 
     * @param configMimetype
     * @param mimetypeService
     * @return the list of mime types which match the wildcard, or a list with just the given mimetype
     */
    private List<String> getMatchingMimetypes(
            String configMimetype, MimetypeService mimetypeService)
    {
        if (configMimetype == null)
        {
            return null;
        }
        List<String> matchingMimetypes = new ArrayList<String>(1);
        if (isMimetypeSubtypeWildcard(configMimetype))
        {
            String mimetypeWildcardRegex = configMimetype.replaceAll("\\" + ANY, ".*");
            for (String mimetype : mimetypeService.getMimetypes())
            {
                if (mimetype.matches(mimetypeWildcardRegex))
                {
                    matchingMimetypes.add(mimetype);
                }
            }
        }
        else
        {
            matchingMimetypes.add(configMimetype);
        }
        return matchingMimetypes;
    }

    /**
     * Sets the supported/unsupported mimetype transformations created from system properties.  
     */
    private void setSupported(ChildApplicationContextFactory subsystem, MimetypeService mimetypeService)
    {
        supported = new HashMap<String, SupportedAndUnsupportedTransformations>();

        // Gets all the supported and unsupported transformer, source and target combinations
        Set<Triple<String, String, String>> transformerNamesAndMimetypes =
                getTransformerNamesAndExt(MIMETYPES_SEPARATOR, Collections.singletonList(SUPPORTED), false, subsystem, mimetypeService);
        
        // Populate the transformer values
        for (Triple<String, String, String> triple: transformerNamesAndMimetypes)
        {
            String transformerName = triple.getFirst();
            String sourceExt = triple.getSecond();
            String targetExt = triple.getThird();

            SupportedAndUnsupportedTransformations supportedBytransformer = this.supported.get(transformerName);
            
            if (supportedBytransformer == null)
            {
                supportedBytransformer = new SupportedAndUnsupportedTransformations();
                this.supported.put(transformerName, supportedBytransformer);
            }
            boolean supported = getValueFromProperties(transformerName, sourceExt, targetExt, subsystem, SUPPORTED);
            
            List<String> sourceMimetypes = new ArrayList<String>(1);
            List<String> targetMimetypes = new ArrayList<String>(1);
            if (isMimetype(sourceExt))
            {
                sourceMimetypes.addAll(getMatchingMimetypes(sourceExt, mimetypeService));
            }
            else
            {
                sourceMimetypes.add(ANY.equals(sourceExt) ? ANY : mimetypeService.getMimetype(sourceExt));
            }
            if (isMimetype(targetExt))
            {
                targetMimetypes.addAll(getMatchingMimetypes(targetExt, mimetypeService));
            }
            else
            {
                targetMimetypes.add(ANY.equals(targetExt) ? ANY : mimetypeService.getMimetype(targetExt));
            }
            for (String sourceMimetype : sourceMimetypes)
            {
                for (String targetMimetype : targetMimetypes)
                {
                    supportedBytransformer.put(sourceMimetype, targetMimetype, supported);
                }
            }
        }
    }

    private boolean getValueFromProperties(String transformerName, String sourceExt, String targetExt,
            ChildApplicationContextFactory subsystem, String propertySuffix)
    {
        String propertyName = CONTENT+transformerName+
                (sourceExt == null ? "" : MIMETYPES_SEPARATOR+sourceExt+'.'+targetExt)+
                propertySuffix;
         String value = subsystem.getProperty(propertyName);
         return value == null || value.equalsIgnoreCase("true");
    }

    /**
     * See {@link TransformerConfig#isSupportedTransformation(ContentTransformer, String, String, TransformationOptions)}.
     */
    public boolean isSupportedTransformation(ContentTransformer transformer, String sourceMimetype,
            String targetMimetype, TransformationOptions options)
    {
        if (sourceMimetype == null)
        {
            sourceMimetype = ANY;
        }
        
        if (targetMimetype == null)
        {
            targetMimetype = ANY;
        }
        
        boolean isSupported = true;
        String name = transformer.getName();
        SupportedAndUnsupportedTransformations supportedBytransformer = supported.get(name);
        if (supportedBytransformer != null)
        {
            isSupported = supportedBytransformer.isSupported(sourceMimetype, targetMimetype);
        }
        return isSupported;
    }
    
    // Class contains both supported and unsupported combinations to avoid having to
    // add in an extra ANY to ANY combination which could be true or false. Having an
    // extra combination might reduce understandability.
    private class SupportedAndUnsupportedTransformations
    {
        DoubleMap<String, String, Boolean> supportedTransformations;
        DoubleMap<String, String, Boolean> unsupportedTransformations;
        
        boolean isSupported(String sourceMimetype, String targetMimetype)
        {
            boolean isSupported = true;
            if (supportedTransformations != null)
            {
                Boolean sup = supportedTransformations.get(sourceMimetype, targetMimetype);            
                isSupported = sup != null;
            }
            if (isSupported && unsupportedTransformations != null)
            {
                Boolean sup = unsupportedTransformations.get(sourceMimetype, targetMimetype);            
                isSupported = sup == null;
            }
            return isSupported;
        }

        public void put(String sourceMimetype, String targetMimetype, boolean supported)
        {
            if (supported)
            {
                if (supportedTransformations == null)
                {
                    supportedTransformations = new DoubleMap<String, String, Boolean>(ANY, ANY);
                }
                supportedTransformations.put(sourceMimetype, targetMimetype, supported);
            }
            else
            {
                if (unsupportedTransformations == null)
                {
                    unsupportedTransformations = new DoubleMap<String, String, Boolean>(ANY, ANY);
                }
                unsupportedTransformations.put(sourceMimetype, targetMimetype, supported);
            }
        }
    }
}
