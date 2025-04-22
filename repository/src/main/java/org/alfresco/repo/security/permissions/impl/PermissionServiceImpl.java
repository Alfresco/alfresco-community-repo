/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.security.permissions.impl;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.domain.permissions.FixedAclUpdater;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.authority.AuthorityServiceImpl;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.repo.security.permissions.AccessControlListProperties;
import org.alfresco.repo.security.permissions.DynamicAuthority;
import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.PermissionServicePolicies;
import org.alfresco.repo.security.permissions.PermissionServicePolicies.OnGrantLocalPermission;
import org.alfresco.repo.security.permissions.PermissionServicePolicies.OnInheritPermissionsDisabled;
import org.alfresco.repo.security.permissions.PermissionServicePolicies.OnInheritPermissionsEnabled;
import org.alfresco.repo.security.permissions.PermissionServicePolicies.OnRevokeLocalPermission;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.repo.security.permissions.impl.traitextender.PermissionServiceExtension;
import org.alfresco.repo.security.permissions.impl.traitextender.PermissionServiceTrait;
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
import org.alfresco.traitextender.AJProxyTrait;
import org.alfresco.traitextender.Extend;
import org.alfresco.traitextender.ExtendedTrait;
import org.alfresco.traitextender.Extensible;
import org.alfresco.traitextender.Trait;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;
import org.alfresco.util.PolicyIgnoreUtil;
import org.alfresco.util.PropertyCheck;

/**
 * The Alfresco implementation of a permissions service against our APIs for the permissions model and permissions persistence.
 * 
 * @author andyh
 */
public class PermissionServiceImpl extends AbstractLifecycleBean implements PermissionServiceSPI, Extensible
{
    static SimplePermissionReference OLD_ALL_PERMISSIONS_REFERENCE = new SimplePermissionReference(
            QName.createQName("", PermissionService.ALL_PERMISSIONS),
            PermissionService.ALL_PERMISSIONS);

    private static Log log = LogFactory.getLog(PermissionServiceImpl.class);

    /** a transactionally-safe cache to be injected */
    protected SimpleCache<Serializable, AccessStatus> accessCache;

    protected SimpleCache<Serializable, Set<String>> readersCache;

    protected SimpleCache<Serializable, Set<String>> readersDeniedCache;

    /* Access to the model */
    protected ModelDAO modelDAO;

    /* Access to permissions */
    protected PermissionsDaoComponent permissionsDaoComponent;

    /* Access to the node service */
    protected NodeService nodeService;

    /* Access to the tenant service */
    protected TenantService tenantService;

    /* Access to the data dictionary */
    protected DictionaryService dictionaryService;

    /* Access to the ownable service */
    protected OwnableService ownableService;

    /* Access to the authority component */
    protected AuthorityService authorityService;

    /* Dynamic authorities providers */
    protected List<DynamicAuthority> dynamicAuthorities;

    protected PolicyComponent policyComponent;

    protected AclDAO aclDaoComponent;

    protected PermissionReference allPermissionReference;

    protected FixedAclUpdater fixedAclUpdater;

    protected boolean anyDenyDenies = false;

    private final ExtendedTrait<PermissionServiceTrait> permissionServiceTrait;

    private ClassPolicyDelegate<OnGrantLocalPermission> onGrantLocalPermissionDelegate;
    private ClassPolicyDelegate<OnRevokeLocalPermission> onRevokeLocalPermissionDelegate;
    private ClassPolicyDelegate<OnInheritPermissionsEnabled> onInheritPermissionsEnabledDelegate;
    private ClassPolicyDelegate<OnInheritPermissionsDisabled> onInheritPermissionsDisabledDelegate;

    private PolicyIgnoreUtil policyIgnoreUtil;

    /**
     * Standard spring construction.
     */
    public PermissionServiceImpl()
    {
        super();
        permissionServiceTrait = new ExtendedTrait<PermissionServiceTrait>(AJProxyTrait.create(this, PermissionServiceTrait.class));
    }

    //
    // Inversion of control
    //

    /**
     * Set the dictionary service
     * 
     * @param dictionaryService
     *            DictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param anyDenyDenies
     *            the anyDenyDenies to set
     */
    public void setAnyDenyDenies(boolean anyDenyDenies)
    {
        this.anyDenyDenies = anyDenyDenies;
        accessCache.clear();
        readersCache.clear();
        readersDeniedCache.clear();
    }

    public boolean getAnyDenyDenies()
    {
        return anyDenyDenies;
    }

    /**
     * Set the permissions model dao
     * 
     * @param modelDAO
     *            ModelDAO
     */
    public void setModelDAO(ModelDAO modelDAO)
    {
        this.modelDAO = modelDAO;
    }

