/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.cmr.repository;

import java.io.Serializable;

/**
 * Interface contract for the conversion of file name to a fully qualified icon image path for use by
 * templating and scripting engines executing within the repository context.
 * <p>
 * Generally this contract will be implemented by classes that have access to say the webserver
 * context which can be used to generate an icon image for a specific filename.
 * 
 * @author Kevin Roast
 */
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
