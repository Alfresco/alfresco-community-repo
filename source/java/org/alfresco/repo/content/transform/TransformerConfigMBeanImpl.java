/*
 * Copyright 2005-2013 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.content.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.service.cmr.repository.MimetypeService;

/**
 * Implements the JMX interface for monitoring Content Transformer configuration
 * and statistics.
 * 
 * @author Alan Davis
 */
public class TransformerConfigMBeanImpl implements TransformerConfigMBean
{
    private static final String NO_TRANSFORMATIONS_TO_REPORT = "No transformations to report";
    private ContentTransformerRegistry transformerRegistry;
    private TransformerDebug transformerDebug;
    private TransformerConfig transformerConfig;
    private MimetypeService mimetypeService;
    private LogEntries transformerLog;
    private LogEntries transformerDebugLog;
    
    public void setContentTransformerRegistry(ContentTransformerRegistry transformerRegistry)
    {
        this.transformerRegistry = transformerRegistry;
    }

    public void setTransformerDebug(TransformerDebug transformerDebug)
    {
        this.transformerDebug = transformerDebug;
    }

    public void setTransformerConfig(TransformerConfig transformerConfig)
    {
        this.transformerConfig = transformerConfig;
    }

    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    public void setTransformerLog(LogEntries transformerLog)
    {
        this.transformerLog = transformerLog;
    }

    public void setTransformerDebugLog(LogEntries transformerDebugLog)
    {
        this.transformerDebugLog = transformerDebugLog;
    }

    @Override
    public String[] getTransformerNames()
    {
        List<String> transformerNames = new ArrayList<String>();
        Collection<ContentTransformer> transformers = transformerDebug.sortTransformersByName(null);
        for (ContentTransformer transformer: transformers)
        {
            String name = transformer.getName();
            name = name.startsWith(TransformerConfig.TRANSFORMER)
                    ? name.substring(TransformerConfig.TRANSFORMER.length())
                    : name;
            transformerNames.add(name);
        }
        return transformerNames.toArray(new String[transformerNames.size()]);
    }

    @Override
    public String[] getExtensionsAndMimetypes()
    {
        List<String> extensionsAndMimetypes = new ArrayList<String>();
        for (String mimetype: mimetypeService.getMimetypes(null))
        {
            String extension = mimetypeService.getExtension(mimetype);
            extensionsAndMimetypes.add(extension+" - "+mimetype);
        }
        return extensionsAndMimetypes.toArray(new String[extensionsAndMimetypes.size()]);
    }

    @Override
    public String getTransformationsByTransformer(String simpleTransformerName, String use)
    {
        use = nullDefaultParam(use);
        try
        {
            // Need to be able to generate 4.1.4ish output to compare with previous
            // releases without too much effort cutting and pasting to change the order
            return "41".equals(simpleTransformerName)
                ? transformerDebug.transformationsByTransformer(null, true, false, use)
                : transformerDebug.transformationsByTransformer(
                        getTransformerNameParam(simpleTransformerName), true, true, use);
        }
        catch (IllegalArgumentException e)
        {
            return e.getMessage();
        }
    }

    @Override
    public String getTransformationsByExtension(String sourceExtension, String targetExtension, String use)
    {
        use = nullDefaultParam(use);
        try
        {
            // 41: Need to be able to generate 4.1.4ish output to compare with previous
            //     releases without too much effort cutting and pasting to change the order
            // 00: (prefix) Finds only non deterministic transformations
            if ("41".equals(sourceExtension))
            {
                return transformerDebug.transformationsByExtension(null, null, true, false, false, null);
            }
            else
            {
                boolean onlyNonDeterministic = false;
                if (sourceExtension != null && sourceExtension.startsWith("00"))
                {
                    onlyNonDeterministic = true;
                    sourceExtension = sourceExtension.substring(2);
                }
                return transformerDebug.transformationsByExtension(nullDefaultLowerParam(sourceExtension),
                        nullDefaultLowerParam(targetExtension), true, true, onlyNonDeterministic, use);
            }
        }
        catch (IllegalArgumentException e)
        {
            return e.getMessage();
        }
    }
    
