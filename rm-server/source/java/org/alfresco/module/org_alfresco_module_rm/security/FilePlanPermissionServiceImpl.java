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

import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * File plan permission service.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class FilePlanPermissionServiceImpl implements FilePlanPermissionService,
                                                      RecordsManagementModel
{
    /** Permission service */
    private PermissionService permissionService;

    /** Policy component */
    private PolicyComponent policyComponent;

    /** Records management service */
    private RecordsManagementService recordsManagementService;

    /** Node service */
    private NodeService nodeService;
    
    /** File plan service */
    private FilePlanService filePlanService;

    /** Logger */
    private static Log logger = LogFactory.getLog(FilePlanPermissionServiceImpl.class);
    
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
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                TYPE_RECORD_FOLDER,
                new JavaBehaviour(this, "onCreateRecordFolder", NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnAddAspectPolicy.QNAME, 
                ASPECT_RECORD, 
                new JavaBehaviour(this, "onAddRecord", NotificationFrequency.TRANSACTION_COMMIT));
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
     * @param childAssocRef
     */
    public void onCreateRMContainer(ChildAssociationRef childAssocRef)
    {
        final NodeRef recordCategory = childAssocRef.getChildRef();
        setUpPermissions(recordCategory);

        // Pull any permissions found on the parent (ie the record category)
        final NodeRef parentNodeRef = childAssocRef.getParentRef();
        if (parentNodeRef != null && nodeService.exists(parentNodeRef) == true)
        {
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
            {
                public Object doWork()
                {
                    boolean fillingOnly = false;
                    if (filePlanService.isFilePlan(parentNodeRef) == true)
                    {
                        fillingOnly = true;
                    }

                    // since this is not a root category, inherit from parent
                    Set<AccessPermission> perms = permissionService.getAllSetPermissions(parentNodeRef);
                    for (AccessPermission perm : perms)
                    {
                        if (fillingOnly == false ||
                            RMPermissionModel.FILING.equals(perm.getPermission()) == true)
                        {
                            AccessStatus accessStatus = perm.getAccessStatus();
                            boolean allow = false;
                            if (AccessStatus.ALLOWED.equals(accessStatus) == true)
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
        if (nodeService.exists(catNodeRef) == true)
        {
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
            {
                public Object doWork()
                {
                    Set<AccessPermission> perms = permissionService.getAllSetPermissions(catNodeRef);
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
                    
                    setUpPermissions(record);
                    
                    Set<AccessPermission> perms = permissionService.getAllSetPermissions(recordFolder);
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
                                    record,
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

    /**
     *
     * @param nodeRef
     */
    public void setUpPermissions(final NodeRef nodeRef)
    {
        if (nodeService.exists(nodeRef) == true)
        {
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
            {
                public Object doWork()
                {
                    // break inheritance
                    permissionService.setInheritParentPermissions(nodeRef, false);

                    // set extended reader permissions
                    permissionService.setPermission(nodeRef, ExtendedReaderDynamicAuthority.EXTENDED_READER, RMPermissionModel.READ_RECORDS, true);
                    permissionService.setPermission(nodeRef, ExtendedWriterDynamicAuthority.EXTENDED_WRITER, RMPermissionModel.FILING, true);

                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
        }
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
                if (filePlanService.isFilePlan(nodeRef) == true)
                {
                   setPermissionDown(nodeRef, authority, permission);
                }
                else if (recordsManagementService.isRecordsManagementContainer(nodeRef) == true || 
                         recordsManagementService.isRecordFolder(nodeRef) == true ||
                         recordsManagementService.isRecord(nodeRef) == true)
                {
                    setReadPermissionUp(nodeRef, authority);
                    setPermissionDown(nodeRef, authority, permission);
                }
                else
                {
                    if (logger.isWarnEnabled() == true)
                    {
                        logger.warn("Setting permissions for this node is not supported.  (nodeRef=" + nodeRef + ", authority=" + authority + ", permission=" + permission + ")");
                    }
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Helper method to set the read permission up the hierarchy
     *
     * @param nodeRef       node reference
     * @param authority     authority
     */
    private void setReadPermissionUp(NodeRef nodeRef, String authority)
    {
        NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
        if (parent != null &&
            filePlanService.isFilePlan(parent) == false)
        {
            setPermissionImpl(parent, authority, RMPermissionModel.READ_RECORDS);
            setReadPermissionUp(parent, authority);
        }
    }

    /**
     * Helper method to set the permission down the hierarchy
     *
     * @param nodeRef       node reference
     * @param authority     authority
     * @param permission    permission
     */
    private void setPermissionDown(NodeRef nodeRef, String authority, String permission)
    {
        setPermissionImpl(nodeRef, authority, permission);
        if (recordsManagementService.isRecordsManagementContainer(nodeRef) == true ||
            recordsManagementService.isRecordFolder(nodeRef) == true)
        {
            List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef assoc : assocs)
            {
                NodeRef child = assoc.getChildRef();
                if (recordsManagementService.isRecordsManagementContainer(child) == true ||
                    recordsManagementService.isRecordFolder(child) == true ||
                    recordsManagementService.isRecord(child) == true)
                {
                    setPermissionDown(child, authority, permission);
                }
            }
        }
    }

    /**
     * Set the permission, taking into account that filing is a superset of read
     *
     * @param nodeRef
     * @param authority
     * @param permission
     */
    private void setPermissionImpl(NodeRef nodeRef, String authority, String permission)
    {
        if (RMPermissionModel.FILING.equals(permission) == true)
        {
            // Remove record read permission before adding filing permission
            permissionService.deletePermission(nodeRef, authority, RMPermissionModel.READ_RECORDS);
        }

        permissionService.setPermission(nodeRef, authority, permission, true);
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
                // Delete permission on this node
                permissionService.deletePermission(nodeRef, authority, permission);

                if (recordsManagementService.isRecordsManagementContainer(nodeRef) == true ||
                    recordsManagementService.isRecordFolder(nodeRef) == true)
                {
                    List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
                    for (ChildAssociationRef assoc : assocs)
                    {
                        NodeRef child = assoc.getChildRef();
                        if (recordsManagementService.isRecordsManagementContainer(child) == true ||
                            recordsManagementService.isRecordFolder(child) == true ||
                            recordsManagementService.isRecord(child) == true)
                        {
                            deletePermission(child, authority, permission);
                        }
                    }
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

}
