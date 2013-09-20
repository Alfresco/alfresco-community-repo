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
import static org.alfresco.repo.content.transform.TransformerConfig.AVAILABLE;
import static org.alfresco.repo.content.transform.TransformerConfig.CONTENT;
import static org.alfresco.repo.content.transform.TransformerConfig.ERROR_TIME;
import static org.alfresco.repo.content.transform.TransformerConfig.FAILOVER;
import static org.alfresco.repo.content.transform.TransformerConfig.INITIAL_COUNT;
import static org.alfresco.repo.content.transform.TransformerConfig.INITIAL_TIME;
import static org.alfresco.repo.content.transform.TransformerConfig.MAX_PAGES;
import static org.alfresco.repo.content.transform.TransformerConfig.MAX_SOURCE_SIZE_K_BYTES;
import static org.alfresco.repo.content.transform.TransformerConfig.PAGE_LIMIT;
import static org.alfresco.repo.content.transform.TransformerConfig.PIPELINE;
import static org.alfresco.repo.content.transform.TransformerConfig.PRIORITY;
import static org.alfresco.repo.content.transform.TransformerConfig.READ_LIMIT_K_BYTES;
import static org.alfresco.repo.content.transform.TransformerConfig.READ_LIMIT_TIME_MS;
import static org.alfresco.repo.content.transform.TransformerConfig.SUPPORTED;
import static org.alfresco.repo.content.transform.TransformerConfig.THRESHOLD_COUNT;
import static org.alfresco.repo.content.transform.TransformerConfig.TIMEOUT_MS;
import static org.alfresco.repo.content.transform.TransformerConfig.TRANSFORMER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.util.Pair;



/**
 * Provides methods to set and remove transformer properties and values.
 * 
 * @author Alan Davis
 */
public class TransformerPropertySetter
{
    /**
     * Matches lines
     */
    private static final Pattern LINE_SPLIT = Pattern.compile("^(.*)$", Pattern.MULTILINE);
    
    /**
     * Matches leading white space and trailing comments
     */
    private static final Pattern COMMENT = Pattern.compile("(^\\s+)|(\\s*#.*)|(\\s+$)");
    
    /**
     * Tries to match a space that should be a newline. JConsole has turned \n into spaces!
     */
    private static Pattern NEWLINE_CORRECTION = Pattern.compile("([^#]\\s*) ((content\\.transformer\\.)|(transformer\\.))");
    
    /**
     * Integer Value
     */
    private static final Pattern INTEGER = Pattern.compile("^-?\\d+$");

    private final TransformerProperties transformerProperties;
    private final MimetypeService mimetypeService;
    private final ContentTransformerRegistry transformerRegistry;

    public TransformerPropertySetter(TransformerProperties transformerProperties, MimetypeService mimetypeService,
            ContentTransformerRegistry transformerRegistry)
    {
        this.transformerProperties = transformerProperties;
        this.mimetypeService = mimetypeService;
        this.transformerRegistry = transformerRegistry;
    }

   /**
    * Sets transformer properties from the supplied multi line propertyNamesAndValues.
    * @throws IllegalArgumentException if an unexpected line is found
    */
    public int setProperties(String propertyNamesAndValues)
    {
        Map<String, String> map = new HashMap<String, String>();
        Map<String, String> transformerReferences = new HashMap<String, String>();
        Set<String> dynamicTransformerNames = new HashSet<String>();
        for (String propertyNameAndValue: extractProperties(propertyNamesAndValues, true, transformerReferences, dynamicTransformerNames))
        {
            Pair<String, String> pair = splitNameAndValue(propertyNameAndValue);
            String propertyName = pair.getFirst();
            String value = pair.getSecond();
            if (map.containsKey(propertyName))
            {
                throw new IllegalArgumentException(propertyName+" has been specified more than once");
            }
            if (!value.equals(transformerProperties.getProperty(propertyName)))
            {
                map.put(propertyName, value);
            }
        }
        
        // Check transformer names exist or will exist
        checkTransformerReferences(transformerReferences, dynamicTransformerNames);
        
        int size = map.size();
        if (size > 0)
        {
            transformerProperties.setProperties(map);
        }
        return size;
    }

