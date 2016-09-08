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
package org.alfresco.module.org_alfresco_module_rm.action;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.module.org_alfresco_module_rm.capability.AbstractCapability;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.event.EventCompletionDetails;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventType;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordService;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.StringUtils;

/**
 * Records management action executer base class
 * 
 * @author Roy Wetherall
 */
public abstract class RMActionExecuterAbstractBase  extends ActionExecuterAbstractBase
                                                    implements RecordsManagementAction,
                                                               RecordsManagementModel,
                                                               BeanNameAware
{
    /** Namespace service */
    protected NamespaceService namespaceService;
    
    /** Used to control transactional behaviour including post-commit auditing */
    protected TransactionService transactionService;
    
    /** Node service */
    protected NodeService nodeService;
    
    /** Dictionary service */
    protected DictionaryService dictionaryService;
    
    /** Content service */
    protected ContentService contentService;
    
    /** Action service */
    protected ActionService actionService;
    
    /** Records management action service */
    protected RecordsManagementAuditService recordsManagementAuditService;
    
    /** Records management action service */
    protected RecordsManagementActionService recordsManagementActionService;
    
    /** Records management service */
    protected RecordsManagementService recordsManagementService;
    
    /** Disposition service */
    protected DispositionService dispositionService;
    
    /** Vital record service */
    protected VitalRecordService vitalRecordService;
    
    /** Records management event service */
    protected RecordsManagementEventService recordsManagementEventService;
    
    /** Records management action service */
    protected RecordsManagementAdminService recordsManagementAdminService;
    
    /** Ownable service **/
    protected OwnableService ownableService;
    
    protected LinkedList<AbstractCapability> capabilities = new LinkedList<AbstractCapability>();;
    
    /** Default constructor */
    public RMActionExecuterAbstractBase()
    {
    }
    
    /**
     * Set the namespace service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    /**
     * Set the namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * Set node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * Set the content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * Set action service 
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    /**
     * Set the audit service that action details will be sent to
     */
    public void setRecordsManagementAuditService(RecordsManagementAuditService recordsManagementAuditService)
    {
        this.recordsManagementAuditService = recordsManagementAuditService;
    }

    /**
     * Set records management service
     */
    public void setRecordsManagementActionService(RecordsManagementActionService recordsManagementActionService)
    {
        this.recordsManagementActionService = recordsManagementActionService;
    }
    
    /**
     * Set records management service
     */
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }    
    
    /**
     * Set the disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }
    
    /**
     * @param vitalRecordService    vital record service
     */
    public void setVitalRecordService(VitalRecordService vitalRecordService)
    {
        this.vitalRecordService = vitalRecordService;
    }
    
    /** 
     * Set records management event service
     */
    public void setRecordsManagementEventService(RecordsManagementEventService recordsManagementEventService)
    {
        this.recordsManagementEventService = recordsManagementEventService;
    }
    
    
    /**
     * Set the ownable service
     * @param ownableSerice
     */
    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }

    /**
     * Register with a single capability
     * @param capability
     */
    public void setCapability(AbstractCapability capability)
    {
        capabilities.add(capability);
    }
    
    /**
     * Register with several capabilities
     * @param capabilities
     */
    public void setCapabilities(Collection<AbstractCapability> capabilities)
    {
        this.capabilities.addAll(capabilities);
    }
    
    public void setRecordsManagementAdminService(RecordsManagementAdminService recordsManagementAdminService)
    {
        this.recordsManagementAdminService = recordsManagementAdminService;
    }

    public RecordsManagementAdminService getRecordsManagementAdminService()
    {
        return recordsManagementAdminService;
    }

    /**
     * Init method
     */
    @Override
    public void init()
    {
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "contentService", contentService);
        PropertyCheck.mandatory(this, "actionService", actionService);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "recordsManagementAuditService", recordsManagementAuditService);
        PropertyCheck.mandatory(this, "recordsManagementActionService", recordsManagementActionService);
        PropertyCheck.mandatory(this, "recordsManagementService", recordsManagementService);
        PropertyCheck.mandatory(this, "recordsManagementAdminService", recordsManagementAdminService);
        PropertyCheck.mandatory(this, "recordsManagementEventService", recordsManagementEventService);
        for(AbstractCapability capability : capabilities)
        {
            capability.registerAction(this);
        }
    }
    
    /**
     * @see org.alfresco.repo.action.CommonResourceAbstractBase#setBeanName(java.lang.String)
     */
    @Override
    public void setBeanName(String name)
    {
        this.name = name;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAction#getName()
     */
    public String getName()
    {
        return this.name;
    }
    
    /*
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction#getLabel()
     */
    public String getLabel()
    {
        String label = I18NUtil.getMessage(this.getTitleKey());
        
        if (label == null)
        {
            // default to the name of the action with first letter capitalised
            label = StringUtils.capitalize(this.name);
        }
        
        return label;
    }
    
    /*
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction#getDescription()
     */
    public String getDescription()
    {
        String desc = I18NUtil.getMessage(this.getDescriptionKey());
        
        if (desc == null)
        {
            // default to the name of the action with first letter capitalised
            desc = StringUtils.capitalize(this.name);
        }
        
        return desc;
    }

    /**
     * By default an action is not a disposition action
     * 
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAction#isDispositionAction()
     */
    public boolean isDispositionAction()
    {
        return false;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAction#execute(org.alfresco.service.cmr.repository.NodeRef, java.util.Map)
     */
    public RecordsManagementActionResult execute(NodeRef filePlanComponent, Map<String, Serializable> parameters)
    {
        isExecutableImpl(filePlanComponent, parameters, true);
        
        // Create the action
        Action action = this.actionService.createAction(name);
        action.setParameterValues(parameters);
        
        recordsManagementAuditService.auditRMAction(this, filePlanComponent, parameters);
        
        // Execute the action
        this.actionService.executeAction(action, filePlanComponent);          
        
        // Get the result
        Object value = action.getParameterValue(ActionExecuterAbstractBase.PARAM_RESULT);
        return new RecordsManagementActionResult(value);
    }
    
    /**
     * Function to pad a string with zero '0' characters to the required length
     * 
     * @param s     String to pad with leading zero '0' characters
     * @param len   Length to pad to
     * 
     * @return padded string or the original if already at >=len characters 
     */
    protected String padString(String s, int len)
    {
       String result = s;
       for (int i=0; i<(len - s.length()); i++)
       {
           result = "0" + result;
       }
       return result;
    }    
    
    /**
     * By default there are no parameters.
     * 
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        // No parameters
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction#getProtectedProperties()
     */
    public Set<QName> getProtectedProperties()
    {
       return Collections.<QName>emptySet();
    }
    

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction#getProtectedAspects()
     */
    public Set<QName> getProtectedAspects()
    {
        return Collections.<QName>emptySet();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction#isExecutable(org.alfresco.service.cmr.repository.NodeRef, java.util.Map)
     */
    public boolean isExecutable(NodeRef filePlanComponent, Map<String, Serializable> parameters)
    {
        return isExecutableImpl(filePlanComponent, parameters, false);
    }
    
    protected abstract boolean isExecutableImpl(NodeRef filePlanComponent, Map<String, Serializable> parameters, boolean throwException);

    /**
     * By default, rmActions do not provide an implicit target nodeRef.
     */
    public NodeRef getImplicitTargetNodeRef()
    {
        return null;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#updateNextDispositionAction(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void updateNextDispositionAction(NodeRef nodeRef)
    {
        // Get this disposition instructions for the node
        DispositionSchedule di = dispositionService.getDispositionSchedule(nodeRef);
        if (di != null)
        {
            // Get the current action node
            NodeRef currentDispositionAction = null;
            if (this.nodeService.hasAspect(nodeRef, ASPECT_DISPOSITION_LIFECYCLE) == true)
            {
                List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(nodeRef, ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL);
                if (assocs.size() > 0)
                {
                    currentDispositionAction = assocs.get(0).getChildRef();
                }
            }
            
            if (currentDispositionAction != null)
            {
                // Move it to the history association
                this.nodeService.moveNode(currentDispositionAction, nodeRef, ASSOC_DISPOSITION_ACTION_HISTORY, ASSOC_DISPOSITION_ACTION_HISTORY);
            }
           
            List<DispositionActionDefinition> dispositionActionDefinitions = di.getDispositionActionDefinitions();
            DispositionActionDefinition currentDispositionActionDefinition = null;
            DispositionActionDefinition nextDispositionActionDefinition = null;
            
            if (currentDispositionAction == null)
            {
                if (dispositionActionDefinitions.isEmpty() == false)
                {
                    // The next disposition action is the first action
                    nextDispositionActionDefinition = dispositionActionDefinitions.get(0);
                }
            }
            else
            {
                // Get the current action
                String currentADId = (String)this.nodeService.getProperty(currentDispositionAction, PROP_DISPOSITION_ACTION_ID);
                currentDispositionActionDefinition = di.getDispositionActionDefinition(currentADId);
                
                // Get the next disposition action
                int index = currentDispositionActionDefinition.getIndex();
                index++;
                if (index < dispositionActionDefinitions.size())
                {
                    nextDispositionActionDefinition = dispositionActionDefinitions.get(index);
                }
            }
            
            if (nextDispositionActionDefinition != null)
            {
                if (this.nodeService.hasAspect(nodeRef, ASPECT_DISPOSITION_LIFECYCLE) == false)
                {
                    // Add the disposition life cycle aspect
                    this.nodeService.addAspect(nodeRef, ASPECT_DISPOSITION_LIFECYCLE, null);
                }
                
                // Create the properties
                Map<QName, Serializable> props = new HashMap<QName, Serializable>(10);
                
                // Calculate the asOf date
                Date asOfDate = null;
                Period period = nextDispositionActionDefinition.getPeriod();
                if (period != null)
                {
                    Date contextDate = null;
                    
                    // Get the period properties value
                    QName periodProperty = nextDispositionActionDefinition.getPeriodProperty();
                    if (periodProperty != null)
                    {
                        // doesn't matter if the period property isn't set ... the asOfDate will get updated later
                        // when the value of the period property is set
                        contextDate = (Date)this.nodeService.getProperty(nodeRef, periodProperty);                     
                    }
                    else
                    {
                        // for now use 'NOW' as the default context date 
                        // TODO set the default period property ... cut off date or last disposition date depending on context
                        contextDate = new Date();
                    }
                    
                    // Calculate the as of date
                    if (contextDate != null)
                    {
                        asOfDate = period.getNextDate(contextDate);
                    }
                }            
                
                // Set the property values
                props.put(PROP_DISPOSITION_ACTION_ID, nextDispositionActionDefinition.getId());
                props.put(PROP_DISPOSITION_ACTION, nextDispositionActionDefinition.getName());
                if (asOfDate != null)
                {
                    props.put(PROP_DISPOSITION_AS_OF, asOfDate);
                }
                
                // Create a new disposition action object
                NodeRef dispositionActionNodeRef = this.nodeService.createNode(
                        nodeRef, 
                        ASSOC_NEXT_DISPOSITION_ACTION, 
                        ASSOC_NEXT_DISPOSITION_ACTION, 
                        TYPE_DISPOSITION_ACTION,
                        props).getChildRef();     
                
                // Create the events
                List<RecordsManagementEvent> events = nextDispositionActionDefinition.getEvents();
                for (RecordsManagementEvent event : events)
                {
                    // For every event create an entry on the action
                    createEvent(event, dispositionActionNodeRef);
                }
            }
        }
    }
    
    /**
     * Creates the given records management event for the given 'next action'.
     * 
     * @param event The event to create
     * @param nextActionNodeRef The next action node
     * @return The created event NodeRef
     */
    protected NodeRef createEvent(RecordsManagementEvent event, NodeRef nextActionNodeRef)
    {
        NodeRef eventNodeRef = null;
        
        Map<QName, Serializable> eventProps = new HashMap<QName, Serializable>(7);
        eventProps.put(PROP_EVENT_EXECUTION_NAME, event.getName());
        // TODO display label
        RecordsManagementEventType eventType = recordsManagementEventService.getEventType(event.getType());
        eventProps.put(PROP_EVENT_EXECUTION_AUTOMATIC, eventType.isAutomaticEvent());
        eventProps.put(PROP_EVENT_EXECUTION_COMPLETE, false);
        
        // Create the event execution object
        this.nodeService.createNode(nextActionNodeRef, ASSOC_EVENT_EXECUTIONS,
                ASSOC_EVENT_EXECUTIONS, TYPE_EVENT_EXECUTION, eventProps);
        
        return eventNodeRef;
    }
    
    /**
     * Calculates and updates the <code>rma:dispositionEventsEligible</code>
     * property for the given next disposition action.
     * 
     * @param nextAction The next disposition action
     * @return The result of calculation
     */
    protected boolean updateEventEligible(DispositionAction nextAction)
    {
        List<EventCompletionDetails> events = nextAction.getEventCompletionDetails();
        
        boolean eligible = false;
        if (nextAction.getDispositionActionDefinition().eligibleOnFirstCompleteEvent() == false)
        {
            eligible = true;
            for (EventCompletionDetails event : events)
            {
                if (event.isEventComplete() == false)
                {
                    eligible = false;
                    break;
                }
            }
        }
        else
        {
            for (EventCompletionDetails event : events)
            {
                if (event.isEventComplete() == true)
                {
                    eligible = true;
                    break;
                }
            }
        }
        
        // Update the property with the eligible value
        this.nodeService.setProperty(nextAction.getNodeRef(), PROP_DISPOSITION_EVENTS_ELIGIBLE, eligible);
        
        return eligible;
    }
}
