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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.RenditionModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.disposableitem.RecordService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;

/**
 * Extended security service implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class ExtendedSecurityServiceImpl implements ExtendedSecurityService,
                                                    RecordsManagementModel,
                                                    NodeServicePolicies.OnMoveNodePolicy
{
    /** Policy component */
    private PolicyComponent policyComponent;
    
    /** Node service */
    private NodeService nodeService;
    
    /** Records management service */
    private RecordsManagementService recordsManagementService;
    
    /** Record service */
    private RecordService recordService;
    
    /**
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }
    
    /**
     * @param recordsManagementService  records management service
     */
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }
    
    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Init method
     */
    public void init()
    {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnMoveNodePolicy.QNAME, 
                ASPECT_EXTENDED_READERS, 
                new JavaBehaviour(this, "onMoveNode", NotificationFrequency.TRANSACTION_COMMIT));
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#hasExtendedReaders(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean hasExtendedReaders(NodeRef nodeRef)
    {
        boolean result = false;
        Set<String> extendedReaders = getExtendedReaders(nodeRef);
        if (extendedReaders != null && extendedReaders.size() != 0)
        {
            result = true;
        }
        return result;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#getExtendedReaders(org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getExtendedReaders(NodeRef nodeRef)
    {
        Set<String> result = null;
        
        Map<String, Integer> readerMap = (Map<String, Integer>)nodeService.getProperty(nodeRef, PROP_READERS);
        if (readerMap != null)
        {
            result = readerMap.keySet();
        }
        
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#setExtendedReaders(org.alfresco.service.cmr.repository.NodeRef, java.util.Set)
     */
    @Override
    public void setExtendedReaders(NodeRef nodeRef, Set<String> readers)
    {
        setExtendedReaders(nodeRef, readers, true);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#setExtendedReaders(org.alfresco.service.cmr.repository.NodeRef, java.util.Set, boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setExtendedReaders(NodeRef nodeRef, java.util.Set<String> readers, boolean applyToParents)
    {        
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("readers", readers);
        ParameterCheck.mandatory("applyToParents", applyToParents);
        
        if (nodeRef != null && readers.isEmpty() == false)
        {
            // add the aspect if missing
            if (nodeService.hasAspect(nodeRef, ASPECT_EXTENDED_READERS) == false)
            {
                nodeService.addAspect(nodeRef, ASPECT_EXTENDED_READERS, null);
            }
            
            // get reader map
            Map<String, Integer> readersMap = (Map<String, Integer>)nodeService.getProperty(nodeRef, PROP_READERS);
            if (readersMap == null)
            {
                // create reader map
                readersMap = new HashMap<String, Integer>(7);
            }
            
            for (String reader : readers)
            {
                if (readersMap.containsKey(reader) == true)
                {
                    // increment reference count
                    Integer count = readersMap.get(reader);
                    readersMap.put(reader, Integer.valueOf(count.intValue()+1));
                }
                else
                {
                    // add reader with initial count
                    readersMap.put(reader, Integer.valueOf(1));
                }
            }
            
            // set the readers property (this will in turn apply the aspect if required)
            nodeService.setProperty(nodeRef, PROP_READERS, (Serializable)readersMap);
            
            // apply the readers to any renditions of the content
            if (recordService.isRecord(nodeRef) == true)
            {
                List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, RenditionModel.ASSOC_RENDITION, RegexQNamePattern.MATCH_ALL);
                for (ChildAssociationRef assoc : assocs)
                {
                    NodeRef child = assoc.getChildRef();
                    setExtendedReaders(child, readers, false);
                }
            }
            
            if (applyToParents == true)
            {            
                // apply the extended readers up the file plan primary hierarchy
                NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
                if (parent != null &&
                    recordsManagementService.isFilePlanComponent(parent) == true)
                {
                    setExtendedReaders(parent, readers);
                }
            }
        }
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#removeExtendedReaders(org.alfresco.service.cmr.repository.NodeRef, java.util.Set)
     */
    @Override
    public void removeExtendedReaders(NodeRef nodeRef, Set<String> readers)
    {
        removeExtendedReaders(nodeRef, readers, true);        
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#removeExtendedReaders(org.alfresco.service.cmr.repository.NodeRef, java.util.Set, boolean)
     */
    @Override
    public void removeExtendedReaders(NodeRef nodeRef, Set<String> readers, boolean applyToParents)
    {
        if (hasExtendedReaders(nodeRef) == true)
        {
            removeExtendedReadersImpl(nodeRef, readers);
            
            // remove the readers from any renditions of the content
            if (recordService.isRecord(nodeRef) == true)
            {
                List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, RenditionModel.ASSOC_RENDITION, RegexQNamePattern.MATCH_ALL);
                for (ChildAssociationRef assoc : assocs)
                {
                    NodeRef child = assoc.getChildRef();
                    removeExtendedReadersImpl(child, readers);
                }
            }
            
            if (applyToParents == true)
            {
                // apply the extended readers up the file plan primary hierarchy
                NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
                if (parent != null &&
                    recordsManagementService.isFilePlanComponent(parent) == true)
                {
                    removeExtendedReaders(parent, readers, applyToParents);
                }                
            }
        }
    }
    
    /**
     * Removes a set of readers from a node reference.
     * <p>
     * Removes the aspect and resets the property to null if all readers are removed.
     * 
     * @param nodeRef   node reference
     * @param readers   {@link Set} of readers
     */
    @SuppressWarnings("unchecked")
    private void removeExtendedReadersImpl(NodeRef nodeRef, Set<String> readers)
    {
        Map<String, Integer> readersMap = (Map<String, Integer>)nodeService.getProperty(nodeRef, PROP_READERS);
        
        // remove the readers
        for (String reader : readers)
        {
            Integer readerCount = readersMap.get(reader);
            if (readerCount != null)
            {
                if (readerCount == 1)
                {
                    // remove entry all together if the reference count is now 0
                    readersMap.remove(reader);
                }
                else
                {
                    // decrement the reference count by 1
                    readersMap.put(reader, Integer.valueOf(readerCount.intValue()-1));
                }
            }
        }
        
        // reset the map to null if now empty
        if (readersMap.isEmpty() == true)
        {
            readersMap = null;
        }
        
        // set the property and remove the aspect if appropriate
        nodeService.setProperty(nodeRef, PROP_READERS, (Serializable)readersMap);
        if (readersMap == null)
        {
            nodeService.removeAspect(nodeRef, ASPECT_EXTENDED_READERS);
        }
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#removeAllExtendedReaders(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void removeAllExtendedReaders(NodeRef nodeRef)
    {
        removeAllExtendedReaders(nodeRef, true);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#removeAllExtendedReaders(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    @Override
    public void removeAllExtendedReaders(NodeRef nodeRef, boolean applyToParents)
    {
        if (hasExtendedReaders(nodeRef) == true)
        {
            Set<String> readers = getExtendedReaders(nodeRef);
            if (readers != null && readers.isEmpty() == false)
            {
                removeExtendedReaders(nodeRef, readers);
            }
        }        
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy#onMoveNode(org.alfresco.service.cmr.repository.ChildAssociationRef, org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    public void onMoveNode(final ChildAssociationRef origAssoc, final ChildAssociationRef newAssoc)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>() 
        {
            @Override
            public Void doWork() throws Exception
            {
                NodeRef record = newAssoc.getChildRef();
                NodeRef newParent = newAssoc.getParentRef();
                NodeRef oldParent = origAssoc.getParentRef();
                
                Set<String> readers = getExtendedReaders(record);
                if (readers != null && readers.size() != 0)
                {
                    setExtendedReaders(newParent, readers);
                    removeExtendedReaders(oldParent, readers);
                }
                
                return null;
            }
        });
    }
}
