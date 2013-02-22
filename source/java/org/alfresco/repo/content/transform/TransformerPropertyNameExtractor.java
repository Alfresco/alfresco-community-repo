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
import java.util.Collections;
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
    protected Collection<TransformerSourceTargetSuffixValue> getTransformerSourceTargetValues(Collection<String> suffixes,
            boolean includeSummary, ChildApplicationContextFactory subsystem, MimetypeService mimetypeService)
    {
        Map<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue> transformerSourceTargetSuffixValues =
                new HashMap<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue>();
        
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
                                    List<String> sourceExtensions = (separator == TransformerConfig.EXTENSIONS)
                                            ? getMatchingExtensionsFromExtensions(ext[0], mimetypeService)
                                            : getMatchingExtensionsFromMimetypes( ext[0], mimetypeService);
                                    List<String> targetExtensions = (separator == TransformerConfig.EXTENSIONS)
                                            ? getMatchingExtensionsFromExtensions(ext[1], mimetypeService)
                                            : getMatchingExtensionsFromMimetypes( ext[1], mimetypeService);
                                    for (String sourceExt : sourceExtensions)
                                    {
                                        for (String targetExt : targetExtensions)
                                        {
                                            addTransformerSourceTargetValue(transformerSourceTargetSuffixValues,
                                                    (separator == TransformerConfig.MIMETYPES),
                                                    name, sourceExt, targetExt, suffix,
                                                    value, mimetypeService);
                                        }
                                    }
                                    break suffixesLoop;
                                }
                            }
                        }
                        
                        if (!separatorMatch && includeSummary)
                        {
                            addTransformerSourceTargetValue(transformerSourceTargetSuffixValues, false, name, ANY, ANY,
                                    suffix, value, mimetypeService);
                            break suffixesLoop;
                        }
                    }
                }
            }
        }
        
        return transformerSourceTargetSuffixValues.values();
    }

    /**
     * Optionally adds a new TransformerSourceTargetValue. If the supplied value is constructed from
     * from a mimetypes property a new value is always added and will replace any existing value. If 
     * from an extensions property the value is only added if there is not a current value.
     * In other words properties that include mimetypes win out over those that use extensions.
     */
    private void addTransformerSourceTargetValue(
            Map<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue> transformerSourceTargetSuffixValues,
            boolean mimetypeProperty, String name, String sourceExt, String targetExt, String suffix,
            String value, MimetypeService mimetypeService)
    {
        TransformerSourceTargetSuffixValue transformerSourceTargetSuffixValue =
                new TransformerSourceTargetSuffixValue(name, sourceExt, targetExt, suffix, value, mimetypeService);
        TransformerSourceTargetSuffixKey key = transformerSourceTargetSuffixValue.key();

        if (mimetypeProperty || !transformerSourceTargetSuffixValues.containsKey(key))
        {
            transformerSourceTargetSuffixValues.put(key, transformerSourceTargetSuffixValue);
        }
    }

    /**
     * Splits the extensions into two parts. It does this by looking for a '.'
     * that is not escaped (preceded by a back slash '\').
     * This is to allow regular expressions to be used for mimetypes.
     */
    static String[] splitExt(String extensions)
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
     * Returns a regex Pattern for the supplied expression where '*' represents zero
     * or more characters.
     */
    static Pattern pattern(String expression)
    {
        // Turn the pattern into a regular expression where any special regex
        // characters have no meaning and then get any * values to represent
        // zero or more chars.
        String regex = Pattern.quote(expression).replaceAll("\\*", "\\\\E.*\\\\Q");
        return Pattern.compile(regex);
    }

    /**
     * Gets the extensions of the mimetypes that match the given expression.
     * However if the expression is "*", only the ANY ("*") extension is returned.
     * @param expression which may contain '*' to represent zero or more characters.
     * @param mimetypeService
     * @return the list of extensions of mimetypes that match
     */
    private List<String> getMatchingExtensionsFromMimetypes(
            String expression, MimetypeService mimetypeService)
    {
        if (ANY.equals(expression))
        {
            return Collections.singletonList(ANY);
        }
        Pattern pattern = pattern(expression);
        List<String> matchingMimetypes = new ArrayList<String>(1);
        for (String mimetype : mimetypeService.getMimetypes())
        {
            if (pattern.matcher(mimetype).matches())
            {
                String ext = mimetypeService.getExtension(mimetype);
                matchingMimetypes.add(ext);
            }
        }
        return matchingMimetypes;
    }
    
    /**
     * Gets the extensions that match the given expression. Only the main extension
     * of each mimetype is checked.
     * However if the expression is "*", only the ANY ("*") extension is returned.
     * @param expression which may contain '*' to represent zero or more characters.
     * @param mimetypeService
     * @return the list of extensions that match
     */
    private List<String> getMatchingExtensionsFromExtensions(
            String expression, MimetypeService mimetypeService)
    {
        if (ANY.equals(expression))
        {
            return Collections.singletonList(ANY);
        }
        Pattern pattern = pattern(expression);
        List<String> matchingMimetypes = new ArrayList<String>(1);
        for (String mimetype : mimetypeService.getMimetypes())
        {
            String ext = mimetypeService.getExtension(mimetype);
            if (pattern.matcher(ext).matches())
            {
                matchingMimetypes.add(ext);
            }
        }
        return matchingMimetypes;
    }
}

