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
import static org.alfresco.repo.content.transform.TransformerConfig.PREFIX;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.util.Triple;

/**
 * Provides access to transformer property names and values.
 * 
 * @author Alan Davis
 */
public abstract class TransformerPropertyNameExtractor
{
    private static String[] SEPARATORS = new String[] {TransformerConfig.EXTENSIONS , TransformerConfig.MIMETYPES};
    private static Pattern EXTENSIONS_SEPARATOR = Pattern.compile("[^]\\\\]\\.");
    private static String[] NO_EXT_MATCH = new String[0];

    /**
     * Returns a set of transformer name, source extension, target extension and value
     * from property names that defined transformation limits. When the separator indicates
     * it is followed by a regular expression that matches multiple mimetypes, more than one
     * value may be added. When there is a value defined for specific extensions it wins over
     * any regular expression value.
     * @param suffixes possible endings to the property names after the target mimetype extension.
     *        Must start with a '.' if there is a suffix.
     * @param includeSummary if true will also look for property names without the separator,
     *        source mimetype and target mimetype.
     * @param subsystem that provides the properties
     * @param mimetypeService
     */
    protected Collection<TransformerSourceTargetValue> getTransformerSourceTargetValues(Collection<String> suffixes,
            boolean includeSummary, ChildApplicationContextFactory subsystem, MimetypeService mimetypeService)
    {
        Map<Triple<String, String, String>, TransformerSourceTargetValue> TransformerSourceTargetValues =
                new HashMap<Triple<String, String, String>, TransformerSourceTargetValue>();
        
        for (String propertyName: subsystem.getPropertyNames())
        {
            if (propertyName.startsWith(PREFIX))
            {
                suffixesLoop:
                for (String suffix: suffixes)
                {
                    if (propertyName.endsWith(suffix))
                    {
                        String value = subsystem.getProperty(propertyName);
                        String name = propertyName.substring(CONTENT.length(), propertyName.length()-suffix.length());
                        boolean separatorMatch = false;
                        for (String separator: SEPARATORS)
                        {
                            int i = name.lastIndexOf(separator);
                            if (i != -1)
                            {
                                separatorMatch = true;
                                String extensions = name.substring(i+separator.length());
                                String[] ext = splitExt(extensions);
                                if (ext.length == 2)
                                {
                                    name = name.substring(0,  i);
                                    if (separator == TransformerConfig.EXTENSIONS)
                                    {
                                        addTransformerSourceTargetValue(TransformerSourceTargetValues,
                                                false, name, ext[0], ext[1], value, suffix, mimetypeService);
                                    }
                                    else if (separator == TransformerConfig.MIMETYPES)
                                    {
                                        List<String> sourceExtensions = getMatchingExtensions(ext[0], mimetypeService);
                                        List<String> targetExtensions = getMatchingExtensions(ext[1], mimetypeService);
                                        for (String sourceExt : sourceExtensions)
                                        {
                                            for (String targetExt : targetExtensions)
                                            {
                                                addTransformerSourceTargetValue(TransformerSourceTargetValues,
                                                        true, name, sourceExt, targetExt, value,
                                                        suffix, mimetypeService);
                                            }
                                        }
                                    }
                                    break suffixesLoop;
                                }
                            }
                            
                        }
                        
                        if (!separatorMatch && includeSummary)
                        {
                            addTransformerSourceTargetValue(TransformerSourceTargetValues, false, name, ANY, ANY,
                                    value, suffix, mimetypeService);
                            break suffixesLoop;
                        }
                    }
                }
            }
        }
        
        return TransformerSourceTargetValues.values();
    }

    /**
     * Optionally adds a new TransformerSourceTargetValue. If the supplied value is not constructed from
     * from a property that uses a regular expression against a mimetype, a new value is always added and
     * will replace any existing value. If not, the value is only added if there is not a current value.
     * In other words properties that include extensions win out over those that use mimetypes.
     */
    private void addTransformerSourceTargetValue(
            Map<Triple<String, String, String>, TransformerSourceTargetValue> transformerSourceTargetValues,
            boolean mimetypeProperty, String name, String sourceExt, String targetExt, String value,
            String suffix, MimetypeService mimetypeService)
    {
        TransformerSourceTargetValue transformerSourceTargetValue =
                new TransformerSourceTargetValue(name, sourceExt, targetExt, value, suffix, mimetypeService);
        Triple<String, String, String> key = transformerSourceTargetValue.key();

        if (!mimetypeProperty || !transformerSourceTargetValues.containsKey(key))
        {
            transformerSourceTargetValues.put(key, transformerSourceTargetValue);
        }
    }

