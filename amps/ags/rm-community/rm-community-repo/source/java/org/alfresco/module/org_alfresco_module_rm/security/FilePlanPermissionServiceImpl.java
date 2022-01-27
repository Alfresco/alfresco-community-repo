/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.security;

import static java.util.Collections.singletonMap;

import static org.alfresco.repo.policy.Behaviour.NotificationFrequency.TRANSACTION_COMMIT;
import static org.alfresco.repo.policy.annotation.BehaviourKind.CLASS;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.getSystemUserName;
import static org.alfresco.service.cmr.security.OwnableService.NO_OWNER;
import static org.alfresco.util.ParameterCheck.mandatory;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.module.org_alfresco_module_rm.audit.event.AuditEvent;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * File plan permission service.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@BehaviourBean
public class FilePlanPermissionServiceImpl extends    ServiceBaseImpl
                                           implements FilePlanPermissionService,
                                                      RMPermissionModel,
                                                      NodeServicePolicies.OnMoveNodePolicy
{
    /** An audit key for the set permission event. */
    private static final String AUDIT_SET_PERMISSION = "set-permission";

    /** An namespace to use when constructing QNames to use for auditing changes to permissions. */
    private static final String AUDIT_NAMESPACE = "audit://permissions/";

    /** Permission service */
    private PermissionService permissionService;

    /** Ownable service */
    private OwnableService ownableService;

    /** Policy component */
    private PolicyComponent policyComponent;

    /** Authority service */
    private AuthorityService authorityService;

    /** File plan role service */
    private FilePlanRoleService filePlanRoleService;

    /** File plan service */
    private FilePlanService filePlanService;

    /** The RM audit service. */
    private RecordsManagementAuditService recordsManagementAuditService;

    /** Logger */
    private static final Log LOGGER = LogFactory.getLog(FilePlanPermissionServiceImpl.class);

    /**
     * Initialisation method
     */
    public void init()
    {
        getPolicyComponent().bindClassBehaviour(
                NodeServicePolicies.OnAddAspectPolicy.QNAME,
                ASPECT_RECORD,
                new JavaBehaviour(this, "onAddRecord", TRANSACTION_COMMIT));
        getPolicyComponent().bindClassBehaviour(
                NodeServicePolicies.OnMoveNodePolicy.QNAME,
                ASPECT_RECORD,
                new JavaBehaviour(this, "onMoveRecord", TRANSACTION_COMMIT));
        getPolicyComponent().bindClassBehaviour(
                NodeServicePolicies.OnMoveNodePolicy.QNAME,
                TYPE_RECORD_CATEGORY,
                new JavaBehaviour(this, "onMoveNode", TRANSACTION_COMMIT));

        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                recordsManagementAuditService.registerAuditEvent(new AuditEvent(AUDIT_SET_PERMISSION, "rm.audit.set-permission"));
                return null;
            }
        });
    }

    /**
     * Gets the permission service
     *
     * @return The permission service
     */
    protected PermissionService getPermissionService()
    {
        return this.permissionService;
    }

    /**
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * Gets the policy component
     *
     * @return The policy component
     */
    protected PolicyComponent getPolicyComponent()
    {
        return this.policyComponent;
    }

    /**
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Gets the ownable service
     *
     * @return The ownable service
     */
    protected OwnableService getOwnableService()
    {
        return this.ownableService;
    }

    /**
     * @param ownableService    ownable service
     */
    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }

    /**
     * Gets the authority service
     *
     * @return The authority service
     */
    public AuthorityService getAuthorityService()
    {
        return this.authorityService;
    }

    /**
     * Sets the authority service
     *
     * @param authorityService The authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * Gets the file plan role service
     *
     * @return The file plan role service
     */
    public FilePlanRoleService getFilePlanRoleService()
    {
        return this.filePlanRoleService;
    }

    /**
     * Sets the file plan role service
     *
     * @param filePlanRoleService The file plan role service to set
     */
    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
    }

    /**
     * Gets the file plan service
     *
     * @return The file plan service
     */
    public FilePlanService getFilePlanService()
    {
        return this.filePlanService;
    }

    /**
     * Sets the file plan service
     *
     * @param filePlanService The file plan service to set
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * Set the RM audit service.
     *
     * @param recordsManagementAuditService The RM audit service.
     */
    public void setRecordsManagementAuditService(RecordsManagementAuditService recordsManagementAuditService)
    {
        this.recordsManagementAuditService = recordsManagementAuditService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.FilePlanPermissionService#setupRecordCategoryPermissions(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void setupRecordCategoryPermissions(final NodeRef recordCategory)
    {
        mandatory("recordCategory", recordCategory);

        // assert that we have a record category in our hands
        if (!instanceOf(recordCategory, TYPE_RECORD_CATEGORY))
        {
            throw new AlfrescoRuntimeException("Unable to setup record category permissions, because node is not a record category.");
        }

        // setup category permissions
        NodeRef parentNodeRef = nodeService.getPrimaryParent(recordCategory).getParentRef();
        setupPermissions(parentNodeRef, recordCategory);
    }

    /**
     * Setup permissions on new unfiled record folder
     *
     * @param childAssocRef child association reference
     */
    @Behaviour
    (
            type = "rma:unfiledRecordFolder",
            kind = CLASS,
            policy = "alf:onCreateNode",
            notificationFrequency = TRANSACTION_COMMIT
    )
    public void onCreateUnfiledRecordFolder(ChildAssociationRef childAssocRef)
    {
        mandatory("childAssocRef", childAssocRef);
        setupPermissions(childAssocRef.getParentRef(), childAssocRef.getChildRef());
    }

    /**
     * Setup permissions on new record folder
     *
     * @param childAssocRef child association reference
     */
    @Behaviour
    (
            type = "rma:recordFolder",
            kind = CLASS,
            policy = "alf:onCreateNode",
            notificationFrequency = TRANSACTION_COMMIT
    )
    public void onCreateRecordFolder(ChildAssociationRef childAssocRef)
    {
        mandatory("childAssocRef", childAssocRef);
        setupPermissions(childAssocRef.getParentRef(), childAssocRef.getChildRef());
    }

    /**
     * Setup permissions on newly created hold.
     *
     * @param childAssocRef child association reference
     */
    @Behaviour
    (
            type = "rma:hold",
            kind = CLASS,
            policy = "alf:onCreateNode",
            notificationFrequency = TRANSACTION_COMMIT
    )
    public void onCreateHold(final ChildAssociationRef childAssocRef)
    {
        createContainerElement(childAssocRef);
    }

    /**
     * Setup permissions on newly created transfer.
     *
     * @param childAssocRef child association reference
     */
    @Behaviour
    (
            type = "rma:transfer",
            kind = CLASS,
            policy = "alf:onCreateNode",
            notificationFrequency = TRANSACTION_COMMIT
    )
    public void onCreateTransfer(final ChildAssociationRef childAssocRef)
    {
        createContainerElement(childAssocRef);
    }

    /**
     * Helper method to create a container element, e.g. transfer folder or hold
     *
     * @param childAssocRef
     */
    private void createContainerElement(final ChildAssociationRef childAssocRef)
    {
        mandatory("childAssocRef", childAssocRef);
        NodeRef childRef = childAssocRef.getChildRef();
        setupPermissions(childAssocRef.getParentRef(), childRef);
        grantFilingPermissionToCreator(childRef);
    }

    /**
     * Helper method to give filing permissions to the currently logged in user who creates the node (transfer folder, hold, etc.)
     *
     * @param nodeRef The node reference of the created object
     */
    private void grantFilingPermissionToCreator(final NodeRef nodeRef)
    {
        final String user = AuthenticationUtil.getFullyAuthenticatedUser();

        final boolean hasUserPermission = authenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Boolean>()
        {
            @Override
            public Boolean doWork()
            {
                return getPermissionService().hasPermission(nodeRef, RMPermissionModel.FILING) == AccessStatus.ALLOWED;
            }
        }, user);

        if (!hasUserPermission)
        {
            authenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    getPermissionService().setPermission(nodeRef, user, RMPermissionModel.FILING, true);
                    return null;
                }
            });
        }
    }

    /**
     * Helper method to setup permissions.
     *
     * @param parent        parent node reference
     * @param nodeRef       child node reference
     */
    @Override
    public void setupPermissions(final NodeRef parent, final NodeRef nodeRef)
    {
        mandatory("parent", parent);
        mandatory("nodeRef", nodeRef);

        if (nodeService.exists(nodeRef) && nodeService.exists(parent))
        {
            authenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>()
            {
                @Override
                public Object doWork()
                {
                    // set inheritance
                    boolean isParentNodeFilePlan = isRecordCategory(nodeRef) && isFilePlan(parent);
                    boolean inheritanceAllowed = isInheritanceAllowed(nodeRef, isParentNodeFilePlan);
                    getPermissionService().setInheritParentPermissions(nodeRef, inheritanceAllowed);

                    Set<AccessPermission> keepPerms = new HashSet<>(5);
                    Set<AccessPermission> origionalPerms= getPermissionService().getAllSetPermissions(nodeRef);

                    for (AccessPermission perm : origionalPerms)
                    {
                        if (perm.getAuthority().startsWith(PermissionService.GROUP_PREFIX + ExtendedSecurityService.IPR_GROUP_PREFIX))
                        {
                            // then we can assume this is a permission we want to preserve
                            keepPerms.add(perm);
                        }
                    }

                    // clear all existing permissions and start again
                    getPermissionService().clearPermission(nodeRef, null);

                    // re-add keep'er permissions
                    for (AccessPermission keeper : keepPerms)
                    {
                        setPermission(nodeRef, keeper.getAuthority(), keeper.getPermission());
                    }

                    if (!inheritanceAllowed)
                    {
                        String adminRole = getAdminRole(nodeRef);
                        getPermissionService().setPermission(nodeRef, adminRole, RMPermissionModel.FILING, true);
                    }

                    // remove owner
                    getOwnableService().setOwner(nodeRef, NO_OWNER);

                    if (isParentNodeFilePlan)
                    {
                        Set<AccessPermission> perms = permissionService.getAllSetPermissions(parent);
                        for (AccessPermission perm : perms)
                        {
                            if (RMPermissionModel.FILING.equals(perm.getPermission()))
                            {
                                AccessStatus accessStatus = perm.getAccessStatus();
                                boolean allow = false;
                                if (AccessStatus.ALLOWED.equals(accessStatus))
                                {
                                    allow = true;
                                }
                                permissionService.setPermission(
                                        nodeRef,
                                        perm.getAuthority(),
                                        perm.getPermission(),
                                        allow);
                            }
                        }
                    }

                    return null;
                }
            });
        }
    }

    private String getAdminRole(NodeRef nodeRef)
    {
        NodeRef filePlan = getFilePlan(nodeRef);
        if (filePlan == null)
        {
            throw new AlfrescoRuntimeException("The file plan could not be found for the give node: '" + nodeRef + "'.");
        }
        return authorityService.getName(AuthorityType.GROUP, FilePlanRoleService.ROLE_ADMIN + filePlan.getId());
    }

    /**
     * Indicates whether the default behaviour is to inherit permissions or not.
     * 
     * @param nodeRef               node reference
     * @param isParentNodeFilePlan  true if parent node is a file plan, false otherwise
     * @return boolean              true if inheritance true, false otherwise
     */
    private boolean isInheritanceAllowed(NodeRef nodeRef, Boolean isParentNodeFilePlan)
    {
        return !(isFilePlan(nodeRef) || 
                 isTransfer(nodeRef) || 
                 isHold(nodeRef) || 
                 isUnfiledRecordsContainer(nodeRef) || 
                 (isRecordCategory(nodeRef) && isTrue(isParentNodeFilePlan)));
    }

    /**
     * Sets ups records permission when aspect is added.
     *
     * @see NodeServicePolicies.OnAddAspectPolicy#onAddAspect(NodeRef, QName)
     *
     * @param record
     * @param aspectTypeQName
     */
    public void onAddRecord(final NodeRef record, final QName aspectTypeQName)
    {
        mandatory("childAssocRef", record);
        mandatory("childAssocRef", aspectTypeQName);

        authenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>()
        {
            @Override
            public Object doWork()
            {
                if (nodeService.exists(record) && nodeService.hasAspect(record, aspectTypeQName))
                {
                    NodeRef recordFolder = nodeService.getPrimaryParent(record).getParentRef();
                    setupPermissions(recordFolder, record);
                }

                return null;
            }
        });
    }

    /**
     * onMoveRecord behaviour
     *
     * @param sourceAssocRef        source association reference
     * @param destinationAssocRef   destination association reference
     */
    public void onMoveRecord(final ChildAssociationRef sourceAssocRef, final ChildAssociationRef destinationAssocRef)
    {
        mandatory("sourceAssocRef", sourceAssocRef);
        mandatory("destinationAssocRef", destinationAssocRef);

        authenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                NodeRef record = sourceAssocRef.getChildRef();
                if (nodeService.exists(record) && nodeService.hasAspect(record, ASPECT_RECORD))
                {
                    boolean inheritParentPermissions = permissionService.getInheritParentPermissions(record);

                    Set<AccessPermission> keepPerms = new HashSet<>(5);
                    Set<AccessPermission> origionalRecordPerms= permissionService.getAllSetPermissions(record);

                    for (AccessPermission recordPermission : origionalRecordPerms)
                    {
                        String permission = recordPermission.getPermission();
                        if ((RMPermissionModel.FILING.equals(permission) || RMPermissionModel.READ_RECORDS.equals(permission)) &&
                             recordPermission.isSetDirectly())
                        {
                            // then we can assume this is a permission we want to preserve
                            keepPerms.add(recordPermission);
                        }
                    }

                    // re-setup the records permissions
                    setupPermissions(destinationAssocRef.getParentRef(), record);

                    // re-add keep'er permissions
                    for (AccessPermission keeper : keepPerms)
                    {
                        setPermission(record, keeper.getAuthority(), keeper.getPermission());
                    }

                    permissionService.setInheritParentPermissions(record, inheritParentPermissions);
                }

                return null;
            }
        }, getSystemUserName());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.FilePlanPermissionService#setPermission(NodeRef, String, String)
     */
    @Override
    public void setPermission(final NodeRef nodeRef, final String authority, final String permission)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("authority", authority);
        ParameterCheck.mandatory("permission", permission);

        authenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>()
        {
            @Override
            public Void doWork()
            {
                if (canPerformPermissionAction(nodeRef))
                {
                    QName auditProperty = constructAuditEventName(authority, permission);
                    Map<QName, Serializable> oldPermission = getCurrentPermissionForAuthority(nodeRef, authority, permission, auditProperty);
                    // Set the permission on the node
                    getPermissionService().setPermission(nodeRef, authority, permission, true);
                    // Add an entry in the audit log.
                    recordsManagementAuditService.auditOrUpdateEvent(nodeRef, AUDIT_SET_PERMISSION, oldPermission,
                                new HashMap<>(singletonMap(auditProperty, (Serializable) true)), true);
                }
                else
                {
                    if (LOGGER.isWarnEnabled())
                    {
                        LOGGER.warn("Setting permissions for this node is not supported.  (nodeRef=" + nodeRef + ", authority=" + authority + ", permission=" + permission + ")");
                    }
                }

                return null;
            }
        });
    }

    /**
     * Get the current permission on a node for an authority.
     *
     * @param nodeRef The node.
     * @param authority The authority.
     * @param auditProperty The QName used as the key in the returned map.
     * @return A map from the audit property to true or false depending on whether the user currently has permission.
     */
    private Map<QName, Serializable> getCurrentPermissionForAuthority(NodeRef nodeRef, String authority, String permission, QName auditProperty)
    {
        Set<AccessPermission> allSetPermissions = getPermissionService().getAllSetPermissions(nodeRef);
        for (AccessPermission setPermission : allSetPermissions)
        {
            if (setPermission.getAuthority().equals(authority) && setPermission.getPermission().equals(permission))
            {
                return new HashMap<>(singletonMap(auditProperty, (Serializable) true));
            }
        }
        return new HashMap<>(singletonMap(auditProperty, (Serializable) false));
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#deletePermission(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
     */
    @Override
    public void deletePermission(final NodeRef nodeRef, final String authority, final String permission)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("authority", authority);
        ParameterCheck.mandatory("permission", permission);

        authenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>()
        {
            @Override
            public Void doWork()
            {
                if (canPerformPermissionAction(nodeRef))
                {
                    QName auditProperty = constructAuditEventName(authority, permission);
                    Map<QName, Serializable> oldPermission = getCurrentPermissionForAuthority(nodeRef, authority, permission, auditProperty);
                    // Delete permission on this node
                    getPermissionService().deletePermission(nodeRef, authority, permission);
                    // Add an entry in the audit log.
                    recordsManagementAuditService.auditOrUpdateEvent(nodeRef, AUDIT_SET_PERMISSION, oldPermission,
                                new HashMap<>(singletonMap(auditProperty, (Serializable) false)), true);
                }
                else
                {
                    if (LOGGER.isWarnEnabled())
                    {
                        LOGGER.warn("Deleting permissions for this node is not supported.  (nodeRef=" + nodeRef + ", authority=" + authority + ", permission=" + permission + ")");
                    }
                }

                return null;
            }
        });
    }

    /**
     * Construct a QName so that the authority and permission are visible in the log.
     *
     * @param authority The authority whose permission is being changed.
     * @param permission The name of the permission being changed.
     * @return A QName such that the local name will make sense to the end user.
     */
    private QName constructAuditEventName(String authority, String permission)
    {
        return QName.createQName(AUDIT_NAMESPACE, permission + " " + authority);
    }

    private boolean canPerformPermissionAction(NodeRef nodeRef)
    {
        return isFilePlanContainer(nodeRef) || isRecordFolder(nodeRef) || isRecord(nodeRef) || isTransfer(nodeRef) || isHold(nodeRef);
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy#onMoveNode(org.alfresco.service.cmr.repository.ChildAssociationRef, org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        if (isFilePlan(newChildAssocRef.getParentRef()))
        {
            permissionService.setInheritParentPermissions(oldChildAssocRef.getChildRef(), false);
        }
    }
}
