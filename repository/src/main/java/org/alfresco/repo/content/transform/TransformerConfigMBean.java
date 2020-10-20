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
 */
public interface TransformerConfigMBean
{
    /**
     * Lists all configured mimetypes, proceeded by its primary file extension.
     */
    public String[] getExtensionsAndMimetypes();

    /**
     * Lists all possible transformations sorted by source and then target mimetype extension.
     * @param sourceExtension to be checked. If null all source mimetypes are included.
     * @param targetExtension to be checked. If null all target mimetypes are included.
     */
    public String getTransformationsByExtension(String sourceExtension, String targetExtension);
    
    /**
     * Returns the last n entries in the transformation log.
     */
    public String[] getTransformationLog(int n);
    
    /**
     * Returns the last n entries in the transformation debug log.
     */
    public String[] getTransformationDebugLog(int n);
    
    /**
     * Transforms a small test file from one mimetype to another and then shows the debug of the
     * transform, which would indicate if it was successful or even if it was possible.
     * @param sourceExtension used to identify the mimetype
     * @param targetExtension used to identify the mimetype
     * @return Text indicating if the transform was possible and any debug
     */
    public String testTransform(String sourceExtension, String targetExtension);
    
    /**
     * Lists the extensions of available test files.
     */
    public String[] getTestFileExtensionsAndMimetypes();
    
    /**
     * Returns a description of each method and its parameters.
     */
    public String help();
}
