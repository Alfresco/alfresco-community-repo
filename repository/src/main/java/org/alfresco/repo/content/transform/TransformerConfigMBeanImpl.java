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

import java.util.ArrayList;
import java.util.List;

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
    private AdminUiTransformerDebug transformerDebug;
    private MimetypeService mimetypeService;
    private LogEntries transformerLog;
    private LogEntries transformerDebugLog;
    
    public void setTransformerDebug(AdminUiTransformerDebug transformerDebug)
    {
        this.transformerDebug = transformerDebug;
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
    public String getTransformationsByExtension(String sourceExtension, String targetExtension)
    {
        try
        {
            return transformerDebug.transformationsByExtension(nullDefaultLowerParam(sourceExtension),
                    nullDefaultLowerParam(targetExtension), true);
        }
        catch (IllegalArgumentException e)
        {
            return e.getMessage();
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
    public String testTransform(String sourceExtension, String targetExtension)
    {
        try
        {
            return transformerDebug.testTransform(sourceExtension, targetExtension);
        }
        catch (IllegalArgumentException e)
        {
            return e.getMessage();
        }
    }
    
    @Override
    public String[] getTestFileExtensionsAndMimetypes()
    {
        return transformerDebug.getTestFileExtensionsAndMimetypes();
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
        return  "getTransformationDebugLog(n)\n" +
                "   Lists the latest entries in the transformation debug log.\n" +
                "   - n the number of entries to include. If blank all available entries are listed\n" +
                "\n" +
                "getTransformationLog(n)\n" +
                "   Lists the latest entries in the transformation log.\n" +
                "   - n the number of entries to include. If blank all available entries are listed\n" +
                "\n" +
                "getExtensionsAndMimetypes()\n" +
                "   Lists all configured mimetypes and the primary file extension\n" +
                "\n" +
                "testTransform(sourceExtension, targetExtension, use)\n" +
                "   Transforms a small test file from one mimetype to another and then shows the \n" +
                "   debug of the transform, which would indicate if it was successful or even if \n" +
                "   it was possible.\n" +
                "   - sourceExtension used to identify the mimetype\n" +
                "   - targetExtension used to identify the mimetype\n" +
                "   - use or context in which to test the transformation (\"doclib\",\n" +
                "     \"index\", \"webpreview\", \"syncRule\", \"asyncRule\"...) or blank for\n" +
                "     the default.\n" +
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
                "getTestFileExtensionsAndMimetypes()\n" +
                "   Lists the extensions of available test files";
    }
}
