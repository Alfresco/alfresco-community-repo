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
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
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
    /** Ad hoc properties used for reference counting */
    private final static QName PROP_EXTENDED_READER_ROLE = QName.createQName(RM_URI, "extendedReaderRole");
    private final static QName PROP_EXTENDED_WRITER_ROLE = QName.createQName(RM_URI, "extendedWriterRole");
    
    /** Policy component */
    private PolicyComponent policyComponent;
    
    /** Record service */
    private RecordService recordService;
    
    /** File plan service */
    private FilePlanService filePlanService;
    
    /** File plan role service */
    private FilePlanRoleService filePlanRoleService;
    
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
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }
    
    /**
     * @param filePlanRoleService   file plan role service
     */
    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
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
    @Override
    public void addExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers, boolean applyToParents)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("applyToParents", applyToParents);
        
        if (nodeRef != null)
        {
            addExtendedSecurityImpl(nodeRef, readers, writers, applyToParents);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void addExtendedSecurityImpl(NodeRef nodeRef, Set<String> readers, Set<String> writers, boolean applyToParents)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("applyToParents", applyToParents);
        
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
                addExtendedSecurityImpl(child, readers, writers, false);
            }
        }
        
        // add to the extended security roles
        addExtendedSecurityRoles(nodeRef, readers, writers);
        
        if (applyToParents == true)
        {   
            // apply the extended readers up the file plan primary hierarchy
            NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
            if (parent != null &&
                filePlanService.isFilePlanComponent(parent) == true)
            {
                addExtendedSecurityImpl(parent, readers, null, applyToParents);
                addExtendedSecurityImpl(parent, writers, null, applyToParents);
            }
        }      
    }
    
    /**
     * 
     * @param nodeRef
     * @param readers
     * @param writers
     */
    private void addExtendedSecurityRoles(NodeRef nodeRef, Set<String> readers, Set<String> writers)
    {
        NodeRef filePlan = filePlanService.getFilePlan(nodeRef);
        
        addExtendedSecurityRolesImpl(filePlan, readers, PROP_EXTENDED_READER_ROLE, FilePlanRoleService.ROLE_EXTENDED_READERS);
        addExtendedSecurityRolesImpl(filePlan, writers, PROP_EXTENDED_WRITER_ROLE, FilePlanRoleService.ROLE_EXTENDED_WRITERS);
    }   
    
    /**
     * 
     * @param filePlan
     * @param authorities
     * @param propertyName
     * @param roleName
     */
    @SuppressWarnings("unchecked")
    private void addExtendedSecurityRolesImpl(NodeRef filePlan, Set<String> authorities, QName propertyName, String roleName)
    {
        if (authorities != null)
        {
            // get the reference count
            Map<String, Integer> referenceCountMap = (Map<String, Integer>)nodeService.getProperty(filePlan, propertyName);
            
            for (String authority : authorities)
            {         
                if (authority.equals(PermissionService.ALL_AUTHORITIES) == false &&
                    authority.equals(PermissionService.OWNER_AUTHORITY) == false)
                {
                    if (referenceCountMap == null ||
                        referenceCountMap.containsKey(authority) == false)
                    {
                        // add the authority to the role
                        filePlanRoleService.assignRoleToAuthority(filePlan, roleName, authority);
                    }
                }
            }
            
            // update the reference count
            nodeService.setProperty(filePlan, propertyName, (Serializable)addToMap(referenceCountMap, authorities));
        }        
    }
    
    @SuppressWarnings("unused")
    private void removeExtendedSecurityRoles(NodeRef nodeRef, Set<String> readers, Set<String> writers)
    {
        NodeRef filePlan = filePlanService.getFilePlan(nodeRef);
        
        removeExtendedSecurityRolesImpl(filePlan, readers, PROP_EXTENDED_READER_ROLE, FilePlanRoleService.ROLE_EXTENDED_READERS);
        removeExtendedSecurityRolesImpl(filePlan, writers, PROP_EXTENDED_WRITER_ROLE, FilePlanRoleService.ROLE_EXTENDED_WRITERS);
    } 
    
    @SuppressWarnings("unchecked")
    private void removeExtendedSecurityRolesImpl(NodeRef filePlan, Set<String> authorities, QName propertyName, String roleName)
    {
        if (authorities != null)
        {
            // get the reference count
            Map<String, Integer> referenceCountMap = (Map<String, Integer>)nodeService.getProperty(filePlan, propertyName);
            
            for (String authority : authorities)
            {       
                if (authority.equals(PermissionService.ALL_AUTHORITIES) == false)
                {
                    if (referenceCountMap == null)
                    {
                        // remove the authority from the role
                        filePlanRoleService.unassignRoleFromAuthority(filePlan, roleName, authority);
                    }
                    else
                    {
                        Integer count = referenceCountMap.get(authority);
                        if (count == null || count == 1)
                        {
                            // remove the authority from the role
                            filePlanRoleService.unassignRoleFromAuthority(filePlan, roleName, authority);
                        }
                    }
                }
            }
            
            // update the reference count
            nodeService.setProperty(filePlan, propertyName, (Serializable)removeFromMap(referenceCountMap, authorities));
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
            if (key.equals(PermissionService.ALL_AUTHORITIES) == false)
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
                    filePlanService.isFilePlanComponent(parent) == true)
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
                if (key.equals(PermissionService.ALL_AUTHORITIES) == false)
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
