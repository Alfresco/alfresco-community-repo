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
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;

/**
 * Extended security service implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class ExtendedSecurityServiceImpl extends ServiceBaseImpl
                                         implements ExtendedSecurityService,
                                                    RecordsManagementModel,
                                                    NodeServicePolicies.OnMoveNodePolicy
{
    /** Policy component */
    private PolicyComponent policyComponent;
    
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
     * Init method
     */
    public void init()
    {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnMoveNodePolicy.QNAME, 
                ASPECT_EXTENDED_SECURITY, 
                new JavaBehaviour(this, "onMoveNode", NotificationFrequency.TRANSACTION_COMMIT));
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#hasExtendedSecurity(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean hasExtendedSecurity(NodeRef nodeRef)
    {
        return nodeService.hasAspect(nodeRef, ASPECT_EXTENDED_SECURITY);
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
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#getExtendedWriters(org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getExtendedWriters(NodeRef nodeRef)
    {
        Set<String> result = null;
        
        Map<String, Integer> map = (Map<String, Integer>)nodeService.getProperty(nodeRef, PROP_WRITERS);
        if (map != null)
        {
            result = map.keySet();
        }
        
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#addExtendedSecurity(org.alfresco.service.cmr.repository.NodeRef, java.util.Set, java.util.Set)
     */
    @Override
    public void addExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers)
    {
        addExtendedSecurity(nodeRef, readers, writers, true);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#addExtendedSecurity(org.alfresco.service.cmr.repository.NodeRef, java.util.Set, java.util.Set, boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void addExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers, boolean applyToParents)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("applyToParents", applyToParents);
        
        if (nodeRef != null)
        {
            // add the aspect if missing
            if (nodeService.hasAspect(nodeRef, ASPECT_EXTENDED_SECURITY) == false)
            {
                nodeService.addAspect(nodeRef, ASPECT_EXTENDED_SECURITY, null);
            }
            
            // update the readers map
            if (readers != null && readers.size() != 0)
            {
                // get reader map
                Map<String, Integer> readersMap = (Map<String, Integer>)nodeService.getProperty(nodeRef, PROP_READERS);
                
                // set the readers property (this will in turn apply the aspect if required)
                nodeService.setProperty(nodeRef, PROP_READERS, (Serializable)addToMap(readersMap, readers));
            }
            
            // update the writers map
            if (writers != null && writers.size() != 0)
            {
                // get writer map
                Map<String, Integer> writersMap = (Map<String, Integer>)nodeService.getProperty(nodeRef, PROP_WRITERS);
                
                // set the writers property (this will in turn apply the aspect if required)
                nodeService.setProperty(nodeRef, PROP_WRITERS, (Serializable)addToMap(writersMap, writers));
            }
            
            // apply the readers to any renditions of the content
            if (recordService.isRecord(nodeRef) == true)
            {
                List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, RenditionModel.ASSOC_RENDITION, RegexQNamePattern.MATCH_ALL);
                for (ChildAssociationRef assoc : assocs)
                {
                    NodeRef child = assoc.getChildRef();
                    addExtendedSecurity(child, readers, writers, false);
                }
            }
            
            if (applyToParents == true)
            {   
                // apply the extended readers up the file plan primary hierarchy
                NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
                if (parent != null &&
                    recordsManagementService.isFilePlanComponent(parent) == true)
                {
                    addExtendedSecurity(parent, readers, null);
                    addExtendedSecurity(parent, writers, null);
                }
            }
        }
    }
    
    /**
     * 
     * @param map
     * @param keys
     * @return
     */
    private Map<String, Integer> addToMap(Map<String, Integer> map, Set<String> keys)
    {
        if (map == null)
        {
            // create map
            map = new HashMap<String, Integer>(7);
        }
        
        for (String key : keys)
        {
            if (map.containsKey(key) == true)
            {
                // increment reference count
                Integer count = map.get(key);
                map.put(key, Integer.valueOf(count.intValue()+1));
            }
            else
            {
                // add key with initial count
                map.put(key, Integer.valueOf(1));
            }
        }
        
        return map;
    }
    

    @Override
    public void removeExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers)
    {
        removeExtendedSecurity(nodeRef, readers, writers, true);        
    }
    
    @Override
    public void removeExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String>writers, boolean applyToParents)
    {
        if (hasExtendedSecurity(nodeRef) == true)
        {
            removeExtendedSecurityImpl(nodeRef, readers, writers);
            
            // remove the readers from any renditions of the content
            if (recordService.isRecord(nodeRef) == true)
            {
                List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, RenditionModel.ASSOC_RENDITION, RegexQNamePattern.MATCH_ALL);
                for (ChildAssociationRef assoc : assocs)
                {
                    NodeRef child = assoc.getChildRef();
                    removeExtendedSecurityImpl(child, readers, writers);
                }
            }
            
            if (applyToParents == true)
            {
                // apply the extended readers up the file plan primary hierarchy
                NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
                if (parent != null &&
                    recordsManagementService.isFilePlanComponent(parent) == true)
                {
                    removeExtendedSecurity(parent, readers, null, applyToParents);
                    removeExtendedSecurity(parent, writers, null, applyToParents);
                }                
            }
        }
    }
    
    /**
     * Removes a set of readers and writers from a node reference.
     * <p>
     * Removes the aspect and resets the property to null if all readers and writers are removed.
     * 
     * @param nodeRef   node reference
     * @param readers   {@link Set} of readers
     * @param writers   {@link Set} of writers
     */
    @SuppressWarnings("unchecked")
    private void removeExtendedSecurityImpl(NodeRef nodeRef, Set<String> readers, Set<String> writers)
    {
        Map<String, Integer> readersMap = (Map<String, Integer>)nodeService.getProperty(nodeRef, PROP_READERS);
        nodeService.setProperty(nodeRef, PROP_READERS, (Serializable)removeFromMap(readersMap, readers));
        
        Map<String, Integer> writersMap = (Map<String, Integer>)nodeService.getProperty(nodeRef, PROP_WRITERS);
        nodeService.setProperty(nodeRef, PROP_WRITERS, (Serializable)removeFromMap(writersMap, writers));
        
        if (readersMap == null && writersMap == null)
        {
            // remove the aspect
            nodeService.removeAspect(nodeRef, ASPECT_EXTENDED_SECURITY);
        }        
    }
    
    private Map<String, Integer> removeFromMap(Map<String, Integer> map, Set<String> keys)
    {
        if (map != null && keys != null && keys.size() != 0)
        {
            // remove the keys
            for (String key : keys)
            {
                Integer count = map.get(key);
                if (count != null)
                {
                    if (count == 1)
                    {
                        // remove entry all together if the reference count is now 0
                        map.remove(key);
                    }
                    else
                    {
                        // decrement the reference count by 1
                        map.put(key, Integer.valueOf(count.intValue()-1));
                    }
                }
            }
        }
        
        // reset the map to null if now empty
        if (map != null && map.isEmpty() == true)
        {
            map = null;
        }
        
        return map;
    }
    
    @Override
    public void removeAllExtendedSecurity(NodeRef nodeRef)
    {
        removeAllExtendedSecurity(nodeRef, true);
    }
    
    @Override
    public void removeAllExtendedSecurity(NodeRef nodeRef, boolean applyToParents)
    {
        if (hasExtendedSecurity(nodeRef) == true)
        {
            removeExtendedSecurity(nodeRef, getExtendedReaders(nodeRef), getExtendedWriters(nodeRef));            
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
                Set<String> writers = getExtendedWriters(record);
                
                addExtendedSecurity(newParent, readers, writers);
                removeExtendedSecurity(oldParent, readers, writers);
                
                return null;
            }
        });
    }
}
