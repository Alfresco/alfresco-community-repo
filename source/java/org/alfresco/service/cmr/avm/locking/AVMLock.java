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
 * http://www.alfresco.com/legal/licensing
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
    public static String PATH = "path";
    public static String STORE = "store";
    public static String OWNERS = "owners";
    public static String WEBPROJECT = "webproject";
    public static String TYPE = "type";
    
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
        fPath = path;
        while (fPath.startsWith("/"))
        {
            fPath = fPath.substring(1);
        }
        while (fPath.endsWith("/"))
        {
            fPath = fPath.substring(0, fPath.length() - 1);
        }
        fPath = fPath.replaceAll("/+", "/");
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
            // The value is a dummy.
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
     * @return the Store
     */
    public String getStore()
    {
        return fStore;
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
}
