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

package org.alfresco.repo.avm.locking;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.attributes.Attribute;
import org.alfresco.repo.attributes.ListAttributeValue;
import org.alfresco.repo.attributes.MapAttributeValue;
import org.alfresco.repo.attributes.StringAttributeValue;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.attributes.AttrQueryEquals;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.avm.AVMExistsException;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.locking.AVMLock;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.util.MD5;
import org.alfresco.util.Pair;

/**
 * Implementation of the lock service.
 * @author britt
 */
public class AVMLockingServiceImpl implements AVMLockingService
{
    public static final String LOCK_TABLE = ".avm_lock_table";
    public static final String WEB_PROJECTS = "web_projects";
    public static final String USERS = "users";
    
    /**
     * AttributeService reference.
     */
    private AttributeService fAttributeService;
    
    /**
     * Transaction Helper reference.
     */
    private RetryingTransactionHelper fRetryingTransactionHelper;
    
    public AVMLockingServiceImpl()
    {
    }

    /**
     * Setter for AttributeService reference.
     * @param service
     */
    public void setAttributeService(AttributeService service)
    {
        fAttributeService = service;
    }

    /**
     * Setter for RetryingTransactionHelper reference.
     * @param helper
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper helper)
    {
        fRetryingTransactionHelper = helper;
    }
    
    public void init()
    {
        RetryingTransactionCallback callback = new RetryingTransactionCallback()
        {
            public Object execute()
            {
                Attribute table = fAttributeService.getAttribute(LOCK_TABLE);
                if (table != null)
                {
                    return null;
                }
                fAttributeService.setAttribute("", LOCK_TABLE, new MapAttributeValue());
                fAttributeService.setAttribute(LOCK_TABLE, WEB_PROJECTS, new MapAttributeValue());
                fAttributeService.setAttribute(LOCK_TABLE, USERS, new MapAttributeValue());
                return null;
            }
        };
        fRetryingTransactionHelper.doInTransaction(callback, false);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.locking.AVMLockingService#getLock(java.lang.String, java.lang.String)
     */
    public AVMLock getLock(String webProject, String path)
    {
        path = normalizePath(path);
        List<String> keys = new ArrayList<String>();
        keys.add(LOCK_TABLE);
        keys.add(WEB_PROJECTS);
        keys.add(webProject);
        List<Pair<String, Attribute>> attrs = 
            fAttributeService.query(keys, new AttrQueryEquals(MD5.Digest(path.getBytes())));
        if (attrs.size() == 0)
        {
            return null;
        }
        return new AVMLock(attrs.get(0).getSecond());
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
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.locking.AVMLockingService#getUsersLocks(java.lang.String)
     */
    public List<AVMLock> getUsersLocks(String user)
    {
        List<String> keys = new ArrayList<String>();
        keys.add(LOCK_TABLE);
        keys.add(USERS);
        keys.add(user);
        Attribute userLocks = fAttributeService.getAttribute(keys);
        List<AVMLock> locks = new ArrayList<AVMLock>();
        if (userLocks == null)
        {
            return locks;
        }
        for (Attribute entry : userLocks)
        {
            String webProject = entry.get("web_project").getStringValue();
            String path = entry.get("path").getStringValue();
            locks.add(getLock(webProject, path));
        }
        return locks;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.locking.AVMLockingService#lockPath(org.alfresco.service.cmr.avm.locking.AVMLock)
     */
    public void lockPath(AVMLock lock)
    {
        List<String> keys = new ArrayList<String>();
        Attribute lockData = lock.getAttribute();
        keys.add(LOCK_TABLE);
        keys.add(WEB_PROJECTS);
        keys.add(lock.getWebProject());
        if (fAttributeService.getAttribute(keys) == null)
        {
            throw new AVMExistsException("Lock Exists: " + keys);
        }
        fAttributeService.setAttribute(keys, MD5.Digest(lock.getPath().getBytes()), lockData);
        keys.clear();
        keys.add(LOCK_TABLE);
        keys.add(USERS);
        for (String user : lock.getOwners())
        {
            keys.add(user);
            Attribute userEntry = fAttributeService.getAttribute(keys);
            keys.remove(2);
            if (userEntry == null)
            {
                fAttributeService.setAttribute(keys, user, new ListAttributeValue());
            }
            keys.add(user);
            Attribute entry = new MapAttributeValue();
            entry.put("web_project", new StringAttributeValue(lock.getWebProject()));
            entry.put("path", new StringAttributeValue(lock.getPath()));
            fAttributeService.addAttribute(keys, entry);
            keys.remove(2);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.locking.AVMLockingService#removeLock(java.lang.String, java.lang.String)
     */
    public void removeLock(String webProject, String path)
    {
        path = normalizePath(path);
        String pathKey = MD5.Digest(path.getBytes());
        List<String> keys = new ArrayList<String>();
        keys.add(LOCK_TABLE);
        keys.add(WEB_PROJECTS);
        keys.add(webProject);
        keys.add(pathKey);
        Attribute lockData = fAttributeService.getAttribute(keys);
        if (lockData == null)
        {
            throw new AVMNotFoundException("Lock does not exist: " + webProject + " " + path);
        }
        keys.remove(3);
        fAttributeService.removeAttribute(keys, pathKey);
        AVMLock lock = new AVMLock(lockData);
        List<String> userKeys = new ArrayList<String>();
        userKeys.add(LOCK_TABLE);
        userKeys.add(USERS);
        for (String user : lock.getOwners())
        {
            userKeys.add(user);            
            Attribute userLocks = fAttributeService.getAttribute(userKeys);
            for (int i = 0; i < userLocks.size(); i++)
            {
                Attribute lockInfo = userLocks.get(i);
                if (lockInfo.get("web_project").getStringValue().equals(lock.getWebProject())
                    && lockInfo.get("path").getStringValue().equals(lock.getPath()))
                {
                    fAttributeService.removeAttribute(userKeys, i);
                    break;
                }
            }
            userKeys.remove(2);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.locking.AVMLockingService#addWebProject(java.lang.String)
     */
    public void addWebProject(String webProject)
    {
        List<String> keys = new ArrayList<String>();
        keys.add(LOCK_TABLE);
        keys.add(WEB_PROJECTS);
        keys.add(webProject);
        if (fAttributeService.getAttribute(keys) != null)
        {
            throw new AVMExistsException("Web Project Exists: " + webProject);
        }
        keys.remove(2);
        fAttributeService.setAttribute(keys, webProject, new MapAttributeValue());
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.locking.AVMLockingService#getWebProjectLocks(java.lang.String)
     */
    public List<AVMLock> getWebProjectLocks(String webProject)
    {
        List<String> keys = new ArrayList<String>();
        keys.add(LOCK_TABLE);
        keys.add(WEB_PROJECTS);
        keys.add(webProject);
        Attribute locksMap = fAttributeService.getAttribute(keys);
        List<AVMLock> result = new ArrayList<AVMLock>();
        if (locksMap != null)
        {
            for (Attribute lockData : locksMap.values())
            {
                result.add(new AVMLock(lockData));
            }
        }
        return result;        
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.locking.AVMLockingService#removeWebProject(java.lang.String)
     */
    public void removeWebProject(String webProject)
    {
        List<String> userKeys = new ArrayList<String>();
        userKeys.add(LOCK_TABLE);
        userKeys.add(USERS);
        List<String> users = fAttributeService.getKeys(userKeys);
        for (String user : users)
        {
            userKeys.add(user);
            Attribute userLocks = fAttributeService.getAttribute(userKeys);
            Iterator<Attribute> iter = userLocks.iterator();
            while (iter.hasNext())
            {
                Attribute lockInfo = iter.next();
                if (lockInfo.get("web_project").getStringValue().equals(webProject))
                {
                    iter.remove();
                }
            }
            userKeys.remove(2);
            fAttributeService.setAttribute(userKeys, user, userLocks);
        }
        List<String> keys = new ArrayList<String>();
        keys.add(LOCK_TABLE);
        keys.add(WEB_PROJECTS);
        fAttributeService.removeAttribute(keys, webProject);
    }
}
