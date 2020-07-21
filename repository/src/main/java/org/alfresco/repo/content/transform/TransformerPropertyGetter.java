/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.content.transform;

import static org.alfresco.repo.content.transform.TransformerConfig.PREFIX;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.alfresco.service.cmr.repository.MimetypeService;


/**
 * Provides access to a String representation of all transformer properties and values
 * where these are sorted into groups.
 * 
 * @author Alan Davis
 */
public class TransformerPropertyGetter
{
    private final String string;
    
    public TransformerPropertyGetter(boolean changesOnly, TransformerProperties transformerProperties,
            MimetypeService mimetypeService, ContentTransformerRegistry transformerRegistry,
            TransformerLog transformerLog, TransformerDebugLog transformerDebugLog)
    {
        List<ContentTransformer> availableTransformers = transformerRegistry.getTransformers();
        StringBuilder sb = new StringBuilder();

        // Log entries
        appendLoggerSetting(sb, changesOnly, transformerLog, transformerDebugLog, transformerProperties);

        // Miscellaneous
        appendMiscellaneousSettings(sb, changesOnly, transformerProperties);

        // Default transformer
        Set<String> alreadySpecified = new HashSet<String>();
        appendConfiguredTransformerSettings(sb, changesOnly, transformerProperties, mimetypeService, availableTransformers,
                transformerRegistry, true, alreadySpecified,
                "# Default transformer settings\n" +
                "# ============================\n");
        
        // Other transformers with configuration properties
        appendConfiguredTransformerSettings(sb, changesOnly, transformerProperties, mimetypeService, availableTransformers,
                transformerRegistry, false, alreadySpecified,
                "# Transformers with configuration settings\n" +
                "# ========================================\n" +
                "# Commented out settings are hard coded values for information purposes\n");

        // Other transformers without configuration properties
        if (!changesOnly)
        {
            appendUnconfiguredTransformerSettings(sb, mimetypeService, alreadySpecified,
                    availableTransformers, transformerRegistry);
        }
        
        if (sb.length() == 0)
        {
            sb.append(changesOnly
                    ? "No custom transformer properties defined"
                    : "No transformer properties defined");
        }

        string = sb.toString();
    }

    private void appendLoggerSetting(StringBuilder sb, boolean changesOnly, TransformerLog transformerLog,
            TransformerDebugLog transformerDebugLog, TransformerProperties transformerProperties)
    {
        Properties defaultProperties = transformerProperties.getDefaultProperties();
        String logEntries = transformerLog.getPropertyAndValue(defaultProperties);
        String debugEntries = transformerDebugLog.getPropertyAndValue(defaultProperties);
        boolean logEntriesChanged = !logEntries.startsWith("#");
        boolean debugEntriesChanged = !debugEntries.startsWith("#");

        if (!changesOnly || logEntriesChanged || debugEntriesChanged)
        {
            sb.append("# LOG and DEBUG history sizes\n");
            sb.append("# ===========================\n");
            sb.append("# Use small values as these logs are held in memory. 0 to disable.\n");
            if (!changesOnly || logEntriesChanged)
            {
                sb.append(logEntries);
                sb.append("\n");
            }
            if (!changesOnly || debugEntriesChanged)
            {
                sb.append(debugEntries);
                sb.append("\n");
            }
        }
    }
    
    private void appendMiscellaneousSettings(StringBuilder sb, boolean changesOnly,
        TransformerProperties transformerProperties)
    {
        Properties defaultProperties = transformerProperties.getDefaultProperties();
        boolean first = true;
        for (String propertyName: getMiscellaneousPropertyNames(defaultProperties))
        {
            String defaultValue = defaultProperties.getProperty(propertyName);
            String value = transformerProperties.getProperty(propertyName);
            boolean isDefaultValue = value == null || value.equals(defaultValue);
            value = value == null ? defaultValue : value;
            
            if (!changesOnly || !isDefaultValue)
            {
                if (first)
                {
                    sb.append("\n");
                    sb.append("# Miscellaneous settings\n");
                    sb.append("# ======================\n");
                    first = false;
                }
                appendProperty(sb, propertyName, value, defaultValue);
                sb.append("\n");
            }
        }
    }

    // Gets names from transformers.properties that are not log or content.transformer values.
    private Set<String> getMiscellaneousPropertyNames(Properties defaultProperties)
    {
        Set<String> propertyNames = new TreeSet<String>();
        for (Object key: defaultProperties.keySet())
        {
            String propertyName = key.toString();
            if (!propertyName.startsWith(PREFIX) &&
                !propertyName.equals(TransformerConfig.LOG_ENTRIES) &&
                !propertyName.equals(TransformerConfig.DEBUG_ENTRIES))
            {
                propertyNames.add(propertyName);
            }
        }
        return propertyNames;
    }

    public static void appendProperty(StringBuilder sb, String propertyName, String value, String defaultValue)
    {
        boolean isDefaultValue = value.equals(defaultValue);
        if (isDefaultValue)
        {
            sb.append("# ");
        }
        sb.append(propertyName);
        sb.append('=');
        sb.append(value);
        if (!isDefaultValue)
        {
            sb.append("  # default=");
            sb.append(defaultValue);
        }
    }

