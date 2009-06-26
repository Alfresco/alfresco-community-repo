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
package org.alfresco.repo.imap.config;

import org.springframework.beans.factory.BeanNameAware;

/**
 * Standard ImapConfig bean. 
 */
public class ImapConfigBean implements BeanNameAware
{

    /** The IMAP folder name. */
    private String name;

    /** The Alfresco store name. */
    private String store;

    /** The path within the store to the root node. */
    private String rootPath;

    /**
     * Gets the IMAP folder name.
     * 
     * @return the IMAP folder name
     */
    public String getName()
    {
        return this.name;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the Alfresco store name.
     * 
     * @return the Alfresco store name
     */
    public String getStore()
    {
        return this.store;
    }

    /**
     * Sets the Alfresco store name.
     * 
     * @param store
     *            the Alfresco store name
     */
    public void setStore(String store)
    {
        this.store = store;
    }

    /**
     * Gets the path within the store to the root node.
     * 
     * @return the path within the store to the root node
     */
    public String getRootPath()
    {
        return this.rootPath;
    }

    /**
     * Sets the path within the store to the root node.
     * 
     * @param rootPath
     *            the path within the store to the root node
     */
    public void setRootPath(String rootPath)
    {
        this.rootPath = rootPath;
    }

}
