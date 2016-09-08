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
package org.alfresco.module.org_alfresco_module_rm.vital;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * Vital record service interface implementation.
 * 
 * @author Roy Wetherall
 * @since 2.0
 */
public class VitalRecordServiceImpl implements VitalRecordService, 
                                               RecordsManagementModel,
                                               NodeServicePolicies.OnUpdatePropertiesPolicy,
                                               NodeServicePolicies.OnAddAspectPolicy
{
    private static final Period PERIOD_NONE = new Period("none|0");
    
    /** Services */
    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private RecordsManagementService rmService;
    private RecordsManagementActionService rmActionService;
    
    /** Behaviours */
    private JavaBehaviour onUpdateProperties;
    private JavaBehaviour onAddAspect;
    
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
     * @param rmService records management service
     */
    public void setRecordsManagementService(RecordsManagementService rmService)
    {
        this.rmService = rmService;
    }
    
    /**
     * @param rmActionService   records management action service
     */
    public void setRecordsManagementActionService(RecordsManagementActionService rmActionService)
    {
        this.rmActionService = rmActionService;
    }
    
    /**
     * Init method.
     */
    public void init()
    {
        onUpdateProperties = new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT);
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                ASPECT_VITAL_RECORD_DEFINITION,
                onUpdateProperties);
        
        onAddAspect = new JavaBehaviour(this, "onAddAspect", NotificationFrequency.TRANSACTION_COMMIT);
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnAddAspectPolicy.QNAME, 
                ASPECT_VITAL_RECORD_DEFINITION, 
                onAddAspect);
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        if (nodeService.exists(nodeRef) == true)
        {
            rmActionService.executeRecordsManagementAction(nodeRef, "broadcastVitalRecordDefinition");
        }        
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy#onAddAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public void onAddAspect(final NodeRef nodeRef, final QName aspectTypeQName)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("aspectTypeQName", aspectTypeQName);
        
        if (nodeService.exists(nodeRef) == true)
        {
            onUpdateProperties.disable();
            try
            {
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception 
                    {
                        // get the current review period value
                        Period currentReviewPeriod = (Period)nodeService.getProperty(nodeRef, PROP_REVIEW_PERIOD);
                        if (currentReviewPeriod == null ||
                            PERIOD_NONE.equals(currentReviewPeriod) == true)
                        {                        
                            // get the immediate parent
                            NodeRef parentRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
                            
                            // is the parent a record category
                            if (parentRef != null && 
                                FilePlanComponentKind.RECORD_CATEGORY.equals(rmService.getFilePlanComponentKind(parentRef)) == true)
                            {
                                // is the child a record category or folder
                                FilePlanComponentKind kind = rmService.getFilePlanComponentKind(nodeRef);
                                if (kind.equals(FilePlanComponentKind.RECORD_CATEGORY) == true ||
                                    kind.equals(FilePlanComponentKind.RECORD_FOLDER) == true)
                                {
                                    // set the vital record definition values to match that of the parent
                                    nodeService.setProperty(nodeRef, 
                                                            PROP_VITAL_RECORD_INDICATOR, 
                                                            nodeService.getProperty(parentRef, PROP_VITAL_RECORD_INDICATOR));
                                    nodeService.setProperty(nodeRef, 
                                                            PROP_REVIEW_PERIOD, 
                                                            nodeService.getProperty(parentRef, PROP_REVIEW_PERIOD));
                                }
                            }
                        }
                        
                        return null;
                    }
                }, AuthenticationUtil.getSystemUserName());
            }
            finally
            {
                onUpdateProperties.enable();
            }
        }
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#getVitalRecordDefinition(org.alfresco.service.cmr.repository.NodeRef)
     */
    public VitalRecordDefinition getVitalRecordDefinition(NodeRef nodeRef)
    {
        VitalRecordDefinition result = null;
        
        FilePlanComponentKind kind = rmService.getFilePlanComponentKind(nodeRef);
        if (FilePlanComponentKind.RECORD.equals(kind) == true)
        {
            result = resolveVitalRecordDefinition(nodeRef);
        }
        else
        {
            if (nodeService.hasAspect(nodeRef, ASPECT_VITAL_RECORD_DEFINITION) == true)
            {
                result = VitalRecordDefinitionImpl.create(nodeService, nodeRef);
            }
        }
        
        return result;
    }
    
    /**
     * Resolves the record vital definition.
     * <p>
     * NOTE:  Currently we only support the resolution of the vital record definition from the 
     * primary record parent.  ie the record folder the record was originally filed within.
     * <p>
     * TODO:  Add an algorithm to resolve the correct vital record definition when a record is filed in many
     * record folders.
     * 
     * @param record
     * @return VitalRecordDefinition
     */
    private VitalRecordDefinition resolveVitalRecordDefinition(NodeRef record)
    {
        NodeRef parent = nodeService.getPrimaryParent(record).getParentRef();
        return getVitalRecordDefinition(parent);        
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordService#setVitalRecordDefintion(org.alfresco.service.cmr.repository.NodeRef, boolean, org.alfresco.service.cmr.repository.Period)
     */
    @Override
    public VitalRecordDefinition setVitalRecordDefintion(NodeRef nodeRef, boolean enabled, Period reviewPeriod)
    {
        // Check params
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("enabled", enabled);
        
        // Set the properties (will automatically add the vital record definition aspect)
        nodeService.setProperty(nodeRef, PROP_VITAL_RECORD_INDICATOR, enabled);
        nodeService.setProperty(nodeRef, PROP_REVIEW_PERIOD, reviewPeriod);
        
        return new VitalRecordDefinitionImpl(enabled, reviewPeriod);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#isVitalRecord(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isVitalRecord(NodeRef nodeRef)
    {
        return nodeService.hasAspect(nodeRef, ASPECT_VITAL_RECORD);
    }
}
