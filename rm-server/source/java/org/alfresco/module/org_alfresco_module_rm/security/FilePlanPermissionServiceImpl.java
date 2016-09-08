/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.security;

import static org.apache.commons.lang.BooleanUtils.isTrue;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
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
public class FilePlanPermissionServiceImpl extends    ServiceBaseImpl
                                           implements FilePlanPermissionService,
                                                      RecordsManagementModel,
                                                      NodeServicePolicies.OnMoveNodePolicy
{
    /** Permission service */
    protected PermissionService permissionService;

    /** Policy component */
    protected PolicyComponent policyComponent;

    /** Records management service */
    protected RecordsManagementService recordsManagementService;

    /** File plan service */
    protected FilePlanService filePlanService;

    /** Record service */
    protected RecordService recordService;

    /** Logger */
    protected static Log logger = LogFactory.getLog(FilePlanPermissionServiceImpl.class);

    /**
     * Initialisation method
     */
    public void init()
    {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                TYPE_RECORD_CATEGORY,
                new JavaBehaviour(this, "onCreateRMContainer", NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnMoveNodePolicy.QNAME,
                TYPE_RECORD_CATEGORY,
                new JavaBehaviour(this, "onMoveNode", NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                TYPE_RECORD_FOLDER,
                new JavaBehaviour(this, "onCreateRecordFolder", NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnAddAspectPolicy.QNAME,
                ASPECT_RECORD,
                new JavaBehaviour(this, "onAddRecord", NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnMoveNodePolicy.QNAME,
                ASPECT_RECORD,
                new JavaBehaviour(this, "onMoveRecord", NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                TYPE_HOLD,
                new JavaBehaviour(this, "onCreateHoldTransfer", NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                TYPE_TRANSFER,
                new JavaBehaviour(this, "onCreateHoldTransfer", NotificationFrequency.TRANSACTION_COMMIT));
    }

    /**
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @param recordsManagementService  records management service
     */
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @param childAssocRef
     */
    public void onCreateRMContainer(final ChildAssociationRef childAssocRef)
    {
        // Pull any permissions found on the parent (ie the record category)
        final NodeRef parentNodeRef = childAssocRef.getParentRef();
        if (parentNodeRef != null && nodeService.exists(parentNodeRef))
        {
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
            {
                public Object doWork()
                {
                    NodeRef recordCategory = childAssocRef.getChildRef();
                    boolean isParentNodeFilePlan = filePlanService.isFilePlan(parentNodeRef);
                    setUpPermissions(recordCategory, isParentNodeFilePlan);

                    // since this is not a root category, inherit from parent
                    if (isParentNodeFilePlan)
                    {
                        Set<AccessPermission> perms = permissionService.getAllSetPermissions(parentNodeRef);
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
                                        recordCategory,
                                        perm.getAuthority(),
                                        perm.getPermission(),
                                        allow);
                            }
                        }
                    }

                    return null;
                }

            }, AuthenticationUtil.getSystemUserName());
        }
    }

    /**
     * @param childAssocRef
     */
    public void onCreateRecordFolder(ChildAssociationRef childAssocRef)
    {
        final NodeRef folderNodeRef = childAssocRef.getChildRef();

        // initialise the permissions
        setUpPermissions(folderNodeRef);

        // Pull any permissions found on the parent (ie the record category)
        final NodeRef catNodeRef = childAssocRef.getParentRef();
        if (!permissionService.getInheritParentPermissions(folderNodeRef) &&
                nodeService.exists(catNodeRef))
        {
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
            {
                public Object doWork()
                {
                    Set<AccessPermission> perms = permissionService.getAllSetPermissions(catNodeRef);
                    for (AccessPermission perm : perms)
                    {
                        if (!ExtendedReaderDynamicAuthority.EXTENDED_READER.equals(perm.getAuthority()) &&
                            !ExtendedWriterDynamicAuthority.EXTENDED_WRITER.equals(perm.getAuthority()))
                        {
                            AccessStatus accessStatus = perm.getAccessStatus();
                            boolean allow = false;
                            if (AccessStatus.ALLOWED.equals(accessStatus) == true)
                            {
                                allow = true;
                            }
                            permissionService.setPermission(
                                    folderNodeRef,
                                    perm.getAuthority(),
                                    perm.getPermission(),
                                    allow);
                        }
                    }

                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
        }
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
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork()
            {
                if (nodeService.exists(record) == true && nodeService.hasAspect(record, aspectTypeQName) == true)
                {
                    NodeRef recordFolder = nodeService.getPrimaryParent(record).getParentRef();
                    initialiseRecordPermissions(record, recordFolder);
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Sets up permissions for transfer and hold objects
     *
     * @param childAssocRef
     */
    public void onCreateHoldTransfer(final ChildAssociationRef childAssocRef)
    {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>()
        {
            public Void doWork()
            {
                NodeRef nodeRef = childAssocRef.getChildRef();
                if (nodeService.exists(nodeRef) == true)
                {
                    setUpPermissions(nodeRef);

                    NodeRef parent = childAssocRef.getParentRef();
                    Set<AccessPermission> perms = permissionService.getAllSetPermissions(parent);
                    for (AccessPermission perm : perms)
                    {
                        if (ExtendedReaderDynamicAuthority.EXTENDED_READER.equals(perm.getAuthority()) == false &&
                            ExtendedWriterDynamicAuthority.EXTENDED_WRITER.equals(perm.getAuthority()) == false)
                        {
                            AccessStatus accessStatus = perm.getAccessStatus();
                            boolean allow = false;
                            if (AccessStatus.ALLOWED.equals(accessStatus) == true)
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

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy#onMoveNode(org.alfresco.service.cmr.repository.ChildAssociationRef, org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    public void onMoveNode(final ChildAssociationRef oldChildAssocRef, final ChildAssociationRef newChildAssocRef)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                NodeRef sourceCategory = oldChildAssocRef.getChildRef();
                boolean inheritParentPermissions = permissionService.getInheritParentPermissions(sourceCategory);

                Set<AccessPermission> keepPerms = new HashSet<AccessPermission>(5);
                Set<AccessPermission> origionalCategoryPerms= permissionService.getAllSetPermissions(sourceCategory);

                for (AccessPermission categoryPermission : origionalCategoryPerms)
                {
                    String permission = categoryPermission.getPermission();
                    String authority = categoryPermission.getAuthority();
                    if ((RMPermissionModel.FILING.equals(permission) || RMPermissionModel.READ_RECORDS.equals(permission)) &&
                            categoryPermission.isSetDirectly() &&
                            !ExtendedReaderDynamicAuthority.EXTENDED_READER.equals(authority) &&
                            !ExtendedWriterDynamicAuthority.EXTENDED_WRITER.equals(authority))
                    {
                        // then we can assume this is a permission we want to preserve
                        keepPerms.add(categoryPermission);
                    }
                }

                // clear all existing permissions and start again
                permissionService.deletePermissions(sourceCategory);

                // re-add keep'er permissions
                for (AccessPermission keeper : keepPerms)
                {
                    setPermission(sourceCategory, keeper.getAuthority(), keeper.getPermission());
                }

                permissionService.setInheritParentPermissions(sourceCategory, isFilePlan(newChildAssocRef.getParentRef()) ? false : inheritParentPermissions);

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Initialise the record permissions for the given parent.
     *
     * NOTE: method is public so it can be accessed via the associated patch bean.
     *
     * @param record        record
     * @param parent        records permission parent
     */
    public void initialiseRecordPermissions(NodeRef record, NodeRef parent)
    {
        setUpPermissions(record);

        if (!permissionService.getInheritParentPermissions(record))
        {
            Set<AccessPermission> perms = permissionService.getAllSetPermissions(parent);
            for (AccessPermission perm : perms)
            {
                if (!ExtendedReaderDynamicAuthority.EXTENDED_READER.equals(perm.getAuthority()) &&
                        !ExtendedWriterDynamicAuthority.EXTENDED_WRITER.equals(perm.getAuthority()))
                {
                    AccessStatus accessStatus = perm.getAccessStatus();
                    boolean allow = false;
                    if (AccessStatus.ALLOWED.equals(accessStatus) == true)
                    {
                        allow = true;
                    }
                    permissionService.setPermission(
                            record,
                            perm.getAuthority(),
                            perm.getPermission(),
                            allow);
                }
            }
        }
    }

    /**
     * onMoveRecord behaviour
     *
     * @param sourceAssocRef        source association reference
     * @param destinationAssocRef   destination association reference
     */
    public void onMoveRecord(final ChildAssociationRef sourceAssocRef, final ChildAssociationRef destinationAssocRef)
    {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
        {
            public Void doWork()
            {
                NodeRef record = sourceAssocRef.getChildRef();
                if (nodeService.exists(record) && nodeService.hasAspect(record, ASPECT_RECORD))
                {
                    boolean inheritParentPermissions = permissionService.getInheritParentPermissions(record);

                    Set<AccessPermission> keepPerms = new HashSet<AccessPermission>(5);
                    Set<AccessPermission> origionalRecordPerms= permissionService.getAllSetPermissions(record);

                    for (AccessPermission recordPermission : origionalRecordPerms)
                    {
                        String permission = recordPermission.getPermission();
                        String authority = recordPermission.getAuthority();
                        if ((RMPermissionModel.FILING.equals(permission) || RMPermissionModel.READ_RECORDS.equals(permission)) &&
                                recordPermission.isSetDirectly() &&
                                !ExtendedReaderDynamicAuthority.EXTENDED_READER.equals(authority) &&
                                !ExtendedWriterDynamicAuthority.EXTENDED_WRITER.equals(authority))
                        {
                            // then we can assume this is a permission we want to preserve
                            keepPerms.add(recordPermission);
                        }
                    }

                    // clear all existing permissions and start again
                    permissionService.deletePermissions(record);

                    // re-setup the records permissions
                    initialiseRecordPermissions(record, destinationAssocRef.getParentRef());

                    // re-add keep'er permissions
                    for (AccessPermission keeper : keepPerms)
                    {
                        setPermission(record, keeper.getAuthority(), keeper.getPermission());
                    }

                    permissionService.setInheritParentPermissions(record, inheritParentPermissions);
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    private void setUpPermissions(final NodeRef nodeRef)
    {
        setUpPermissions(nodeRef, null);
    }

    private void setUpPermissions(final NodeRef nodeRef, final Boolean isParentNodeFilePlan)
    {
        if (nodeService.exists(nodeRef))
        {
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
            {
                public Object doWork()
                {
                    // set inheritance
                    boolean inheritanceAllowed = isInheritanceAllowed(nodeRef, isParentNodeFilePlan);
                    permissionService.setInheritParentPermissions(nodeRef, inheritanceAllowed);

                    if (!inheritanceAllowed)
                    {
                        // set extended reader permissions
                        permissionService.setPermission(nodeRef, ExtendedReaderDynamicAuthority.EXTENDED_READER, RMPermissionModel.READ_RECORDS, true);
                        permissionService.setPermission(nodeRef, ExtendedWriterDynamicAuthority.EXTENDED_WRITER, RMPermissionModel.FILING, true);
                    }

                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
        }
    }

    private boolean isInheritanceAllowed(NodeRef nodeRef, Boolean isParentNodeFilePlan)
    {
        return !(isFilePlan(nodeRef) || isTransfer(nodeRef) || isHold(nodeRef) || isUnfiledRecordsContainer(nodeRef) || (isRecordCategory(nodeRef) && isTrue(isParentNodeFilePlan)));
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#setPermission(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String, boolean)
     */
    public void setPermission(final NodeRef nodeRef, final String authority, final String permission)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("authority", authority);
        ParameterCheck.mandatory("permission", permission);

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Boolean doWork() throws Exception
            {
                if (canPerformPermissionAction(nodeRef))
                {
                    // Set the permission on the node
                    permissionService.setPermission(nodeRef, authority, permission, true);
                }
                else
                {
                    if (logger.isWarnEnabled())
                    {
                        logger.warn("Setting permissions for this node is not supported.  (nodeRef=" + nodeRef + ", authority=" + authority + ", permission=" + permission + ")");
                    }
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#deletePermission(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
     */
    public void deletePermission(final NodeRef nodeRef, final String authority, final String permission)
    {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Boolean doWork() throws Exception
            {
                if (canPerformPermissionAction(nodeRef))
                {
                    // Delete permission on this node
                    permissionService.deletePermission(nodeRef, authority, permission);
                }
                else
                {
                    if (logger.isWarnEnabled())
                    {
                        logger.warn("Deleting permissions for this node is not supported.  (nodeRef=" + nodeRef + ", authority=" + authority + ", permission=" + permission + ")");
                    }
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    private boolean canPerformPermissionAction(NodeRef nodeRef)
    {
        return filePlanService.isFilePlanContainer(nodeRef) || recordsManagementService.isRecordFolder(nodeRef) || recordService.isRecord(nodeRef);
    }
}