    @Override
    public String getTransformationStatistics(String simpleTransformerName, String sourceExtension, String targetExtension)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            
            String transformerName = getTransformerNameParam(simpleTransformerName);
            sourceExtension = nullDefaultLowerParam(sourceExtension);
            targetExtension = nullDefaultLowerParam(targetExtension);

            Collection<ContentTransformer> transformers = transformerDebug.sortTransformersByName(transformerName);
            Collection<String> sourceMimetypes = transformerDebug.getSourceMimetypes(sourceExtension);
            Collection<String> targetMimetypes = transformerDebug.getTargetMimetypes(sourceExtension, targetExtension, sourceMimetypes);

            // Only report system wide mimetype summary if transformer not specified
            boolean includeSystemWideSummary = transformerName == null;
            if (includeSystemWideSummary)
            {
                getTransformationStatistics(sourceExtension, targetExtension, sb,
                        null, sourceMimetypes, targetMimetypes, false);
            }
            
            for (ContentTransformer transformer: transformers)
            {
                getTransformationStatistics(sourceExtension, targetExtension, sb,
                        transformer, sourceMimetypes, targetMimetypes, includeSystemWideSummary);
            }
            
            if (sb.length() == 0)
            {
                sb.append(NO_TRANSFORMATIONS_TO_REPORT);
            }
            
