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

/**
 * A management interface for monitoring Content Transformer configuration
 * and statistics.
 * 
 * @author Alan Davis
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public interface TransformerConfigMBean
{
    /**
     * Lists the names of all top level transformers.
     */
    public String[] getTransformerNames();

    /**
     * Lists all configured mimetypes, proceeded by its primary file extension.
     */
    public String[] getExtensionsAndMimetypes();

    /**
     * Lists all possible transformations sorted by Transformer name.
     * @param transformerName to be checked. If null all transformers are included.
     * @param use or context in which the transformation will be used ("doclib",
     *        "index", "webpreview", "syncRule", "asyncRule"...) or null for the default.
     */
    @Deprecated
    public String getTransformationsByTransformer(String transformerName, String use);

    /**
     * Lists all possible transformations sorted by source and then target mimetype extension.
     * @param sourceExtension to be checked. If null all source mimetypes are included.
     * @param targetExtension to be checked. If null all target mimetypes are included.
     * @param use or context in which the transformation will be used ("doclib",
     *        "index", "webpreview", "syncRule", "asyncRule"...) or null for the default.
     */
    public String getTransformationsByExtension(String sourceExtension, String targetExtension, String use);
    
    /**
     * Lists the transformation statistics for the current node.
     * @param transformerName to be checked. If null all transformers are included.
     * @param sourceExtension to be checked. If null all source mimetypes are included.
     * @param targetExtension to be checked. If null all target mimetypes are included.
     */
    @Deprecated
    public String getTransformationStatistics(String transformerName, String sourceExtension, String targetExtension);
    
    /**
     * Returns the last n entries in the transformation log.
     */
    public String[] getTransformationLog(int n);
    
    /**
     * Returns the last n entries in the transformation debug log.
     */
    public String[] getTransformationDebugLog(int n);
    
    /**
     * Returns custom and default transformer propertiest.
     * @param listAll list both default and custom values, otherwise includes
     *        only custom values.
     */
    @Deprecated
    public String getProperties(boolean listAll);
    
    /**
     * Adds or replaces new transformer properties.
     * @param propertyNamesAndValues
     * @return a confirmation or failure message
     */
    @Deprecated
    public String setProperties(String propertyNamesAndValues);
    
    /**
     * Removes transformer properties.
     * @param propertyNames to be removed. Any values after the property name are ignored.
     * @return a confirmation or failure message
     */
    @Deprecated
    String removeProperties(String propertyNames);
    
    /**
     * Transforms a small test file from one mimetype to another and then shows the debug of the
     * transform, which would indicate if it was successful or even if it was possible.
     * @param transformerName to be used. If not specified the ContentService is used to select one.
     * @param sourceExtension used to identify the mimetype
     * @param targetExtension used to identify the mimetype
     * @param use or context in which to test the transformation ("doclib", "index", "webpreview",
     *        "syncRule", "asyncRule"...) or blank for the default.";
     * @return Text indicating if the transform was possible and any debug
     */
    public String testTransform(String transformerName, String sourceExtension, String targetExtension, String use);
    
    /**
     * Lists the names of the contexts or uses.
     */
    public String[] getContextNames();

    /**
     * Lists custom (non default) property names.
     */
    public String[] getCustomePropertyNames();
    
    /**
     * Lists the extensions of available test files.
     */
    public String[] getTestFileExtensionsAndMimetypes();
    
    /**
     * Returns a description of each method and its parameters.
     */
    public String help();
}
