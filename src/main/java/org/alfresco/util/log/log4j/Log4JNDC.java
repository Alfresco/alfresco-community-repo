/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.util.log.log4j;

import org.alfresco.util.log.NDCDelegate;
import org.apache.log4j.NDC;

/**
 * A stand in for the org.apache.log4j.NDC class that avoids introducing runtime dependencies against the otherwise
 * optional log4j.
 * 
 * @author dward
 */
public class Log4JNDC implements NDCDelegate
{
    // Force resolution of the log4j NDC class by the classloader (thus forcing an error if unavailable)
    @SuppressWarnings("unused")
    private static final Class<?> NDC_REF = NDC.class;

    /**
     * Push new diagnostic context information for the current thread.
     * 
     * @param message
     *            The new diagnostic context information.
     */
    public void push(String message)
    {
        NDC.push(message);
    }

    /**
     * Remove the diagnostic context for this thread.
     */
    public void remove()
    {
        NDC.remove();
    }
}