class TransformerSourceTargetSuffixKey
{
    final String transformerName;
    final String sourceExt;
    final String targetExt;
    final String suffix;

    public TransformerSourceTargetSuffixKey(String transformerName, String sourceExt, String targetExt, String suffix)
    {
        this.transformerName = transformerName;
        this.sourceExt = sourceExt;
        this.targetExt = targetExt;
        this.suffix = suffix;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sourceExt == null) ? 0 : sourceExt.hashCode());
        result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
        result = prime * result + ((targetExt == null) ? 0 : targetExt.hashCode());
        result = prime * result + ((transformerName == null) ? 0 : transformerName.hashCode());
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
        TransformerSourceTargetSuffixKey other = (TransformerSourceTargetSuffixKey) obj;
        if (sourceExt == null)
        {
            if (other.sourceExt != null)
                return false;
        }
        else if (!sourceExt.equals(other.sourceExt))
            return false;
        if (suffix == null)
        {
            if (other.suffix != null)
                return false;
        }
        else if (!suffix.equals(other.suffix))
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
        return true;
    }
}

class TransformerSourceTargetSuffixValue
{
    final String transformerName;
    final String sourceExt;
    final String targetExt;
    final String suffix;
    final String value;

    final String sourceMimetype;
    final String targetMimetype;
    
    public TransformerSourceTargetSuffixValue(String transformerName, String sourceExt,
            String targetExt, String suffix, String value, MimetypeService mimetypeService)
    {
        this.transformerName = transformerName;
        this.sourceExt = sourceExt;
        this.targetExt = targetExt;
        this.suffix = suffix;
        this.value = value;

        this.sourceMimetype = ANY.equals(sourceExt) ? ANY : mimetypeService.getMimetype(sourceExt);
        this.targetMimetype = ANY.equals(targetExt) ? ANY : mimetypeService.getMimetype(targetExt);
    }
    
    public TransformerSourceTargetSuffixKey key()
    {
        return new TransformerSourceTargetSuffixKey(transformerName, sourceExt, targetExt, suffix);
    }
    
    public String toString()
    {
        return transformerName+(sourceExt.equals(ANY) && targetExt.equals(ANY)
                ? ""
                : TransformerConfig.EXTENSIONS+sourceExt+'.'+targetExt)+
                suffix+'='+value;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sourceExt == null) ? 0 : sourceExt.hashCode());
        result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
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
        TransformerSourceTargetSuffixValue other = (TransformerSourceTargetSuffixValue) obj;
        if (sourceExt == null)
        {
            if (other.sourceExt != null)
                return false;
        }
        else if (!sourceExt.equals(other.sourceExt))
            return false;
        if (suffix == null)
        {
            if (other.suffix != null)
                return false;
        }
        else if (!suffix.equals(other.suffix))
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