    private void appendConfiguredTransformerSettings(final StringBuilder sb, final boolean changesOnly, 
            TransformerProperties transformerProperties, MimetypeService mimetypeService,
            final List<ContentTransformer> availableTransformers,
            final ContentTransformerRegistry transformerRegistry,
            final boolean defaultTransformer, final Set<String> alreadySpecified,
            final String header)
    {
        final StringBuilder prefix = new StringBuilder();
        final StringBuilder general = new StringBuilder();
        final StringBuilder mimetypes = new StringBuilder();
        final AtomicInteger start = new AtomicInteger(-1);
        final AtomicReference<String> currentName = new AtomicReference<String>();
        final Properties defaultProperties = transformerProperties.getDefaultProperties();
        
        new TransformerPropertyNameExtractor()
        {
            /**
             * Uses the propertyName and values rather than expanding the property name into multiple
             * entries, in order to build up the string.
             */
            @Override
            protected void handleProperty(
                    String name,
                    String separator,
                    String firstExpression,
                    String secondExpression,
                    String suffix,
                    String use,
                    String value,
                    String propertyName,
                    Map<TransformerSourceTargetSuffixKey, TransformerSourceTargetSuffixValue> transformerSourceTargetSuffixValues, MimetypeService mimetypeService)
            {
                if (defaultTransformer == TransformerConfig.DEFAULT_TRANSFORMER.equals(name))
                {
                    String defaultValue = defaultProperties == null ? null : defaultProperties.getProperty(propertyName);
                    boolean isDefaultValue = value.equals(defaultValue);
                    if (!changesOnly || !isDefaultValue)
                    {
                        String prevName = currentName.getAndSet(name);
                        if (prevName != null && !prevName.equals(name))
                        {
                            appendTransformerSettings(sb, start, prevName, prefix, general, mimetypes, mimetypeService,
                                    alreadySpecified, availableTransformers, transformerRegistry, defaultTransformer, header);
                        }
                        
                        StringBuilder tmp =
                                separator != null
                                ? mimetypes
                                : TransformerConfig.PIPELINE.equals(suffix)
                                ? prefix
                                : general;
                        if (isDefaultValue)
                        {
                            tmp.append("# ");
                        }
                        tmp.append(propertyName);
                        tmp.append('=');
                        tmp.append(value);
                        if (!isDefaultValue && defaultValue != null)
                        {
                            tmp.append("  # default=");
                            tmp.append(defaultValue);
                        }
                        tmp.append("\n");
                    }
                }
            }
        }.getTransformerSourceTargetValues(TransformerConfig.ALL_SUFFIXES, true, true, transformerProperties, mimetypeService);
        String currName = currentName.get();
        if (currName != null)
        {
            if (defaultTransformer == TransformerConfig.DEFAULT_TRANSFORMER.equals(currName))
            {
                appendTransformerSettings(sb, start, currName, prefix, general, mimetypes, mimetypeService,
                        alreadySpecified, availableTransformers, transformerRegistry, defaultTransformer, header);
            }
        }
    }
    
    private void appendTransformerSettings(StringBuilder sb, AtomicInteger start, String transformerName, StringBuilder prefix,
            StringBuilder general, StringBuilder mimetypes, MimetypeService mimetypeService, Set<String> alreadySpecified,
            List<ContentTransformer> availableTransformers, ContentTransformerRegistry transformerRegistry,
            boolean defaultTransformer, String header)
    {
        if (start.get() == -1)
        {
            if (sb.length() != 0)
            {
                sb.append('\n');
            }
            sb.append(header);
            start.set(sb.length());
        }
        
        if (!defaultTransformer)
        {
            alreadySpecified.add(transformerName);
            ContentTransformer transformer;
            prefix.insert(0, '\n');
            try
            {
                transformer = transformerRegistry.getTransformer(transformerName);
                boolean available = availableTransformers.contains(transformer);
                prefix.insert(1, transformer.getComments(available));
            }
            catch (IllegalArgumentException e)
            {
                if (transformerName.startsWith(TransformerConfig.TRANSFORMER))
                {
                    transformerName = transformerName.substring(TransformerConfig.TRANSFORMER.length());
                }
                prefix.insert(1, ContentTransformerHelper.getCommentName(transformerName)+"# Unregistered transformer\n");
                transformer = null;
            }
            
            sb.append(prefix.toString());
        }

        sb.append(general.toString());
        sb.append(mimetypes.toString());
        
        // Reset for next one
        prefix.setLength(0);
        general.setLength(0);
        mimetypes.setLength(0);
    }

    private void appendUnconfiguredTransformerSettings(StringBuilder sb, MimetypeService mimetypeService,
            Set<String> alreadySpecified, List<ContentTransformer> availableTransformers,
            ContentTransformerRegistry transformerRegistry)
    {
        boolean first = true;
        for (ContentTransformer transformer: transformerRegistry.getAllTransformers())
        {
            String name = transformer.getName();
            if (!alreadySpecified.contains(name))
            {
                if (first)
                {
                    sb.append("\n");
                    sb.append("# Transformers without extra configuration settings\n");
                    sb.append("# =================================================\n\n");
                    first = false;
                }
                else
                {
                    sb.append('\n');
                }
                boolean available = availableTransformers.contains(transformer);
                sb.append(transformer.getComments(available));
            }
        }
    }

    public String toString()
    {
        return string;
    }
}