    private void checkTransformerReferences(Map<String, String> transformerReferences,
            Set<String> dynamicTransformerNames) throws IllegalArgumentException
    {
        // Add on the static transformer names into the dynamic transformer names supplied
        Set<String> allTransformerNames = new HashSet<String>(dynamicTransformerNames);
        for (ContentTransformer transformer: transformerRegistry.getAllTransformers())
        {
            String name = transformer.getName();
            allTransformerNames.add(name);
        }
        allTransformerNames.add(TransformerConfig.DEFAULT_TRANSFORMER);
        
        for (String transformerSimpleName: transformerReferences.keySet())
        {
            if (!ANY.equals(transformerSimpleName))
            {
                String name = TRANSFORMER+transformerSimpleName;
                if (!allTransformerNames.contains(name))
                {
                    String line = transformerReferences.get(transformerSimpleName);
                    throw unexpectedProperty("Transformer "+transformerSimpleName+" does not exist", line);
                }
            }
        }
    }
    
    /**
     * Removes transformer properties from the supplied multi line propertyNames.
     * @param propertyNames which optionally include a value
     * @throws IllegalArgumentException if an unexpected line is found
     */
    public int removeProperties(String propertyNames)
    {
        Set<String> remove = new HashSet<String>();
        Map<String, String> transformerReferences = new HashMap<String, String>();
        Set<String> dynamicTransformerNames = new HashSet<String>();
        Properties defaultProperties = transformerProperties.getDefaultProperties();
        for (String propertyNameAndValue: extractProperties(propertyNames, false, transformerReferences, dynamicTransformerNames))
        {
            Pair<String, String> pair = splitNameAndValue(propertyNameAndValue);
            String propertyName = pair.getFirst();
            if (transformerProperties.getProperty(propertyName) == null)
            {
                throw unexpectedProperty("Does not exist", propertyName);
            }
            if (defaultProperties.getProperty(propertyName) != null)
            {
                throw unexpectedProperty("Is a deafult property so may not be removed", propertyName);
            }
            remove.add(propertyName);
        }
        transformerProperties.removeProperties(remove);
        return remove.size();
    }

    /**
     * Returns an array of transformer property names (and optional values).
     * @param text to be parsed
     * @param hasValue values are required, optionally exist when false.
     * @param transformerReferences map of transformerReferences to a line that referenced them.
     * @param dynamicTransformerNames added via properties
     * @return a list of cleaned up transformer properties from the text
     * @throws IllegalArgumentException if an unexpected line is found
     */
    private List<String> extractProperties(String text, boolean hasValue, Map<String,
            String> transformerReferences, Set<String> dynamicTransformerNames)
    {
        List<String> properties = new ArrayList<String>();
        text = fixJConsolesMissingNewlines(text);
        
        // Split the lines
        Matcher lineMatcher = LINE_SPLIT.matcher(text);
        while (lineMatcher.find())
        {
            String line = lineMatcher.group();
            
            // Strip comments from lines
            Matcher commentMatcher = COMMENT.matcher(line);
            line = commentMatcher.replaceAll("");
            
            // Ignore blank lines
            if (line.length() != 0)
            {
                String lowerLine = line.toLowerCase(); // Should we set the lower case value anyway
                if (lowerLine.startsWith(TransformerConfig.PREFIX))
                {
                    checkTransformerProperty(hasValue, line, transformerReferences, dynamicTransformerNames);
                    properties.add(line);
                }
                else if (lowerLine.startsWith(TransformerConfig.DEBUG_ENTRIES))
                {
                    checkInteger(hasValue, line, TransformerConfig.DEBUG_ENTRIES.length());
                    properties.add(line);
                }
                else if (lowerLine.startsWith(TransformerConfig.LOG_ENTRIES))
                {
                    checkInteger(hasValue, line, TransformerConfig.LOG_ENTRIES.length());
                    properties.add(line);
                }
                else
                {
                    throw unexpectedProperty("Not a transformer property", line);
                }
            }
        }
        
        return properties;
    }

    /**
     * Multi-line Strings from JConsole have their end of line bytes replaced by a
     * single space. The following method tries to put them back in.<p>
     * 
     * It tries to avoid commented out transformers.
     * 
     * @param text to scan
     * @return modified text
     */
    String fixJConsolesMissingNewlines(String text)
    {
        Matcher newlineMatcher = NEWLINE_CORRECTION.matcher(text);
        text = newlineMatcher.replaceAll("$1\n$2");
        return text;
    }

