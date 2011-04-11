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
package org.alfresco.repo.lock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.version.VersionServicePolicies;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.lock.UnableToAquireLockException;
import org.alfresco.service.cmr.lock.UnableToReleaseLockException;
import org.alfresco.service.cmr.repository.AspectMissingException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Simple Lock service implementation
 * 
 * @author Roy Wetherall
 */
public class LockServiceImpl implements LockService,
                                        NodeServicePolicies.BeforeCreateChildAssociationPolicy,
                                        NodeServicePolicies.BeforeUpdateNodePolicy,
                                        NodeServicePolicies.BeforeDeleteNodePolicy,
                                        CopyServicePolicies.OnCopyNodePolicy,
                                        VersionServicePolicies.BeforeCreateVersionPolicy,
                                        VersionServicePolicies.OnCreateVersionPolicy
{
    /**
     * The node service
     */
    private NodeService nodeService;
    
    /**
     * The tenant service
     */
    private TenantService tenantService;

    /**
     * The policy component
     */
    private PolicyComponent policyComponent;
    
    /** Key to the nodes ref's to ignore when checking for locks */
    private static final String KEY_IGNORE_NODES = "lockService.ignoreNodes";
    
    /**
     * The authentication service
     */
    private AuthenticationService authenticationService;

    /**
     * The ownable service
     * 
     */
    private OwnableService ownableService;
    
    /**
     * The search service
     */
    private SearchService searchService;

    /**
     * Set the node service
     * 
     * @param nodeService
     *            the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
 
    /**
     * Set the tenant service
     * 
     * @param tenantService
     *            the tenant service
     */   
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * Sets the policy component
     * 
     * @param policyComponent
     *            the policy componentO
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Sets the authentication service
     * 
     * @param authenticationService
     *            the authentication service
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    /**
     * Sets the ownable service
     * 
     * @param ownableService
     *            the ownable service
     */
    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }
    
    /**
     * Set the search service
     * 
     * @param searchService     the search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * Initialise methods called by Spring framework
     */
    public void init()
    {
        // Register the various class behaviours to enable lock checking
        this.policyComponent.bindAssociationBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeCreateChildAssociation"),
                ContentModel.ASPECT_LOCKABLE,
                new JavaBehaviour(this, "beforeCreateChildAssociation"));
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeUpdateNode"),
                ContentModel.ASPECT_LOCKABLE,
                new JavaBehaviour(this, "beforeUpdateNode"));
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                ContentModel.ASPECT_LOCKABLE,
                new JavaBehaviour(this, "beforeDeleteNode"));

        // Register copy class behaviour
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
                ContentModel.ASPECT_LOCKABLE,
                new JavaBehaviour(this, "getCopyCallback"));

        // Register the onCreateVersion behavior for the version aspect
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeCreateVersion"),
                ContentModel.ASPECT_LOCKABLE,
                new JavaBehaviour(this, "beforeCreateVersion"));
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateVersion"),
                ContentModel.ASPECT_LOCKABLE,
                new JavaBehaviour(this, "onCreateVersion"));
    }
    
    @SuppressWarnings("unchecked")
    private void addToIgnoreSet(NodeRef nodeRef)
    {
        Set<NodeRef> ignoreNodeRefs = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(KEY_IGNORE_NODES);
        if (ignoreNodeRefs == null)
        {
            ignoreNodeRefs = new HashSet<NodeRef>();
            AlfrescoTransactionSupport.bindResource(KEY_IGNORE_NODES, ignoreNodeRefs);
        }
        ignoreNodeRefs.add(nodeRef);
    }
    
    @SuppressWarnings("unchecked")
    private void removeFromIgnoreSet(NodeRef nodeRef)
    {
        Set<NodeRef> ignoreNodeRefs = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(KEY_IGNORE_NODES);
        if (ignoreNodeRefs != null)
        {
            ignoreNodeRefs.remove(nodeRef);
        }
    }
    
    @SuppressWarnings("unchecked")
    private boolean ignore(NodeRef nodeRef)
    {
        Set<NodeRef> ignoreNodeRefs = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(KEY_IGNORE_NODES);
        if (ignoreNodeRefs != null)
        {
            return ignoreNodeRefs.contains(nodeRef);
        }
        return false;
    }
    
    /**
     * @see org.alfresco.service.cmr.lock.LockService#lock(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.cmr.lock.LockType)
     */
    public void lock(NodeRef nodeRef, LockType lockType)
    {
        // Lock with no expiration
        lock(nodeRef, lockType, 0);
    }

    /**
     * @see org.alfresco.service.cmr.lock.LockService#lock(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.cmr.lock.LockType, int)
     */
    public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire)
    {
        nodeRef = tenantService.getName(nodeRef);
        
        // Check for lock aspect
        checkForLockApsect(nodeRef);
        
        // Get the current user name
        String userName = getUserName();

        // Set a default value
        if (lockType == null)
        {
            lockType = LockType.WRITE_LOCK;
        }

        LockStatus currentLockStatus = getLockStatus(nodeRef, userName);
        if (LockStatus.LOCKED.equals(currentLockStatus) == true)
        {
            // Error since we are trying to lock a locked node
            throw new UnableToAquireLockException(nodeRef);
        }
        else if (LockStatus.NO_LOCK.equals(currentLockStatus) == true ||
                 LockStatus.LOCK_EXPIRED.equals(currentLockStatus) == true ||
                 LockStatus.LOCK_OWNER.equals(currentLockStatus) == true)
        {
            addToIgnoreSet(nodeRef);
            try
            {
                // Set the current user as the lock owner
                this.nodeService.setProperty(nodeRef, ContentModel.PROP_LOCK_OWNER, userName);
                this.nodeService.setProperty(nodeRef, ContentModel.PROP_LOCK_TYPE, lockType.toString());
                setExpiryDate(nodeRef, timeToExpire);
            } 
            finally
            {
                removeFromIgnoreSet(nodeRef);
            }
        }
    }

    /**
     * Helper method to set the expiry date based on the time to expire provided
     * 
     * @param nodeRef       the node reference
     * @param timeToExpire  the time to expire (in seconds)
     */
    private void setExpiryDate(NodeRef nodeRef, int timeToExpire)
    {
        // Set the expiry date
        Date expiryDate = null;
        if (timeToExpire > 0)
        {
            expiryDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(expiryDate);
            calendar.add(Calendar.SECOND, timeToExpire);
            expiryDate = calendar.getTime();
        }
        
        this.nodeService.setProperty(nodeRef, ContentModel.PROP_EXPIRY_DATE, expiryDate);
    }

    /**
     * @see org.alfresco.service.cmr.lock.LockService#lock(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.cmr.lock.LockType, int, boolean)
     */
    public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire, boolean lockChildren)
            throws UnableToAquireLockException
    {
        lock(nodeRef, lockType, timeToExpire);

        if (lockChildren == true)
        {
            Collection<ChildAssociationRef> childAssocRefs = this.nodeService.getChildAssocs(nodeRef);
            for (ChildAssociationRef childAssocRef : childAssocRefs)
            {
                lock(childAssocRef.getChildRef(), lockType, timeToExpire, lockChildren);
            }
        }
    }

    /**
     * @see org.alfresco.service.cmr.lock.LockService#lock(java.util.Collection, java.lang.String, org.alfresco.service.cmr.lock.LockType, int)
     */
    public void lock(Collection<NodeRef> nodeRefs, LockType lockType, int timeToExpire)
            throws UnableToAquireLockException
    {
        // Lock each of the specifed nodes
        for (NodeRef nodeRef : nodeRefs)
        {
            lock(nodeRef, lockType, timeToExpire);
        }
    }    

    /**
     * @see org.alfresco.service.cmr.lock.LockService#unlock(NodeRef, String)
     */
    public void unlock(NodeRef nodeRef) throws UnableToReleaseLockException
    {
        nodeRef = tenantService.getName(nodeRef);
        
        // Check for lock aspect
        checkForLockApsect(nodeRef);
        
        addToIgnoreSet(nodeRef);
        try
        {
            // Clear the lock
            this.nodeService.removeAspect(nodeRef, ContentModel.ASPECT_LOCKABLE);
        }
        finally
        {
            removeFromIgnoreSet(nodeRef);
        }
    }

    /**
     * @see org.alfresco.service.cmr.lock.LockService#unlock(NodeRef, String,
     *      boolean)
     */
    public void unlock(NodeRef nodeRef, boolean unlockChildren)
            throws UnableToReleaseLockException
    {
        // Unlock the parent
        unlock(nodeRef);

        if (unlockChildren == true)
        {
            // Get the children and unlock them
            Collection<ChildAssociationRef> childAssocRefs = this.nodeService.getChildAssocs(nodeRef);
            for (ChildAssociationRef childAssocRef : childAssocRefs)
            {
                unlock(childAssocRef.getChildRef(), unlockChildren);
            }
        }
    }

    /**
     * @see org.alfresco.repo.lock.LockService#unlock(Collection<NodeRef>,
     *      String)
     */
    public void unlock(Collection<NodeRef> nodeRefs) throws UnableToReleaseLockException
    {
        for (NodeRef nodeRef : nodeRefs)
        {
            unlock(nodeRef);
        }
    }

    /**
     * @see org.alfresco.service.cmr.lock.LockService#getLockStatus(NodeRef)
     */
    public LockStatus getLockStatus(NodeRef nodeRef)
    {
        nodeRef = tenantService.getName(nodeRef);
        
        return getLockStatus(nodeRef, getUserName());
    }

    /**
     * Gets the lock status for a node and a user name
     * 
     * @param nodeRef   the node reference
     * @param userName  the user name
     * @return          the lock status
     */
    public LockStatus getLockStatus(NodeRef nodeRef, String userName)
    {
        LockStatus result = LockStatus.NO_LOCK;

        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE) == true)
        {
            // Get the current lock owner
            String currentUserRef = (String) this.nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_OWNER);
            String owner = ownableService.getOwner(nodeRef);
            if (currentUserRef != null)
            {
                Date expiryDate = (Date)this.nodeService.getProperty(nodeRef, ContentModel.PROP_EXPIRY_DATE);
                if (expiryDate != null && expiryDate.before(new Date()) == true)
                {
                    // Indicate that the lock has expired
                    result = LockStatus.LOCK_EXPIRED;
                }
                else
                {
                    if (currentUserRef.equals(userName) == true)
                    {
                        result = LockStatus.LOCK_OWNER;
                    }
                    else if ((owner != null) && owner.equals(userName))
                    {
                        result = LockStatus.LOCK_OWNER;
                    }
                    else
                    {
                        result = LockStatus.LOCKED;
                    }
                }
            }

        }
        return result;

    }

    /**
     * @see LockService#getLockType(NodeRef)
     */
    public LockType getLockType(NodeRef nodeRef)
    {
        LockType result = null;

        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE) == true)
        {
            String lockTypeString = (String) this.nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_TYPE);
            if (lockTypeString != null)
            {
                result = LockType.valueOf(lockTypeString);
            }
        }

        return result;
    }

    /**
     * Checks for the lock aspect. Adds if missing.
     * 
     * @param nodeRef
     *            the node reference
     */
    private void checkForLockApsect(NodeRef nodeRef)
    {
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE) == false)
        {
            this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_LOCKABLE, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void checkForLock(NodeRef nodeRef) throws NodeLockedException
    {
        String userName = getUserName();
        
        nodeRef = tenantService.getName(nodeRef);
 
        // Ensure we have found a node reference
        if (nodeRef != null && userName != null)
        {
            String effectiveUserName = AuthenticationUtil.getRunAsUser();
            // Check to see if should just ignore this node - note: special MT System due to AuditableAspect
            if (! (ignore(nodeRef) || tenantService.getBaseNameUser(effectiveUserName).equals(AuthenticationUtil.getSystemUserName())))
            {
                try
                {
                    // Get the current lock status on the node ref
                    LockStatus currentLockStatus = getLockStatus(nodeRef, userName);

                    LockType lockType = getLockType(nodeRef);
                    if (LockType.WRITE_LOCK.equals(lockType) == true && 
                        LockStatus.LOCKED.equals(currentLockStatus) == true)
                    {
                        // Lock is of type Write Lock and the node is locked by another owner.
                        throw new NodeLockedException(nodeRef);
                    }
                    else if (LockType.READ_ONLY_LOCK.equals(lockType) == true &&
                             (LockStatus.LOCKED.equals(currentLockStatus) == true || LockStatus.LOCK_OWNER.equals(currentLockStatus) == true))
                    {
                        // Error since there is a read only lock on this object and all
                        // modifications are prevented
                        throw new NodeLockedException(nodeRef);
                    }
                    else if (LockType.NODE_LOCK.equals(lockType) == true &&
                            (LockStatus.LOCKED.equals(currentLockStatus) == true || LockStatus.LOCK_OWNER.equals(currentLockStatus) == true))
                    {
                        // Error since there is a read only lock on this object and all
                        // modifications are prevented
                        throw new NodeLockedException(nodeRef);
                    }

                }
                catch (AspectMissingException exception)
                {
                    // Ignore since this indicates that the node does not have the lock aspect applied
                }
            }
        }
    }

    /**
     * Ensures that the parent is not locked.
     * 
     * @see #checkForLock(NodeRef)
     */
    public void beforeCreateChildAssociation(
            NodeRef parentNodeRef,
            NodeRef childNodeRef,
            QName assocTypeQName,
            QName assocQName,
            boolean isNewNode)
    {
        LockType lockType = getLockType(parentNodeRef);
        if(lockType != null)
        {
        
            switch (lockType)
            {
                case WRITE_LOCK:
                case READ_ONLY_LOCK:
                    checkForLock(parentNodeRef);
                    break;
                case NODE_LOCK:
                // don't check for lock
            }
        }
    }

    /**
     * Ensures that node is not locked.
     * 
     * @see #checkForLock(NodeRef)
     */
    public void beforeUpdateNode(NodeRef nodeRef)
    {
        checkForLock(nodeRef);
    }

    /**
     * Ensures that node is not locked.
     * 
     * @see #checkForLock(NodeRef)
     */
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        checkForLock(nodeRef);
    }

    /**
     * @return              Returns {@link LockableAspectCopyBehaviourCallback}
     */
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return LockableAspectCopyBehaviourCallback.INSTANCE;
    }

    /**
     * Extends the default copy behaviour to prevent copying of lockable properties.
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    private static class LockableAspectCopyBehaviourCallback extends DefaultCopyBehaviourCallback
    {
        private static final CopyBehaviourCallback INSTANCE = new LockableAspectCopyBehaviourCallback();
        
        /**
         * @return          Returns an empty map
         */
        @Override
        public Map<QName, Serializable> getCopyProperties(
                QName classQName, CopyDetails copyDetails, Map<QName, Serializable> properties)
        {
            return Collections.emptyMap();
        }
    }
    
    /**
     * Ensures that node is not locked.
     * 
     * @see #checkForLock(NodeRef)
     */
    public void beforeCreateVersion(NodeRef versionableNode)
    {
        checkForLock(versionableNode);
    }

    /**
     * OnCreateVersion behaviour for the lock aspect
     * <p>
     * Ensures that the property values of the lock aspect are not 'frozen' in
     * the version store.
     */
    public void onCreateVersion(
            QName classRef,
            NodeRef versionableNode,
            Map<String, Serializable> versionProperties,
            PolicyScope nodeDetails)
    {
        // Add the lock aspect, but do not version the property values
        nodeDetails.addAspect(ContentModel.ASPECT_LOCKABLE);
    }

    /**
     * Get the current user reference
     * 
     * @return the current user reference
     */
    private String getUserName()
    {
        return this.authenticationService.getCurrentUserName();
    }

    /**
     * @see org.alfresco.service.cmr.lock.LockService#getLocks()
     */
    public List<NodeRef> getLocks(StoreRef storeRef)
    {
        return getLocks(
                storeRef,
                "ASPECT:\"" + ContentModel.ASPECT_LOCKABLE.toString() + 
                "\" +@\\{http\\://www.alfresco.org/model/content/1.0\\}" + ContentModel.PROP_LOCK_OWNER.getLocalName() + ":\"" + getUserName() + "\"");
    }
    
    /**
     * Get the locks given a store and query string.
     * 
     * @param storeRef      the store reference
     * @param query         the query string
     * @return              the locked nodes
     */
    private List<NodeRef> getLocks(StoreRef storeRef, String query)
    {
        List<NodeRef> result = new ArrayList<NodeRef>();
        ResultSet resultSet = null;
        try
        {
            resultSet = this.searchService.query(
                    storeRef,
                    SearchService.LANGUAGE_LUCENE, 
                    query);
            result = resultSet.getNodeRefs();
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
        return result;
    }

    /**
     * @see org.alfresco.service.cmr.lock.LockService#getLocks(org.alfresco.service.cmr.lock.LockType)
     */
    public List<NodeRef> getLocks(StoreRef storeRef, LockType lockType)
    {
        return getLocks(
                storeRef,
                "ASPECT:\"" + ContentModel.ASPECT_LOCKABLE.toString() + 
                "\" +@\\{http\\://www.alfresco.org/model/content/1.0\\}" + ContentModel.PROP_LOCK_OWNER.getLocalName() + ":\"" + getUserName() + "\"" +
                " +@\\{http\\://www.alfresco.org/model/content/1.0\\}" + ContentModel.PROP_LOCK_TYPE.getLocalName() + ":\"" + lockType.toString() + "\"");
    }
}
