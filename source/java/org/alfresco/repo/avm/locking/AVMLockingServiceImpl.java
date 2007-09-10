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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.attributes.Attribute;
import org.alfresco.repo.attributes.MapAttributeValue;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.attributes.AttrQueryEquals;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMExistsException;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.locking.AVMLock;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.MD5;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the lock service.
 * @author britt
 */
public class AVMLockingServiceImpl implements AVMLockingService
{
    public static final String LOCK_TABLE = ".avm_lock_table";
    public static final String WEB_PROJECTS = "web_projects";
    public static final String USERS = "users";
    public static final String STORES = "stores";
    
    private static final String ROLE_CONTENT_MANAGER = "ContentManager";
    
    private static final Log logger = LogFactory.getLog(AVMLockingServiceImpl.class);
    
    /**
     * Store name containing the web project nodes.
     */
    private String webProjectStore;
    
    /**
     * SearchService for access to web project properties.
     */
    private SearchService fSearchService;
    
    /**
     * AttributeService reference.
     */
    private AttributeService fAttributeService;
    
    /**
     * AuthorityService reference.
     */
    private AuthorityService fAuthorityService;
    
    /**
     * PersonService reference.
     */
    private PersonService fPersonService;

    /**
     * The NodeService.
     */
    private NodeService fNodeService;
    