    private void checkTransformerProperty(boolean hasValue, String line,
            Map<String, String> transformerReferences, Set<String> dynamicTransformerNames)
    {
        int j = line.indexOf('=');
        String propertyName = j != -1 ? line.substring(0, j) : line;
        TransformerPropertyNameExtractor extractor = new TransformerPropertyNameExtractor() {};

        boolean validPropertyName = false;
        String transformerName = null;
        String suffix = null;
        String separator = null;

        suffixesLoop:
        for (String aSuffix: TransformerConfig.ALL_SUFFIXES)
        {
            if (propertyName.endsWith(aSuffix))
            {
                transformerName = propertyName.substring(CONTENT.length(), propertyName.length()-aSuffix.length());
                suffix = aSuffix;
                boolean separatorMatch = false;
                for (String aSeparator: TransformerConfig.SEPARATORS)
                {
                    int i = transformerName.lastIndexOf(aSeparator);
                    if (i != -1)
                    {
                        separatorMatch = true;
                        String extensions = transformerName.substring(i+aSeparator.length());
                        String[] ext = extractor.splitExt(extensions);
                        if (ext.length == 2)
                        {
                            String firstExpression = ext[0];
                            String secondExpression = ext[1];
                            if (aSeparator == TransformerConfig.EXTENSIONS)
                            {
                                if (extractor.getMatchingExtensionsFromExtensions(firstExpression, mimetypeService).size() == 0)
                                {
                                    throw unexpectedProperty("Invalid source extension "+firstExpression, line);
                                }
                                if (extractor.getMatchingExtensionsFromExtensions(secondExpression, mimetypeService).size() == 0)
                                {
                                    throw unexpectedProperty("Invalid target extension "+secondExpression, line);
                                }
                            }
                            else // if (separator == TransformerConfig.MIMETYPES)
                            {
                                if (extractor.getMatchingExtensionsFromMimetypes(firstExpression, mimetypeService).size() == 0)
                                {
                                    throw unexpectedProperty("Invalid source mimetype "+firstExpression, line);
                                }
                                if (extractor.getMatchingExtensionsFromMimetypes(secondExpression, mimetypeService).size() == 0)
                                {
                                    throw unexpectedProperty("Invalid target mimetype "+secondExpression, line);
                                }
                            }
                            transformerName = transformerName.substring(0,  i);
                            separator = aSeparator;
                            validPropertyName = true;
                            break suffixesLoop;
                        }
                    }
                }
                separator = null;
                
                if (!separatorMatch)
                {
                    validPropertyName = true;
                    break suffixesLoop;
                }
            }
        }
        
        if (!validPropertyName)
        {
            throw unexpectedProperty("Possible typo in the property name", line);
        }
        
        checkTransformerPropertyValue(hasValue, line, j, transformerName, separator, suffix,
                transformerReferences, dynamicTransformerNames);
    }

    private void checkTransformerPropertyValue(boolean hasValue, String line, int i,
            String transformerName, String separator, String suffix,
            Map<String, String> transformerReferences, Set<String> dynamicTransformerNames)
    {
        if (MAX_SOURCE_SIZE_K_BYTES.equals(suffix) ||
            TIMEOUT_MS.equals(suffix) ||
            READ_LIMIT_K_BYTES.equals(suffix) ||
            READ_LIMIT_TIME_MS.equals(suffix) ||
            INITIAL_COUNT.equals(suffix) ||
            INITIAL_TIME.equals(suffix) ||
            ERROR_TIME.equals(suffix))
        {
            checkLong(hasValue, line, i);
        }
        else if (MAX_PAGES.equals(suffix) ||
                 PAGE_LIMIT.equals(suffix) ||
                 THRESHOLD_COUNT.equals(suffix) ||            
                 PRIORITY.equals(suffix))
        {
            checkInteger(hasValue, line, i);
        }
        else if (SUPPORTED.equals(suffix) ||
                 AVAILABLE.equals(suffix))
        {
            checkBoolean(hasValue, line, i);
        }
        else if (PIPELINE.equals(suffix) ||
                 FAILOVER.equals(suffix))
        {
            dynamicTransformerNames.add(transformerName);
            if (separator != null)
            {
                throw unexpectedProperty("Separator was not expected", line);
            }
            
            if (PIPELINE.equals(suffix))
            {
                checkPipelineValue(hasValue, line, i, transformerReferences);
            }
            else
            {
                checkFailoverValue(hasValue, line, i, transformerReferences);
            }
        }
    }
    
