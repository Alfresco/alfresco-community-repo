/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