    /**
     * Transaction Helper reference.
     */
    private RetryingTransactionHelper fRetryingTransactionHelper;
    
    
    /**
     * @param webProjectStore The webProjectStore to set
     */
    public void setWebProjectStore(String webProjectStore)
    {
        this.webProjectStore = webProjectStore;
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
     * Set the authority service reference.
     * @param service
     */
    public void setAuthorityService(AuthorityService service)
    {
        fAuthorityService = service;
    }
    
    /**
     * Set the person service reference.
     * @param service
     */
    public void setPersonService(PersonService service)
    {
        fPersonService = service;
    }
    
    /**
     * Setter for RetryingTransactionHelper reference.
     * @param helper
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper helper)
    {
        fRetryingTransactionHelper = helper;
    }
    
    public void setSearchService(SearchService service)
    {
        fSearchService = service;
    }

    public void setNodeService(NodeService service)
    {
        fNodeService = service;
    }
    
    public void init()
    {
        RetryingTransactionHelper.RetryingTransactionCallback<Object> callback = 
            new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute()
            {
                if (!fAttributeService.exists(LOCK_TABLE))
                {
                    fAttributeService.setAttribute("", LOCK_TABLE, new MapAttributeValue());
                }
                if (!fAttributeService.exists(LOCK_TABLE + '/' + WEB_PROJECTS))
                {
                    fAttributeService.setAttribute(LOCK_TABLE, WEB_PROJECTS, new MapAttributeValue());
                }
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
        List<String> keys = new ArrayList<String>(3);
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
//        List<String> keys = new ArrayList<String>(3);
//        keys.add(LOCK_TABLE);
//        keys.add(USERS);
//        keys.add(user);
//        Attribute userLocks = fAttributeService.getAttribute(keys);
//        List<AVMLock> locks = new ArrayList<AVMLock>();
//        if (userLocks == null)
//        {
//            return locks;
//        }
//        for (Attribute entry : userLocks)
//        {
//            String webProject = entry.get("web_project").getStringValue();
//            String path = entry.get("path").getStringValue();
//            locks.add(getLock(webProject, path));
//        }
//        return locks;
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.locking.AVMLockingService#lockPath(org.alfresco.service.cmr.avm.locking.AVMLock)
     */
    public void lockPath(AVMLock lock)
    {
        for (String authority : lock.getOwners())
        {
            if (!fAuthorityService.authorityExists(authority) &&
                !fPersonService.personExists(authority))
            {
                throw new AVMBadArgumentException("Not an Authority: " + authority);
            }
        }
        List<String> keys = new ArrayList<String>();
        Attribute lockData = lock.getAttribute();
        keys.add(LOCK_TABLE);
        keys.add(WEB_PROJECTS);
        keys.add(lock.getWebProject());
        String digest = MD5.Digest(lock.getPath().getBytes());
        keys.add(digest);
        if (fAttributeService.getAttribute(keys) != null)
        {
            throw new AVMExistsException("Lock Exists: " + keys);
        }
        keys.remove(3);
        fAttributeService.setAttribute(keys, digest, lockData);
//        Attribute reverseEntry = new MapAttributeValue();
//        reverseEntry.put("web_project", new StringAttributeValue(lock.getWebProject()));
//        reverseEntry.put("path", new StringAttributeValue(lock.getPath()));        
//        keys.clear();
//        keys.add(LOCK_TABLE);
//        keys.add(USERS);
//        for (String user : lock.getOwners())
//        {
//            keys.add(user);
//            Attribute userEntry = fAttributeService.getAttribute(keys);
//            keys.remove(2);
//            if (userEntry == null)
//            {
//                fAttributeService.setAttribute(keys, user, new ListAttributeValue());
//            }
//            keys.add(user);
//            fAttributeService.addAttribute(keys, reverseEntry);
//            keys.remove(2);
//        }
//        String store = lock.getStore();
//        keys.clear();
//        keys.add(LOCK_TABLE);
//        keys.add(STORES);
//        keys.add(store);
//        Attribute storeEntry = fAttributeService.getAttribute(keys);
//        keys.remove(2);
//        if (storeEntry == null)
//        {
//            fAttributeService.setAttribute(keys, store, new ListAttributeValue());
//        }
//        keys.add(store);
//        fAttributeService.addAttribute(keys, reverseEntry);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.locking.AVMLockingService#removeLock(java.lang.String, java.lang.String)
     */
    public void removeLock(String webProject, String path)
    {
        path = normalizePath(path);
        String pathKey = MD5.Digest(path.getBytes());
        List<String> keys = new ArrayList<String>(4);
        keys.add(LOCK_TABLE);
        keys.add(WEB_PROJECTS);
        keys.add(webProject);
        keys.add(pathKey);
        Attribute lockData = fAttributeService.getAttribute(keys);
        if (lockData == null)
        {
            return;
        }
        keys.remove(3);
        fAttributeService.removeAttribute(keys, pathKey);
//        AVMLock lock = new AVMLock(lockData);
//        List<String> userKeys = new ArrayList<String>();
//        userKeys.add(LOCK_TABLE);
//        userKeys.add(USERS);
//        for (String user : lock.getOwners())
//        {
//            userKeys.add(user);            
//            Attribute userLocks = fAttributeService.getAttribute(userKeys);
//            for (int i = userLocks.size() - 1; i >= 0; i--)
//            {
//                Attribute lockInfo = userLocks.get(i);
//                if (lockInfo.get("web_project").getStringValue().equals(lock.getWebProject())
//                    && lockInfo.get("path").getStringValue().equals(lock.getPath()))
//                {
//                    fAttributeService.removeAttribute(userKeys, i);
//                    break;
//                }
//            }
//            userKeys.remove(2);
//        }
//        List<String> storeKeys = new ArrayList<String>(3);
//        storeKeys.add(LOCK_TABLE);
//        storeKeys.add(STORES);
//        String store = lock.getStore();
//        storeKeys.add(store);
//        Attribute storeLocks = fAttributeService.getAttribute(storeKeys);
//        for (int i = storeLocks.size() - 1; i >= 0; i--)
//        {
//            Attribute lockInfo = storeLocks.get(i);
//            if (lockInfo.get("web_project").getStringValue().equals(lock.getWebProject()) &&
//                lockInfo.get("path").getStringValue().equals(lock.getPath()))
//            {
//                fAttributeService.removeAttribute(storeKeys, i);
//                break;
//            }
//        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.locking.AVMLockingService#addWebProject(java.lang.String)
     */
    public void addWebProject(String webProject)
    {
        List<String> keys = new ArrayList<String>(3);
        keys.add(LOCK_TABLE);
        keys.add(WEB_PROJECTS);
        keys.add(webProject);
        if (fAttributeService.exists(keys))
        {
            return;
        }
        keys.remove(2);
        fAttributeService.setAttribute(keys, webProject, new MapAttributeValue());
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.locking.AVMLockingService#getWebProjectLocks(java.lang.String)
     */
    public List<AVMLock> getWebProjectLocks(String webProject)
    {
        List<String> keys = new ArrayList<String>(3);
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
//        List<String> userKeys = new ArrayList<String>(2);
//        userKeys.add(LOCK_TABLE);
//        userKeys.add(USERS);
//        List<String> users = fAttributeService.getKeys(userKeys);
//        // TODO This works incredibly slowly. AttributeService has to support
//        // extended querying on values.
//        for (String user : users)
//        {
//            userKeys.add(user);
//            Attribute userLocks = fAttributeService.getAttribute(userKeys);
//            Iterator<Attribute> iter = userLocks.iterator();
//            while (iter.hasNext())
//            {
//                Attribute lockInfo = iter.next();
//                if (lockInfo.get("web_project").getStringValue().equals(webProject))
//                {
//                    iter.remove();
//                }
//            }
//            userKeys.remove(2);
//            fAttributeService.setAttribute(userKeys, user, userLocks);
//        }
//        List<String> storeKeys = new ArrayList<String>();
//        storeKeys.add(LOCK_TABLE);
//        storeKeys.add(STORES);
//        List<String> stores = fAttributeService.getKeys(storeKeys);
//        // TODO Ditto.
//        for (String store : stores)
//        {
//            storeKeys.add(store);
//            Attribute storeLocks = fAttributeService.getAttribute(storeKeys);
//            Iterator<Attribute> iter = storeLocks.iterator();
//            while (iter.hasNext())
//            {
//                Attribute lockInfo = iter.next();
//                if (lockInfo.get("web_project").getStringValue().equals(webProject))
//                {
//                    iter.remove();
//                }
//            }
//            storeKeys.remove(2);
//            fAttributeService.setAttribute(storeKeys, store, storeLocks);
//        }
        List<String> keys = new ArrayList<String>(2);
        keys.add(LOCK_TABLE);
        keys.add(WEB_PROJECTS);
        fAttributeService.removeAttribute(keys, webProject);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.locking.AVMLockingService#getStoreLocks(java.lang.String)
     */
    public List<AVMLock> getStoreLocks(String store)
    {
        return null;
//        List<AVMLock> locks = new ArrayList<AVMLock>(3);
//        List<String> keys = new ArrayList<String>();
//        keys.add(LOCK_TABLE);
//        keys.add(STORES);
//        keys.add(store);
//        List<String> lockKeys = new ArrayList<String>();
//        lockKeys.add(LOCK_TABLE);
//        lockKeys.add(WEB_PROJECTS);
//        Attribute storeLocks = fAttributeService.getAttribute(keys);
//        for (Attribute lockInfo : storeLocks)
//        {
//            String webProject = lockInfo.get("web_project").getStringValue();
//            String path = lockInfo.get("path").getStringValue();
//            lockKeys.add(webProject);
//            lockKeys.add(MD5.Digest(path.getBytes()));
//            Attribute lockData = fAttributeService.getAttribute(lockKeys);
//            locks.add(new AVMLock(lockData));
//            lockKeys.remove(3);
//            lockKeys.remove(2);
//        }
//        return locks;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.locking.AVMLockingService#modifyLock(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List, java.util.List)
     */
    public void modifyLock(String webProject, String path, String newPath, String newStore, List<String> usersToRemove, List<String> usersToAdd)
    {
        AVMLock lock = getLock(webProject, path);
        if (lock == null)
        {
            throw new AVMNotFoundException("Lock not found for " + webProject + ":" + path);
        }
        removeLock(webProject, path);
        if (newPath != null)
        {
            lock.setPath(newPath);
        }
        if (newStore != null)
        {
            lock.setStore(newStore);
        }
        if (usersToRemove != null)
        {
            for (String user : usersToRemove)
            {
                lock.getOwners().remove(user);
            }
        }
        if (usersToAdd != null)
        {
            for (String user : usersToAdd)
            {
                if (!fAuthorityService.authorityExists(user) &&
                    !fPersonService.personExists(user))
                {
                    throw new AVMBadArgumentException("Not an authority: " + user);
                }
                if (lock.getOwners().contains(user))
                {
                    continue;
                }
                lock.getOwners().add(user);
            }
        }
        lockPath(lock);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.locking.AVMLockingService#removeStoreLocks(java.lang.String)
     */
    public void removeStoreLocks(String store)
    {
        String webProject = store;
        int index = store.indexOf("--");
        if (index >= 0)
        {
            webProject = store.substring(0, index);   
        }
        List<String> keys = new ArrayList<String>(3);
        keys.add(LOCK_TABLE);
        keys.add(WEB_PROJECTS);
        keys.add(webProject);
        Attribute project = fAttributeService.getAttribute(keys);
        if (project == null)
        {
            return;
        }
        for (Map.Entry<String, Attribute>  entry: project.entrySet())
        {
            AVMLock lock = new AVMLock(entry.getValue());
            if (lock.getStore().equals(store))
            {
                project.remove(entry.getKey());
            }
        }
        keys.remove(2);
        fAttributeService.setAttribute(keys, webProject, project);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.locking.AVMLockingService#hasAccess(java.lang.String, java.lang.String)
     */
    public boolean hasAccess(String webProject, String avmPath, String user)
    {
        if (fPersonService.getPerson(user) == null &&
            !fAuthorityService.authorityExists(user))
        {
            return false;
        }
        if (fAuthorityService.isAdminAuthority(user))
        {
            return true;
        }
        StoreRef storeRef = new StoreRef(this.webProjectStore);
        ResultSet results = fSearchService.query(
                storeRef,
                SearchService.LANGUAGE_LUCENE,
                "@wca\\:avmstore:\"" + webProject + '"');
        if (results.getNodeRefs().size() == 1)
        {
            return hasAccess(webProject, results.getNodeRefs().get(0), avmPath, user);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.locking.AVMLockingService#hasAccess(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
     */
    public boolean hasAccess(NodeRef webProjectRef, String avmPath, String user)
    {
        if (fPersonService.getPerson(user) == null &&
            !fAuthorityService.authorityExists(user))
        {
            return false;
        }
        if (fAuthorityService.isAdminAuthority(user))
        {
            return true;
        }
        String webProject = (String)fNodeService.getProperty(webProjectRef, WCMAppModel.PROP_AVMSTORE);
        return hasAccess(webProject, webProjectRef, avmPath, user);
    }

    private boolean hasAccess(String webProject, NodeRef webProjectRef, String avmPath, String user)
    {
        String[] storePath = avmPath.split(":");
        if (storePath.length != 2)
        {
            throw new AVMBadArgumentException("Malformed AVM Path : " + avmPath);
        }
        
        if (logger.isDebugEnabled())
           logger.debug("Testing lock access on path: " + avmPath + " for user: " + user + " in webproject: " + webProject);
        
        // check if a lock exists at all for this path in the specified webproject id
        String path = normalizePath(storePath[1]);
        AVMLock lock = getLock(webProject, path);
        if (lock == null)
        {
            if (logger.isDebugEnabled())
                logger.debug(" GRANTED: No lock found.");
            return true;
        }
        
        // locks are ignored in a workflow store
        if (storePath[0].contains("--workflow"))
        {
            if (logger.isDebugEnabled())
                logger.debug(" GRANTED: Workflow store path.");
            return true;
        }
        
        // locks are specific to a store - no access if the stores are different
        if (!lock.getStore().equals(storePath[0]))
        {
            if (logger.isDebugEnabled())
                logger.debug(" DENIED: Store on path and lock (" + lock.getStore().toString() + ") do not match.");
            return false;
        }
        
        // check for content manager role - we allow access to all managers within the same store
        List<ChildAssociationRef> children = fNodeService.getChildAssocs(
                webProjectRef, WCMAppModel.ASSOC_WEBUSER, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef child : children)
        {
            NodeRef childRef = child.getChildRef();
            if (fNodeService.getProperty(childRef, WCMAppModel.PROP_WEBUSERNAME).equals(user) &&
                fNodeService.getProperty(childRef, WCMAppModel.PROP_WEBUSERROLE).equals(ROLE_CONTENT_MANAGER))
            {
                if (logger.isDebugEnabled())
                    logger.debug(" GRANTED: Store match and user is ContentManager role in webproject.");
                return true;
            }
        }
        
        // finally check the owners of the lock against the specified authority
        List<String> owners = lock.getOwners();
        for (String owner : owners)
        {
            if (AuthorityType.getAuthorityType(owner) == AuthorityType.EVERYONE)
            {
                if (logger.isDebugEnabled())
                    logger.debug(" GRANTED: Authority EVERYONE matched lock owner.");
                return true;
            }
            if (checkAgainstAuthority(user, owner))
            {
                if (logger.isDebugEnabled())
                    logger.debug(" GRANTED: User matched as lock owner.");
                return true;
            }
        }
        
        if (logger.isDebugEnabled())
            logger.debug(" DENIED: User did not match as lock owner.");
        return false;
    }
    
    /**
     * Helper function that checks the transitive closure of authorities for user.
     * @param user
     * @param authority
     * @return
     */
    private boolean checkAgainstAuthority(String user, String authority)
    {
        if (user.equalsIgnoreCase(authority))
        {
            return true;
        }
        Set<String> containing = fAuthorityService.getContainingAuthorities(null, user, false);
        for (String parent : containing)
        {
            if (parent.equalsIgnoreCase(authority))
            {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avm.locking.AVMLockingService#getWebProjects()
     */
    public List<String> getWebProjects()
    {
        List<String> keys = new ArrayList<String>(2);
        keys.add(LOCK_TABLE);
        keys.add(WEB_PROJECTS);
        return fAttributeService.getKeys(keys);
    }
}