            return sb.toString();
        }
        catch (IllegalArgumentException e)
        {
            return e.getMessage();
        }
    }

    private void getTransformationStatistics(String sourceExtension, String targetExtension,
            StringBuilder sb, ContentTransformer transformer, Collection<String> sourceMimetypes,
            Collection<String> targetMimetypes, boolean includeSystemWideSummary)
    {
        AtomicInteger counter = new AtomicInteger(0);
        int i = sb.length();

        for (String sourceMimetype: sourceMimetypes)
        {
            for (String targetMimetype: targetMimetypes)
            {
                getTransformationStatistics(sb, transformer, sourceMimetype, targetMimetype, counter, includeSystemWideSummary);
            }
        }
        
        // Only report transformer summary if there is more than one to summarise
        // and we were asked for all
        if (sourceExtension == null && targetExtension == null && counter.get() > 1)
        {
            StringBuilder sb2 = new StringBuilder();
            getTransformationStatistics(sb2, transformer, null, null, counter, includeSystemWideSummary);
            sb2.append('\n');
            sb.insert((i == 0 ? 0 : i+2), sb2);
        }
    }
    
    private void getTransformationStatistics(StringBuilder sb, ContentTransformer transformer,
            String sourceMimetype, String targetMimetype, AtomicInteger counter,
            boolean includeSystemWideSummary)
    {
        TransformerStatistics statistics = transformerConfig.getStatistics(transformer,
                sourceMimetype, targetMimetype, false);
        if (statistics != null)
        {
            long count = statistics.getCount();
            if (count > 0)
            {
                if (sb.length() > 0)
                {
                    sb.append('\n');
                }
                if (counter.incrementAndGet() == 1 && includeSystemWideSummary)
                {
                    sb.append('\n');
                }
                
                sb.append(statistics.getTransformerName());
                sb.append(' ');
                sb.append(statistics.getSourceExt());
                sb.append(' ');
                sb.append(statistics.getTargetExt());
                sb.append(" count=");
                sb.append(count);
                sb.append(" errors=");
                sb.append(statistics.getErrorCount());
                sb.append(" averageTime=");
                sb.append(statistics.getAverageTime());
                sb.append(" ms");
            }
        }
    }

    @Override
    public String[] getTransformationLog(int n)
    {
        String[] entries = transformerLog.getEntries(n);
        return entries.length == 0
                ? new String[] {NO_TRANSFORMATIONS_TO_REPORT}
                : entries;
    }
    
    @Override
    public String[] getTransformationDebugLog(int n)
    {
        String[] entries = transformerDebugLog.getEntries(n);
        return entries.length == 0
                ? new String[] {NO_TRANSFORMATIONS_TO_REPORT}
                : entries;
    }

    @Override
    public String getProperties(boolean listAll)
    {
        return transformerConfig.getProperties(!listAll);
    }
    
    @Override
    public String setProperties(String propertyNamesAndValues)
    {
        try
        {
            String nullPropertyNamesAndValues = nullDefaultParam(propertyNamesAndValues);
            int n = nullPropertyNamesAndValues == null
                ? 0
                : transformerConfig.setProperties(nullPropertyNamesAndValues);
            return "Properties added or changed: "+n;
        }
        catch (IllegalArgumentException e)
        {
            return e.getMessage();
        }
    }
    
    @Override
    public String removeProperties(String propertyNames)
    {
        try
        {
            propertyNames = nullDefaultParam(propertyNames);
            return "Properties removed: "+
                    (propertyNames == null
                    ? 0
                    : transformerConfig.removeProperties(propertyNames));
        }
        catch (IllegalArgumentException e)
        {
            return e.getMessage();
        }
    }
    
    @Override
    public String testTransform(final String simpleTransformerName, String sourceExtension,
            String targetExtension, String use)
    {
        use = nullDefaultParam(use);
        try
        {
            String transformerName = getTransformerNameParam(simpleTransformerName);
            return transformerName == null 
                    ? transformerDebug.testTransform(                 sourceExtension, targetExtension, use)
                    : transformerDebug.testTransform(transformerName, sourceExtension, targetExtension, use);
        }
        catch (IllegalArgumentException e)
        {
            return e.getMessage();
        }
    }
    
    @Override
    public String[] getContextNames()
    {
        return new String[] {"", "doclib", "index", "webpreview", "syncRule", "asyncRule", "pdf"};
    }

    @Override
    public String[] getCustomePropertyNames()
    {
        List<String> propertyNames = new ArrayList<String>();
        String[] lines = getProperties(false).split("\\n");
        for (String line: lines)
        {
            if (!line.isEmpty() && !line.startsWith("#") && line.indexOf(" # default=") == -1)
            {
                int i = line.indexOf('=');
                if (i != 0)
                {
                    String propertyName = line.substring(0, i);
                    propertyNames.add(propertyName);
                }
            }
        }
        return propertyNames.toArray(new String[propertyNames.size()]);
    }
    
    @Override
    public String[] getTestFileExtensionsAndMimetypes()
    {
        return transformerDebug.getTestFileExtensionsAndMimetypes();
    }

    /**
     * Returns a full transformer name given a simple transformer name parameter.
     * @param simpleTransformerName the name of the transformer without the
     *        {@link TransformerConfig#TRANSFORMER} prefix.
     * @return a null or a full transformer name
     */
    private String getTransformerNameParam(String simpleTransformerName)
    {
        simpleTransformerName = nullDefaultParam(simpleTransformerName);
        String transformerName = simpleTransformerName == null
                ? null
                : simpleTransformerName.startsWith(TransformerConfig.TRANSFORMER)
                ? simpleTransformerName
                : TransformerConfig.TRANSFORMER+simpleTransformerName;
        
        // Throws an IllegalArgumentException if unknown
        transformerRegistry.getTransformer(transformerName);

        return transformerName;
    }
    
    /**
     * Changes the default JConsole parameter value "String" (and the zero length
     * String) to null and forces other values to lower case.
     */
    private String nullDefaultLowerParam(String parameter)
    {
        parameter = nullDefaultParam(parameter);
        if (parameter != null)
        {
            parameter = parameter.toLowerCase();
        }
        return parameter;
    }
    
    /**
     * Changes the default JConsole parameter value "String" (and the zero length
     * String) to null.
     */
    private String nullDefaultParam(String parameter)
    {
        if ("String".equals(parameter) || "".equals(parameter) || parameter == null)
        {
            parameter = null;
        }
        return parameter;
    }

    @Override
    public String help()
    {
        return  "getProperties(listAll)\n" +
                "   Lists all transformer properties that are set.\n" +
                "   - listAll if true, list both default and custom values, otherwise includes\n" +
                "     only custom values\n" +
                "\n" +
                "setProperties(propertyNamesAndValues)\n" +
                "   Adds or replaces transformer properties.\n" +
                "   - propertyNamesAndValues to be set. May include comments but these are removed.\n" +
                "     To clear a custom values, set its value back to the default.\n" +
                "     To remove a custom property use removeProperties(...)\n" +
                "\n" +
                "getTransformationDebugLog(n)\n" +
                "   Lists the latest entries in the transformation debug log.\n" +
                "   - n the number of entries to include. If blank all available entries are listed\n" +
                "\n" +
                "getTransformationLog(n)\n" +
                "   Lists the latest entries in the transformation log.\n" +
                "   - n the number of entries to include. If blank all available entries are listed\n" +
                "\n" +
                "getTransformationStatistics(transformerName, sourceExtension, targetExtension)\n" +
                "   Lists the transformation statistics for the current node.\n" +
                "   - transformerName to be checked. If blank all transformers are included\n" +
                "   - sourceExtension to be checked. If blank all source mimetypes are included\n" +
                "   - targetExtension to be checked. If blank all target mimetypes are included\n" +
                "\n" +
                "getExtensionsAndMimetypes()\n" +
                "   Lists all configured mimetypes and the primary file extension\n" +
                "\n" +
                "getTransformerNames()\n" +
                "   Lists the names of all top level transformers\n" +
                "\n" +
                "testTransform(transformerName, sourceExtension, targetExtension, use)\n" +
                "   Transforms a small test file from one mimetype to another and then shows the \n" +
                "   debug of the transform, which would indicate if it was successful or even if \n" +
                "   it was possible.\n" +
                "   - transformerName to be used. If blank the ContentService is used to select one.\n" +
                "   - sourceExtension used to identify the mimetype\n" +
                "   - targetExtension used to identify the mimetype\n" +
                "   - use or context in which to test the transformation (\"doclib\",\n" +
                "     \"index\", \"webpreview\", \"syncRule\", \"asyncRule\"...) or blank for\n" +
                "     the default.\n" +
                "\n" +
                "removeProperties(String propertyNames)\n" +
                "   Removes transformer properties.\n" +
                "   - propertyNames to be removed. May include =<value> after the property name.\n" +
                "     The value is ignored. Only custom properties should be removed.\n" +
                "\n" +
                "getTransformationsByExtension(sourceExtension, targetExtension, use)\n" +
                "   Lists all possible transformations sorted by source and then target mimetype\n" +
                "   extension.\n" +
                "   - sourceExtension to be checked. If blank all source mimetypes are included\n" +
                "   - targetExtension to be checked. If blank all target mimetypes are included.\n" +
                "   - use or context in which the transformation will be used (\"doclib\",\n" +
                "     \"index\", \"webpreview\", \"syncRule\", \"asyncRule\"...) or blank for\n" +
                "     the default.\n" +
                "\n" +
                "getTransformationsByTransformer(transformerName, use)\n" +
                "   Lists all possible transformations sorted by Transformer name\n" +
                "   - transformerName to be checked. If blank all transformers are included\n" +
                "   - use or context in which the transformation will be used (\"doclib\",\n" +
                "     \"index\", \"webpreview\", \"syncRule\", \"asyncRule\"...) or blank for\n" +
                "     the default.\n"+
                "\n" +
                "getCustomePropertyNames()\n" +
                "   Lists custom (non default) property names\n" +
                "\n" +
                "getContextNames()\n" +
                "   Lists the names of the contexts or uses\n" +
                "\n" +
                "getTestFileExtensionsAndMimetypes()\n" +
                "   Lists the extensions of available test files";
    }
}