    private void checkInteger(boolean hasValue, String line, int i)
    {
        String value = checkValue(hasValue, line, i);

        if (value != null)
        {
            if (!INTEGER.matcher(value).find())
            {
                throw unexpectedProperty("Expected an integer value", line);
            }
            try
            {
                Integer.parseInt(value);
            }
            catch (NumberFormatException e)
            {
                throw unexpectedProperty("Expected an int value", line);
            }
        }
    }

    private void checkLong(boolean hasValue, String line, int i)
    {
        String value = checkValue(hasValue, line, i);

        if (value != null)
        {
            if (!INTEGER.matcher(value).find())
            {
                throw unexpectedProperty("Expected an integer value", line);
            }
            try
            {
                Long.parseLong(value);
            }
            catch (NumberFormatException e)
            {
                throw unexpectedProperty("Expected a long value", line);
            }
        }
    }

    private void checkBoolean(boolean hasValue, String line, int i)
    {
        String value = checkValue(hasValue, line, i);

        if (value != null)
        {
            if (!value.equalsIgnoreCase("true") &&
                !value.equalsIgnoreCase("false"))
            {
                throw unexpectedProperty("Expected true or false value", line);
            }
        }
    }

    private void checkPipelineValue(boolean hasValue, String line, int i,
            Map<String, String> transformerReferences)
    {
        String value = checkValue(hasValue, line, i);

        if (value != null)
        {
            String[] transformerNamesAndExtensions = value.split("\\|");

            int count = transformerNamesAndExtensions.length;
            
            // Must be an even number of | characters, as they should be
            // an initial transformer and then pairs of extension and transformer.
            if (count < 2 || count % 2 == 0)
            {
                throw unexpectedProperty("Incomplete pipeline value", line);
            }
            
            for (int j=0; j < count; j++)
            {
                if (j % 2 == 0)
                {
                    // Added reference to the transformer if not ANY transformer
                    if (transformerNamesAndExtensions[j].length() > 0)
                    {
                        transformerReferences.put(transformerNamesAndExtensions[j], line);
                    }
                }
                else
                {
                    String extension = transformerNamesAndExtensions[j];
                    TransformerPropertyNameExtractor extractor= new TransformerPropertyNameExtractor() {};
                    if (extractor.getMatchingExtensionsFromExtensions(extension, mimetypeService).size() == 0)
                    {
                        throw unexpectedProperty("Invalid intermediate extension "+extension, line);
                    }
                }
            }
        }
    }

    private void checkFailoverValue(boolean hasValue, String line, int i,
            Map<String, String> transformerReferences)
    {
        String value = checkValue(hasValue, line, i);

        if (value != null)
        {
            String[] transformerNames = value.split("\\|");
            int count = transformerNames.length;
            
            // Should be more than one transformer
            if (count < 2)
            {
                throw unexpectedProperty("Can't failover if there is only on transformer", line);
            }
            
            // Add every component
            for (int j=0; j < count; j++)
            {
                if (transformerNames[j].length() > 0)
                {
                    transformerReferences.put(transformerNames[j], line);
                }
            }
        }
    }

    /**
     * Check that a line has an assignment of a value if hasValue is true and
     * if false the assignment is optional.
     * @param hasValue
     * @param line
     * @param i the offset where the assignment should start
     * @return the value if there is an assignment. null otherwise.
     * @throws IllegalArgumentException if there is a problem.
     */
    private String checkValue(boolean hasValue, String line, int i)
    {
        int l = line.length();
        if (( hasValue && (i == -1 || l <= i || line.charAt(i) != '=')) ||
            (!hasValue && (           l <  i || (i != -1 && l > i && line.charAt(i) != '='))))
        {
            throw unexpectedProperty("Expected a value after an '=' at char "+i, line);
        }
        
        return (hasValue || (i != -1 && l > i)) ? line.substring(i+1) : null;
    }

    private IllegalArgumentException unexpectedProperty(String context, String line) throws IllegalArgumentException
    {
        return new IllegalArgumentException("Unexpected property: "+line+"    "+context);
    }

    Pair<String, String> splitNameAndValue(String propertyNameAndValue)
    {
        int i = propertyNameAndValue.indexOf('=');
        String name = i != -1 ? propertyNameAndValue.substring(0, i) : propertyNameAndValue;
        String value = i != -1 ? propertyNameAndValue.substring(i+1) : "";
        Pair<String, String> pair = new Pair<String, String>(name, value);
        return pair;
    }
}
