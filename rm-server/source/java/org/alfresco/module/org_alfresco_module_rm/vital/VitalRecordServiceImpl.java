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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.security.FilePlanAuthenticationService;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyMap;

/**
 * Vital record service interface implementation.
 * 
 * @author Roy Wetherall
 * @since 2.0
 */
public class VitalRecordServiceImpl implements VitalRecordService, 
                                               RecordsManagementModel,
                                               NodeServicePolicies.OnUpdatePropertiesPolicy,
                                               NodeServicePolicies.OnCreateChildAssociationPolicy
{
    /** Services */
    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private RecordsManagementService rmService;
    private RecordsManagementActionService rmActionService;
    private FilePlanAuthenticationService filePlanAuthenticationService;
    private FilePlanService filePlanService;
    
    /** Behaviours */
    private JavaBehaviour onUpdateProperties;
    private JavaBehaviour onCreateChildAssociation;
    
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
    
    public void setFilePlanAuthenticationService(FilePlanAuthenticationService filePlanAuthenticationService)
    {
        this.filePlanAuthenticationService = filePlanAuthenticationService;
    }
    
    /**
     * @param filePlanService	file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService) 
    {
		this.filePlanService = filePlanService;
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
        
        onCreateChildAssociation = new JavaBehaviour(this, "onCreateChildAssociation", NotificationFrequency.TRANSACTION_COMMIT);        
        policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME, 
                TYPE_RECORD_FOLDER, 
                ContentModel.ASSOC_CONTAINS,
                onCreateChildAssociation);
        policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME, 
                TYPE_RECORD_CATEGORY, 
                ContentModel.ASSOC_CONTAINS,
                onCreateChildAssociation);
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    @Override
    public void onUpdateProperties(final NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        if (nodeService.exists(nodeRef) == true && 
            nodeService.hasAspect(nodeRef, ASPECT_FILE_PLAN_COMPONENT) == true)
        {
            // check that vital record definition has been changed in the first place
            Map<QName, Serializable> changedProps = PropertyMap.getChangedProperties(before, after);
            if (changedProps.containsKey(PROP_VITAL_RECORD_INDICATOR) == true ||
                changedProps.containsKey(PROP_REVIEW_PERIOD) == true)
            {
                filePlanAuthenticationService.runAsRmAdmin(new RunAsWork<Void>()
                {
                    @Override
                    public Void doWork() throws Exception
                    {
                        rmActionService.executeRecordsManagementAction(nodeRef, "broadcastVitalRecordDefinition");
                        return null;
                    }}
                );
            }
        }        
    }
    
    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    public void onCreateChildAssociation(ChildAssociationRef childAssociationRef, boolean bNew)
    {
        if (childAssociationRef != null)
        {
           final NodeRef nodeRef = childAssociationRef.getChildRef();
           if (nodeService.exists(nodeRef) == true)
           {
              onCreateChildAssociation.disable();
              onUpdateProperties.disable();               
              try
              {
                  AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
                  {
                      @Override
                      public Void doWork() throws Exception
                      {
                          if (filePlanService.isRecordCategory(nodeRef) == true ||
                              rmService.isRecordFolder(nodeRef) == true)
                          {
                              inheritVitalRecordDefinition(nodeRef);
                          }
                          
                          return null;
                      }
                  });
              }
              finally
              {
                  onCreateChildAssociation.enable();
                  onUpdateProperties.enable();  
              }
           }
        }
    }
    
    /**
     * Helper method to set the inherited vital record definition details.
     * 
     * @param nodeRef   node reference
     */
    private void inheritVitalRecordDefinition(NodeRef nodeRef)
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
                FilePlanComponentKind.RECORD_CATEGORY.equals(filePlanService.getFilePlanComponentKind(parentRef)) == true)
            {
                // is the child a record category or folder
                FilePlanComponentKind kind = filePlanService.getFilePlanComponentKind(nodeRef);
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
    }
    
    /**
     * Helper method used by services with access to the private bean to initialise vital record details.
     * 
     * TODO consider what (if any of this) should be on the public interface
     * 
     * @param nodeRef   node reference to initialise with vital record details
     */
    public void initialiseVitalRecord(NodeRef nodeRef)
    {
        // Calculate the review schedule
        VitalRecordDefinition viDef = getVitalRecordDefinition(nodeRef);
        if (viDef != null && viDef.isEnabled() == true)
        {
            Date reviewAsOf = viDef.getNextReviewDate();
            if (reviewAsOf != null)
            {
                Map<QName, Serializable> reviewProps = new HashMap<QName, Serializable>(1);
                reviewProps.put(RecordsManagementModel.PROP_REVIEW_AS_OF, reviewAsOf);
                
                if (nodeService.hasAspect(nodeRef, ASPECT_VITAL_RECORD) == false)
                {
                    nodeService.addAspect(nodeRef, RecordsManagementModel.ASPECT_VITAL_RECORD, reviewProps);
                }
                else
                {
                    Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
                    props.putAll(reviewProps);
                    nodeService.setProperties(nodeRef, props);
                }
            }
        }       
        else
        {
            // if we are re-filling then remove the vital aspect if it is not longer a vital record
            if (nodeService.hasAspect(nodeRef, ASPECT_VITAL_RECORD) == true)
            {
                nodeService.removeAspect(nodeRef, ASPECT_VITAL_RECORD);
            }
        }
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#getVitalRecordDefinition(org.alfresco.service.cmr.repository.NodeRef)
     */
    public VitalRecordDefinition getVitalRecordDefinition(NodeRef nodeRef)
    {
        VitalRecordDefinition result = null;
        
        FilePlanComponentKind kind = filePlanService.getFilePlanComponentKind(nodeRef);
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
