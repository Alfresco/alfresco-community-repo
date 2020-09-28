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
package org.alfresco.service.cmr.repository;

import java.io.Serializable;

import org.alfresco.api.AlfrescoPublicApi;   

/**
 * Interface contract for the conversion of file name to a fully qualified icon image path for use by
 * templating and scripting engines executing within the repository context.
 * <p>
 * Generally this contract will be implemented by classes that have access to say the webserver
 * context which can be used to generate an icon image for a specific filename.
 * 
 * @author Kevin Roast
 */
@AlfrescoPublicApi
public interface TemplateImageResolver extends Serializable
{
    /**
     * Resolve the qualified icon image path for the specified filename 
     * 
     * @param filename      The file name to resolve image path for
     * @param size          Enum representing the size of the image to retrieve
     * 
     * @return image path for the specified filename and image size
     */
    public String resolveImagePathForName(String filename, FileTypeImageSize size);
}
