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
package org.alfresco.web.scripts;

import java.util.Map;
import java.util.TreeMap;


/**
 * Basic implementation of a Web Script Path
 * 
 * Used for package & url trees.
 * 
 * @author davidc
 */
public class Path implements WebScriptPath
{
    private String path;
    private Path parent = null;
    private Map<String, Path> children = new TreeMap<String, Path>();
    private Map<String, WebScript> scripts = new TreeMap<String, WebScript>();
    

    /**
     * Helper to concatenate paths
     * 
     * @param path1
     * @param path2
     * @return  concatenated path
     */
    public static String concatPath(String path1, String path2)
    {
        return path1.equals("/") ? path1 + path2 : path1 + "/" + path2;
    }

    
    /**
     * Construct
     * 
     * @param path
     */
    public Path(String path)
    {
        this.path = path;
    }

    /**
     * Create a Child Path
     * 
     * @param path  child path name
     * @return  child path
     */
    public Path createChildPath(String path)
    {
        Path child = new Path(concatPath(this.path, path));
        child.parent = this;
        children.put(child.path, child);
        return child;
    }

    /**
     * Associate Web Script with Path
     * 
     * @param script
     */
    public void addScript(WebScript script)
    {
        scripts.put(script.getDescription().getId(), script);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptPath#getChildren()
     */
    public WebScriptPath[] getChildren()
    {
        WebScriptPath[] childrenArray = new WebScriptPath[children.size()];
        return children.values().toArray(childrenArray);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptPath#getScripts()
     */
    public WebScript[] getScripts()
    {
        WebScript[] scriptsArray = new WebScript[scripts.size()];
        return scripts.values().toArray(scriptsArray);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptPath#getName()
     */
    public String getName()
    {
        String name = "";
        int i = path.lastIndexOf("/");
        if (i != -1 && i != (path.length() -1))
        {
            name = path.substring(i + 1);
        }
        return name;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptPath#getParent()
     */
    public WebScriptPath getParent()
    {
        return parent;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptPath#getPath()
     */
    public String getPath()
    {
        return path;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return path;
    }

}
