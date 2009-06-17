/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.util;

import java.io.File;
import java.io.IOException;

/**
 * A class that attempts to embody OpenOffice's rules for encoding file URIs which appear to differ from Java's. A
 * Windows style path is always prefixed "file:///" whereas a unix one is prefixed "file://".
 * 
 * @author dward
 */
public class OpenOfficeURI
{

    /** The source file. */
    private File source;

    /**
     * Instantiates a new open office URI.
     * 
     * @param source
     *            the source file to convert to a URI
     */
    public OpenOfficeURI(File source)
    {
        this.source = source;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        String absolute;
        try
        {
            absolute = this.source.getCanonicalPath();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        if (File.separatorChar != '/')
        {
            absolute = absolute.replace(File.separatorChar, '/');
        }
        return (absolute.startsWith("/") ? "file://" : "file:///") + absolute;
    }

}
