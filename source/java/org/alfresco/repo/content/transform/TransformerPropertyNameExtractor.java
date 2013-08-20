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

import org.alfresco.service.cmr.repository.MimetypeService;

/**
 * Provides access to transformer property names and values.
 * 
 * @author Alan Davis
 */
public abstract class TransformerPropertyNameExtractor
{
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
     * @param includeUse if true, additionally checks for specific usage values that override
     *        the normal defaults. Such properties have a suffix of ".use.<use>" where <use> 
     *        is a value such as "index", "webpreview", "doclib", "syncRule", "aysncRule".
     * @param transformerProperties that provides the properties
     * @param mimetypeService
     */
    protected Collection<TransformerSourceTargetSuffixValue> getTransformerSourceTargetValues(Collection<String> suffixes,
            boolean includeSummary, boolean includeUse, TransformerProperties transformerProperties, MimetypeService mimetypeService)
    {
        return new ArrayList<TransformerSourceTargetSuffixValue>(
                getTransformerSourceTargetValuesMap(suffixes, includeSummary, true, includeUse, transformerProperties, mimetypeService).values());
    }
    
    /**
     * Returns a map to access transformer name, source extension and target extension to
     * transformer name, source extension, target extension and value, created from property
     * names that defined transformation limits. When the separator indicates
     * it is followed by a regular expression that matches multiple mimetypes, more than one
     * value may be added. When there is a value defined for specific extensions it wins over
     * any regular expression value.
     * @param suffixes possible endings to the property names after the target mimetype extension.
     *        Must start with a '.' if there is a suffix.
     * @param includeSummary if true will include property names without the separator to
     *        source mimetype and target mimetype.
     * @param includeExtensions if false will exclude property names with the separator to
     *        source mimetype and target mimetype.
     * @param includeUse if true, additionally checks for specific usage values that override
     *        the normal defaults. Such properties have a suffix of ".use.<use>" where <use> 
     *        is a value such as "index", "webpreview", "doclib", "syncRule", "aysncRule".
     * @param transformerProperties that provides the properties
     * @param mimetypeService
     */
    protected Map<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue> getTransformerSourceTargetValuesMap(Collection<String> suffixes,
            boolean includeSummary, boolean includeExtensions, boolean includeUse, TransformerProperties transformerProperties, MimetypeService mimetypeService)
    {
        Map<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue> transformerSourceTargetSuffixValues =
                new HashMap<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue>();
        
        List<String> propertyNames = new ArrayList<String>(transformerProperties.getPropertyNames());
        Collections.sort(propertyNames);
        for (String propertyName: propertyNames)
        {
            if (propertyName.startsWith(PREFIX))
            {
                String use = null;
                String propertyNameWithoutUse = propertyName;
                if (includeUse)
                {
                    int i = propertyName.lastIndexOf(TransformerConfig.USE);
                    if (i != -1)
                    {
                        int j = i+TransformerConfig.USE.length();
                        if (propertyName.length()-j > 0)
                        {
                            use = propertyName.substring(j);
                            propertyNameWithoutUse = propertyName.substring(0, i);
                        }
                    }
                }
                
                suffixesLoop:
                for (String suffix: suffixes)
                {
                    if (propertyNameWithoutUse.endsWith(suffix))
                    {
                        String value = transformerProperties.getProperty(propertyName);
                        String transformerName = propertyNameWithoutUse.substring(CONTENT.length(), propertyNameWithoutUse.length()-suffix.length());
                        boolean separatorMatch = false;
                        for (String separator: TransformerConfig.SEPARATORS)
                        {
                            int i = transformerName.lastIndexOf(separator);
                            if (i != -1)
                            {
                                separatorMatch = true;
                                if (includeExtensions)
                                {
                                    String extensions = transformerName.substring(i+separator.length());
                                    String[] ext = splitExt(extensions);
                                    if (ext.length == 2)
                                    {
                                        transformerName = transformerName.substring(0,  i);
                                        String firstExpression = ext[0];
                                        String secondExpression = ext[1];
                                        handleProperty(transformerName,
                                                separator, firstExpression, secondExpression,
                                                suffix, use, value, propertyName, transformerSourceTargetSuffixValues, mimetypeService);
                                        break suffixesLoop;
                                    }
                                }
                            }
                        }
                        
                        if (!separatorMatch && includeSummary)
                        {
                            handleProperty(transformerName, null, null, null, suffix, use, value, propertyName, transformerSourceTargetSuffixValues, mimetypeService);
                            break suffixesLoop;
                        }
                    }
                }
            }
        }
        
        return transformerSourceTargetSuffixValues;
    }

