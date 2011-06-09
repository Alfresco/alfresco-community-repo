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
package org.alfresco.repo.security.permissions.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.GrantedAuthority;
import net.sf.acegisecurity.providers.dao.User;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.AVMRepository;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.repo.security.permissions.AccessControlListProperties;
import org.alfresco.repo.security.permissions.DynamicAuthority;
import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionContext;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.alfresco.util.PropertyCheck;

/**
 * The Alfresco implementation of a permissions service against our APIs for the permissions model and permissions
 * persistence.
 * 
 * @author andyh
 */
public class PermissionServiceImpl extends AbstractLifecycleBean implements PermissionServiceSPI
{
    static SimplePermissionReference OLD_ALL_PERMISSIONS_REFERENCE = new SimplePermissionReference(
            QName.createQName("", PermissionService.ALL_PERMISSIONS),
            PermissionService.ALL_PERMISSIONS);

    private static Log log = LogFactory.getLog(PermissionServiceImpl.class);

    /** a transactionally-safe cache to be injected */
    private SimpleCache<Serializable, AccessStatus> accessCache;
    
    private SimpleCache<Serializable, Set<String>> readersCache;

    /*
     * Access to the model
     */
    private ModelDAO modelDAO;

    /*
     * Access to permissions
     */
    private PermissionsDaoComponent permissionsDaoComponent;

    /*
     * Access to the node service
     */
    private NodeService nodeService;

    /*
     * Access to the tenant service
     */
    private TenantService tenantService;

    /*
     * Access to the data dictionary
     */
    private DictionaryService dictionaryService;

    /*
     * Access to the ownable service
     */
    private OwnableService ownableService;
    
    /*
     * Access to the authority component
     */
    private AuthorityService authorityService;

    /*
     * Dynamic authorities providers
     */
    private List<DynamicAuthority> dynamicAuthorities;

    private PolicyComponent policyComponent;

    private AclDAO aclDaoComponent;
    
    private PermissionReference allPermissionReference;

    /**
     * Standard spring construction.
     */
    public PermissionServiceImpl()
    {
        super();
    }

    //
    // Inversion of control
    //

    /**
     * Set the dictionary service
     * @param dictionaryService 
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the permissions model dao
     * 
     * @param modelDAO
     */
    public void setModelDAO(ModelDAO modelDAO)
    {
        this.modelDAO = modelDAO;
    }

