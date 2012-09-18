/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.action.dm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.permission.RecordReadersDynamicAuthority;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;

/**
 * Creates a new record from an existing content object.
 * 
 * Note:  This is a 'normal' dm action, rather than a records management action.
 * 
 * @author Roy Wetherall
 */
public class CreateRecordAction extends ActionExecuterAbstractBase
                                implements RecordsManagementModel
{
    private RecordsManagementService recordsManagementService;
    
    private RecordService recordService;
    
    private PermissionService permissionService;
    
    private NodeService nodeService;
    
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }
    
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    @Override
    protected void executeImpl(Action action, final NodeRef actionedUponNodeRef)
    {
        // TODO we should use the file plan passed as a parameter
        // grab the file plan
        List<NodeRef> filePlans = recordsManagementService.getFilePlans();
        if (filePlans.size() == 1)
        {
            final NodeRef filePlan = filePlans.get(0);

            AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    // get the documents readers
                    Long aclId = nodeService.getNodeAclId(actionedUponNodeRef);
                    Set<String> readers = permissionService.getReaders(aclId);         
                    
                    // get the documents primary parent assoc
                    ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(actionedUponNodeRef);
                    
                    /// get the new record container for the file plan
                    NodeRef newRecordContainer = recordService.getNewRecordContainer(filePlan);
                    if (newRecordContainer == null)
                    {
                        throw new AlfrescoRuntimeException("Unable to create record, because new record container could not be found.");
                    }
                    
                    // move the document into the file plan
                    nodeService.moveNode(actionedUponNodeRef, newRecordContainer, ContentModel.ASSOC_CONTAINS, parentAssoc.getQName());
                    
                    // maintain the original primary location
                    nodeService.addChild(parentAssoc.getParentRef(), actionedUponNodeRef, parentAssoc.getTypeQName(), parentAssoc.getQName());
                    
                    // add extended security information to the record
                    Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                    props.put(PROP_READERS, (Serializable)readers);
                    nodeService.addAspect(actionedUponNodeRef, ASPECT_EXTENDED_RECORD_SECURITY, props);
                    
                    // add permission so readers can still 'see' the new record
                    // Note: using the regular permission service as we don't want to reflect this permission up (and down) the
                    //       hierarchy
                    permissionService.setPermission(actionedUponNodeRef, 
                                                    RecordReadersDynamicAuthority.RECORD_READERS, 
                                                    RMPermissionModel.READ_RECORDS, 
                                                    true);
                    
                    return null;
                }
            });            
        }
        else
        {
            throw new AlfrescoRuntimeException("Unable to file file plan.");
        }        
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> params)
    {
        // TODO eventually we will need to pass in the file plan as a parameter
        // TODO .. or the RM site
    }
   
}