    /**
     * Handles a property to add values to the supplied transformerSourceTargetSuffixValues.
     * If the separator is null, this indicates that the property provides a transformer
     * wide value, so firstExpression and secondExpression should also be ignored.
     */
    protected void handleProperty(String transformerName, String separator,
            String firstExpression, String secondExpression, String suffix, String use, String value,
            String propertyName,
            Map<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue> transformerSourceTargetSuffixValues, MimetypeService mimetypeService)
    {
        if (separator == null)
        {
            addTransformerSourceTargetValue(transformerSourceTargetSuffixValues,
                    false,
                    transformerName, ANY, ANY, suffix,
                    use, value, mimetypeService);
        }
        else
        {
            List<String> sourceExtensions = (separator == TransformerConfig.EXTENSIONS)
                    ? getMatchingExtensionsFromExtensions(firstExpression, mimetypeService)
                    : getMatchingExtensionsFromMimetypes( firstExpression, mimetypeService);
            List<String> targetExtensions = (separator == TransformerConfig.EXTENSIONS)
                    ? getMatchingExtensionsFromExtensions(secondExpression, mimetypeService)
                    : getMatchingExtensionsFromMimetypes( secondExpression, mimetypeService);
            for (String sourceExt : sourceExtensions)
            {
                for (String targetExt : targetExtensions)
                {
                    addTransformerSourceTargetValue(transformerSourceTargetSuffixValues,
                            (separator == TransformerConfig.MIMETYPES),
                            transformerName, sourceExt, targetExt, suffix,
                            use, value, mimetypeService);
                }
            }
        }
    }

    /**
     * Optionally adds a new TransformerSourceTargetValue. If the supplied value is constructed from
     * from a mimetypes property a new value is always added and will replace any existing value. If 
     * from an extensions property the value is only added if there is not a current value.
     * In other words properties that include mimetypes win out over those that use extensions.
     */
    private void addTransformerSourceTargetValue(
            Map<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue> transformerSourceTargetSuffixValues,
            boolean mimetypeProperty, String transformerName, String sourceExt, String targetExt, String suffix,
            String use, String value, MimetypeService mimetypeService)
    {
        TransformerSourceTargetSuffixValue transformerSourceTargetSuffixValue =
                new TransformerSourceTargetSuffixValue(transformerName, sourceExt, targetExt, suffix, use, value, mimetypeService);
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
    String[] splitExt(String extensions)
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
    Pattern pattern(String expression)
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
    List<String> getMatchingExtensionsFromMimetypes(
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
    List<String> getMatchingExtensionsFromExtensions(
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
    
    /**
     * Returns a transformer property value if it exists from the supplied map.
     * @param transformerName of the transformer
     * @param sourceExt {@code null} indicates this is a transformer wide property.
     * @param targetExt
     * @param suffix
     * @param use
     * @param transformerSourceTargetSuffixValues map of values
     * @return the value or {@code null} if not set.
     */
    protected String getProperty(String transformerName, String sourceExt, String targetExt,
            String suffix, String use, Map<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue> transformerSourceTargetSuffixValues)
    {
        TransformerSourceTargetSuffixKey key = new TransformerSourceTargetSuffixKey(transformerName,
                (sourceExt == null ? ANY : sourceExt), (targetExt == null ? ANY : targetExt), suffix, use);
        TransformerSourceTargetSuffixValue value = transformerSourceTargetSuffixValues.get(key);
        return value == null ? null : value.value;
    }
}

class TransformerSourceTargetSuffixKey
{
    final String transformerName;
    final String sourceExt;
    final String targetExt;
    final String suffix;
    final String use;

    // sourceExt and targetExt should never be null, but be set to ANY
    public TransformerSourceTargetSuffixKey(String transformerName, String sourceExt, String targetExt, String suffix, String use)
    {
        this.transformerName = transformerName;
        this.sourceExt = sourceExt;
        this.targetExt = targetExt;
        this.suffix = suffix;
        this.use = use;
    }

    public String toString()
    {
        return transformerName+(sourceExt.equals(ANY) && targetExt.equals(ANY)
                ? ""
                : TransformerConfig.EXTENSIONS+sourceExt+'.'+targetExt)+
                suffix+
                (use == null ? "" : TransformerConfig.USE + use);
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
        result = prime * result + ((use == null) ? 0 : use.hashCode());
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
        if (use == null)
        {
            if (other.use != null)
                return false;
        }
        else if (!use.equals(other.use))
            return false;
        return true;
    }
}

class TransformerSourceTargetSuffixValue extends TransformerSourceTargetSuffixKey
{
    final String value;
    final String sourceMimetype;
    final String targetMimetype;
    
    // sourceExt and targetExt should never be null, but be set to ANY
    public TransformerSourceTargetSuffixValue(String transformerName, String sourceExt,
            String targetExt, String suffix, String use, String value, MimetypeService mimetypeService)
    {
        super(transformerName, sourceExt, targetExt, suffix, use);
        
        this.value = value;
        this.sourceMimetype = ANY.equals(sourceExt) ? ANY : mimetypeService.getMimetype(sourceExt);
        this.targetMimetype = ANY.equals(targetExt) ? ANY : mimetypeService.getMimetype(targetExt);
    }
    
    public TransformerSourceTargetSuffixKey key()
    {
        return new TransformerSourceTargetSuffixKey(transformerName, sourceExt, targetExt, suffix, use);
    }
    
    public String toString()
    {
        return super.toString()+'='+value;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!super.equals(obj))
            return false;
        TransformerSourceTargetSuffixValue other = (TransformerSourceTargetSuffixValue) obj;
        if (!value.equals(other.value))
            return false;
        return true;
    }
}