    /**
     * Set the node service.
     * 
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the ownable service.
     * 
     * @param ownableService
     */
    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }
    
    /**
     * Set the tenant service.
     * @param tenantService
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * Set the permissions dao component
     * 
     * @param permissionsDaoComponent
     */
    public void setPermissionsDaoComponent(PermissionsDaoComponent permissionsDaoComponent)
    {
        this.permissionsDaoComponent = permissionsDaoComponent;
    }

    /**
     * Set the authority service.
     * 
     * @param authorityService
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * Set the dynamic authorities
     * 
     * @param dynamicAuthorities
     */
    public void setDynamicAuthorities(List<DynamicAuthority> dynamicAuthorities)
    {
        this.dynamicAuthorities = dynamicAuthorities;
    }
    
    /**
     * Set the ACL DAO component.
     * 
     * @param aclDaoComponent
     */
    public void setAclDAO(AclDAO aclDaoComponent)
    {
        this.aclDaoComponent = aclDaoComponent;
    }

    /**
     * Set the permissions access cache.
     * 
     * @param accessCache
     *            a transactionally safe cache
     */
    public void setAccessCache(SimpleCache<Serializable, AccessStatus> accessCache)
    {
        this.accessCache = accessCache;
    }

    /**
     * @param readersCache the readersCache to set
     */
    public void setReadersCache(SimpleCache<Serializable, Set<String>> readersCache)
    {
        this.readersCache = readersCache;
    }
    
    /**
     * Set the policy component
     * 
     * @param policyComponent
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Cache clear on move node
     * 
     * @param oldChildAssocRef
     * @param newChildAssocRef
     */
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        accessCache.clear();
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "modelDAO", modelDAO);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "ownableService", ownableService);
        PropertyCheck.mandatory(this, "permissionsDaoComponent", permissionsDaoComponent);
        PropertyCheck.mandatory(this, "authorityService", authorityService);
        PropertyCheck.mandatory(this, "accessCache", accessCache);
        PropertyCheck.mandatory(this, "readersCache", readersCache);
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "aclDaoComponent", aclDaoComponent);

        allPermissionReference = getPermissionReference(ALL_PERMISSIONS);
    }

    /**
     * No-op
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    }
    
    public void init()
    {
        policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onMoveNode"), ContentModel.TYPE_BASE, new JavaBehaviour(this, "onMoveNode"));
    }

    //
    // Permissions Service
    //

    public String getOwnerAuthority()
    {
        return OWNER_AUTHORITY;
    }

    public String getAllAuthorities()
    {
        return ALL_AUTHORITIES;
    }

    public String getAllPermission()
    {
        return ALL_PERMISSIONS;
    }

    public Set<AccessPermission> getPermissions(NodeRef nodeRef)
    {
        return getAllPermissionsImpl(nodeRef, true, true);
    }

    public Set<AccessPermission> getAllSetPermissions(NodeRef nodeRef)
    {
        HashSet<AccessPermission> accessPermissions = new HashSet<AccessPermission>();
        NodePermissionEntry nodePremissionEntry = getSetPermissions(nodeRef);
        for (PermissionEntry pe : nodePremissionEntry.getPermissionEntries())
        {
            accessPermissions.add(new AccessPermissionImpl(getPermission(pe.getPermissionReference()), pe.getAccessStatus(), pe.getAuthority(), pe.getPosition()));
        }
        return accessPermissions;
    }

    public Set<AccessPermission> getAllSetPermissions(StoreRef storeRef)
    {
        HashSet<AccessPermission> accessPermissions = new HashSet<AccessPermission>();
        NodePermissionEntry nodePremissionEntry = getSetPermissions(storeRef);
        for (PermissionEntry pe : nodePremissionEntry.getPermissionEntries())
        {
            accessPermissions.add(new AccessPermissionImpl(getPermission(pe.getPermissionReference()), pe.getAccessStatus(), pe.getAuthority(), pe.getPosition()));
        }
        return accessPermissions;
    }

    private Set<AccessPermission> getAllPermissionsImpl(NodeRef nodeRef, boolean includeTrue, boolean includeFalse)
    {
        String userName = AuthenticationUtil.getRunAsUser();
        HashSet<AccessPermission> accessPermissions = new HashSet<AccessPermission>();
        for (PermissionReference pr : getSettablePermissionReferences(nodeRef))
        {
            if (hasPermission(nodeRef, pr) == AccessStatus.ALLOWED)
            {
                accessPermissions.add(new AccessPermissionImpl(getPermission(pr), AccessStatus.ALLOWED, userName, -1));
            }
            else
            {
                if (includeFalse)
                {
                    accessPermissions.add(new AccessPermissionImpl(getPermission(pr), AccessStatus.DENIED, userName, -1));
                }
            }
        }
        return accessPermissions;
    }

    public Set<String> getSettablePermissions(NodeRef nodeRef)
    {
        Set<PermissionReference> settable = getSettablePermissionReferences(nodeRef);
        Set<String> strings = new HashSet<String>(settable.size());
        for (PermissionReference pr : settable)
        {
            strings.add(getPermission(pr));
        }
        return strings;
    }

    public Set<String> getSettablePermissions(QName type)
    {
        Set<PermissionReference> settable = getSettablePermissionReferences(type);
        Set<String> strings = new LinkedHashSet<String>(settable.size());
        for (PermissionReference pr : settable)
        {
            strings.add(getPermission(pr));
        }
        return strings;
    }

    public NodePermissionEntry getSetPermissions(NodeRef nodeRef)
    {
        return permissionsDaoComponent.getPermissions(tenantService.getName(nodeRef));
    }

    public NodePermissionEntry getSetPermissions(StoreRef storeRef)
    {
        return permissionsDaoComponent.getPermissions(storeRef);
    }

    public AccessStatus hasPermission(NodeRef passedNodeRef, final PermissionReference permIn)
    {
        // If the node ref is null there is no sensible test to do - and there
        // must be no permissions
        // - so we allow it
        if (passedNodeRef == null)
        {
            return AccessStatus.ALLOWED;
        }

        // If the permission is null we deny
        if (permIn == null)
        {
            return AccessStatus.DENIED;
        }

        // AVM nodes - test for existence underneath
        if (passedNodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_AVM))
        {
            return doAvmCan(passedNodeRef, permIn);
        }
        
        // Note: if we're directly accessing a frozen state (version) node (ie. in the 'version' store) we need to check permissions for the versioned node (ie. in the 'live' store)
        if (isVersionNodeRef(passedNodeRef))
        {
            passedNodeRef = convertVersionNodeRefToVersionedNodeRef(VersionUtil.convertNodeRef(passedNodeRef));
        }
        
        // Allow permissions for nodes that do not exist
        if (!nodeService.exists(passedNodeRef))
        {
            return AccessStatus.ALLOWED;
        }
        
        final NodeRef nodeRef = tenantService.getName(passedNodeRef);

        final PermissionReference perm;
        if (permIn.equals(OLD_ALL_PERMISSIONS_REFERENCE))
        {
            perm = getAllPermissionReference();
        }
        else
        {
            perm = permIn;
        }
        
        if (AuthenticationUtil.getRunAsUser() == null)
        {
            return AccessStatus.DENIED;
        }

        if (AuthenticationUtil.isRunAsUserTheSystemUser())
        {
            return AccessStatus.ALLOWED;
        }
        
        // New ACLs

        AccessControlListProperties properties = permissionsDaoComponent.getAccessControlListProperties(nodeRef);
        if ((properties != null) && (properties.getAclType() != null) && (properties.getAclType() != ACLType.OLD))
        {
            QName typeQname = nodeService.getType(nodeRef);
            Set<QName> aspectQNames = nodeService.getAspects(nodeRef);
            PermissionContext context = new PermissionContext(typeQname);
            context.getAspects().addAll(aspectQNames);
            Authentication auth = AuthenticationUtil.getRunAsAuthentication();
            String user = AuthenticationUtil.getRunAsUser();
            for (String dynamicAuthority : getDynamicAuthorities(auth, nodeRef, perm))
            {
                context.addDynamicAuthorityAssignment(user, dynamicAuthority);
            }
            return hasPermission(properties.getId(), context, perm);
        }

        // Get the current authentications
        // Use the smart authentication cache to improve permissions performance
        Authentication auth = AuthenticationUtil.getRunAsAuthentication();
        final Set<String> authorisations = getAuthorisations(auth, nodeRef, perm);

        // If the node does not support the given permission there is no point
        // doing the test
        Set<PermissionReference> available = AuthenticationUtil.runAs(new RunAsWork<Set<PermissionReference>>()
        {
            public Set<PermissionReference> doWork() throws Exception
            {
                return modelDAO.getAllPermissions(nodeRef);
            }

        }, AuthenticationUtil.getSystemUserName());

        available.add(getAllPermissionReference());
        available.add(OLD_ALL_PERMISSIONS_REFERENCE);

        final Serializable key = generateKey(authorisations, nodeRef, perm, CacheType.HAS_PERMISSION);
        if (!(available.contains(perm)))
        {
            accessCache.put(key, AccessStatus.DENIED);
            return AccessStatus.DENIED;
        }

        if (AuthenticationUtil.isRunAsUserTheSystemUser())
        {
            return AccessStatus.ALLOWED;
        }

        return AuthenticationUtil.runAs(new RunAsWork<AccessStatus>()
        {

            public AccessStatus doWork() throws Exception
            {

                AccessStatus status = accessCache.get(key);
                if (status != null)
                {
                    return status;
                }

                //
                // TODO: Dynamic permissions via evaluators
                //

                /*
                 * Does the current authentication have the supplied permission on the given node.
                 */

                QName typeQname = nodeService.getType(nodeRef);
                Set<QName> aspectQNames = nodeService.getAspects(nodeRef);

                NodeTest nt = new NodeTest(perm, typeQname, aspectQNames);
                boolean result = nt.evaluate(authorisations, nodeRef);
                if (log.isDebugEnabled())
                {
                    log.debug("Permission <"
                            + perm + "> is " + (result ? "allowed" : "denied") + " for " + AuthenticationUtil.getRunAsUser() + " on node "
                            + nodeService.getPath(nodeRef));
                }

                status = result ? AccessStatus.ALLOWED : AccessStatus.DENIED;
                accessCache.put(key, status);
                return status;
            }
        }, AuthenticationUtil.getSystemUserName());

    }

    private AccessStatus doAvmCan(NodeRef nodeRef, PermissionReference permission)
    {
        org.alfresco.util.Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        int version = avmVersionPath.getFirst();
        String path = avmVersionPath.getSecond();
        boolean result = AVMRepository.GetInstance().can(nodeRef.getStoreRef().getIdentifier(), version, path, permission.getName());
        AccessStatus status = result ? AccessStatus.ALLOWED : AccessStatus.DENIED;
        return status;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.security.PermissionService#hasPermission(java.lang.Long, java.lang.String,
     *      java.lang.String)
     */
    public AccessStatus hasPermission(Long aclID, PermissionContext context, String permission)
    {
        return hasPermission(aclID, context, getPermissionReference(permission));
    }

   
    private AccessStatus hasPermission(Long aclId, PermissionContext context, PermissionReference permission)
    {
        if (aclId == null)
        {
            // Enforce store ACLs if set - the AVM default was to "allow" if there are no permissions set ...
            if (context.getStoreAcl() == null)
            {
                return AccessStatus.ALLOWED;
            }
            else
            {
                if (AuthenticationUtil.isRunAsUserTheSystemUser())
                {
                    return AccessStatus.ALLOWED;
                }
                
                Authentication auth = AuthenticationUtil.getRunAsAuthentication();
                if (auth == null)
                {
                    throw new IllegalStateException("Unauthenticated");
                }
                Set<String> storeAuthorisations = getAuthorisations(auth, (PermissionContext) null);
                QName typeQname = context.getType();
                Set<QName> aspectQNames = context.getAspects();
                AclTest aclTest = new AclTest(permission, typeQname, aspectQNames);
                boolean result = aclTest.evaluate(storeAuthorisations, context.getStoreAcl(), context);
                AccessStatus status = result ? AccessStatus.ALLOWED : AccessStatus.DENIED;
                return status;
            }
        }

        if (permission == null)
        {
            return AccessStatus.DENIED;
        }

        if (AuthenticationUtil.getRunAsUser() == null)
        {
            return AccessStatus.DENIED;
        }

        if (AuthenticationUtil.getRunAsUser().equals(AuthenticationUtil.getSystemUserName()))
        {
            return AccessStatus.ALLOWED;
        }

        // Get the current authentications
        // Use the smart authentication cache to improve permissions performance
        Authentication auth = AuthenticationUtil.getRunAsAuthentication();
        if (auth == null)
        {
            throw new IllegalStateException("Unauthenticated");
        }

        Set<String> authorisations = getAuthorisations(auth, context);

        // If the node does not support the given permission there is no point
        // doing the test

        final QName typeQname = context.getType();
        final Set<QName> aspectQNames = context.getAspects();

        Set<PermissionReference> available = AuthenticationUtil.runAs(new RunAsWork<Set<PermissionReference>>()
        {
            public Set<PermissionReference> doWork() throws Exception
            {
                return modelDAO.getAllPermissions(typeQname, aspectQNames);
            }

        }, AuthenticationUtil.getSystemUserName());
        available.add(getAllPermissionReference());
        available.add(OLD_ALL_PERMISSIONS_REFERENCE);

        if (!(available.contains(permission)))
        {
            return AccessStatus.DENIED;
        }

        if (AuthenticationUtil.isRunAsUserTheSystemUser())
        {
            return AccessStatus.ALLOWED;
        }

        if (permission.equals(OLD_ALL_PERMISSIONS_REFERENCE))
        {
            permission = getAllPermissionReference();
        }

        boolean result;
        if (context.getStoreAcl() == null)
        {
            AclTest aclTest = new AclTest(permission, typeQname, aspectQNames);
            result = aclTest.evaluate(authorisations, aclId, context);
        }
        else
        {
            Set<String> storeAuthorisations = getAuthorisations(auth, (PermissionContext) null);
            AclTest aclTest = new AclTest(permission, typeQname, aspectQNames);
            result = aclTest.evaluate(authorisations, aclId, context) && aclTest.evaluate(storeAuthorisations, context.getStoreAcl(), context);
        }
        AccessStatus status = result ? AccessStatus.ALLOWED : AccessStatus.DENIED;
        return status;

    }

    /**
     * Control permissions cache - only used when we do old style permission evaluations 
     * - which should only be in DM stores where no permissions have been set 
     * 
     * @author andyh
     *
     */
    enum CacheType
    {
        /**
         * cache full check
         */
        HAS_PERMISSION, 
        /**
         * Cache single permission check
         */
        SINGLE_PERMISSION, 
        /**
         * Cache single permission check for global permission checks
         */
        SINGLE_PERMISSION_GLOBAL;
    }

    /**
     * Key for a cache object is built from all the known Authorities (which can change dynamically so they must all be
     * used) the NodeRef ID and the permission reference itself. This gives a unique key for each permission test.
     */
    static Serializable generateKey(Set<String> auths, NodeRef nodeRef, PermissionReference perm, CacheType type)
    {
        LinkedHashSet<Serializable> key = new LinkedHashSet<Serializable>();
        key.add(perm.toString());
        key.addAll(auths);
        key.add(nodeRef);
        key.add(type);
        return key;
    }

    /**
     * Get the authorisations for the currently authenticated user
     * 
     * @param auth
     * @return the set of authorisations
     */
    private Set<String> getAuthorisations(Authentication auth, NodeRef nodeRef, PermissionReference required)
    {

        HashSet<String> auths = new HashSet<String>();
        // No authenticated user then no permissions
        if (auth == null)
        {
            return auths;
        }
        // TODO: Refactor and use the authentication service for this.
        User user = (User) auth.getPrincipal();

        String username = user.getUsername();
        auths.add(username);

        if (tenantService.getBaseNameUser(username).equalsIgnoreCase(AuthenticationUtil.getGuestUserName()))
        {
            auths.add(PermissionService.GUEST_AUTHORITY);
        }

        for (GrantedAuthority authority : auth.getAuthorities())
        {
            auths.add(authority.getAuthority());
        }
        auths.addAll(getDynamicAuthorities(auth, nodeRef, required));
        auths.addAll(authorityService.getAuthoritiesForUser(username));
        return auths;
    }
  
    private Set<String> getDynamicAuthorities(Authentication auth, NodeRef nodeRef, PermissionReference required)
    {
        HashSet<String> auths = new HashSet<String>(64);

        if (auth == null)
        {
            return auths;
        }
        User user = (User) auth.getPrincipal();
        String username = user.getUsername();

        nodeRef = tenantService.getName(nodeRef);
        if (nodeRef != null)
        {
            if (dynamicAuthorities != null)
            {
                for (DynamicAuthority da : dynamicAuthorities)
                {
                    Set<PermissionReference> requiredFor = da.requiredFor();
                    if ((requiredFor == null) || (requiredFor.contains(required)))
                    {
                        if (da.hasAuthority(nodeRef, username))
                        {
                            auths.add(da.getAuthority());
                        }
                    }
                }
            }
        }
        auths.addAll(authorityService.getAuthoritiesForUser(user.getUsername()));
        return auths;
    }

    private Set<String> getAuthorisations(Authentication auth, PermissionContext context)
    {
        HashSet<String> auths = new HashSet<String>();
        // No authenticated user then no permissions
        if (auth == null)
        {
            return auths;
        }
        // TODO: Refactor and use the authentication service for this.
        User user = (User) auth.getPrincipal();
        auths.add(user.getUsername());
        for (GrantedAuthority authority : auth.getAuthorities())
        {
            auths.add(authority.getAuthority());
        }
        auths.addAll(authorityService.getAuthoritiesForUser(user.getUsername()));

        if (context != null)
        {
            Map<String, Set<String>> dynamicAuthorityAssignments = context.getDynamicAuthorityAssignment();
            HashSet<String> dynAuths = new HashSet<String>();
            for (String current : auths)
            {
                Set<String> dynos = dynamicAuthorityAssignments.get(current);
                if (dynos != null)
                {
                    dynAuths.addAll(dynos);
                }
            }
            auths.addAll(dynAuths);
        }

        return auths;
    }

    public NodePermissionEntry explainPermission(NodeRef nodeRef, PermissionReference perm)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void clearPermission(StoreRef storeRef, String authority)
    {
        permissionsDaoComponent.deletePermissions(storeRef, authority);
        accessCache.clear();
    }

    public void deletePermission(StoreRef storeRef, String authority, String perm)
    {
        deletePermission(storeRef, authority, getPermissionReference(perm));
    }

    private void deletePermission(StoreRef storeRef, String authority, PermissionReference perm)
    {
        permissionsDaoComponent.deletePermission(storeRef, authority, perm);
        accessCache.clear();
    }

    public void deletePermissions(StoreRef storeRef)
    {
        permissionsDaoComponent.deletePermissions(storeRef);
        accessCache.clear();
    }

    public void setPermission(StoreRef storeRef, String authority, String perm, boolean allow)
    {
        setPermission(storeRef, authority, getPermissionReference(perm), allow);
    }

    private void setPermission(StoreRef storeRef, String authority, PermissionReference permission, boolean allow)
    {
        permissionsDaoComponent.setPermission(storeRef, authority, permission, allow);
        accessCache.clear();
    }

    public void deletePermissions(NodeRef nodeRef)
    {
        permissionsDaoComponent.deletePermissions(tenantService.getName(nodeRef));
        accessCache.clear();
    }

    public void deletePermissions(NodePermissionEntry nodePermissionEntry)
    {
        permissionsDaoComponent.deletePermissions(tenantService.getName(nodePermissionEntry.getNodeRef()));
        accessCache.clear();
    }

    /**
     * @see #deletePermission(NodeRef, String, PermissionReference)
     */
    public void deletePermission(PermissionEntry permissionEntry)
    {
        NodeRef nodeRef = permissionEntry.getNodeRef();
        String authority = permissionEntry.getAuthority();
        PermissionReference permission = permissionEntry.getPermissionReference();
        deletePermission(nodeRef, authority, permission);
    }

    private void deletePermission(NodeRef nodeRef, String authority, PermissionReference perm)
    {
        permissionsDaoComponent.deletePermission(tenantService.getName(nodeRef), authority, perm);
        accessCache.clear();
    }

    public void clearPermission(NodeRef nodeRef, String authority)
    {
        permissionsDaoComponent.deletePermissions(tenantService.getName(nodeRef), authority);
        accessCache.clear();
    }

    private void setPermission(NodeRef nodeRef, String authority, PermissionReference perm, boolean allow)
    {
        permissionsDaoComponent.setPermission(tenantService.getName(nodeRef), authority, perm, allow);
        accessCache.clear();
    }

    public void setPermission(PermissionEntry permissionEntry)
    {
        // TODO - not MT-enabled nodeRef - currently only used by tests
        permissionsDaoComponent.setPermission(permissionEntry);
        accessCache.clear();
    }

    public void setPermission(NodePermissionEntry nodePermissionEntry)
    {
        // TODO - not MT-enabled nodeRef- currently only used by tests
        permissionsDaoComponent.setPermission(nodePermissionEntry);
        accessCache.clear();
    }

    public void setInheritParentPermissions(NodeRef nodeRef, boolean inheritParentPermissions)
    {
        NodeRef actualRef = tenantService.getName(nodeRef);
        permissionsDaoComponent.setInheritParentPermissions(actualRef, inheritParentPermissions);
        accessCache.clear();
    }

    /**
     * @see org.alfresco.service.cmr.security.PermissionService#getInheritParentPermissions(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean getInheritParentPermissions(NodeRef nodeRef)
    {
        return permissionsDaoComponent.getInheritParentPermissions(tenantService.getName(nodeRef));
    }

    public PermissionReference getPermissionReference(QName qname, String permissionName)
    {
        return modelDAO.getPermissionReference(qname, permissionName);
    }

    public PermissionReference getAllPermissionReference()
    {
        return allPermissionReference;
    }

    public String getPermission(PermissionReference permissionReference)
    {
        if (modelDAO.isUnique(permissionReference))
        {
            return permissionReference.getName();
        }
        else
        {
            return permissionReference.toString();
        }
    }

    public PermissionReference getPermissionReference(String permissionName)
    {
        return modelDAO.getPermissionReference(null, permissionName);
    }

    public Set<PermissionReference> getSettablePermissionReferences(QName type)
    {
        return modelDAO.getExposedPermissions(type);
    }

    public Set<PermissionReference> getSettablePermissionReferences(NodeRef nodeRef)
    {
        return modelDAO.getExposedPermissions(tenantService.getName(nodeRef));
    }

    public void deletePermission(NodeRef nodeRef, String authority, String perm)
    {
        deletePermission(nodeRef, authority, getPermissionReference(perm));
    }

    public AccessStatus hasPermission(NodeRef nodeRef, String perm)
    {
        return hasPermission(nodeRef, getPermissionReference(perm));
    }

    public void setPermission(NodeRef nodeRef, String authority, String perm, boolean allow)
    {
        setPermission(nodeRef, authority, getPermissionReference(perm), allow);
    }

    public void deletePermissions(String recipient)
    {
        permissionsDaoComponent.deletePermissions(recipient);
        accessCache.clear();
    }

    /**
     * Optimised read permission evaluation
     * caveats:
     * doesn't take into account dynamic authorities/groups
     * doesn't take into account node types/aspects for permissions
     *  
     */
    public AccessStatus hasReadPermission(NodeRef nodeRef)
    {
        AccessStatus status = AccessStatus.DENIED;

        // If the node ref is null there is no sensible test to do - and there
        // must be no permissions
        // - so we allow it
        if (nodeRef == null)
        {
            return AccessStatus.ALLOWED;
        }

        // Allow permissions for nodes that do not exist
        if (!nodeService.exists(nodeRef))
        {
            return AccessStatus.ALLOWED;
        }

        String runAsUser = AuthenticationUtil.getRunAsUser();
        if (runAsUser == null)
        {
            return AccessStatus.DENIED;
        }

        if (AuthenticationUtil.isRunAsUserTheSystemUser())
        {
            return AccessStatus.ALLOWED;
        }

        // any dynamic authorities other than those defined in the default permissions model with full
        // control or read permission force hasPermission check
        Boolean forceHasPermission = (Boolean)AlfrescoTransactionSupport.getResource("forceHasPermission");
        if(forceHasPermission == null)
        {
            for(DynamicAuthority dynamicAuthority : dynamicAuthorities)
            {
                String authority = dynamicAuthority.getAuthority();
                Set<PermissionReference> requiredFor = dynamicAuthority.requiredFor();
                if(authority != PermissionService.OWNER_AUTHORITY &&
                        authority != PermissionService.ADMINISTRATOR_AUTHORITY &&
                        authority != PermissionService.LOCK_OWNER_AUTHORITY &&
                        (requiredFor == null ||
                                requiredFor.contains(modelDAO.getPermissionReference(null, PermissionService.FULL_CONTROL)) ||
                                requiredFor.contains(modelDAO.getPermissionReference(null, PermissionService.READ))))
                {
                    forceHasPermission = Boolean.TRUE;
                    break;
                }
            }
            AlfrescoTransactionSupport.bindResource("forceHasPermission", forceHasPermission);            
        }

        if(forceHasPermission == Boolean.TRUE)
        {
            return hasPermission(nodeRef, PermissionService.READ);
        }

        Long aclID = nodeService.getNodeAclId(nodeRef);
        if(aclID == null)
        {
            // ACLID is null - need to call default permissions evaluation
            // This will end up calling the old-style ACL code that walks up the ACL tree
            status = hasPermission(nodeRef, getPermissionReference(null, PermissionService.READ));
        }
        else
        {
            status = (canRead(aclID) == AccessStatus.ALLOWED ||
                    adminRead() == AccessStatus.ALLOWED ||
                    ownerRead(runAsUser, nodeRef) == AccessStatus.ALLOWED) ? AccessStatus.ALLOWED : AccessStatus.DENIED;
        }

        return status;
    }

    private AccessStatus adminRead()
    {
        AccessStatus result = AccessStatus.DENIED;

        Set<String> authorisations = getAuthorisations();
        if(authorisations.contains(AuthenticationUtil.getAdminRoleName()))
        {
            result = AccessStatus.ALLOWED;
        }

        // ROLE_ADMINISTRATOR authority has FULL_CONTROL in permissionDefinitions
        // so we don't need to check node requirements
        return result;
    }

    private AccessStatus ownerRead(String username, NodeRef nodeRef)
    {
        AccessStatus result = AccessStatus.DENIED;

        String owner = ownableService.getOwner(nodeRef);
        if(owner == null)
        {
            // TODO node may not have auditable aspect and hence creator property
            result = AccessStatus.DENIED;
        }

        // is the user the owner of the node?
        if(EqualsHelper.nullSafeEquals(username, owner))
        {
            // ROLE_OWNER authority has FULL_CONTROL in permissionDefinitions
            // so we don't need to check node requirements    		
            return AccessStatus.ALLOWED;
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getReaders(Long aclId)
    {
        Set<String> aclReaders = readersCache.get(aclId);
        if (aclReaders == null)
        {
            aclReaders = buildReaders(aclId);
            readersCache.put(aclId, aclReaders);
        }
        return aclReaders;
    }
    
    /**
     * Builds the set of authorities who can read the given ACL.  No caching is done here.
     * 
     * @return              an <b>unmodifiable</b> set of authorities
     */
    private Set<String> buildReaders(Long aclId)
    {
        AccessControlList acl = aclDaoComponent.getAccessControlList(aclId);
        if (acl == null)
        {
            return Collections.emptySet();
        }

        HashSet<String> assigned = new HashSet<String>();
        HashSet<String> readers = new HashSet<String>();

        for (AccessControlEntry ace : acl.getEntries())
        {
            assigned.add(ace.getAuthority());
        }

        for (String authority : assigned)
        {
            UnconditionalAclTest test = new UnconditionalAclTest(getPermissionReference(PermissionService.READ));
            if (test.evaluate(authority, aclId))
            {
                readers.add(authority);
            }
        }

        return Collections.unmodifiableSet(readers);
    }

    private AccessStatus canRead(Long aclId)
    {
        Set<String> authorities = getAuthorisations();

        // test acl readers
        Set<String> aclReaders = getReaders(aclId);

        // both lists are ordered so we can skip scan to find any overlap
        if(authorities.size() < aclReaders.size())
        {
            for(String auth : authorities)
            {
                if(aclReaders.contains(auth))
                {
                    return AccessStatus.ALLOWED;
                }
            }
        }
        else
        {
            for(String auth : aclReaders)
            {
                if(authorities.contains(auth))
                {
                    return AccessStatus.ALLOWED;
                }
            }
        }

        return AccessStatus.DENIED;
    }
  
    //
    // SUPPORT CLASSES
    //

    /**
     * Support class to test the permission on a node.
     * 
     * @author Andy Hind
     */
    private class NodeTest
    {
        /*
         * The required permission.
         */
        PermissionReference required;

        /*
         * Granters of the permission
         */
        Set<PermissionReference> granters;

        /*
         * The additional permissions required at the node level.
         */
        Set<PermissionReference> nodeRequirements = new HashSet<PermissionReference>();

        /*
         * The additional permissions required on the parent.
         */
        Set<PermissionReference> parentRequirements = new HashSet<PermissionReference>();

        /*
         * The permissions required on all children .
         */
        Set<PermissionReference> childrenRequirements = new HashSet<PermissionReference>();

        /*
         * The type name of the node.
         */
        QName typeQName;

        /*
         * The aspects set on the node.
         */
        Set<QName> aspectQNames;

        /*
         * Constructor just gets the additional requirements
         */
        NodeTest(PermissionReference required, QName typeQName, Set<QName> aspectQNames)
        {
            this.required = required;
            this.typeQName = typeQName;
            this.aspectQNames = aspectQNames;

            // Set the required node permissions
            if (required.equals(getPermissionReference(ALL_PERMISSIONS)))
            {
                nodeRequirements = modelDAO.getRequiredPermissions(getPermissionReference(PermissionService.FULL_CONTROL), typeQName, aspectQNames, RequiredPermission.On.NODE);
            }
            else
            {
                nodeRequirements = modelDAO.getRequiredPermissions(required, typeQName, aspectQNames, RequiredPermission.On.NODE);
            }

            parentRequirements = modelDAO.getRequiredPermissions(required, typeQName, aspectQNames, RequiredPermission.On.PARENT);

            childrenRequirements = modelDAO.getRequiredPermissions(required, typeQName, aspectQNames, RequiredPermission.On.CHILDREN);

            // Find all the permissions that grant the allowed permission
            // All permissions are treated specially.
            granters = new LinkedHashSet<PermissionReference>(128, 1.0f);
            granters.addAll(modelDAO.getGrantingPermissions(required));
            granters.add(getAllPermissionReference());
            granters.add(OLD_ALL_PERMISSIONS_REFERENCE);
        }

        /**
         * External hook point
         * @return true if allowed
         */
        boolean evaluate(Set<String> authorisations, NodeRef nodeRef)
        {
            Set<Pair<String, PermissionReference>> denied = new HashSet<Pair<String, PermissionReference>>();
            return evaluate(authorisations, nodeRef, denied, null);
        }

        /**
         * Internal hook point for recursion
         * @return true if allowed
         */
        boolean evaluate(Set<String> authorisations, NodeRef nodeRef, Set<Pair<String, PermissionReference>> denied, MutableBoolean recursiveIn)
        {
            // Do we defer our required test to a parent (yes if not null)
            MutableBoolean recursiveOut = null;

            Set<Pair<String, PermissionReference>> locallyDenied = new HashSet<Pair<String, PermissionReference>>();
            locallyDenied.addAll(denied);
            locallyDenied.addAll(getDenied(nodeRef));

            // Start out true and "and" all other results
            boolean success = true;

            // Check the required permissions but not for sets they rely on
            // their underlying permissions
            if (modelDAO.checkPermission(required))
            {
                if (parentRequirements.contains(required))
                {
                    if (checkGlobalPermissions(authorisations) || checkRequired(authorisations, nodeRef, locallyDenied))
                    {
                        // No need to do the recursive test as it has been found
                        if (recursiveIn != null)
                        {
                            recursiveIn.setValue(true);
                        }
                    }
                    else
                    {
                        // Much cheaper to do this as we go then check all the
                        // stack values for each parent
                        recursiveOut = new MutableBoolean(false);
                    }
                }
                else
                {
                    // We have to do the test as no parent will help us out
                    success &= hasSinglePermission(authorisations, nodeRef);
                }
                if (!success)
                {
                    return false;
                }
            }

            // Check the other permissions required on the node
            for (PermissionReference pr : nodeRequirements)
            {
                // Build a new test
                NodeTest nt = new NodeTest(pr, typeQName, aspectQNames);
                success &= nt.evaluate(authorisations, nodeRef, locallyDenied, null);
                if (!success)
                {
                    return false;
                }
            }

            // Check the permission required of the parent

            if (success)
            {
                ChildAssociationRef car = nodeService.getPrimaryParent(nodeRef);
                if (car.getParentRef() != null)
                {

                    NodePermissionEntry nodePermissions = permissionsDaoComponent.getPermissions(car.getChildRef());
                    if ((nodePermissions == null) || (nodePermissions.inheritPermissions()))
                    {

                        locallyDenied.addAll(getDenied(car.getParentRef()));
                        for (PermissionReference pr : parentRequirements)
                        {
                            if (pr.equals(required))
                            {
                                // Recursive permission
                                success &= this.evaluate(authorisations, car.getParentRef(), locallyDenied, recursiveOut);
                                if ((recursiveOut != null) && recursiveOut.getValue())
                                {
                                    if (recursiveIn != null)
                                    {
                                        recursiveIn.setValue(true);
                                    }
                                }
                            }
                            else
                            {
                                NodeTest nt = new NodeTest(pr, typeQName, aspectQNames);
                                success &= nt.evaluate(authorisations, car.getParentRef(), locallyDenied, null);
                            }

                            if (!success)
                            {
                                return false;
                            }
                        }
                    }
                }
            }

            if ((recursiveOut != null) && (!recursiveOut.getValue()))
            {
                // The required authentication was not resolved in recursion
                return false;
            }

            // Check permissions required of children
            if (childrenRequirements.size() > 0)
            {
                List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(nodeRef);
                for (PermissionReference pr : childrenRequirements)
                {
                    for (ChildAssociationRef child : childAssocRefs)
                    {
                        success &= (hasPermission(child.getChildRef(), pr) == AccessStatus.ALLOWED);
                        if (!success)
                        {
                            return false;
                        }
                    }
                }
            }

            return success;
        }

        boolean hasSinglePermission(Set<String> authorisations, NodeRef nodeRef)
        {
            nodeRef = tenantService.getName(nodeRef);

            Serializable key = generateKey(authorisations, nodeRef, this.required, CacheType.SINGLE_PERMISSION_GLOBAL);

            AccessStatus status = accessCache.get(key);
            if (status != null)
            {
                return status == AccessStatus.ALLOWED;
            }

            // Check global permission

            if (checkGlobalPermissions(authorisations))
            {
                accessCache.put(key, AccessStatus.ALLOWED);
                return true;
            }

            Set<Pair<String, PermissionReference>> denied = new HashSet<Pair<String, PermissionReference>>();

            return hasSinglePermission(authorisations, nodeRef, denied);

        }

        boolean hasSinglePermission(Set<String> authorisations, NodeRef nodeRef, Set<Pair<String, PermissionReference>> denied)
        {
            nodeRef = tenantService.getName(nodeRef);

            // Add any denied permission to the denied list - these can not
            // then
            // be used to given authentication.
            // A -> B -> C
            // If B denies all permissions to any - allowing all permissions
            // to
            // andy at node A has no effect

            denied.addAll(getDenied(nodeRef));

            // Cache non denied
            Serializable key = null;
            if (denied.size() == 0)
            {
                key = generateKey(authorisations, nodeRef, this.required, CacheType.SINGLE_PERMISSION);
            }
            if (key != null)
            {
                AccessStatus status = accessCache.get(key);
                if (status != null)
                {
                    return status == AccessStatus.ALLOWED;
                }
            }

            // If the current node allows the permission we are done
            // The test includes any parent or ancestor requirements
            if (checkRequired(authorisations, nodeRef, denied))
            {
                if (key != null)
                {
                    accessCache.put(key, AccessStatus.ALLOWED);
                }
                return true;
            }

            // Permissions are only evaluated up the primary parent chain
            // TODO: Do not ignore non primary permissions
            ChildAssociationRef car = nodeService.getPrimaryParent(nodeRef);

            // Build the next element of the evaluation chain
            if (car.getParentRef() != null)
            {
                NodePermissionEntry nodePermissions = permissionsDaoComponent.getPermissions(nodeRef);
                if ((nodePermissions == null) || (nodePermissions.inheritPermissions()))
                {
                    if (hasSinglePermission(authorisations, car.getParentRef(), denied))
                    {
                        if (key != null)
                        {
                            accessCache.put(key, AccessStatus.ALLOWED);
                        }
                        return true;
                    }
                    else
                    {
                        if (key != null)
                        {
                            accessCache.put(key, AccessStatus.DENIED);
                        }
                        return false;
                    }
                }
                else
                {
                    if (key != null)
                    {
                        accessCache.put(key, AccessStatus.DENIED);
                    }
                    return false;
                }
            }
            else
            {
                if (key != null)
                {
                    accessCache.put(key, AccessStatus.DENIED);
                }
                return false;
            }
        }

        /**
         * Check if we have a global permission
         * 
         * @return true if allowed
         */
        private boolean checkGlobalPermissions(Set<String> authorisations)
        {
            for (PermissionEntry pe : modelDAO.getGlobalPermissionEntries())
            {
                if (isGranted(pe, authorisations, null))
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * Get the list of permissions denied for this node.
         * 
         * @return the list of denied permissions
         */
        Set<Pair<String, PermissionReference>> getDenied(NodeRef nodeRef)
        {
            Set<Pair<String, PermissionReference>> deniedSet = new HashSet<Pair<String, PermissionReference>>();

            // Loop over all denied permissions
            NodePermissionEntry nodeEntry = permissionsDaoComponent.getPermissions(nodeRef);
            if (nodeEntry != null)
            {
                for (PermissionEntry pe : nodeEntry.getPermissionEntries())
                {
                    if (pe.isDenied())
                    {
                        // All the sets that grant this permission must be
                        // denied
                        // Note that granters includes the orginal permission
                        Set<PermissionReference> granters = modelDAO.getGrantingPermissions(pe.getPermissionReference());
                        for (PermissionReference granter : granters)
                        {
                            deniedSet.add(new Pair<String, PermissionReference>(pe.getAuthority(), granter));
                        }

                        // All the things granted by this permission must be
                        // denied
                        Set<PermissionReference> grantees = modelDAO.getGranteePermissions(pe.getPermissionReference());
                        for (PermissionReference grantee : grantees)
                        {
                            deniedSet.add(new Pair<String, PermissionReference>(pe.getAuthority(), grantee));
                        }

                        // All permission excludes all permissions available for
                        // the node.
                        if (pe.getPermissionReference().equals(getAllPermissionReference()) || pe.getPermissionReference().equals(OLD_ALL_PERMISSIONS_REFERENCE))
                        {
                            for (PermissionReference deny : modelDAO.getAllPermissions(nodeRef))
                            {
                                deniedSet.add(new Pair<String, PermissionReference>(pe.getAuthority(), deny));
                            }
                        }
                    }
                }
            }
            return deniedSet;
        }

        /**
         * Check that a given authentication is available on a node
         * 
         * @return true if the check is required
         */
        boolean checkRequired(Set<String> authorisations, NodeRef nodeRef, Set<Pair<String, PermissionReference>> denied)
        {
            NodePermissionEntry nodeEntry = permissionsDaoComponent.getPermissions(nodeRef);

            // No permissions set - short cut to deny
            if (nodeEntry == null)
            {
                return false;
            }

            // Check if each permission allows - the first wins.
            // We could have other voting style mechanisms here
            for (PermissionEntry pe : nodeEntry.getPermissionEntries())
            {
                if (isGranted(pe, authorisations, denied))
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * Is a permission granted
         * 
         * @param pe -
         *            the permissions entry to consider
         * @param granters -
         *            the set of granters
         * @param authorisations -
         *            the set of authorities
         * @param denied -
         *            the set of denied permissions/authority pais
         * @return true if granted
         */
        private boolean isGranted(PermissionEntry pe, Set<String> authorisations, Set<Pair<String, PermissionReference>> denied)
        {
            // If the permission entry denies then we just deny
            if (pe.isDenied())
            {
                return false;
            }

            // The permission is allowed but we deny it as it is in the denied
            // set

            if (denied != null)
            {
                Pair<String, PermissionReference> specific = new Pair<String, PermissionReference>(pe.getAuthority(), required);
                if (denied.contains(specific))
                {
                    return false;
                }
            }

            // any deny denies

//            if (false)
//            {
//                if (denied != null)
//                {
//                    for (String auth : authorisations)
//                    {
//                        Pair<String, PermissionReference> specific = new Pair<String, PermissionReference>(auth, required);
//                        if (denied.contains(specific))
//                        {
//                            return false;
//                        }
//                        for (PermissionReference perm : granters)
//                        {
//                            specific = new Pair<String, PermissionReference>(auth, perm);
//                            if (denied.contains(specific))
//                            {
//                                return false;
//                            }
//                        }
//                    }
//                }
//            }

            // If the permission has a match in both the authorities and
            // granters list it is allowed
            // It applies to the current user and it is granted
            if (authorisations.contains(pe.getAuthority()) && granters.contains(pe.getPermissionReference()))
            {
                {
                    return true;
                }
            }

            // Default deny
            return false;
        }

    }

    /**
     * Test a permission in the context of the new ACL implementation. All components of the ACL are in the object -
     * there is no need to walk up the parent chain. Parent conditions cna not be applied as there is no context to do
     * this. Child conditions can not be applied as there is no context to do this
     * 
     * @author andyh
     */

    private class AclTest
    {
        /*
         * The required permission.
         */
        PermissionReference required;

        /*
         * Granters of the permission
         */
        Set<PermissionReference> granters;

        /*
         * The additional permissions required at the node level.
         */
        Set<PermissionReference> nodeRequirements = new HashSet<PermissionReference>();

        /*
         * The type name of the node.
         */
        QName typeQName;

        /*
         * The aspects set on the node.
         */
        Set<QName> aspectQNames;

        /*
         * Constructor just gets the additional requirements
         */
        AclTest(PermissionReference required, QName typeQName, Set<QName> aspectQNames)
        {
            this.required = required;
            this.typeQName = typeQName;
            this.aspectQNames = aspectQNames;

            // Set the required node permissions
            if (required.equals(getPermissionReference(ALL_PERMISSIONS)))
            {
                nodeRequirements = modelDAO.getRequiredPermissions(getPermissionReference(PermissionService.FULL_CONTROL), typeQName, aspectQNames, RequiredPermission.On.NODE);
            }
            else
            {
                nodeRequirements = modelDAO.getRequiredPermissions(required, typeQName, aspectQNames, RequiredPermission.On.NODE);
            }

            if (modelDAO.getRequiredPermissions(required, typeQName, aspectQNames, RequiredPermission.On.PARENT).size() > 0)
            {
                throw new IllegalStateException("Parent permissions can not be checked for an acl");
            }

            if (modelDAO.getRequiredPermissions(required, typeQName, aspectQNames, RequiredPermission.On.CHILDREN).size() > 0)
            {
                throw new IllegalStateException("Child permissions can not be checked for an acl");
            }

            // Find all the permissions that grant the allowed permission
            // All permissions are treated specially.
            granters = new LinkedHashSet<PermissionReference>(128, 1.0f);
            granters.addAll(modelDAO.getGrantingPermissions(required));
            granters.add(getAllPermissionReference());
            granters.add(OLD_ALL_PERMISSIONS_REFERENCE);
        }

        /**
         * Internal hook point for recursion
         * 
         * @param authorisations
         * @param nodeRef
         * @param denied
         * @param recursiveIn
         * @return true if granted
         */
        boolean evaluate(Set<String> authorisations, Long aclId, PermissionContext context)
        {
            // Start out true and "and" all other results
            boolean success = true;

            // Check the required permissions but not for sets they rely on
            // their underlying permissions
            if (modelDAO.checkPermission(required))
            {

                // We have to do the test as no parent will help us out
                success &= hasSinglePermission(authorisations, aclId, context);

                if (!success)
                {
                    return false;
                }
            }

            // Check the other permissions required on the node
            for (PermissionReference pr : nodeRequirements)
            {
                // Build a new test
                AclTest nt = new AclTest(pr, typeQName, aspectQNames);
                success &= nt.evaluate(authorisations, aclId, context);
                if (!success)
                {
                    return false;
                }
            }

            return success;
        }

        boolean hasSinglePermission(Set<String> authorisations, Long aclId, PermissionContext context)
        {
            // Check global permission

            if (checkGlobalPermissions(authorisations))
            {
                return true;
            }

            return checkRequired(authorisations, aclId, context);

        }

        /**
         * Check if we have a global permission
         * 
         * @param authorisations
         * @return true if granted
         */
        private boolean checkGlobalPermissions(Set<String> authorisations)
        {
            for (PermissionEntry pe : modelDAO.getGlobalPermissionEntries())
            {
                if (isGranted(pe, authorisations))
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * Check that a given authentication is available on a node
         * 
         * @param authorisations
         * @param nodeRef
         * @param denied
         * @return true if a check is required
         */
        boolean checkRequired(Set<String> authorisations, Long aclId, PermissionContext context)
        {
            AccessControlList acl = aclDaoComponent.getAccessControlList(aclId);

            if (acl == null)
            {
                return false;
            }

            Set<Pair<String, PermissionReference>> denied = new HashSet<Pair<String, PermissionReference>>();

            // Check if each permission allows - the first wins.
            // We could have other voting style mechanisms here
            for (AccessControlEntry ace : acl.getEntries())
            {
                if (isGranted(ace, authorisations, denied, context))
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * Is a permission granted
         * 
         * @param pe -
         *            the permissions entry to consider
         * @param granters -
         *            the set of granters
         * @param authorisations -
         *            the set of authorities
         * @param denied -
         *            the set of denied permissions/authority pais
         * @return true if granted
         */
        private boolean isGranted(AccessControlEntry ace, Set<String> authorisations, Set<Pair<String, PermissionReference>> denied, PermissionContext context)
        {
            // If the permission entry denies then we just deny
            if (ace.getAccessStatus() == AccessStatus.DENIED)
            {
                denied.add(new Pair<String, PermissionReference>(ace.getAuthority(), ace.getPermission()));

                Set<PermissionReference> granters = modelDAO.getGrantingPermissions(ace.getPermission());
                for (PermissionReference granter : granters)
                {
                    denied.add(new Pair<String, PermissionReference>(ace.getAuthority(), granter));
                }

                // All the things granted by this permission must be
                // denied
                Set<PermissionReference> grantees = modelDAO.getGranteePermissions(ace.getPermission());
                for (PermissionReference grantee : grantees)
                {
                    denied.add(new Pair<String, PermissionReference>(ace.getAuthority(), grantee));
                }

                // All permission excludes all permissions available for
                // the node.
                if (ace.getPermission().equals(getAllPermissionReference()) || ace.getPermission().equals(OLD_ALL_PERMISSIONS_REFERENCE))
                {
                    for (PermissionReference deny : modelDAO.getAllPermissions(context.getType(), context.getAspects()))
                    {
                        denied.add(new Pair<String, PermissionReference>(ace.getAuthority(), deny));
                    }
                }

                return false;
            }

            // The permission is allowed but we deny it as it is in the denied
            // set

            if (denied != null)
            {
                Pair<String, PermissionReference> specific = new Pair<String, PermissionReference>(ace.getAuthority(), required);
                if (denied.contains(specific))
                {
                    return false;
                }
            }

            // any deny denies

//            if (false)
//            {
//                if (denied != null)
//                {
//                    for (String auth : authorisations)
//                    {
//                        Pair<String, PermissionReference> specific = new Pair<String, PermissionReference>(auth, required);
//                        if (denied.contains(specific))
//                        {
//                            return false;
//                        }
//                        for (PermissionReference perm : granters)
//                        {
//                            specific = new Pair<String, PermissionReference>(auth, perm);
//                            if (denied.contains(specific))
//                            {
//                                return false;
//                            }
//                        }
//                    }
//                }
//            }

            // If the permission has a match in both the authorities and
            // granters list it is allowed
            // It applies to the current user and it is granted
            if (authorisations.contains(ace.getAuthority()) && granters.contains(ace.getPermission()))
            {
                {
                    return true;
                }
            }

            // Default deny
            return false;
        }

        private boolean isGranted(PermissionEntry pe, Set<String> authorisations)
        {
            // If the permission entry denies then we just deny
            if (pe.isDenied())
            {
                return false;
            }

            // If the permission has a match in both the authorities and
            // granters list it is allowed
            // It applies to the current user and it is granted
            if (granters.contains(pe.getPermissionReference()) && authorisations.contains(pe.getAuthority()))
            {
                {
                    return true;
                }
            }

            // Default deny
            return false;
        }

    }

    /**
     * Ignores type and aspect requirements on the node
     *
     */
    private class UnconditionalAclTest
    {
        /*
         * The required permission.
         */
        PermissionReference required;

        /*
         * Granters of the permission
         */
        Set<PermissionReference> granters;

        /*
         * The additional permissions required at the node level.
         */
        Set<PermissionReference> nodeRequirements = new HashSet<PermissionReference>();

        /*
         * Constructor just gets the additional requirements
         */
        UnconditionalAclTest(PermissionReference required)
        {
            this.required = required;

            // Set the required node permissions
            if (required.equals(getPermissionReference(ALL_PERMISSIONS)))
            {
                nodeRequirements = modelDAO.getUnconditionalRequiredPermissions(getPermissionReference(PermissionService.FULL_CONTROL), RequiredPermission.On.NODE);
            }
            else
            {
                nodeRequirements = modelDAO.getUnconditionalRequiredPermissions(required, RequiredPermission.On.NODE);
            }

            if (modelDAO.getUnconditionalRequiredPermissions(required, RequiredPermission.On.PARENT).size() > 0)
            {
                throw new IllegalStateException("Parent permissions can not be checked for an acl");
            }

            if (modelDAO.getUnconditionalRequiredPermissions(required,  RequiredPermission.On.CHILDREN).size() > 0)
            {
                throw new IllegalStateException("Child permissions can not be checked for an acl");
            }

            // Find all the permissions that grant the allowed permission
            // All permissions are treated specially.
            granters = new LinkedHashSet<PermissionReference>(128, 1.0f);
            granters.addAll(modelDAO.getGrantingPermissions(required));
            granters.add(getAllPermissionReference());
            granters.add(OLD_ALL_PERMISSIONS_REFERENCE);
        }

        /**
         * Internal hook point for recursion
         * 
         * @param authorisations
         * @param nodeRef
         * @param denied
         * @param recursiveIn
         * @return true if granted
         */
        boolean evaluate(String authority, Long aclId)
        {
            // Start out true and "and" all other results
            boolean success = true;

            // Check the required permissions but not for sets they rely on
            // their underlying permissions
            //if (modelDAO.checkPermission(required))
            //{

                // We have to do the test as no parent will help us out
                success &= hasSinglePermission(authority, aclId);

                if (!success)
                {
                    return false;
                }
            //}

            // Check the other permissions required on the node
            for (PermissionReference pr : nodeRequirements)
            {
                // Build a new test
                UnconditionalAclTest nt = new UnconditionalAclTest(pr);
                success &= nt.evaluate(authority, aclId);
                if (!success)
                {
                    return false;
                }
            }

            return success;
        }

        boolean hasSinglePermission(String authority, Long aclId)
        {
            // Check global permission

            if (checkGlobalPermissions(authority))
            {
                return true;
            }

            if(aclId == null)
            {
                return false;
            }
            else
            {
                return checkRequired(authority, aclId);
            }

        }

        /**
         * Check if we have a global permission
         * 
         * @param authorisations
         * @return true if granted
         */
        private boolean checkGlobalPermissions(String authority)
        {
            for (PermissionEntry pe : modelDAO.getGlobalPermissionEntries())
            {
                if (isGranted(pe, authority))
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * Check that a given authentication is available on a node
         * 
         * @param authorisations
         * @param nodeRef
         * @param denied
         * @return true if a check is required
         */
        boolean checkRequired(String authority, Long aclId)
        {
            AccessControlList acl = aclDaoComponent.getAccessControlList(aclId);

            if (acl == null)
            {
                return false;
            }

            Set<Pair<String, PermissionReference>> denied = new HashSet<Pair<String, PermissionReference>>();

            // Check if each permission allows - the first wins.
            // We could have other voting style mechanisms here
            for (AccessControlEntry ace : acl.getEntries())
            {
                if (isGranted(ace, authority, denied))
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * Is a permission granted
         * 
         * @param pe -
         *            the permissions entry to consider
         * @param granters -
         *            the set of granters
         * @param authorisations -
         *            the set of authorities
         * @param denied -
         *            the set of denied permissions/authority pais
         * @return true if granted
         */
        private boolean isGranted(AccessControlEntry ace, String authority, Set<Pair<String, PermissionReference>> denied)
        {
            // If the permission entry denies then we just deny
            if (ace.getAccessStatus() == AccessStatus.DENIED)
            {
                denied.add(new Pair<String, PermissionReference>(ace.getAuthority(), ace.getPermission()));

                Set<PermissionReference> granters = modelDAO.getGrantingPermissions(ace.getPermission());
                for (PermissionReference granter : granters)
                {
                    denied.add(new Pair<String, PermissionReference>(ace.getAuthority(), granter));
                }

                // All the things granted by this permission must be
                // denied
                Set<PermissionReference> grantees = modelDAO.getGranteePermissions(ace.getPermission());
                for (PermissionReference grantee : grantees)
                {
                    denied.add(new Pair<String, PermissionReference>(ace.getAuthority(), grantee));
                }

                // All permission excludes all permissions available for
                // the node.
                if (ace.getPermission().equals(getAllPermissionReference()) || ace.getPermission().equals(OLD_ALL_PERMISSIONS_REFERENCE))
                {
                    for (PermissionReference deny : modelDAO.getAllPermissions())
                    {
                        denied.add(new Pair<String, PermissionReference>(ace.getAuthority(), deny));
                    }
                }

                return false;
            }

            // The permission is allowed but we deny it as it is in the denied
            // set

            if (denied != null)
            {
                Pair<String, PermissionReference> specific = new Pair<String, PermissionReference>(ace.getAuthority(), required);
                if (denied.contains(specific))
                {
                    return false;
                }
            }

            // any deny denies

//            if (false)
//            {
//                if (denied != null)
//                {
//                    for (String auth : authorisations)
//                    {
//                        Pair<String, PermissionReference> specific = new Pair<String, PermissionReference>(auth, required);
//                        if (denied.contains(specific))
//                        {
//                            return false;
//                        }
//                        for (PermissionReference perm : granters)
//                        {
//                            specific = new Pair<String, PermissionReference>(auth, perm);
//                            if (denied.contains(specific))
//                            {
//                                return false;
//                            }
//                        }
//                    }
//                }
//            }

            // If the permission has a match in both the authorities and
            // granters list it is allowed
            // It applies to the current user and it is granted
            if (authority.equals(ace.getAuthority()) && granters.contains(ace.getPermission()))
            {
                {
                    return true;
                }
            }

            // Default deny
            return false;
        }

        private boolean isGranted(PermissionEntry pe, String authority)
        {
            // If the permission entry denies then we just deny
            if (pe.isDenied())
            {
                return false;
            }

            // If the permission has a match in both the authorities and
            // granters list it is allowed
            // It applies to the current user and it is granted
            if (granters.contains(pe.getPermissionReference()) && authority.equals(pe.getAuthority()))
            {
                {
                    return true;
                }
            }

            // Default deny
            return false;
        }
    }
    
    private static class MutableBoolean
    {
        private boolean value;

        MutableBoolean(boolean value)
        {
            this.value = value;
        }

        void setValue(boolean value)
        {
            this.value = value;
        }

        boolean getValue()
        {
            return value;
        }
    }
    
    /**
     * This methods checks whether the specified nodeRef instance is a version nodeRef (ie. in the 'version' store)
     * 
     * @param nodeRef - version nodeRef
     * @return <b>true</b> if version nodeRef <b>false</b> otherwise
     */
    private boolean isVersionNodeRef(NodeRef nodeRef)
    {
    	return nodeRef.getStoreRef().getProtocol().equals(VersionModel.STORE_PROTOCOL);
    }

    /**
     * Converts specified version nodeRef (eg. versionStore://...) to versioned nodeRef (eg. workspace://SpacesStore/...)
     * 
     * @param nodeRef - <b>always</b> version nodeRef (ie. in the 'version' store)
     * @return versioned nodeRef (ie.in the 'live' store)
     */
    @SuppressWarnings("deprecation")
    private NodeRef convertVersionNodeRefToVersionedNodeRef(NodeRef versionNodeRef)
    {
        Map<QName, Serializable> properties = nodeService.getProperties(versionNodeRef);
        
        NodeRef nodeRef = null;
        
        // Switch VersionStore depending on configured impl
        if (versionNodeRef.getStoreRef().getIdentifier().equals(Version2Model.STORE_ID))
        {
            // V2 version store (eg. workspace://version2Store)
            nodeRef = (NodeRef)properties.get(Version2Model.PROP_QNAME_FROZEN_NODE_REF);
        } 
        else if (versionNodeRef.getStoreRef().getIdentifier().equals(VersionModel.STORE_ID))
        {
            // Deprecated V1 version store (eg. workspace://lightWeightVersionStore)
            nodeRef = new NodeRef((String) properties.get(VersionModel.PROP_QNAME_FROZEN_NODE_STORE_PROTOCOL),
                                  (String) properties.get(VersionModel.PROP_QNAME_FROZEN_NODE_STORE_ID),
                                  (String) properties.get(VersionModel.PROP_QNAME_FROZEN_NODE_ID));
        }
        
        return nodeRef;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAuthorisations()
    {
        // Use TX cache 
        
        @SuppressWarnings("unchecked")
        Set<String> auths = (Set<String>) AlfrescoTransactionSupport.getResource("MyAuthCache");
        Authentication auth = AuthenticationUtil.getRunAsAuthentication();
        User user = (User) auth.getPrincipal();
        if(auths != null)
        {
            if(!auths.contains(user.getUsername()))
            {
                auths = null;
            }
        }
        if (auths == null)
        {
            auths = new HashSet<String>();
              
            // No authenticated user then no permissions
            if (auth != null)
            {
                
                auths.add(user.getUsername());
                for (GrantedAuthority authority : auth.getAuthorities())
                {
                    auths.add(authority.getAuthority());
                }
                auths.addAll(authorityService.getAuthoritiesForUser(user.getUsername()));
            }
            
            AlfrescoTransactionSupport.bindResource("MyAuthCache", auths);
        }
        return Collections.unmodifiableSet(auths);   
    }
}
