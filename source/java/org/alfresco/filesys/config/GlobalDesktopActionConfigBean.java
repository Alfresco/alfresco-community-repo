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
package org.alfresco.filesys.config;

// TODO: Auto-generated Javadoc
/**
 * The Class GlobalDesktopActionConfigBean.
 * 
 * @author dward
 */
public class GlobalDesktopActionConfigBean
{

    /** The no confirm. */
    private boolean noConfirm;

    /** The webpath. */
    private String webpath;

    /** The path. */
    private String path;

    /** The debug. */
    private boolean debug;

    /**
     * Checks if is no confirm.
     * 
     * @return true, if is no confirm
     */
    public boolean getNoConfirm()
    {
        return noConfirm;
    }

    /**
     * Sets the no confirm.
     * 
     * @param noConfirm
     *            the new no confirm
     */
    public void setNoConfirm(boolean noConfirm)
    {
        this.noConfirm = noConfirm;
    }

    /**
     * Gets the webpath.
     * 
     * @return the webpath
     */
    public String getWebpath()
    {
        return webpath;
    }

    /**
     * Sets the webpath.
     * 
     * @param webpath
     *            the new webpath
     */
    public void setWebpath(String webpath)
    {
        this.webpath = webpath;
    }

    /**
     * Gets the path.
     * 
     * @return the path
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Sets the path.
     * 
     * @param path
     *            the new path
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * Checks if is debug.
     * 
     * @return true, if is debug
     */
    public boolean getDebug()
    {
        return debug;
    }

    /**
     * Sets the debug.
     * 
     * @param debug
     *            the new debug
     */
    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }

}
