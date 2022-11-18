/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.lock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.lock.LockServicePolicies.BeforeLock;
import org.alfresco.repo.lock.LockServicePolicies.BeforeUnlock;
import org.alfresco.repo.lock.mem.Lifetime;
import org.alfresco.repo.lock.mem.LockState;
import org.alfresco.repo.lock.mem.LockStore;
import org.alfresco.repo.lock.mem.LockableAspectInterceptor;
import org.alfresco.repo.lock.traitextender.LockServiceExtension;
import org.alfresco.repo.lock.traitextender.LockServiceTrait;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.repo.version.VersionServicePolicies;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.lock.UnableToAquireLockException;
import org.alfresco.service.cmr.lock.UnableToReleaseLockException;
import org.alfresco.service.cmr.lock.UnableToReleaseLockException.CAUSE;
import org.alfresco.service.cmr.repository.AspectMissingException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.alfresco.traitextender.AJProxyTrait;
import org.alfresco.traitextender.Extend;
import org.alfresco.traitextender.ExtendedTrait;
import org.alfresco.traitextender.Extensible;
import org.alfresco.traitextender.Trait;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.springframework.util.Assert;

/**
 * Simple Lock service implementation
 * 
 * @author Roy Wetherall
 */
public class LockServiceImpl implements LockService,
                                        NodeServicePolicies.OnCreateChildAssociationPolicy,
                                        NodeServicePolicies.BeforeUpdateNodePolicy,
                                        NodeServicePolicies.BeforeDeleteNodePolicy,
                                        NodeServicePolicies.OnMoveNodePolicy,
                                        CopyServicePolicies.OnCopyNodePolicy,
                                        VersionServicePolicies.OnCreateVersionPolicy, TransactionListener,
                                        Extensible
{
    public static final int MAX_EPHEMERAL_LOCK_SECONDS = 2 * 86400;
    
    /** Key to the nodes ref's to ignore when checking for locks */
    private static final String KEY_IGNORE_NODES = "lockService.ignoreNodes";
    private static final Object KEY_MODIFIED_NODES = "lockService.lockedNode";
    
    private NodeService nodeService;
    private TenantService tenantService;
    private AuthenticationService authenticationService;
    private SearchService searchService;
    private BehaviourFilter behaviourFilter;
    private LockStore lockStore;
    private PolicyComponent policyComponent;
    private LockableAspectInterceptor lockableAspectInterceptor;
    
    /** Class policy delegate's */
    private ClassPolicyDelegate<BeforeLock> beforeLock;
    private ClassPolicyDelegate<BeforeUnlock> beforeUnlock;

    private int ephemeralExpiryThreshold;

    private final ExtendedTrait<LockServiceTrait> lockServiceTrait;
    
    public LockServiceImpl()
    {
        this.lockServiceTrait=new ExtendedTrait<LockServiceTrait>(AJProxyTrait.create(this, LockServiceTrait.class));
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
 
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public void setLockStore(LockStore lockStore)
    {
        this.lockStore = lockStore;
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setLockableAspectInterceptor(LockableAspectInterceptor lockableAspectInterceptor)
    {
        this.lockableAspectInterceptor = lockableAspectInterceptor;
    }

    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * Initialise methods called by Spring framework
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "tenantService",  tenantService);
        PropertyCheck.mandatory(this, "authenticationService", authenticationService);
        PropertyCheck.mandatory(this, "searchService",  searchService);
        PropertyCheck.mandatory(this, "behaviourFilter",  behaviourFilter);
        PropertyCheck.mandatory(this, "policyComponent",  policyComponent);
        
        // Register the policies
        beforeLock   = policyComponent.registerClassPolicy(LockServicePolicies.BeforeLock.class);
        beforeUnlock = policyComponent.registerClassPolicy(LockServicePolicies.BeforeUnlock.class);
        
        // Register the various class behaviours to enable lock checking
        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME,
                ContentModel.ASPECT_LOCKABLE,
                new JavaBehaviour(this, "onCreateChildAssociation"));
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeUpdateNodePolicy.QNAME,
                ContentModel.ASPECT_LOCKABLE,
                new JavaBehaviour(this, "beforeUpdateNode"));
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
                ContentModel.ASPECT_LOCKABLE,
                new JavaBehaviour(this, "beforeDeleteNode"));
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnMoveNodePolicy.QNAME,
                ContentModel.ASPECT_LOCKABLE,
                new JavaBehaviour(this, "onMoveNode"));

        // Register copy class behaviour
        this.policyComponent.bindClassBehaviour(
                CopyServicePolicies.OnCopyNodePolicy.QNAME,
                ContentModel.ASPECT_LOCKABLE,
                new JavaBehaviour(this, "getCopyCallback"));

        // Register the onCreateVersion behavior for the version aspect

        // BeforeCreateVersion behavior was removed
        // we should be able to version a node regardless of its lock state, see ALF-16540

        this.policyComponent.bindClassBehaviour(
                VersionServicePolicies.OnCreateVersionPolicy.QNAME,
                ContentModel.ASPECT_LOCKABLE,
                new JavaBehaviour(this, "onCreateVersion"));
    }
    
    /**
     * Returns all the classes of a node, including its type and aspects.
     * 
     * @param nodeRef       node reference
     * @return List<QName>  list of classes
     */
    private List<QName> getInvokeClasses(NodeRef nodeRef)
    {
        List<QName> result = new ArrayList<QName>(10);        
        result.add(nodeService.getType(nodeRef));
        Set<QName> aspects = nodeService.getAspects(nodeRef);
        for (QName aspect : aspects)
        {
            result.add(aspect);
        }
        return result;      
    }
    
    /**
     * Invoke the before lock policy
     * 
     * @param nodeRef       the node to be locked
     * @param lockType      the lock type
     */
    private void invokeBeforeLock(
            NodeRef nodeRef,
            LockType lockType)
    {
        if (!nodeService.exists(nodeRef))
        {
            return;
        }
        List<QName> classes = getInvokeClasses(nodeRef);
        for (QName invokeClass : classes)
        {            
            Collection<BeforeLock> policies = beforeLock.getList(invokeClass);
            for (BeforeLock policy : policies) 
            {
                policy.beforeLock(nodeRef, lockType);
            }
        }
    }

    /**
     * Invoke the before unlock policy
     *
     * @param nodeRef       the node to be unlocked
     */
    private void invokeBeforeUnlock(NodeRef nodeRef)
    {
        if (!nodeService.exists(nodeRef))
        {
            return;
        }

        List<QName> classes = getInvokeClasses(nodeRef);

        for (QName invokeClass : classes)
        {
            Collection<BeforeUnlock> policies = beforeUnlock.getList(invokeClass);

            for (BeforeUnlock policy : policies)
            {
                policy.beforeUnlock(nodeRef);
            }
        }
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
     * @see org.alfresco.service.cmr.lock.LockService#lock(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.lock.LockType)
     */
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
    public void lock(NodeRef nodeRef, LockType lockType)
    {
        // Lock with no expiration
        lock(nodeRef, lockType, TIMEOUT_INFINITY);
    }

    /**
     * @see org.alfresco.service.cmr.lock.LockService#lock(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.lock.LockType, int)
     */
    @Override
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
    public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire)
    {
        lock(nodeRef, lockType, timeToExpire, Lifetime.PERSISTENT);
    }
    
    /**
     * @see org.alfresco.service.cmr.lock.LockService#lock(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.lock.LockType, int, Lifetime, String)
     */
    @Override
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
    public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire, Lifetime lifetime)
    {
        lock(nodeRef, lockType, timeToExpire, lifetime, null);
    }
    
    /**
     * @see org.alfresco.service.cmr.lock.LockService#lock(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.lock.LockType, int, Lifetime, boolean)
     */
    @Override
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
    public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire, Lifetime lifetime, boolean lockChildren)
    {
        lock(nodeRef, lockType, timeToExpire, lifetime);

        if (lockChildren)
        {
            Collection<ChildAssociationRef> childAssocRefs = this.nodeService.getChildAssocs(nodeRef);
            for (ChildAssociationRef childAssocRef : childAssocRefs)
            {
                lock(childAssocRef.getChildRef(), lockType, timeToExpire, lifetime, lockChildren);
            }
        }
    }
    
    /**
     * @see org.alfresco.service.cmr.lock.LockService#lock(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.lock.LockType, int, Lifetime, String)
     */
    @Override
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
    public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire, Lifetime lifetime, String additionalInfo)
    {
        invokeBeforeLock(nodeRef, lockType);
        validateTimeToExpire(timeToExpire, lifetime);
        lifetime = switchLifetimeMode(timeToExpire, lifetime);
        
        nodeRef = tenantService.getName(nodeRef);
        
        // Get the current user name
        String userName = getUserName();

        // Set a default value
        if (lockType == null)
        {
            lockType = LockType.WRITE_LOCK;
        }

        // Get the current lock info and status for the node ref.
        Pair<LockState, LockStatus> statusAndState = getLockStateAndStatus(nodeRef, userName);
        LockState currentLockInfo = statusAndState.getFirst();
        LockStatus currentLockStatus = statusAndState.getSecond();
        
        if (LockStatus.LOCKED.equals(currentLockStatus) == true)
        {
            // Error since we are trying to lock a locked node
            throw new UnableToAquireLockException(nodeRef);
        }
        else if (LockStatus.NO_LOCK.equals(currentLockStatus) == true ||
                 LockStatus.LOCK_EXPIRED.equals(currentLockStatus) == true ||
                 LockStatus.LOCK_OWNER.equals(currentLockStatus) == true)
        {
            final Date expiryDate = makeExpiryDate(timeToExpire);
            
            // Store the lock in the appropriate place.
            if (lifetime == Lifetime.PERSISTENT)
            {
                lockableAspectInterceptor.disableForThread();
                try
                {
                    // Add lock aspect if not already present
                    ensureLockAspect(nodeRef);
                    persistLockProps(nodeRef, lockType, lifetime, userName, expiryDate, additionalInfo);
                }
                finally
                {
                    lockableAspectInterceptor.enableForThread();
                }
            }
            else if (lifetime == Lifetime.EPHEMERAL)
            {
                // Store the lock only in memory.
                LockState lock = LockState.createLock(nodeRef, lockType, userName,
                            expiryDate, lifetime, additionalInfo);
                lockStore.set(nodeRef, lock);
                // Record the NodeRef being locked and its last known lockstate. This allows
                // it to be reverted to this state on rollback.
                TransactionalResourceHelper.getMap(KEY_MODIFIED_NODES).put(nodeRef, currentLockInfo);
                AlfrescoTransactionSupport.bindListener(this);
            }
            else
            {
                throw new IllegalStateException(lifetime.getClass().getSimpleName() +
                            " is not a valid value: " + lifetime.toString());
            }
        }
    }

    private void validateTimeToExpire(int timeToExpire, Lifetime lifetime) {
        if (lifetime.equals(Lifetime.EPHEMERAL) && (timeToExpire > MAX_EPHEMERAL_LOCK_SECONDS))
        {
            throw new IllegalArgumentException("Attempt to create ephemeral lock for " +
                    timeToExpire + " seconds - exceeds maximum allowed time.");
        }
    }

    private Lifetime switchLifetimeMode(int timeToExpire, Lifetime lifetime) {
        if (lifetime.equals(Lifetime.EPHEMERAL) && (timeToExpire > ephemeralExpiryThreshold))
        {
            return Lifetime.PERSISTENT;
        }
        return lifetime;
    }
    
    private void persistLockProps(NodeRef nodeRef, LockType lockType, Lifetime lifetime, String userName, Date expiryDate, String additionalInfo)
    {  
        addToIgnoreSet(nodeRef);
        try
        {
            // Set the current user as the lock owner
            this.nodeService.setProperty(nodeRef, ContentModel.PROP_LOCK_OWNER, userName);
            this.nodeService.setProperty(nodeRef, ContentModel.PROP_LOCK_TYPE, lockType.toString());
            this.nodeService.setProperty(nodeRef, ContentModel.PROP_LOCK_LIFETIME, lifetime.toString());
            this.nodeService.setProperty(nodeRef, ContentModel.PROP_EXPIRY_DATE, expiryDate);
            this.nodeService.setProperty(nodeRef, ContentModel.PROP_LOCK_ADDITIONAL_INFO, additionalInfo);
        } 
        finally
        {
            removeFromIgnoreSet(nodeRef);
        }
    }

    /**
     * Calculate expiry date based on the time to expire provided
     * 
     * @param timeToExpire  the time to expire (in seconds)
     */
    private Date makeExpiryDate(int timeToExpire)
    {
        boolean permanent = timeToExpire <= TIMEOUT_INFINITY;
        if (permanent) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, timeToExpire);
        Date expiryDate = calendar.getTime();

        return expiryDate;
    }

    /**
     * @see org.alfresco.service.cmr.lock.LockService#lock(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.lock.LockType, int, boolean)
     */
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
    public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire, boolean lockChildren)
            throws UnableToAquireLockException
    {
        lock(nodeRef, lockType, timeToExpire, Lifetime.PERSISTENT, lockChildren);
    }

    /**
     * @see org.alfresco.service.cmr.lock.LockService#lock(java.util.Collection, org.alfresco.service.cmr.lock.LockType, int)
     */
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
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
     * @see org.alfresco.service.cmr.lock.LockService#unlock(NodeRef)
     */
    @Override
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
    public void unlock(NodeRef nodeRef) throws UnableToReleaseLockException
    {
        unlock(nodeRef, false, false);
    }
    
    /**
     * @see org.alfresco.service.cmr.lock.LockService#unlock(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    @Override
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
    public void unlock(NodeRef nodeRef, boolean lockChildren) throws UnableToReleaseLockException
    {
        unlock(nodeRef, lockChildren, false);
    }

    /**
     * @see org.alfresco.service.cmr.lock.LockService#unlock(NodeRef, boolean, boolean)
     */
    @Override
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
    public void unlock(NodeRef nodeRef, boolean unlockChildren, boolean allowCheckedOut)
            throws UnableToReleaseLockException
    {
        invokeBeforeUnlock(nodeRef);

        // Unlock the parent
        nodeRef = tenantService.getName(nodeRef);
        	
        LockState lockState = getLockState(nodeRef);
        
        if (lockState.isLockInfo())
        {
        	// MNT-231: forbidden to unlock a checked out node
            if (!allowCheckedOut && nodeService.hasAspect(nodeRef, ContentModel.ASPECT_CHECKED_OUT))
            {
            	throw new UnableToReleaseLockException(nodeRef, CAUSE.CHECKED_OUT);
            }

            // Remove the lock from persistent storage.
            Lifetime lifetime = lockState.getLifetime();
            if (lifetime == Lifetime.PERSISTENT)
            {
                addToIgnoreSet(nodeRef);
                behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
                lockableAspectInterceptor.disableForThread();
                try
                {
                    // Clear the lock
                    if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE))
                    {
                        nodeService.removeAspect(nodeRef, ContentModel.ASPECT_LOCKABLE);
                    }
                }
                finally
                {
                    behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
                    lockableAspectInterceptor.enableForThread();
                    removeFromIgnoreSet(nodeRef);
                }
            }
            else if (lifetime == Lifetime.EPHEMERAL)
            {
                // Remove the ephemeral lock.
                lockStore.set(nodeRef, LockState.createUnlocked(nodeRef));
            }
            else
            {
                throw new IllegalStateException("Unhandled Lifetime value: " + lifetime);
            }
        }

        if (unlockChildren)
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
     * @see org.alfresco.service.cmr.lock.LockService#unlock(Collection)
     */
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
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
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
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
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
    public LockStatus getLockStatus(NodeRef nodeRef, String userName)
    {
        Pair<LockState, LockStatus> stateAndStatus = getLockStateAndStatus(nodeRef, userName);
        LockStatus lockStatus = stateAndStatus.getSecond();
        return lockStatus;
    }

    private Pair<LockState, LockStatus> getLockStateAndStatus(NodeRef nodeRef, String userName)
    {
        final LockState lockState = getLockState(nodeRef);
        
        String lockOwner = lockState.getOwner();
        Date expiryDate = lockState.getExpires();
        LockStatus status = LockUtils.lockStatus(userName, lockOwner, expiryDate);
        return new Pair<LockState, LockStatus>(lockState, status);
    }
    
    /**
     * @see LockService#getLockType(NodeRef)
     */
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
    public LockType getLockType(NodeRef nodeRef)
    {
        LockType result = null;

        // Don't disable the lockable aspect interceptor - allow it to fetch the lock type
        // from the correct place (persistent storage or lockStore).
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
    private void ensureLockAspect(NodeRef nodeRef)
    {
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE) == false)
        {
            this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_LOCKABLE, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
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
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        LockType lockType = getLockType(childAssocRef.getParentRef());
        if(lockType != null)
        {
        
            switch (lockType)
            {
                case WRITE_LOCK:
                case READ_ONLY_LOCK:
                    checkForLock(childAssocRef.getParentRef());
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
        if (! nodeService.hasAspect(nodeRef, ContentModel.ASPECT_CHECKED_OUT))
        {
            checkForLock(nodeRef);
        }
    }

    /**
     * @return              Returns {@link DoNothingCopyBehaviourCallback}
     */
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return DoNothingCopyBehaviourCallback.getInstance();
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
        // TODO: disable the LockAspectInterceptor for this thread, re-enable in finally.
        //       (we need to add this aspect for real).
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
     * @deprecated Uses search and does not report on ephemeral locks.
     */
    @Deprecated
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
     * @deprecated Uses search and does not report on ephemeral locks.
     */
    @Deprecated
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
     * {@inheritDoc}
     */
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
    public boolean isLocked(NodeRef nodeRef)
    {
        LockStatus lockStatus = getLockStatus(nodeRef);
        switch (lockStatus)
        {
            case LOCKED:
            case LOCK_OWNER:
                return true;
            default:
                return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
    public boolean isLockedAndReadOnly(NodeRef nodeRef)
    {
        LockStatus lockStatus = getLockStatus(nodeRef);
        switch (lockStatus)
        {
            case NO_LOCK:
            case LOCK_EXPIRED:
                return false;
            case LOCK_OWNER:
                return getLockType(nodeRef) != LockType.WRITE_LOCK;
            default:
                return true;
        }
    }

    /**
     * @deprecated Uses search and does not report on ephemeral locks.
     */
    @Deprecated
    public List<NodeRef> getLocks(StoreRef storeRef, LockType lockType)
    {
        return getLocks(
                storeRef,
                "ASPECT:\"" + ContentModel.ASPECT_LOCKABLE.toString() + 
                "\" +@\\{http\\://www.alfresco.org/model/content/1.0\\}" + ContentModel.PROP_LOCK_OWNER.getLocalName() + ":\"" + getUserName() + "\"" +
                " +@\\{http\\://www.alfresco.org/model/content/1.0\\}" + ContentModel.PROP_LOCK_TYPE.getLocalName() + ":\"" + lockType.toString() + "\"");
    }

    @Override
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        NodeRef nodeRef = oldChildAssocRef.getChildRef();
        checkForLock(nodeRef);
    }

    @Override
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
    public void suspendLocks()
    {
       getBehaviourFilter().disableBehaviour(ContentModel.ASPECT_LOCKABLE);
       lockableAspectInterceptor.disableForThread();
    }
    
    @Override
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
    public void enableLocks()
    {
       getBehaviourFilter().enableBehaviour(ContentModel.ASPECT_LOCKABLE);
       lockableAspectInterceptor.enableForThread();
    }

    @Override
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
    public String getAdditionalInfo(NodeRef nodeRef)
    {
        LockState lockState = getLockState(nodeRef);
        String additionalInfo = lockState.getAdditionalInfo();
        return additionalInfo;
    }

    @Override
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
    public LockState getLockState(NodeRef nodeRef)
    {
        // Check in-memory for ephemeral locks first.
        nodeRef = tenantService.getName(nodeRef);
        LockState lockState = lockStore.get(nodeRef);
        
        if (lockState != null)
        {
            String lockOwner = lockState.getOwner();
            Date expiryDate = lockState.getExpires();
            LockStatus status = LockUtils.lockStatus(lockOwner, lockOwner, expiryDate);
            // in-memory ephemeral lock which is expired is irrelevant
            if (status.equals(LockStatus.LOCK_EXPIRED))
            {
                lockState = null;
            }
        }

        //ALF-20361: It is possible that a rollback has resulted in a "non-lock" lock state being added to 
        //the lock store. Because of that, we check both whether the retrieved lockState is null and, if it isn't, 
        //whether it represents a real lock
        if (lockState == null || !lockState.isLockInfo())
        {
            // No in-memory state, so get from the DB.
            if (nodeService.exists(nodeRef) && nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE))
            {
                String lockOwner = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_OWNER);
                
                Date expiryDate = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_EXPIRY_DATE);
                String lockTypeStr = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_TYPE);
                LockType lockType = lockTypeStr != null ? LockType.valueOf(lockTypeStr) : null;
                String lifetimeStr = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_LIFETIME);
                Lifetime lifetime = lifetimeStr != null ? Lifetime.valueOf(lifetimeStr) : null;
                String additionalInfo = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_LOCK_ADDITIONAL_INFO);
                
                // Mark lockstate as PERSISTENT as it was in the persistent storage!
                lockState = LockState.createLock(
                            nodeRef,
                            lockType,
                            lockOwner,
                            expiryDate,
                            lifetime,
                            additionalInfo);
            }
            else
            {
                // There is no lock information
                lockState = LockState.createUnlocked(nodeRef);
            }
        }
        
        // Never return a null LockState
        Assert.notNull(lockState);
        return lockState;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    public BehaviourFilter getBehaviourFilter()
    {
        return behaviourFilter;
    }

    @Override
    public void flush()
    {
    }

    @Override
    public void beforeCommit(boolean readOnly)
    {
    }

    @Override
    public void beforeCompletion()
    {
    }

    @Override
    public void afterCommit()
    {
    }

    @Override
    public void afterRollback()
    {
        // As rollback has occurred we are unable to keep hold of any ephemeral locks set during this transaction.
        Map<NodeRef, LockState> lockedNodes = TransactionalResourceHelper.getMap(KEY_MODIFIED_NODES);
        for (LockState lockInfo : lockedNodes.values())
        {
            lockStore.set(lockInfo.getNodeRef(), lockInfo);
        }
    }

    @Override
    @Extend(traitAPI=LockServiceTrait.class,extensionAPI=LockServiceExtension.class)
    public void setEphemeralExpiryThreshold(int threshSecs)
    {
        ephemeralExpiryThreshold = threshSecs;
    }
    
    public int getEphemeralExpiryThreshold()
    {
        return ephemeralExpiryThreshold;
    }

    @Override
    public <M extends Trait> ExtendedTrait<M> getTrait(Class<? extends M> traitAPI)
    {
        return (ExtendedTrait<M>) lockServiceTrait;
    }
}
