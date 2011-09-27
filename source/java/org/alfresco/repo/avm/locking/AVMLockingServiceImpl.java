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

package org.alfresco.repo.avm.locking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.domain.avm.AVMLockDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.attributes.DuplicateAttributeException;
import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.locking.AVMLockingException;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ParameterCheck;
import org.alfresco.wcm.util.WCMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the lock service.
 * 
 * @author Derek Hulley, janv
 */
public class AVMLockingServiceImpl implements AVMLockingService
{
    public static final String KEY_AVM_LOCKS = ".avm_locks";
    public static final String KEY_LOCK_OWNER = "lock-owner";

    private static final String ROLE_CONTENT_MANAGER = "ContentManager";

    private static final Log logger = LogFactory.getLog(AVMLockingServiceImpl.class);

    private String webProjectStore;
    private SearchService searchService;
    private AttributeService attributeService;
    private AuthorityService authorityService;
    private PersonService personService;
    private NodeService nodeService;
    
    private AVMLockDAO avmLockDAO;

    /**
     * @param webProjectStore The webProjectStore to set
     */
    public void setWebProjectStore(String webProjectStore)
    {
        this.webProjectStore = webProjectStore;
    }

    /**
     * @param attributeService                  the service to persist attributes
     */
    public void setAttributeService(AttributeService attributeService)
    {
        this.attributeService = attributeService;
    }

    /**
     * @param authorityService                  the service to check validity of usernames
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * @param personService                     checks validity of person names
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setAvmLockDAO(AVMLockDAO avmLockDAO)
    {
        this.avmLockDAO = avmLockDAO;
    }
    
    /**
     * Appends the lock owner to the lock data.
     */
    private HashMap<String, String> createLockAttributes(String lockOwner, Map<String, String> lockData)
    {
        HashMap<String, String> lockAttributes = new HashMap<String, String>(lockData);
        lockAttributes.put(KEY_LOCK_OWNER, lockOwner);
        return lockAttributes;
    }