    /**
     * Splits the extensions into two parts. It does this by looking for a '.'
     * that is not escaped (preceded by a back slash '\').
     * This is to allow regular expressions to be used for mimetypes.
     */
    private String[] splitExt(String extensions)
    {
        String[] ext = NO_EXT_MATCH;
        Matcher matcher = EXTENSIONS_SEPARATOR.matcher(extensions);
        if (matcher.find())
        {
            int i = matcher.start();
            ext = new String[2];
            ext[0] = extensions.substring(0, i+1).replaceAll("\\\\\\.", ".");
            ext[1] = extensions.substring(i+2).replaceAll("\\\\\\.", ".");
        }
        return ext;
    }

    /**
     * Gets the extensions of the mimetypes which match the given <code>configMimetype</code> from the given
     * <code>mimetypeService</code>.
     * <p>
     * If the given mimetype string contains one or more wildcards (*) in the subtype, the string
     * is converted to a regular expression and the extensions of the mimetypes which match are returned.
     * <p>
     * If the given mimetype string has no wildcards a list with only the given
     * mimetype's extension is returned.
     * 
     * @param mimetypeWildcardRegex
     * @param mimetypeService
     * @return the list of extensions of mimetypes which match
     */
    private List<String> getMatchingExtensions(
            String mimetypeWildcardRegex, MimetypeService mimetypeService)
    {
        List<String> matchingMimetypes = new ArrayList<String>(1);
        for (String mimetype : mimetypeService.getMimetypes())
        {
            if (mimetype.matches(mimetypeWildcardRegex))
            {
                String ext = mimetypeService.getExtension(mimetype);
                matchingMimetypes.add(ext);
            }
        }
        return matchingMimetypes;
    }
}

class TransformerSourceTargetValue
{
    final String transformerName;
    final String sourceExt;
    final String targetExt;
    final String value;
    final String suffix;
    final String sourceMimetype;
    final String targetMimetype;
    
    public TransformerSourceTargetValue(String transformerName, String sourceExt,
            String targetExt, String value, String suffix, MimetypeService mimetypeService)
    {
        this.transformerName = transformerName;
        this.sourceExt = sourceExt;
        this.targetExt = targetExt;
        this.value = value;
        this.suffix = suffix;
        this.sourceMimetype = ANY.equals(sourceExt) ? ANY : mimetypeService.getMimetype(sourceExt);
        this.targetMimetype = ANY.equals(targetExt) ? ANY : mimetypeService.getMimetype(targetExt);
    }
    
    public Triple<String, String, String> key()
    {
        return new Triple<String, String, String>(transformerName, sourceExt, targetExt);
    }
    
    public String toString()
    {
        return transformerName+'.'+(sourceExt.equals(ANY) && targetExt.equals(ANY)
                ? ""
                : TransformerConfig.EXTENSIONS+'.'+sourceExt+'.'+targetExt+'.')+
                suffix+'='+value;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sourceExt == null) ? 0 : sourceExt.hashCode());
        result = prime * result + ((targetExt == null) ? 0 : targetExt.hashCode());
        result = prime * result + ((transformerName == null) ? 0 : transformerName.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        TransformerSourceTargetValue other = (TransformerSourceTargetValue) obj;
        if (sourceExt == null)
        {
            if (other.sourceExt != null)
                return false;
        }
        else if (!sourceExt.equals(other.sourceExt))
            return false;
        if (targetExt == null)
        {
            if (other.targetExt != null)
                return false;
        }
        else if (!targetExt.equals(other.targetExt))
            return false;
        if (transformerName == null)
        {
            if (other.transformerName != null)
                return false;
        }
        else if (!transformerName.equals(other.transformerName))
            return false;
        if (value == null)
        {
            if (other.value != null)
                return false;
        }
        else if (!value.equals(other.value))
            return false;
        return true;
    }
}
