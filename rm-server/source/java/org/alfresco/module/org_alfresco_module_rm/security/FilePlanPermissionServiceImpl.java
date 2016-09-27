/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.OwnableService;
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
@BehaviourBean
public class FilePlanPermissionServiceImpl extends    ServiceBaseImpl
                                           implements FilePlanPermissionService,
                                                      RMPermissionModel
{
    /** Permission service */
    protected PermissionService permissionService;

    /** Ownable service */
    protected OwnableService ownableService;

    /** Policy component */
    protected PolicyComponent policyComponent;

    /** Logger */
    protected static final Log LOGGER = LogFactory.getLog(FilePlanPermissionServiceImpl.class);

    /**
     * Initialisation method
     */
    public void init()
    {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnAddAspectPolicy.QNAME,
                ASPECT_RECORD,
                new JavaBehaviour(this, "onAddRecord", NotificationFrequency.TRANSACTION_COMMIT));
       policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnMoveNodePolicy.QNAME,
                ASPECT_RECORD,
                new JavaBehaviour(this, "onMoveRecord", NotificationFrequency.TRANSACTION_COMMIT));
    }

    /**
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @param ownableService    ownable service
     */
    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.FilePlanPermissionService#setupRecordCategoryPermissions(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void setupRecordCategoryPermissions(final NodeRef recordCategory)
    {
        ParameterCheck.mandatory("recordCategory", recordCategory);

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
            kind = BehaviourKind.CLASS,
            policy = "alf:onCreateNode",
            notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onCreateUnfiledRecordFolder(ChildAssociationRef childAssocRef)
    {
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
            kind = BehaviourKind.CLASS,
            policy = "alf:onCreateNode",
            notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onCreateRecordFolder(ChildAssociationRef childAssocRef)
    {
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
            kind = BehaviourKind.CLASS,
            policy = "alf:onCreateNode",
            notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onCreateHold(final ChildAssociationRef childAssocRef)
    {
        setupPermissions(childAssocRef.getParentRef(), childAssocRef.getChildRef());
    }

    /**
     * Setup permissions on newly created transfer.
     *
     * @param childAssocRef child association reference
     */
    @Behaviour
    (
            type = "rma:transfer",
            kind = BehaviourKind.CLASS,
            policy = "alf:onCreateNode",
            notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onCreateTransfer(final ChildAssociationRef childAssocRef)
    {
        setupPermissions(childAssocRef.getParentRef(), childAssocRef.getChildRef(), false);
    }

    /**
     * Helper method to setup permissions.
     *
     * @param parent        parent node reference
     * @param nodeRef       child node reference
     */
    public void setupPermissions(final NodeRef parent, final NodeRef nodeRef)
    {
        ParameterCheck.mandatory("parent", parent);
        ParameterCheck.mandatory("nodeRef", nodeRef);        
        setupPermissions(parent, nodeRef, true);
    }
    
    /**
     * Helper method to setup permissions.
     * 
     * @param parent            parent node reference
     * @param nodeRef           child node reference
     * @param includeInPlace    true if in-place permissions should be included, false otherwise
     */
    private void setupPermissions(final NodeRef parent, final NodeRef nodeRef, final boolean includeInPlace)
    {
        if (nodeService.exists(nodeRef))
        {
            // initialise permissions
            initPermissions(nodeRef, includeInPlace);

            if (nodeService.exists(parent))
            {
                runAsSystem(new AuthenticationUtil.RunAsWork<Object>()
                {
                    public Object doWork()
                    {
                        // setup inherited permissions
                        Set<AccessPermission> perms = permissionService.getAllSetPermissions(parent);
                        for (AccessPermission perm : perms)
                        {
                            // only copy filling permissions if the parent is the file plan
                            if (!inheritFillingOnly(parent, nodeRef) ||
                                RMPermissionModel.FILING.equals(perm.getPermission()))
                            {
                                // don't copy the extended reader or writer permissions as they have already been set
                                if (!ExtendedReaderDynamicAuthority.EXTENDED_READER.equals(perm.getAuthority()) &&
                                    !ExtendedWriterDynamicAuthority.EXTENDED_WRITER.equals(perm.getAuthority()))
                                {
                                    // get the access status details
                                    AccessStatus accessStatus = perm.getAccessStatus();
                                    boolean allow = false;
                                    if (AccessStatus.ALLOWED.equals(accessStatus))
                                    {
                                        allow = true;
                                    }

                                    // set the permission on the target node
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
    }

    /**
     * Helper method to determine whether all or just filling permissions should be inherited.
     *
     * @param parent    parent node
     * @param child     child node
     * @return boolean  true if inherit filling only, false otherwise
     */
    private boolean inheritFillingOnly(NodeRef parent, NodeRef child)
    {
        boolean result = false;

        // if root category or
        // if in root of unfiled container or
        // if in root of hold container
        if ((isFilePlan(parent) && isRecordCategory(child)) ||
            FilePlanComponentKind.UNFILED_RECORD_CONTAINER.equals(getFilePlanComponentKind(parent)) ||
            FilePlanComponentKind.HOLD_CONTAINER.equals(getFilePlanComponentKind(parent)))
        {
            result = true;
        }

        return result;
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
        runAsSystem(new AuthenticationUtil.RunAsWork<Object>()
        {
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
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
        {
            public Void doWork()
            {
                NodeRef record = sourceAssocRef.getChildRef();
                if (nodeService.exists(record) && nodeService.hasAspect(record, ASPECT_RECORD))
                {
                    Set<AccessPermission> keepPerms = new HashSet<AccessPermission>(5);

                    // record any permissions specifically set on the record (ie any filling or record_file permisions not on the parent)
                    Set<AccessPermission> origionalParentPerms = permissionService.getAllSetPermissions(sourceAssocRef.getParentRef());
                    Set<AccessPermission> origionalRecordPerms= permissionService.getAllSetPermissions(record);
                    for (AccessPermission perm : origionalRecordPerms)
                    {
                        if (!ExtendedReaderDynamicAuthority.EXTENDED_READER.equals(perm.getAuthority()) &&
                            !ExtendedWriterDynamicAuthority.EXTENDED_WRITER.equals(perm.getAuthority()) &&
                            (perm.getPermission().equals(RMPermissionModel.FILING) || perm.getPermission().equals(RMPermissionModel.FILE_RECORDS)) &&
                            !origionalParentPerms.contains(perm))
                        {
                            // then we can assume this is a permission we want to preserve
                            keepPerms.add(perm);
                        }
                    }

                    // clear all existing permissions and start again
                    permissionService.deletePermissions(record);

                    // re-setup the records permissions
                    setupPermissions(destinationAssocRef.getParentRef(), record);

                    // re-add keep'er permissions
                    for (AccessPermission keeper : keepPerms)
                    {
                        setPermission(record, keeper.getAuthority(), keeper.getPermission());
                    }
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * Init the permissions for the given node.
     *
     * @param nodeRef           node reference
     * @param includeInPlace    true if in-place 
     */
    private void initPermissions(final NodeRef nodeRef, final boolean includeInPlace)
    {
        if (nodeService.exists(nodeRef))
        {
            runAsSystem(new AuthenticationUtil.RunAsWork<Object>()
            {
                public Object doWork()
                {
                    // break inheritance
                    permissionService.setInheritParentPermissions(nodeRef, false);

                    // clear all existing permissions
                    permissionService.clearPermission(nodeRef, null);

                    if (includeInPlace)
                    {
                        // set extended reader permissions
                        permissionService.setPermission(nodeRef, ExtendedReaderDynamicAuthority.EXTENDED_READER, RMPermissionModel.READ_RECORDS, true);
                        permissionService.setPermission(nodeRef, ExtendedWriterDynamicAuthority.EXTENDED_WRITER, RMPermissionModel.FILING, true);
                    }

                    // remove owner
                    ownableService.setOwner(nodeRef, OwnableService.NO_OWNER);

                    return null;
                }
            });
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

        runAsSystem(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Void doWork()
            {
                if (isFilePlan(nodeRef))
                {
                    // set the permission down the file plan hierarchy
                   setPermissionDown(nodeRef, authority, permission);
                }
                else if (isFilePlanContainer(nodeRef) ||
                         isRecordFolder(nodeRef) ||
                         isRecord(nodeRef) ||
                         isHold(nodeRef))
                {
                    // set read permission to the parents of the node
                    setReadPermissionUp(nodeRef, authority);

                    // set the permission on the node and it's children
                    setPermissionDown(nodeRef, authority, permission);
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
     * Helper method to set the read permission up the hierarchy
     *
     * @param nodeRef       node reference
     * @param authority     authority
     */
    private void setReadPermissionUp(NodeRef nodeRef, String authority)
    {
        NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
        if (parent != null && isFilePlanComponent(parent))
        {
            setReadPermissionUpImpl(parent, authority);
        }
    }

    /**
     * Helper method used to set the read permission up the hierarchy
     *
     * @param nodeRef   node reference
     * @param authority authority
     */
    private void setReadPermissionUpImpl(NodeRef nodeRef, String authority)
    {
        setPermissionImpl(nodeRef, authority, RMPermissionModel.READ_RECORDS);

        NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
        if (parent != null && isFilePlanComponent(parent))
        {
            setReadPermissionUpImpl(parent, authority);
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
        // skip out node's that inherit (for example hold and transfer)
        if (!permissionService.getInheritParentPermissions(nodeRef))
        {
            // set permissions
            setPermissionImpl(nodeRef, authority, permission);

            if (isFilePlanContainer(nodeRef) ||
                isRecordFolder(nodeRef))
            {
                List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
                for (ChildAssociationRef assoc : assocs)
                {
                    NodeRef child = assoc.getChildRef();
                    if (isFilePlanContainer(child) ||
                        isRecordFolder(child) ||
                        isRecord(child) ||
                        isHold(child) ||
                        instanceOf(child, TYPE_TRANSFER))
                    {
                        setPermissionDown(child, authority, permission);
                    }
                }
            }
        }
    }

    /**
     * Set the permission, taking into account that filing is a superset of read
     *
     * @param nodeRef       node reference
     * @param authority     authority
     * @param permission    permission
     */
    private void setPermissionImpl(NodeRef nodeRef, String authority, String permission)
    {
        boolean hasRead = false;
        boolean hasFilling = false;
        
        Set<AccessPermission> perms = permissionService.getAllSetPermissions(nodeRef);
        for (AccessPermission perm : perms)
        {
            if (perm.getAuthority().equals(authority))
            {
                if (perm.getPermission().equals(FILING))
                {
                    hasFilling = true;
                }
                else if (perm.getPermission().equals(READ_RECORDS))
                {
                    hasRead = true;
                }
            } 
        }
        
        if (FILING.equals(permission) && hasRead)
        {
            // remove read permission
            permissionService.deletePermission(nodeRef, authority, RMPermissionModel.READ_RECORDS);       
            hasRead = false;
        }
        
        if (!hasRead && !hasFilling)
        {
            // add permission
            permissionService.setPermission(nodeRef, authority, permission, true);            
        }        
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#deletePermission(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
     */
    public void deletePermission(final NodeRef nodeRef, final String authority, final String permission)
    {
        runAsSystem(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Void doWork()
            {
                // can't delete permissions if inherited (eg hold and transfer containers)
                if (!permissionService.getInheritParentPermissions(nodeRef))
                {
                    // Delete permission on this node
                    permissionService.deletePermission(nodeRef, authority, permission);

                    if (isFilePlanContainer(nodeRef) ||
                        isRecordFolder(nodeRef))
                    {
                        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
                        for (ChildAssociationRef assoc : assocs)
                        {
                            NodeRef child = assoc.getChildRef();
                            if (isFilePlanContainer(child) ||
                                isRecordFolder(child) ||
                                isRecord(child)||
                                isHold(child) ||
                                instanceOf(child, TYPE_TRANSFER))
                            {
                                deletePermission(child, authority, permission);
                            }
                        }
                    }
                }

                return null;
            }
        });
    }
}
