/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.framework.metadata;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.alfresco.rest.framework.core.ResourceWithMetadata;

/**
 * Writes out the metadata for resources in the required format.  The resources will already be selected based on api version
 *
 * @author Gethin James
 */
public interface ResourceMetaDataWriter
{
    /**
     * Write the metadata to the OutputStream
     * @param out OutputStream
     * @param resource - the selected resource
     * @param allApiResources - all resources for the API version
     * @throws IOException
     */
    public void writeMetaData(OutputStream out, ResourceWithMetadata resource, Map<String, ResourceWithMetadata> allApiResources) throws IOException;
}