    /**
     * Set the node service.
     * 
     * @param nodeService
     *            NodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the ownable service.
     * 
     * @param ownableService
     *            OwnableService
     */
    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }

    /**
     * Set the tenant service.
     * 
     * @param tenantService
     *            TenantService
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * Set the permissions dao component
     * 
     * @param permissionsDaoComponent
     *            PermissionsDaoComponent
     */
    public void setPermissionsDaoComponent(PermissionsDaoComponent permissionsDaoComponent)
    {
        this.permissionsDaoComponent = permissionsDaoComponent;
    }

    /**
     * Set the authority service.
     * 
     * @param authorityService
     *            AuthorityService
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * Set the dynamic authorities
     * 
     */
    public void setDynamicAuthorities(List<DynamicAuthority> dynamicAuthorities)
    {
        this.dynamicAuthorities = dynamicAuthorities;
    }

    /**
     * Set the ACL DAO component.
     * 
     * @param aclDaoComponent
     *            AclDAO
     */
    public void setAclDAO(AclDAO aclDaoComponent)
    {
        this.aclDaoComponent = aclDaoComponent;
    }

    public void setFixedAclUpdater(FixedAclUpdater fixedAclUpdater)
    {
        this.fixedAclUpdater = fixedAclUpdater;
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
     * @param readersCache
     *            the readersCache to set
     */
    public void setReadersCache(SimpleCache<Serializable, Set<String>> readersCache)
    {
        this.readersCache = readersCache;
    }

    /**
     * @param readersDeniedCache
     *            the readersDeniedCache to set
     */
    public void setReadersDeniedCache(SimpleCache<Serializable, Set<String>> readersDeniedCache)
    {
        this.readersDeniedCache = readersDeniedCache;
    }

    /**
     * Set the policy component
     * 
     * @param policyComponent
     *            PolicyComponent
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setPolicyIgnoreUtil(PolicyIgnoreUtil policyIgnoreUtil)
    {
        this.policyIgnoreUtil = policyIgnoreUtil;
    }

    /**
     * Cache clear on move node
     * 
     * @param oldChildAssocRef
     *            ChildAssociationRef
     * @param newChildAssocRef
     *            ChildAssociationRef
     */
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        accessCache.clear();
    }

    /**
     * Cache clear on create of a child association from an authority container.
     * 
     * @param childAssocRef
     *            ChildAssociationRef
     */
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef)
    {
        accessCache.clear();
    }

    /**
     * Cache clear on delete of a child association from an authority container.
     * 
     * @param childAssocRef
     *            ChildAssociationRef
     */
    public void beforeDeleteChildAssociation(ChildAssociationRef childAssocRef)
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
    {}

    public void init()
    {
        policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onMoveNode"), ContentModel.TYPE_BASE, new JavaBehaviour(this, "onMoveNode"));

        policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateChildAssociation"), ContentModel.TYPE_AUTHORITY_CONTAINER, new JavaBehaviour(this, "onCreateChildAssociation"));
        policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteChildAssociation"), ContentModel.TYPE_AUTHORITY_CONTAINER, new JavaBehaviour(this, "beforeDeleteChildAssociation"));

        onGrantLocalPermissionDelegate = policyComponent.registerClassPolicy(PermissionServicePolicies.OnGrantLocalPermission.class);
        onRevokeLocalPermissionDelegate = policyComponent.registerClassPolicy(PermissionServicePolicies.OnRevokeLocalPermission.class);
        onInheritPermissionsEnabledDelegate = policyComponent.registerClassPolicy(PermissionServicePolicies.OnInheritPermissionsEnabled.class);
        onInheritPermissionsDisabledDelegate = policyComponent.registerClassPolicy(PermissionServicePolicies.OnInheritPermissionsDisabled.class);
    }

    //
    // Permissions Service
    //

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public String getOwnerAuthority()
    {
        return OWNER_AUTHORITY;
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public String getAllAuthorities()
    {
        return ALL_AUTHORITIES;
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public String getAllPermission()
    {
        return ALL_PERMISSIONS;
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public Set<AccessPermission> getPermissions(NodeRef nodeRef)
    {
        return getAllPermissionsImpl(nodeRef, true, true);
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
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

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
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

    protected Set<AccessPermission> getAllPermissionsImpl(NodeRef nodeRef, boolean includeTrue, boolean includeFalse)
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

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
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

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
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

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public NodePermissionEntry getSetPermissions(NodeRef nodeRef)
    {
        return permissionsDaoComponent.getPermissions(tenantService.getName(nodeRef));
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public NodePermissionEntry getSetPermissions(StoreRef storeRef)
    {
        return permissionsDaoComponent.getPermissions(storeRef);
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
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

        // Note: if we're directly accessing a frozen state (version) node (ie. in the 'version' store) we need to check permissions for the versioned node (ie. in the 'live' store)
        if (isVersionNodeRef(passedNodeRef))
        {
            passedNodeRef = convertVersionNodeRefToVersionedNodeRef(VersionUtil.convertNodeRef(passedNodeRef));
        }

        // Allow permissions for nodes that do not exist
        if (passedNodeRef == null || !nodeService.exists(passedNodeRef))
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
            if (auth != null)
            {
                String user = AuthenticationUtil.getRunAsUser();
                for (String dynamicAuthority : getDynamicAuthorities(auth, nodeRef, perm))
                {
                    context.addDynamicAuthorityAssignment(user, dynamicAuthority);
                }
            }
            return hasPermission(properties.getId(), context, perm);
        }

        // Get the current authentications
        // Use the smart authentication cache to improve permissions performance
        Authentication auth = AuthenticationUtil.getRunAsAuthentication();
        final Set<String> authorisations = getAuthorisations(auth, nodeRef, perm);

        // If the node does not support the given permission there is no point
        // doing the test
        Set<PermissionReference> available = AuthenticationUtil.runAs(new RunAsWork<Set<PermissionReference>>() {
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

        return AuthenticationUtil.runAs(new RunAsWork<AccessStatus>() {

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

                /* Does the current authentication have the supplied permission on the given node. */

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

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public AccessStatus hasPermission(Long aclID, PermissionContext context, String permission)
    {
        return hasPermission(aclID, context, getPermissionReference(permission));
    }

    protected AccessStatus hasPermission(Long aclId, PermissionContext context, PermissionReference permission)
    {
        if (aclId == null)
        {
            // Enforce store ACLs if set
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

        Set<PermissionReference> available = AuthenticationUtil.runAs(new RunAsWork<Set<PermissionReference>>() {
            public Set<PermissionReference> doWork() throws Exception
            {
                return modelDAO.getAllPermissions(typeQname, aspectQNames);
            }

        }, AuthenticationUtil.getSystemUserName());
        available.add(getAllPermissionReference());
        available.add(OLD_ALL_PERMISSIONS_REFERENCE);

        if (!(available.contains(permission)))
        {
            Set<PermissionReference> permissionsSystemBase = AuthenticationUtil.runAsSystem(new RunAsWork<Set<PermissionReference>>() {
                public Set<PermissionReference> doWork() throws Exception
                {
                    return modelDAO.getAllPermissions(ContentModel.TYPE_BASE, aspectQNames);
                }
            });
            if (permissionsSystemBase.contains(permission) && authorisations.contains(AuthenticationUtil.getAdminRoleName()))
            {
                return AccessStatus.ALLOWED;
            }
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
     * Control permissions cache - only used when we do old style permission evaluations - which should only be in DM stores where no permissions have been set
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
     * Key for a cache object is built from all the known Authorities (which can change dynamically so they must all be used) the NodeRef ID and the permission reference itself. This gives a unique key for each permission test.
     */
    Serializable generateKey(Set<String> auths, NodeRef nodeRef, PermissionReference perm, CacheType type)
    {
        LinkedHashSet<Serializable> key = new LinkedHashSet<Serializable>();
        key.add(perm.toString());
        // We will just have to key our dynamic sets by username. We wrap it so as not to be confused with a static set
        if (auths instanceof AuthorityServiceImpl.UserAuthoritySet)
        {
            key.add((Serializable) Collections.singleton(((AuthorityServiceImpl.UserAuthoritySet) auths).getUsername()));
        }
        else
        {
            key.addAll(auths);
        }
        key.add(nodeRef);
        // Ensure some concept of node version or transaction is included in the key so we can track without cache replication
        NodeRef.Status nodeStatus = nodeService.getNodeStatus(nodeRef);
        key.add(nodeStatus == null ? "null" : nodeStatus.getChangeTxnId());
        key.add(type);
        return key;
    }

    /**
     * Get the core authorisations for this {@code auth}. If {@code null} this will be an empty set. Otherwise it will be a Lazy loaded Set of authorities from the authority node structure PLUS any granted authorities.
     */
    protected Set<String> getCoreAuthorisations(Authentication auth)
    {
        if (auth == null)
        {
            return Collections.<String> emptySet();
        }

        User user = (User) auth.getPrincipal();
        String username = user.getUsername();
        Set<String> auths = authorityService.getAuthoritiesForUser(username);

        auths.add(username);

        for (GrantedAuthority grantedAuthority : auth.getAuthorities())
        {
            auths.add(grantedAuthority.getAuthority());
        }
        return auths;
    }

    /**
     * Get the authorisations for the currently authenticated user
     * 
     * @param auth
     *            Authentication
     * @param nodeRef
     *            NodeRef
     * @param required
     *            PermissionReference
     * @return the set of authorisations
     */
    protected Set<String> getAuthorisations(Authentication auth, NodeRef nodeRef, PermissionReference required)
    {
        Set<String> auths = getCoreAuthorisations(auth);
        if (auth != null)
        {
            auths.addAll(getDynamicAuthorities(auth, nodeRef, required));
        }
        return auths;
    }

    protected Set<String> getDynamicAuthorities(Authentication auth, NodeRef nodeRef, PermissionReference required)
    {
        Set<String> dynAuths = new HashSet<String>(64);
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
                            dynAuths.add(da.getAuthority());
                        }
                    }
                }
            }
        }
        return dynAuths;
    }

    protected Set<String> getAuthorisations(Authentication auth, PermissionContext context)
    {
        Set<String> auths = getCoreAuthorisations(auth);
        if (auth != null)
        {
            if (context != null)
            {
                auths.addAll(getDynamicAuthorities(auth, context, auths));
            }
        }
        return auths;
    }

    protected Set<String> getDynamicAuthorities(Authentication auth, PermissionContext context, Set<String> auths)
    {
        Set<String> dynAuths = new HashSet<String>();
        Map<String, Set<String>> dynamicAuthorityAssignments = context.getDynamicAuthorityAssignment();
        for (String dynKey : dynamicAuthorityAssignments.keySet())
        {
            if (auths.contains(dynKey))
            {
                Set<String> dynos = dynamicAuthorityAssignments.get(dynKey);
                if (dynos != null)
                {
                    dynAuths.addAll(dynos);
                }
            }
        }
        return dynAuths;
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public NodePermissionEntry explainPermission(NodeRef nodeRef, PermissionReference perm)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public void clearPermission(StoreRef storeRef, String authority)
    {
        permissionsDaoComponent.deletePermissions(storeRef, authority);
        accessCache.clear();
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public void deletePermission(StoreRef storeRef, String authority, String perm)
    {
        deletePermission(storeRef, authority, getPermissionReference(perm));
    }

    protected void deletePermission(StoreRef storeRef, String authority, PermissionReference perm)
    {
        permissionsDaoComponent.deletePermission(storeRef, authority, perm);
        accessCache.clear();
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public void deletePermissions(StoreRef storeRef)
    {
        permissionsDaoComponent.deletePermissions(storeRef);
        accessCache.clear();
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public void setPermission(StoreRef storeRef, String authority, String perm, boolean allow)
    {
        setPermission(storeRef, authority, getPermissionReference(perm), allow);
    }

    protected void setPermission(StoreRef storeRef, String authority, PermissionReference permission, boolean allow)
    {
        permissionsDaoComponent.setPermission(storeRef, authority, permission, allow);
        accessCache.clear();
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public void deletePermissions(NodeRef nodeRef)
    {
        permissionsDaoComponent.deletePermissions(tenantService.getName(nodeRef));
        accessCache.clear();

        invokeUpdateLocalPermissionsPolicy(nodeRef, null, null, false);
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public void deletePermissions(NodePermissionEntry nodePermissionEntry)
    {
        permissionsDaoComponent.deletePermissions(tenantService.getName(nodePermissionEntry.getNodeRef()));
        accessCache.clear();
    }

    /**
     * @see #deletePermission(NodeRef, String, PermissionReference)
     */
    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public void deletePermission(PermissionEntry permissionEntry)
    {
        NodeRef nodeRef = permissionEntry.getNodeRef();
        String authority = permissionEntry.getAuthority();
        PermissionReference permission = permissionEntry.getPermissionReference();
        deletePermission(nodeRef, authority, permission);
    }

    protected void deletePermission(NodeRef nodeRef, String authority, PermissionReference perm)
    {
        permissionsDaoComponent.deletePermission(tenantService.getName(nodeRef), authority, perm);
        accessCache.clear();

        invokeUpdateLocalPermissionsPolicy(nodeRef, authority, (perm != null ? perm.getName() : null), false);
    }

    private void invokeUpdateLocalPermissionsPolicy(NodeRef nodeRef, String authority, String permission, boolean grantPermission)
    {
        if (!policyIgnoreUtil.ignorePolicy(nodeRef))
        {
            if (grantPermission)
            {
                OnGrantLocalPermission grantPermPolicy = onGrantLocalPermissionDelegate.get(nodeService.getType(nodeRef));
                grantPermPolicy.onGrantLocalPermission(nodeRef, authority, permission);
            }
            else
            {
                OnRevokeLocalPermission revokePermPolicy = onRevokeLocalPermissionDelegate.get(nodeService.getType(nodeRef));
                revokePermPolicy.onRevokeLocalPermission(nodeRef, authority, permission);
            }
        }
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public void clearPermission(NodeRef nodeRef, String authority)
    {
        permissionsDaoComponent.deletePermissions(tenantService.getName(nodeRef), authority);
        accessCache.clear();
    }

    protected void setPermission(NodeRef nodeRef, String authority, PermissionReference perm, boolean allow)
    {
        permissionsDaoComponent.setPermission(tenantService.getName(nodeRef), authority, perm, allow);
        accessCache.clear();

        invokeUpdateLocalPermissionsPolicy(nodeRef, authority, perm.getName(), allow);
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public void setPermission(PermissionEntry permissionEntry)
    {
        // TODO - not MT-enabled nodeRef - currently only used by tests
        permissionsDaoComponent.setPermission(permissionEntry);
        accessCache.clear();
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public void setPermission(NodePermissionEntry nodePermissionEntry)
    {
        // TODO - not MT-enabled nodeRef- currently only used by tests
        permissionsDaoComponent.setPermission(nodePermissionEntry);
        accessCache.clear();
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public void setInheritParentPermissions(NodeRef nodeRef, boolean inheritParentPermissions)
    {
        NodeRef actualRef = tenantService.getName(nodeRef);
        permissionsDaoComponent.setInheritParentPermissions(actualRef, inheritParentPermissions);
        accessCache.clear();

        invokeOnPermissionsInheritedPolicy(nodeRef, inheritParentPermissions, false);
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public void setInheritParentPermissions(NodeRef nodeRef, final boolean inheritParentPermissions, boolean asyncCall)
    {
        final NodeRef actualRef = tenantService.getName(nodeRef);
        if (asyncCall)
        {
            // use transaction resource to determine later on in ADMAccessControlListDAO.setFixedAcl if asynchronous call may be required
            AlfrescoTransactionSupport.bindResource(FixedAclUpdater.FIXED_ACL_ASYNC_CALL_KEY, true);
            permissionsDaoComponent.setInheritParentPermissions(actualRef, inheritParentPermissions);
            // check if asynchronous call was required
            boolean asyncCallRequired = toBoolean((Boolean) AlfrescoTransactionSupport.getResource(FixedAclUpdater.FIXED_ACL_ASYNC_REQUIRED_KEY));
            if (asyncCallRequired)
            {
                // after transaction is committed FixedAclUpdater will be started in a new thread to process pending nodes
                AlfrescoTransactionSupport.bindListener(fixedAclUpdater);
            }
            invokeOnPermissionsInheritedPolicy(nodeRef, inheritParentPermissions, asyncCallRequired);
        }
        else
        {
            // regular method call
            permissionsDaoComponent.setInheritParentPermissions(actualRef, inheritParentPermissions);

            invokeOnPermissionsInheritedPolicy(nodeRef, inheritParentPermissions, false);
        }

        accessCache.clear();
    }

    private void invokeOnPermissionsInheritedPolicy(NodeRef nodeRef, final boolean inheritParentPermissions, boolean async)
    {
        if (!policyIgnoreUtil.ignorePolicy(nodeRef))
        {
            if (inheritParentPermissions)
            {
                OnInheritPermissionsEnabled onInheritEnabledPolicy = onInheritPermissionsEnabledDelegate.get(ContentModel.TYPE_BASE);
                onInheritEnabledPolicy.onInheritPermissionsEnabled(nodeRef);
            }
            else
            {
                OnInheritPermissionsDisabled onInheritDisabledPolicy = onInheritPermissionsDisabledDelegate.get(ContentModel.TYPE_BASE);
                onInheritDisabledPolicy.onInheritPermissionsDisabled(nodeRef, async);
            }
        }
    }

    /**
     * @see org.alfresco.service.cmr.security.PermissionService#getInheritParentPermissions(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public boolean getInheritParentPermissions(NodeRef nodeRef)
    {
        return permissionsDaoComponent.getInheritParentPermissions(tenantService.getName(nodeRef));
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public PermissionReference getPermissionReference(QName qname, String permissionName)
    {
        return modelDAO.getPermissionReference(qname, permissionName);
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public PermissionReference getAllPermissionReference()
    {
        return allPermissionReference;
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
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

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public PermissionReference getPermissionReference(String permissionName)
    {
        return modelDAO.getPermissionReference(null, permissionName);
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public Set<PermissionReference> getSettablePermissionReferences(QName type)
    {
        return modelDAO.getExposedPermissions(type);
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public Set<PermissionReference> getSettablePermissionReferences(NodeRef nodeRef)
    {
        return modelDAO.getExposedPermissions(tenantService.getName(nodeRef));
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public void deletePermission(NodeRef nodeRef, String authority, String perm)
    {
        deletePermission(nodeRef, authority, getPermissionReference(perm));
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public AccessStatus hasPermission(NodeRef nodeRef, String perm)
    {
        return hasPermission(nodeRef, getPermissionReference(perm));
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public void setPermission(NodeRef nodeRef, String authority, String perm, boolean allow)
    {
        setPermission(nodeRef, authority, getPermissionReference(perm), allow);
    }

    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public void deletePermissions(String recipient)
    {
        permissionsDaoComponent.deletePermissions(recipient);
        accessCache.clear();
    }

    /**
     * Optimised read permission evaluation caveats: doesn't take into account dynamic authorities/groups doesn't take into account node types/aspects for permissions
     * 
     */
    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
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
        Boolean forceHasPermission = (Boolean) AlfrescoTransactionSupport.getResource("forceHasPermission");
        if (forceHasPermission == null)
        {
            for (DynamicAuthority dynamicAuthority : dynamicAuthorities)
            {
                String authority = dynamicAuthority.getAuthority();
                Set<PermissionReference> requiredFor = dynamicAuthority.requiredFor();
                if (authority != PermissionService.OWNER_AUTHORITY &&
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

        if (forceHasPermission == Boolean.TRUE)
        {
            return hasPermission(nodeRef, PermissionService.READ);
        }

        Long aclID = nodeService.getNodeAclId(nodeRef);
        if (aclID == null)
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

    protected AccessStatus adminRead()
    {
        AccessStatus result = AccessStatus.DENIED;

        Set<String> authorisations = getAuthorisations();
        if (authorisations.contains(AuthenticationUtil.getAdminRoleName()))
        {
            result = AccessStatus.ALLOWED;
        }

        // ROLE_ADMINISTRATOR authority has FULL_CONTROL in permissionDefinitions
        // so we don't need to check node requirements
        return result;
    }

    protected AccessStatus ownerRead(String username, NodeRef nodeRef)
    {
        // Reviewed the behaviour of deny and ownership with Mike F
        // ATM ownership takes precendence over READ deny
        // TODO: check that global owner rights are set

        AccessStatus result = AccessStatus.DENIED;

        String owner = ownableService.getOwner(nodeRef);
        if (owner == null)
        {
            // TODO node may not have auditable aspect and hence creator property
            result = AccessStatus.DENIED;
        }

        // is the user the owner of the node?
        if (EqualsHelper.nullSafeEquals(username, owner))
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
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public Set<String> getReaders(Long aclId)
    {
        AccessControlList acl = aclDaoComponent.getAccessControlList(aclId);
        if (acl == null)
        {
            return Collections.emptySet();
        }

        Set<String> aclReaders = readersCache.get((Serializable) acl.getProperties());
        if (aclReaders != null)
        {
            return aclReaders;
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

        aclReaders = Collections.unmodifiableSet(readers);
        readersCache.put((Serializable) acl.getProperties(), aclReaders);
        return aclReaders;
    }

    /**
     * @param aclId
     *            Long
     * @return set of authorities denied permission on the ACL
     */
    @Override
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public Set<String> getReadersDenied(Long aclId)
    {
        AccessControlList acl = aclDaoComponent.getAccessControlList(aclId);

        if (acl == null)
        {
            return Collections.emptySet();
        }
        Set<String> denied = readersDeniedCache.get(aclId);
        if (denied != null)
        {
            return denied;
        }
        denied = new HashSet<String>();
        Set<String> assigned = new HashSet<String>();

        for (AccessControlEntry ace : acl.getEntries())
        {
            assigned.add(ace.getAuthority());
        }

        for (String authority : assigned)
        {
            UnconditionalDeniedAclTest test = new UnconditionalDeniedAclTest(getPermissionReference(PermissionService.READ));
            if (test.evaluate(authority, aclId))
            {
                denied.add(authority);
            }
        }

        readersDeniedCache.put((Serializable) acl.getProperties(), denied);

        return denied;
    }

    protected AccessStatus canRead(Long aclId)
    {
        Set<String> authorities = getAuthorisations();

        // test denied

        if (anyDenyDenies)
        {

            Set<String> aclReadersDenied = getReadersDenied(aclId);

            for (String auth : aclReadersDenied)
            {
                if (authorities.contains(auth))
                {
                    return AccessStatus.DENIED;
                }
            }

        }

        // test acl readers
        Set<String> aclReaders = getReaders(aclId);

        for (String auth : aclReaders)
        {
            if (authorities.contains(auth))
            {
                return AccessStatus.ALLOWED;
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
     * Not fixed up for deny as should not be used
     * 
     * @author Andy Hind
     */
    protected class NodeTest
    {
        /* The required permission. */
        PermissionReference required;

        /* Granters of the permission */
        Set<PermissionReference> granters;

        /* The additional permissions required at the node level. */
        Set<PermissionReference> nodeRequirements = new HashSet<PermissionReference>();

        /* The additional permissions required on the parent. */
        Set<PermissionReference> parentRequirements = new HashSet<PermissionReference>();

        /* The permissions required on all children . */
        Set<PermissionReference> childrenRequirements = new HashSet<PermissionReference>();

        /* The type name of the node. */
        QName typeQName;

        /* The aspects set on the node. */
        Set<QName> aspectQNames;

        /* Constructor just gets the additional requirements */
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
         * 
         * @return true if allowed
         */
        boolean evaluate(Set<String> authorisations, NodeRef nodeRef)
        {
            Set<Pair<String, PermissionReference>> denied = new HashSet<Pair<String, PermissionReference>>();
            return evaluate(authorisations, nodeRef, denied, null);
        }

        /**
         * Internal hook point for recursion
         * 
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
         * @param pe
         *            - the permissions entry to consider
         * @param authorisations
         *            - the set of authorities
         * @param denied
         *            - the set of denied permissions/authority pais
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

            if (anyDenyDenies)
            {
                if (denied != null)
                {
                    for (String auth : authorisations)
                    {
                        Pair<String, PermissionReference> specific = new Pair<String, PermissionReference>(auth, required);
                        if (denied.contains(specific))
                        {
                            return false;
                        }
                        for (PermissionReference perm : granters)
                        {
                            specific = new Pair<String, PermissionReference>(auth, perm);
                            if (denied.contains(specific))
                            {
                                return false;
                            }
                        }
                    }
                }
            }

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
     * Test a permission in the context of the new ACL implementation. All components of the ACL are in the object - there is no need to walk up the parent chain. Parent conditions cna not be applied as there is no context to do this. Child conditions can not be applied as there is no context to do this
     * 
     * @author andyh
     */

    protected class AclTest
    {
        /* The required permission. */
        PermissionReference required;

        /* Granters of the permission */
        Set<PermissionReference> granters;

        /* The additional permissions required at the node level. */
        Set<PermissionReference> nodeRequirements = new HashSet<PermissionReference>();

        /* The type name of the node. */
        QName typeQName;

        /* The aspects set on the node. */
        Set<QName> aspectQNames;

        /* Constructor just gets the additional requirements */
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
         *            Set<String>
         * @param aclId
         *            Long
         * @param context
         *            PermissionContext
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
         *            Set<String>
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
         *            Set<String>
         * @param aclId
         *            Long
         * @param context
         *            PermissionContext
         * @return true if a check is required
         */
        boolean checkRequired(Set<String> authorisations, Long aclId, PermissionContext context)
        {
            AccessControlList acl = aclDaoComponent.getAccessControlList(aclId);

            if (acl == null)
            {
                return false;
            }

            if (anyDenyDenies)
            {
                Set<Pair<String, PermissionReference>> allowed = new HashSet<Pair<String, PermissionReference>>();

                // Check if each permission allows - the first wins.
                // We could have other voting style mechanisms here
                for (AccessControlEntry ace : acl.getEntries())
                {
                    if (isDenied(ace, authorisations, allowed, context))
                    {
                        return false;
                    }
                }
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
         * @param ace
         *            AccessControlEntry
         * @param authorisations
         *            - the set of authorities
         * @param denied
         *            - the set of denied permissions/authority pais
         * @param context
         *            PermissionContext
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

        /**
         * Is a permission granted
         * 
         * @param ace
         *            AccessControlEntry
         * @param authorisations
         *            - the set of authorities
         * @param allowed
         *            - the set of denied permissions/authority pais
         * @param context
         *            PermissionContext
         * @return true if granted
         */
        private boolean isDenied(AccessControlEntry ace, Set<String> authorisations, Set<Pair<String, PermissionReference>> allowed, PermissionContext context)
        {
            // If the permission entry denies then we just deny
            if (ace.getAccessStatus() == AccessStatus.ALLOWED)
            {
                allowed.add(new Pair<String, PermissionReference>(ace.getAuthority(), ace.getPermission()));

                Set<PermissionReference> granters = modelDAO.getGrantingPermissions(ace.getPermission());
                for (PermissionReference granter : granters)
                {
                    allowed.add(new Pair<String, PermissionReference>(ace.getAuthority(), granter));
                }

                // All the things granted by this permission must be
                // denied
                Set<PermissionReference> grantees = modelDAO.getGranteePermissions(ace.getPermission());
                for (PermissionReference grantee : grantees)
                {
                    allowed.add(new Pair<String, PermissionReference>(ace.getAuthority(), grantee));
                }

                // All permission excludes all permissions available for
                // the node.
                if (ace.getPermission().equals(getAllPermissionReference()) || ace.getPermission().equals(OLD_ALL_PERMISSIONS_REFERENCE))
                {
                    for (PermissionReference deny : modelDAO.getAllPermissions(context.getType(), context.getAspects()))
                    {
                        allowed.add(new Pair<String, PermissionReference>(ace.getAuthority(), deny));
                    }
                }

                return false;
            }

            // The permission is denied but we allow it as it is in the allowed
            // set

            if (allowed != null)
            {
                Pair<String, PermissionReference> specific = new Pair<String, PermissionReference>(ace.getAuthority(), required);
                if (allowed.contains(specific))
                {
                    return false;
                }
            }

            // If the permission has a match in both the authorities and
            // granters list it is allowed
            // It applies to the current user and it is granted
            if (authorisations.contains(ace.getAuthority()) && granters.contains(ace.getPermission()))
            {
                {
                    return true;
                }
            }

            // Default allow
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
    protected class UnconditionalAclTest
    {
        /* The required permission. */
        PermissionReference required;

        /* Granters of the permission */
        Set<PermissionReference> granters;

        /* The additional permissions required at the node level. */
        Set<PermissionReference> nodeRequirements = new HashSet<PermissionReference>();

        /* Constructor just gets the additional requirements */
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

            if (modelDAO.getUnconditionalRequiredPermissions(required, RequiredPermission.On.CHILDREN).size() > 0)
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
         * @param authority
         *            String
         * @param aclId
         *            Long
         * @return true if granted
         */
        boolean evaluate(String authority, Long aclId)
        {
            // Start out true and "and" all other results
            boolean success = true;

            // Check the required permissions but not for sets they rely on
            // their underlying permissions
            // if (modelDAO.checkPermission(required))
            // {

            // We have to do the test as no parent will help us out
            success &= hasSinglePermission(authority, aclId);

            if (!success)
            {
                return false;
            }
            // }

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

            if (aclId == null)
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
         * @param authority
         *            String
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
         * @param authority
         *            String
         * @param aclId
         *            Long
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
         * @param ace
         *            AccessControlEntry
         * @param authority
         *            String
         * @param denied
         *            - the set of denied permissions/authority pais
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

    /**
     * Ignores type and aspect requirements on the node
     *
     */
    protected class UnconditionalDeniedAclTest
    {
        /* The required permission. */
        PermissionReference required;

        /* Granters of the permission */
        Set<PermissionReference> granters;

        /* The additional permissions required at the node level. */
        Set<PermissionReference> nodeRequirements = new HashSet<PermissionReference>();

        /* Constructor just gets the additional requirements */
        UnconditionalDeniedAclTest(PermissionReference required)
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

            if (modelDAO.getUnconditionalRequiredPermissions(required, RequiredPermission.On.CHILDREN).size() > 0)
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
         * @param authority
         *            String
         * @param aclId
         *            Long
         * @return true if granted
         */
        boolean evaluate(String authority, Long aclId)
        {
            // Start out true and "and" all other results
            boolean success = true;

            // Check the required permissions but not for sets they rely on
            // their underlying permissions
            // if (modelDAO.checkPermission(required))
            // {

            // We have to do the test as no parent will help us out
            success &= hasSinglePermission(authority, aclId);

            if (!success)
            {
                return false;
            }
            // }

            // Check the other permissions required on the node
            for (PermissionReference pr : nodeRequirements)
            {
                // Build a new test
                UnconditionalDeniedAclTest nt = new UnconditionalDeniedAclTest(pr);
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

            if (aclId == null)
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
         * @param authority
         *            String
         * @return true if granted
         */
        private boolean checkGlobalPermissions(String authority)
        {
            for (PermissionEntry pe : modelDAO.getGlobalPermissionEntries())
            {
                if (isDenied(pe, authority))
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * Check that a given authentication is available on a node
         * 
         * @param authority
         *            String
         * @param aclId
         *            Long
         * @return true if a check is required
         */
        boolean checkRequired(String authority, Long aclId)
        {
            AccessControlList acl = aclDaoComponent.getAccessControlList(aclId);

            if (acl == null)
            {
                return false;
            }

            Set<Pair<String, PermissionReference>> allowed = new HashSet<Pair<String, PermissionReference>>();

            // Check if each permission allows - the first wins.
            // We could have other voting style mechanisms here
            for (AccessControlEntry ace : acl.getEntries())
            {
                if (isDenied(ace, authority, allowed))
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * Is a permission granted
         * 
         * @param ace
         *            AccessControlEntry
         * @param authority
         *            String
         * @param allowed
         *            - the set of allowed permissions/authority pais
         * @return true if granted
         */
        private boolean isDenied(AccessControlEntry ace, String authority, Set<Pair<String, PermissionReference>> allowed)
        {
            // If the permission entry denies then we just deny
            if (ace.getAccessStatus() == AccessStatus.ALLOWED)
            {
                allowed.add(new Pair<String, PermissionReference>(ace.getAuthority(), ace.getPermission()));

                Set<PermissionReference> granters = modelDAO.getGrantingPermissions(ace.getPermission());
                for (PermissionReference granter : granters)
                {
                    allowed.add(new Pair<String, PermissionReference>(ace.getAuthority(), granter));
                }

                // All the things granted by this permission must be
                // denied
                Set<PermissionReference> grantees = modelDAO.getGranteePermissions(ace.getPermission());
                for (PermissionReference grantee : grantees)
                {
                    allowed.add(new Pair<String, PermissionReference>(ace.getAuthority(), grantee));
                }

                // All permission excludes all permissions available for
                // the node.
                if (ace.getPermission().equals(getAllPermissionReference()) || ace.getPermission().equals(OLD_ALL_PERMISSIONS_REFERENCE))
                {
                    for (PermissionReference deny : modelDAO.getAllPermissions())
                    {
                        allowed.add(new Pair<String, PermissionReference>(ace.getAuthority(), deny));
                    }
                }

                return false;
            }

            // The permission is allowed but we deny it as it is in the denied
            // set

            if (allowed != null)
            {
                Pair<String, PermissionReference> specific = new Pair<String, PermissionReference>(ace.getAuthority(), required);
                if (allowed.contains(specific))
                {
                    return false;
                }
            }

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

        private boolean isDenied(PermissionEntry pe, String authority)
        {
            // If the permission entry denies then we just deny
            if (pe.isAllowed())
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

    protected static class MutableBoolean
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
     * @param nodeRef
     *            - version nodeRef
     * @return <b>true</b> if version nodeRef <b>false</b> otherwise
     */
    protected boolean isVersionNodeRef(NodeRef nodeRef)
    {
        return nodeRef.getStoreRef().getProtocol().equals(VersionModel.STORE_PROTOCOL) || nodeRef.getStoreRef().getIdentifier().equals(Version2Model.STORE_ID);
    }

    /**
     * Converts specified version nodeRef (eg. versionStore://...) to versioned nodeRef (eg. workspace://SpacesStore/...)
     * 
     * @param versionNodeRef
     *            - <b>always</b> version nodeRef (ie. in the 'version' store)
     * @return versioned nodeRef (ie.in the 'live' store)
     */
    @SuppressWarnings("deprecation")
    protected NodeRef convertVersionNodeRefToVersionedNodeRef(NodeRef versionNodeRef)
    {
        Map<QName, Serializable> properties = nodeService.getProperties(versionNodeRef);

        NodeRef nodeRef = null;

        // Switch VersionStore depending on configured impl
        if (versionNodeRef.getStoreRef().getIdentifier().equals(Version2Model.STORE_ID))
        {
            // V2 version store (eg. workspace://version2Store)
            nodeRef = (NodeRef) properties.get(Version2Model.PROP_QNAME_FROZEN_NODE_REF);
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
    @Extend(traitAPI = PermissionServiceTrait.class, extensionAPI = PermissionServiceExtension.class)
    public Set<String> getAuthorisations()
    {
        // Use TX cache
        @SuppressWarnings("unchecked")
        Set<String> auths = (Set<String>) AlfrescoTransactionSupport.getResource("MyAuthCache");
        Authentication auth = AuthenticationUtil.getRunAsAuthentication();
        if (auths != null)
        {
            if (auth == null || !auths.contains(((User) auth.getPrincipal()).getUsername()))
            {
                auths = null;
            }
        }
        if (auths == null)
        {
            auths = getCoreAuthorisations(auth);
            AlfrescoTransactionSupport.bindResource("MyAuthCache", auths);
        }
        return Collections.unmodifiableSet(auths);
    }

    @Override
    public <M extends Trait> ExtendedTrait<M> getTrait(Class<? extends M> traitAPI)
    {
        return (ExtendedTrait<M>) permissionServiceTrait;
    }
}
