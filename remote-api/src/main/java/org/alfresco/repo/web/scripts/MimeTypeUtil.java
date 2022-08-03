/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.repo.web.scripts;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class MimeTypeUtil
{

    /**
     * Get the file mimetype from the file ContentReader, and if its null then set the mimetype to binary by default
     * and try to get the correct one from file extension
     *
     *
     * @param reader            reader of the file in the request
     * @param req               request relating to the file
     * @param mimetypeService   MimetypeService
     *
     * @return  mimetype of the file as a string
     */
    public static String determineMimetype(ContentReader reader, WebScriptRequest req, MimetypeService mimetypeService)
    {
        String mimetype = reader.getMimetype();
        if (mimetype == null || mimetype.length() == 0)
        {
            String extensionPath = req.getExtensionPath();
            mimetype = MimetypeMap.MIMETYPE_BINARY;
            int extIndex = extensionPath.lastIndexOf('.');
            if (extIndex != -1)
            {
                String ext = extensionPath.substring(extIndex + 1);
                mimetype = mimetypeService.getMimetype(ext);
            }
        }
        return mimetype;
    }

}
