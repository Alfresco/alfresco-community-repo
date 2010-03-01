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

package org.alfresco.service.cmr.avm.locking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.attributes.Attribute;
import org.alfresco.repo.attributes.ListAttribute;
import org.alfresco.repo.attributes.ListAttributeValue;
import org.alfresco.repo.attributes.MapAttribute;
import org.alfresco.repo.attributes.MapAttributeValue;
import org.alfresco.repo.attributes.StringAttributeValue;

/**
 * Struct representing an AVM lock.
 * @author britt
 */
public class AVMLock implements Serializable
{
    public static final String PATH = "path";
    public static final String STORE = "store";
    public static final String OWNERS = "owners";
    public static final String WEBPROJECT = "webproject";
    public static final String TYPE = "type";
    
    private static final long serialVersionUID = -8026344276097527239L;

    /**
     * The store relative path of the lock.
     */
    private String fPath;
    
    /**
     * The store of the actual locked version.
     */
    private String fStore;
    
    /**
     * The list of users who can access the locked asset.
     */
    private List<String> fOwners;
    
    /**
     * The web project for which this lock applies.
     */
    private String fWebProject;
    
    /**
     * The type of the lock.
     */
    private AVMLockingService.Type fType;
    
    public AVMLock(String webProject,
                   String store,
                   String path,
                   AVMLockingService.Type type,
                   List<String> owners)
    {
        fWebProject = webProject;
        fStore = store;
        fPath = normalizePath(path);
        fType = type;
        fOwners = owners;
    }
                   
    public AVMLock(Attribute lockData)
    {
        fPath = lockData.get(PATH).getStringValue();
        fStore = lockData.get(STORE).getStringValue();
        fOwners = new ArrayList<String>();
        for (Attribute owner : lockData.get(OWNERS))
        {
            fOwners.add(owner.getStringValue());
        }
        fType = AVMLockingService.Type.valueOf(lockData.get(TYPE).getStringValue());
        fWebProject = lockData.get(WEBPROJECT).getStringValue();
    }

    public Attribute getAttribute()
    {
        MapAttribute lockData = new MapAttributeValue();
        lockData.put(PATH, new StringAttributeValue(fPath));
        lockData.put(STORE, new StringAttributeValue(fStore));
        lockData.put(TYPE, new StringAttributeValue(fType.name()));
        lockData.put(WEBPROJECT, new StringAttributeValue(fWebProject));
        ListAttribute owners = new ListAttributeValue();
        for (String owner : fOwners)
        {
            owners.add(new StringAttributeValue(owner));
        }
        lockData.put(OWNERS, owners);
        return lockData;
    }
    
    /**
     * @return the owners
     */
    public List<String> getOwners()
    {
        return fOwners;
    }

    /**
     * @return the Path
     */
    public String getPath()
    {
        return fPath;
    }
    
    /**
     * Set the path of this lock.
     * @param path
     */
    public void setPath(String path)
    {
        fPath = normalizePath(path);
    }

    /**
     * @return the Store
     */
    public String getStore()
    {
        return fStore;
    }
    
    /**
     * Set the store of this lock.
     * @param store
     */
    public void setStore(String store)
    {
        fStore = store;
    }

    /**
     * @return the Type
     */
    public AVMLockingService.Type getType()
    {
        return fType;
    }

    /**
     * @return the WebProject
     */
    public String getWebProject()
    {
        return fWebProject;
    }
    
    public String toString()
    {
       StringBuilder buffer = new StringBuilder();
       buffer.append(" (webproject=").append(this.fWebProject);
       buffer.append(" store=").append(this.fStore);
       buffer.append(" path=").append(this.fPath);
       buffer.append(" type=").append(this.fType);
       buffer.append(" owners=").append(this.fOwners).append(")");
       return buffer.toString();
    }
    
    /**
     * Utility to get relative paths into canonical form.
     * @param path The incoming path.
     * @return The normalized path.
     */
    private String normalizePath(String path)
    {
        while (path.startsWith("/"))
        {
            path = path.substring(1);
        }
        while (path.endsWith("/"))
        {
            path = path.substring(0, path.length() - 1);
        }
        return path.replaceAll("/+", "/");
    }
}