    /**
     * {@inheritDoc}
     */
    public void lock(String avmStore, String path, String lockOwner, Map<String, String> lockData)
    {
        ParameterCheck.mandatoryString("avmStore", avmStore);
        ParameterCheck.mandatoryString("path", path);
        ParameterCheck.mandatoryString("lockOwner", lockOwner);
        path = AVMLockingServiceImpl.normalizePath(path);
        
        if (!authorityService.authorityExists(lockOwner) &&
            !personService.personExists(lockOwner))
        {
            throw new AVMBadArgumentException("Not an Authority: " + lockOwner);
        }
        
        LockState lockState = getLockState(avmStore, path, lockOwner);
        switch (lockState)
        {
        case LOCK_NOT_OWNER:
            throw new AVMLockingException("avmlockservice.locked", path, lockOwner);
        case NO_LOCK:
            // Lock it, assuming that the lock doesn't exist (concurrency-safe).
            try
            {
                HashMap<String, String> lockAttributes = createLockAttributes(lockOwner, lockData);
                attributeService.createAttribute(
                        lockAttributes,
                        KEY_AVM_LOCKS, avmStore, path);
            }
            catch (DuplicateAttributeException e)
            {
                String currentLockOwner = getLockOwner(avmStore, path);
                // Should trigger a retry, hence we pass the exception out
                throw new AVMLockingException(e, "avmlockservice.locked", path, currentLockOwner);
            }
            break;
        case LOCK_OWNER:
            // Nothing to do
            break;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean modifyLock(
            String avmStore, String path, String lockOwner,
            String newAvmStore, String newPath,
            Map<String, String> lockData)
    {
        ParameterCheck.mandatoryString("avmStore", avmStore);
        ParameterCheck.mandatoryString("path", path);
        ParameterCheck.mandatoryString("lockOwner", lockOwner);
        ParameterCheck.mandatoryString("newAvmStore", newAvmStore);
        ParameterCheck.mandatoryString("newPath", newPath);
        path = AVMLockingServiceImpl.normalizePath(path);
        newPath = AVMLockingServiceImpl.normalizePath(newPath);

        LockState currentLockState = getLockState(avmStore, path, lockOwner);
        switch (currentLockState)
        {
        case LOCK_NOT_OWNER:
        case LOCK_OWNER:
            // Remove the lock first
            attributeService.removeAttribute(KEY_AVM_LOCKS, avmStore, path);
            HashMap<String, String> lockAttributes = createLockAttributes(lockOwner, lockData);
            attributeService.setAttribute(
                    lockAttributes,
                    KEY_AVM_LOCKS, newAvmStore, newPath);
            return true;
        case NO_LOCK:
            // Do nothing
            return false;
        default:
            throw new IllegalStateException("Unexpected enum constant");
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getLockOwner(String avmStore, String path)
    {
        ParameterCheck.mandatoryString("path", path);
        path = AVMLockingServiceImpl.normalizePath(path);

        Map<String, String> lockAttributes = getLockData(avmStore, path);
        if (lockAttributes == null)
        {
            return null;
        }
        else if (!lockAttributes.containsKey(KEY_LOCK_OWNER))
        {
            logger.warn("AVM lock does not have a lock owner: " + avmStore + "-" + path);
            return null;
        }
        return lockAttributes.get(KEY_LOCK_OWNER);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getLockData(String avmStore, String path)
    {
        ParameterCheck.mandatoryString("avmStore", avmStore);
        ParameterCheck.mandatoryString("path", path);
        path = AVMLockingServiceImpl.normalizePath(path);

        Map<String, String> lockAttributes = (Map<String, String>) attributeService.getAttribute(
                KEY_AVM_LOCKS, avmStore, path);
        return lockAttributes;
    }

    /**
     * {@inheritDoc}
     */
    public LockState getLockState(String avmStore, String path, String lockOwner)
    {
        ParameterCheck.mandatoryString("avmStore", avmStore);
        ParameterCheck.mandatoryString("lockOwner", lockOwner);
        path = AVMLockingServiceImpl.normalizePath(path);
        
        String currentLockOwner = getLockOwner(avmStore, path);
        if (currentLockOwner == null)
        {
            return LockState.NO_LOCK;
        }
        else if (currentLockOwner.equals(lockOwner))
        {
            return LockState.LOCK_OWNER;
        }
        else
        {
            return LockState.LOCK_NOT_OWNER;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeLock(String avmStore, String path)
    {
        ParameterCheck.mandatoryString("avmStore", avmStore);
        ParameterCheck.mandatoryString("path", path);
        path = AVMLockingServiceImpl.normalizePath(path);

        attributeService.removeAttribute(KEY_AVM_LOCKS, avmStore, path);
    }

    /**
     * {@inheritDoc}
     */
    public void removeLocks(String avmStore)
    {
        ParameterCheck.mandatoryString("avmStore", avmStore);
        
        attributeService.removeAttributes(KEY_AVM_LOCKS, avmStore);
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeLocks(String avmStore, String dirPath, final Map<String, String> lockDataToMatch)
    {
        ParameterCheck.mandatoryString("avmStore", avmStore);
        ParameterCheck.mandatory("lockDataToMatch", lockDataToMatch);
        
        final String dirPathStart;
        if (dirPath == null)
        {
            dirPathStart = null;
        }
        else
        {
            dirPath = normalizePath(dirPath);
            if (! dirPath.endsWith("/"))
            {
                dirPath = dirPath + '/';
            }
            
            dirPathStart = dirPath;
        }
        
        // optimised to delete with single DB query
        avmLockDAO.removeLocks(avmStore, dirPathStart, lockDataToMatch);
    }

    /**
     * {@inheritDoc}
     */
    public void removeLocks(String avmStore, final Map<String, String> lockDataToMatch)
    {
        removeLocks(avmStore, null, lockDataToMatch);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasAccess(String webProject, String avmPath, String lockOwner)
    {
        if (personService.getPerson(lockOwner) == null && !authorityService.authorityExists(lockOwner))
        {
            return false;
        }
        if (authorityService.isAdminAuthority(lockOwner))
        {
            return true;
        }
        StoreRef storeRef = new StoreRef(this.webProjectStore);
        ResultSet results = searchService.query(
                storeRef,
                SearchService.LANGUAGE_LUCENE,
                "@wca\\:avmstore:\"" + webProject + '"');
        try
        {
            if (results.getNodeRefs().size() == 1)
            {
                return hasAccess(webProject, results.getNodeRefs().get(0), avmPath, lockOwner);
            }
            return false;
        }
        finally
        {
            results.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasAccess(NodeRef webProjectRef, String avmPath, String lockOwner)
    {
        if (personService.getPerson(lockOwner) == null &&
            !authorityService.authorityExists(lockOwner))
        {
            return false;
        }
        if (authorityService.isAdminAuthority(lockOwner))
        {
            return true;
        }
        String webProject = (String)nodeService.getProperty(webProjectRef, WCMAppModel.PROP_AVMSTORE);
        return hasAccess(webProject, webProjectRef, avmPath, lockOwner);
    }

    private boolean hasAccess(String webProject, NodeRef webProjectRef, String avmPath, String lockOwner)
    {
        String[] storePath = avmPath.split(":");
        if (storePath.length != 2)
        {
            throw new AVMBadArgumentException("Malformed AVM Path : " + avmPath);
        }
        
        if (logger.isDebugEnabled())
           logger.debug(
                   "Testing lock access on path: " + avmPath +
                   " for user: " + lockOwner + " in webproject: " + webProject);
        
        // check if a lock exists at all for this path in the specified webproject id
        String path = normalizePath(storePath[1]);
        
        Map<String, String> lockData = getLockData(webProject, path);
        
        if (lockData == null)
        {
            if (logger.isDebugEnabled())
                logger.debug(" GRANTED: No lock found.");
            return true;
        }
        
        String currentLockOwner = lockData.get(KEY_LOCK_OWNER);
        String currentLockStore = lockData.get(WCMUtil.LOCK_KEY_STORE_NAME);
        
        
        // locks are ignored in a workflow store
        if (storePath[0].contains("--workflow"))
        {
            if (logger.isDebugEnabled())
                logger.debug(" GRANTED: Workflow store path.");
            return true;
        }
        
        // locks are specific to a store - no access if the stores are different
        if (! ((currentLockStore != null) && (currentLockStore.equals(storePath[0]))))
        {
            if (logger.isDebugEnabled())
                logger.debug(" DENIED: Store on path and lock (" + currentLockStore + ") do not match.");
            return false;
        }
        
        // check for content manager role - we allow access to all managers within the same store
        // TODO as part of WCM refactor, consolidate with WebProject.getWebProjectUserRole
        StringBuilder query = new StringBuilder(128);
        query.append("+PARENT:\"").append(webProjectRef).append("\" ");
        query.append("+TYPE:\"").append(WCMAppModel.TYPE_WEBUSER).append("\" ");
        query.append("+@").append(NamespaceService.WCMAPP_MODEL_PREFIX).append("\\:username:\"");
        query.append(lockOwner);
        query.append("\"");
        ResultSet resultSet = searchService.query(
                new StoreRef(this.webProjectStore),
                SearchService.LANGUAGE_LUCENE,
                query.toString());
        List<NodeRef> nodes = resultSet.getNodeRefs();
        resultSet.close();
        
        if (nodes.size() == 1)
        {
            String userrole = (String)nodeService.getProperty(nodes.get(0), WCMAppModel.PROP_WEBUSERROLE);
            if (ROLE_CONTENT_MANAGER.equals(userrole))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("GRANTED: Store match and user is ContentManager role in webproject.");
                }
                return true;
            }
        }
        else if (nodes.size() == 0)
        {
        	logger.warn("hasAccess: user role not found for " + lockOwner);
        }
        else
        {
            logger.warn("hasAccess: more than one user role found for " + lockOwner);
        }
        
        // finally check the owner of the lock against the specified authority
        if (AuthorityType.getAuthorityType(currentLockOwner) == AuthorityType.EVERYONE)
        {
            if (logger.isDebugEnabled())
                logger.debug(" GRANTED: Authority EVERYONE matched lock owner.");
            return true;
        }
        
        if (checkAgainstAuthority(lockOwner, currentLockOwner))
        {
            if (logger.isDebugEnabled())
                logger.debug(" GRANTED: User matched as lock owner.");
            return true;
        }
        
        if (logger.isDebugEnabled())
            logger.debug(" DENIED: User did not match as lock owner.");
        return false;
    }

    /**
     * Helper function that checks the transitive closure of authorities for user.
     */
    private boolean checkAgainstAuthority(String user, String authority)
    {
        if (user.equalsIgnoreCase(authority))
        {
            return true;
        }
        return authorityService.getAuthoritiesForUser(user).contains(authority);
    }
    
    /**
     * Utility to get relative paths into canonical lock form
     * 
     * - remove first forward slash
     * - multiple forward slashes collapsed into single foward slash
     * 
     * @param path The incoming path.
     * @return The normalized path.
     */
    public static String normalizePath(String path)
    {
        path = path.toLowerCase(); // note: enables optimised removal of locks (based on path dir start)
        
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
