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
package org.alfresco.module.org_alfresco_module_rm.disposition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry;
import org.alfresco.module.org_alfresco_module_rm.disposition.property.DispositionProperty;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Disposition service implementation.
 *
 * @author Roy Wetherall
 */
@BehaviourBean
public class DispositionServiceImpl extends    ServiceBaseImpl
                                    implements DispositionService,
                                               RecordsManagementModel,
                                               RecordsManagementPolicies.OnFileRecord
{
    /** Logger */
    private static Log logger = LogFactory.getLog(DispositionServiceImpl.class);

    /** Behaviour filter */
    private BehaviourFilter behaviourFilter;

    /** Records management service registry */
    private RecordsManagementServiceRegistry serviceRegistry;

    /** Disposition selection strategy */
    private DispositionSelectionStrategy dispositionSelectionStrategy;

    /** File plan service */
    private FilePlanService filePlanService;

    /** Record Folder Service */
    private RecordFolderService recordFolderService;

    /** Record Service */
    private RecordService recordService;

    /** Freeze Service */
    private FreezeService freezeService;

    /** Disposition properties */
    private Map<QName, DispositionProperty> dispositionProperties = new HashMap<QName, DispositionProperty>(4);

    /**
     * Set node service
     *
     * @param nodeService   the node service
     */
    @Override
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the dictionary service
     *
     * @param dictionaryServic  the dictionary service
     */
    @Override
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the behaviour filter.
     *
     * @param behaviourFilter   the behaviour filter
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * Set the records management service registry
     *
     * @param serviceRegistry   records management registry service
     */
    public void setRecordsManagementServiceRegistry(RecordsManagementServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * @param filePlanService	file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
		this.filePlanService = filePlanService;
	}

    /**
     * @param recordFolderService   record folder service
     */
    public void setRecordFolderService(RecordFolderService recordFolderService)
    {
        this.recordFolderService = recordFolderService;
    }

    /**
     * @param recordService     record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService =  recordService;
    }

    /**
     * @param freezeService     freeze service
     */
    public void setFreezeService(FreezeService freezeService)
    {
        this.freezeService = freezeService;
    }

    /**
     * Set the dispositionSelectionStrategy bean.
     *
     * @param dispositionSelectionStrategy
     */
    public void setDispositionSelectionStrategy(DispositionSelectionStrategy dispositionSelectionStrategy)
    {
        this.dispositionSelectionStrategy = dispositionSelectionStrategy;
    }

    /**
     * Behavior to initialize the disposition schedule of a newly filed record.
     *
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.OnFileRecord#onFileRecord(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Behaviour(kind=BehaviourKind.CLASS, type="rma:record")
    public void onFileRecord(NodeRef nodeRef)
    {
        // initialise disposition details
        if (!nodeService.hasAspect(nodeRef, ASPECT_DISPOSITION_LIFECYCLE))
        {
            DispositionSchedule di = getDispositionSchedule(nodeRef);
            if (di != null && di.isRecordLevelDisposition())
            {
                nodeService.addAspect(nodeRef, ASPECT_DISPOSITION_LIFECYCLE, null);
            }
        }
    };

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#refreshDispositionAction(NodeRef)
     */
    @Override
    public void refreshDispositionAction(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        // get this disposition instructions for the node
        DispositionSchedule di = getDispositionSchedule(nodeRef);
        if (di != null)
        {
            List<DispositionActionDefinition> dispositionActionDefinitions = di.getDispositionActionDefinitions();
            if (!dispositionActionDefinitions.isEmpty())
            {
                // get the first disposition action definition
                DispositionActionDefinition nextDispositionActionDefinition = dispositionActionDefinitions.get(0);

                // initialise the details of the next disposition action
                initialiseDispositionAction(nodeRef, nextDispositionActionDefinition);
            }
        }
    }

    /** ========= Disposition Property Methods ========= */

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#registerDispositionProperty(org.alfresco.module.org_alfresco_module_rm.disposition.property.DispositionProperty)
     */
    @Override
    public void registerDispositionProperty(DispositionProperty dispositionProperty)
    {
        dispositionProperties.put(dispositionProperty.getQName(), dispositionProperty);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#getDispositionProperties(boolean, java.lang.String)
     */
    @Override
    public Collection<DispositionProperty> getDispositionProperties(boolean isRecordLevel, String dispositionAction)
    {
        Collection<DispositionProperty> values = dispositionProperties.values();
        List<DispositionProperty> result = new ArrayList<DispositionProperty>(values.size());
        for (DispositionProperty dispositionProperty : values)
        {
            boolean test = dispositionProperty.applies(isRecordLevel, dispositionAction);
            if (test)
            {
                result.add(dispositionProperty);
            }
        }
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#getDispositionProperties()
     */
    @Override
    public Collection<DispositionProperty> getDispositionProperties()
    {
        return dispositionProperties.values();
    }

    /** ========= Disposition Schedule Methods ========= */

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#getDispositionSchedule(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public DispositionSchedule getDispositionSchedule(NodeRef nodeRef)
    {
        DispositionSchedule di = null;
        NodeRef diNodeRef = null;
        if (isRecord(nodeRef))
        {
            // Get the record folders for the record
            List<NodeRef> recordFolders = recordFolderService.getRecordFolders(nodeRef);
            // At this point, we may have disposition instruction objects from 1..n folders.
            diNodeRef = dispositionSelectionStrategy.selectDispositionScheduleFrom(recordFolders);
        }
        else
        {
            // Get the disposition instructions for the node reference provided
            diNodeRef = getDispositionScheduleImpl(nodeRef);
        }

        if (diNodeRef != null)
        {
            di = new DispositionScheduleImpl(serviceRegistry, nodeService, diNodeRef);
        }

        return di;
    }

    /**
     * This method returns a NodeRef
     * Gets the disposition instructions
     *
     * @param nodeRef
     * @return
     */
    private NodeRef getDispositionScheduleImpl(NodeRef nodeRef)
    {
        NodeRef result = getAssociatedDispositionScheduleImpl(nodeRef);

        if (result == null)
        {
            NodeRef parent = this.nodeService.getPrimaryParent(nodeRef).getParentRef();
            if (parent != null && filePlanService.isRecordCategory(parent))
            {
                result = getDispositionScheduleImpl(parent);
            }
        }
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#getAssociatedDispositionSchedule(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public DispositionSchedule getAssociatedDispositionSchedule(NodeRef nodeRef)
    {
        DispositionSchedule ds = null;

        // Check the noderef parameter
        ParameterCheck.mandatory("nodeRef", nodeRef);
        if (nodeService.exists(nodeRef))
        {
            // Get the associated disposition schedule node reference
            NodeRef dsNodeRef = getAssociatedDispositionScheduleImpl(nodeRef);
            if (dsNodeRef != null)
            {
                // Cerate disposition schedule object
                ds = new DispositionScheduleImpl(serviceRegistry, nodeService, dsNodeRef);
            }
        }

        return ds;
    }

    /**
     * Gets the node reference of the disposition schedule associated with the container.
     *
     * @param nodeRef   node reference of the container
     * @return {@link NodeRef}  node reference of the disposition schedule, null if none
     */
    private NodeRef getAssociatedDispositionScheduleImpl(NodeRef nodeRef)
    {
        NodeRef result = null;
        ParameterCheck.mandatory("nodeRef", nodeRef);

        // Make sure we are dealing with an RM node
        if (!filePlanService.isFilePlanComponent(nodeRef))
        {
            throw new AlfrescoRuntimeException("Can not find the associated disposition schedule for a non records management component. (nodeRef=" + nodeRef.toString() + ")");
        }

        if (this.nodeService.hasAspect(nodeRef, ASPECT_SCHEDULED))
        {
            List<ChildAssociationRef> childAssocs = this.nodeService.getChildAssocs(nodeRef, ASSOC_DISPOSITION_SCHEDULE, RegexQNamePattern.MATCH_ALL);
            if (childAssocs.size() != 0)
            {
                ChildAssociationRef firstChildAssocRef = childAssocs.get(0);
                result = firstChildAssocRef.getChildRef();
            }
        }

        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#getAssociatedRecordsManagementContainer(org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule)
     */
    @Override
    public NodeRef getAssociatedRecordsManagementContainer(DispositionSchedule dispositionSchedule)
    {
        ParameterCheck.mandatory("dispositionSchedule", dispositionSchedule);
        NodeRef result = null;

        NodeRef dsNodeRef = dispositionSchedule.getNodeRef();
        if (nodeService.exists(dsNodeRef))
        {
            List<ChildAssociationRef> assocs = this.nodeService.getParentAssocs(dsNodeRef, ASSOC_DISPOSITION_SCHEDULE, RegexQNamePattern.MATCH_ALL);
            if (assocs.size() != 0)
            {
                if (assocs.size() != 1)
                {
                    // TODO in the future we should be able to support disposition schedule reuse, but for now just warn that
                    //      only the first disposition schedule will be considered
                    if (logger.isWarnEnabled())
                    {
                        logger.warn("Disposition schedule has more than one associated records management container.  " +
                        		    "This is not currently supported so only the first container will be considered. " +
                        		    "(dispositionScheduleNodeRef=" + dispositionSchedule.getNodeRef().toString() + ")");
                    }
                }

                // Get the container reference
                ChildAssociationRef assoc = assocs.get(0);
                result = assoc.getParentRef();
            }
        }

        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#hasDisposableItems(org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule)
     */
    @Override
    public boolean hasDisposableItems(DispositionSchedule dispositionSchdule)
    {
    	return !getDisposableItems(dispositionSchdule).isEmpty();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#getDisposableItems(org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule)
     */
    @Override
    public List<NodeRef> getDisposableItems(DispositionSchedule dispositionSchedule)
    {
        ParameterCheck.mandatory("dispositionSchedule", dispositionSchedule);

        // Get the associated container
        NodeRef rmContainer = getAssociatedRecordsManagementContainer(dispositionSchedule);

        // Return the disposable items
        return getDisposableItemsImpl(dispositionSchedule.isRecordLevelDisposition(), rmContainer);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#isDisposableItem(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean isDisposableItem(NodeRef nodeRef)
    {
        return nodeService.hasAspect(nodeRef, ASPECT_DISPOSITION_LIFECYCLE);
    }

    /**
     *
     * @param isRecordLevelDisposition
     * @param rmContainer
     * @param root
     * @return
     */
    private List<NodeRef> getDisposableItemsImpl(boolean isRecordLevelDisposition, NodeRef rmContainer)
    {
        List<NodeRef> items = filePlanService.getAllContained(rmContainer);
        List<NodeRef> result = new ArrayList<NodeRef>(items.size());
        for (NodeRef item : items)
        {
            if (recordFolderService.isRecordFolder(item))
            {
                if (isRecordLevelDisposition)
                {
                    result.addAll(recordService.getRecords(item));
                }
                else
                {
                    result.add(item);
                }
            }
            else if (filePlanService.isRecordCategory(item) && getAssociatedDispositionScheduleImpl(item) == null)
            {
                result.addAll(getDisposableItemsImpl(isRecordLevelDisposition, item));
            }
        }
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#createDispositionSchedule(org.alfresco.service.cmr.repository.NodeRef, java.util.Map)
     */
    @Override
    public DispositionSchedule createDispositionSchedule(NodeRef nodeRef, Map<QName, Serializable> props)
    {
        NodeRef dsNodeRef = null;

        // Check mandatory parameters
        ParameterCheck.mandatory("nodeRef", nodeRef);

        // Check exists
        if (!nodeService.exists(nodeRef))
        {
            throw new AlfrescoRuntimeException("Unable to create disposition schedule, because node does not exist. (nodeRef=" + nodeRef.toString() + ")");
        }

        // Check is sub-type of rm:recordCategory
        QName nodeRefType = nodeService.getType(nodeRef);
        if (!TYPE_RECORD_CATEGORY.equals(nodeRefType) &&
            !dictionaryService.isSubClass(nodeRefType, TYPE_RECORD_CATEGORY))
        {
            throw new AlfrescoRuntimeException("Unable to create disposition schedule on a node that is not a records management container.");
        }

        behaviourFilter.disableBehaviour(nodeRef, ASPECT_SCHEDULED);
        try
        {
            // Add the schedules aspect if required
            if (!nodeService.hasAspect(nodeRef, ASPECT_SCHEDULED))
            {
                nodeService.addAspect(nodeRef, ASPECT_SCHEDULED, null);
            }

            // Check whether there is already a disposition schedule object present
            List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ASSOC_DISPOSITION_SCHEDULE, RegexQNamePattern.MATCH_ALL);
            if (assocs.size() == 0)
            {
            	DispositionSchedule currentDispositionSchdule = getDispositionSchedule(nodeRef);
            	if (currentDispositionSchdule != null)
            	{
            		List<NodeRef> items = getDisposableItemsImpl(currentDispositionSchdule.isRecordLevelDisposition(), nodeRef);
            		if (items.size() != 0)
            		{
            			throw new AlfrescoRuntimeException("Can not create a disposition schedule if there are disposable items already under the control of an other disposition schedule");
            		}
            	}

                // Create the disposition schedule object
                dsNodeRef = nodeService.createNode(
                        nodeRef,
                        ASSOC_DISPOSITION_SCHEDULE,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName("dispositionSchedule")),
                        TYPE_DISPOSITION_SCHEDULE,
                        props).getChildRef();
            }
            else
            {
                // Error since the node already has a disposition schedule set
                throw new AlfrescoRuntimeException("Unable to create disposition schedule on node that already has a disposition schedule.");
            }
        }
        finally
        {
            behaviourFilter.enableBehaviour(nodeRef, ASPECT_SCHEDULED);
        }

        // Create the return object
        return new DispositionScheduleImpl(serviceRegistry, nodeService, dsNodeRef);
    }

    /** ========= Disposition Action Definition Methods ========= */

    /**
     *
     */
    @Override
    public DispositionActionDefinition addDispositionActionDefinition(
                                            DispositionSchedule schedule,
                                            Map<QName, Serializable> actionDefinitionParams)
    {
        // make sure at least a name has been defined
        String name = (String)actionDefinitionParams.get(PROP_DISPOSITION_ACTION_NAME);
        if (name == null || name.length() == 0)
        {
            throw new IllegalArgumentException("'name' parameter is mandatory when creating a disposition action definition");
        }

        // TODO: also check the action name is valid?

        // create the child association from the schedule to the action definition
        NodeRef actionNodeRef = this.nodeService.createNode(schedule.getNodeRef(),
                    RecordsManagementModel.ASSOC_DISPOSITION_ACTION_DEFINITIONS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                    QName.createValidLocalName(name)),
                    RecordsManagementModel.TYPE_DISPOSITION_ACTION_DEFINITION, actionDefinitionParams).getChildRef();

        // get the updated disposition schedule and retrieve the new action definition
        NodeRef scheduleParent = this.nodeService.getPrimaryParent(schedule.getNodeRef()).getParentRef();
        DispositionSchedule updatedSchedule = this.getDispositionSchedule(scheduleParent);
        return updatedSchedule.getDispositionActionDefinition(actionNodeRef.getId());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#removeDispositionActionDefinition(org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule, org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition)
     */
    @Override
    public void removeDispositionActionDefinition(DispositionSchedule schedule, DispositionActionDefinition actionDefinition)
    {
        // check first whether action definitions can be removed
        if (hasDisposableItems(schedule))
        {
            throw new AlfrescoRuntimeException("Can not remove action definitions from schedule '" +
                        schedule.getNodeRef() + "' as one or more record or record folders are present.");
        }

        // remove the child node representing the action definition
        this.nodeService.removeChild(schedule.getNodeRef(), actionDefinition.getNodeRef());
    }

    /**
     * Updates the given disposition action definition belonging to the given disposition
     * schedule.
     *
     * @param schedule The DispositionSchedule the action belongs to
     * @param actionDefinition The DispositionActionDefinition to update
     * @param actionDefinitionParams Map of parameters to use to update the action definition
     * @return The updated DispositionActionDefinition
     */
    @Override
    public DispositionActionDefinition updateDispositionActionDefinition(
                                                DispositionActionDefinition actionDefinition,
                                                Map<QName, Serializable> actionDefinitionParams)
    {
        // update the node with properties
        this.nodeService.addProperties(actionDefinition.getNodeRef(), actionDefinitionParams);

        // get the updated disposition schedule and retrieve the updated action definition
        NodeRef ds = this.nodeService.getPrimaryParent(actionDefinition.getNodeRef()).getParentRef();
        DispositionSchedule updatedSchedule = new DispositionScheduleImpl(serviceRegistry, nodeService, ds);
        return updatedSchedule.getDispositionActionDefinition(actionDefinition.getId());
    }

    /** ========= Disposition Action Methods ========= */

    /**
     * Initialises the details of the next disposition action based on the details of a disposition
     * action definition.
     *
     *  @param  nodeRef node reference
     *  @param  dispositionActionDefinition disposition action definition
     */
    private void initialiseDispositionAction(NodeRef nodeRef, DispositionActionDefinition dispositionActionDefinition)
    {
        // Create the properties
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(10);

        // Calculate the asOf date
        Date asOfDate = null;
        Period period = dispositionActionDefinition.getPeriod();
        if (period != null)
        {
            Date contextDate = null;

            // Get the period properties value
            QName periodProperty = dispositionActionDefinition.getPeriodProperty();
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
        props.put(PROP_DISPOSITION_ACTION_ID, dispositionActionDefinition.getId());
        props.put(PROP_DISPOSITION_ACTION, dispositionActionDefinition.getName());
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
        DispositionAction da = new DispositionActionImpl(serviceRegistry, dispositionActionNodeRef);

        // Create the events
        List<RecordsManagementEvent> events = dispositionActionDefinition.getEvents();
        for (RecordsManagementEvent event : events)
        {
            // For every event create an entry on the action
            da.addEventCompletionDetails(event);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#isNextDispositionActionEligible(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean isNextDispositionActionEligible(NodeRef nodeRef)
    {
        boolean result = false;

        // Get the disposition instructions
        DispositionSchedule di = getDispositionSchedule(nodeRef);
        NodeRef nextDa = getNextDispositionActionNodeRef(nodeRef);
        if (di != null &&
            this.nodeService.hasAspect(nodeRef, ASPECT_DISPOSITION_LIFECYCLE) &&
            nextDa != null)
        {
            // If it has an asOf date and it is greater than now the action is eligible
            Date asOf = (Date)this.nodeService.getProperty(nextDa, PROP_DISPOSITION_AS_OF);
            if (asOf != null &&
                asOf.before(new Date()))
            {
                result = true;
            }

            if (!result)
            {
                DispositionAction da = new DispositionActionImpl(serviceRegistry, nextDa);
                DispositionActionDefinition dad = da.getDispositionActionDefinition();
                if (dad != null)
                {
                    boolean firstComplete = dad.eligibleOnFirstCompleteEvent();

                    List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(nextDa, ASSOC_EVENT_EXECUTIONS, RegexQNamePattern.MATCH_ALL);
                    for (ChildAssociationRef assoc : assocs)
                    {
                        NodeRef eventExecution = assoc.getChildRef();
                        Boolean isCompleteValue = (Boolean)this.nodeService.getProperty(eventExecution, PROP_EVENT_EXECUTION_COMPLETE);
                        boolean isComplete = false;
                        if (isCompleteValue != null)
                        {
                            isComplete = isCompleteValue.booleanValue();

                            // implement AND and OR combination of event completions
                            if (isComplete)
                            {
                                result = true;
                                if (firstComplete)
                                {
                                    break;
                                }
                            }
                            else
                            {
                                result = false;
                                if (!firstComplete)
                                {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Get the next disposition action node.  Null if none present.
     *
     * @param nodeRef       the disposable node reference
     * @return NodeRef      the next disposition action, null if none
     */
    private NodeRef getNextDispositionActionNodeRef(NodeRef nodeRef)
    {
        NodeRef result = null;
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ASSOC_NEXT_DISPOSITION_ACTION, ASSOC_NEXT_DISPOSITION_ACTION, 1, true);
        if (assocs.size() != 0)
        {
            result = assocs.get(0).getChildRef();
        }
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#getNextDispositionAction(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public DispositionAction getNextDispositionAction(NodeRef nodeRef)
    {
        DispositionAction result = null;
        NodeRef dispositionActionNodeRef = getNextDispositionActionNodeRef(nodeRef);

        if (dispositionActionNodeRef != null)
        {
            result = new DispositionActionImpl(serviceRegistry, dispositionActionNodeRef);
        }
        return result;
    }


    /** ========= Disposition Action History Methods ========= */

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#getCompletedDispositionActions(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public List<DispositionAction> getCompletedDispositionActions(NodeRef nodeRef)
    {
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ASSOC_DISPOSITION_ACTION_HISTORY, RegexQNamePattern.MATCH_ALL);
        List<DispositionAction> result = new ArrayList<DispositionAction>(assocs.size());
        for (ChildAssociationRef assoc : assocs)
        {
            NodeRef dispositionActionNodeRef = assoc.getChildRef();
            result.add(new DispositionActionImpl(serviceRegistry, dispositionActionNodeRef));
        }
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#getLastCompletedDispostionAction(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public DispositionAction getLastCompletedDispostionAction(NodeRef nodeRef)
    {
       DispositionAction result = null;
       List<DispositionAction> list = getCompletedDispositionActions(nodeRef);
       if (!list.isEmpty())
       {
           // Get the last disposition action in the list
           result = list.get(list.size()-1);
       }
       return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#isDisposableItemCutoff(NodeRef)
     */
    @Override
    public boolean isDisposableItemCutoff(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        return nodeService.hasAspect(nodeRef, ASPECT_CUT_OFF);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#updateNextDispositionAction(NodeRef)
     */
    @Override
    public void updateNextDispositionAction(final NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        RunAsWork<Void> runAsWork = new RunAsWork<Void>()
        {
            /**
             * @see org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork#doWork()
             */
            @Override
            public Void doWork()
            {
                // Get this disposition instructions for the node
                DispositionSchedule di = getDispositionSchedule(nodeRef);
                if (di != null)
                {
                    // Get the current action node
                    NodeRef currentDispositionAction = null;
                    if (nodeService.hasAspect(nodeRef, ASPECT_DISPOSITION_LIFECYCLE))
                    {
                        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ASSOC_NEXT_DISPOSITION_ACTION, ASSOC_NEXT_DISPOSITION_ACTION);
                        if (assocs.size() > 0)
                        {
                            currentDispositionAction = assocs.get(0).getChildRef();
                        }
                    }

                    if (currentDispositionAction != null)
                    {
                        // Move it to the history association
                        nodeService.moveNode(currentDispositionAction, nodeRef, ASSOC_DISPOSITION_ACTION_HISTORY, ASSOC_DISPOSITION_ACTION_HISTORY);
                    }

                    List<DispositionActionDefinition> dispositionActionDefinitions = di.getDispositionActionDefinitions();
                    DispositionActionDefinition currentDispositionActionDefinition = null;
                    DispositionActionDefinition nextDispositionActionDefinition = null;

                    if (currentDispositionAction == null)
                    {
                        if (!dispositionActionDefinitions.isEmpty())
                        {
                            // The next disposition action is the first action
                            nextDispositionActionDefinition = dispositionActionDefinitions.get(0);
                        }
                    }
                    else
                    {
                        // Get the current action
                        String currentADId = (String) nodeService.getProperty(currentDispositionAction, PROP_DISPOSITION_ACTION_ID);
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
                        if (!nodeService.hasAspect(nodeRef, ASPECT_DISPOSITION_LIFECYCLE))
                        {
                            // Add the disposition life cycle aspect
                            nodeService.addAspect(nodeRef, ASPECT_DISPOSITION_LIFECYCLE, null);
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
                            if (periodProperty != null &&
                                !RecordsManagementModel.PROP_DISPOSITION_AS_OF.equals(periodProperty))
                            {
                                // doesn't matter if the period property isn't set ... the asOfDate will get updated later
                                // when the value of the period property is set
                                contextDate = (Date) nodeService.getProperty(nodeRef, periodProperty);
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
                        NodeRef dispositionActionNodeRef = nodeService.createNode(
                                nodeRef,
                                ASSOC_NEXT_DISPOSITION_ACTION,
                                ASSOC_NEXT_DISPOSITION_ACTION,
                                TYPE_DISPOSITION_ACTION,
                                props).getChildRef();
                        DispositionAction da = new DispositionActionImpl(serviceRegistry, dispositionActionNodeRef);

                        // Create the events
                        List<RecordsManagementEvent> events = nextDispositionActionDefinition.getEvents();
                        for (RecordsManagementEvent event : events)
                        {
                            // For every event create an entry on the action
                            da.addEventCompletionDetails(event);
                        }
                    }
                }

                return null;
            }
        };

        AuthenticationUtil.runAsSystem(runAsWork);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#cutoffDisposableItem(NodeRef)
     */
    @Override
    public void cutoffDisposableItem(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        // check that the node ref is a filed record or record folder
        if (FilePlanComponentKind.RECORD_FOLDER.equals(filePlanService.getFilePlanComponentKind(nodeRef)) ||
            FilePlanComponentKind.RECORD.equals(filePlanService.getFilePlanComponentKind(nodeRef)))
        {
            if (!isDisposableItemCutoff(nodeRef) && !isFrozenOrHasFrozenChildren(nodeRef))
            {
                if (recordFolderService.isRecordFolder(nodeRef))
                {
                    // cut off all the children first
                    for (NodeRef record : recordService.getRecords(nodeRef))
                    {
                        applyCutoff(record);
                    }
                }

                // apply cut off
                applyCutoff(nodeRef);

                // remove uncut off aspect if applied
                if(nodeService.hasAspect(nodeRef, ASPECT_UNCUT_OFF))
                {
                    nodeService.removeAspect(nodeRef, ASPECT_UNCUT_OFF);
                }

                // close the record folder if it isn't already closed!
                if (recordFolderService.isRecordFolder(nodeRef) &&
                    !recordFolderService.isRecordFolderClosed(nodeRef))
                {
                    recordFolderService.closeRecordFolder(nodeRef);
                }
            }
        }
        else
        {
            throw new AlfrescoRuntimeException("Unable to peform cutoff, because node is not a disposible item. (nodeRef=" + nodeRef.toString() + ")");
        }
    }

    /**
     * Helper method to determine if a node is frozen or has frozen children
     *
     * @param nodeRef Node to be checked
     * @return <code>true</code> if the node is frozen or has frozen children, <code>false</code> otherwise
     */
    private boolean isFrozenOrHasFrozenChildren(NodeRef nodeRef)
    {
        boolean result = false;

        if (recordFolderService.isRecordFolder(nodeRef))
        {
            result = freezeService.isFrozen(nodeRef) || freezeService.hasFrozenChildren(nodeRef);
        }
        else if (recordService.isRecord(nodeRef))
        {
            result = freezeService.isFrozen(nodeRef);
        }
        else
        {
            throw new AlfrescoRuntimeException("The nodeRef '" + nodeRef + "' is neither a record nor a record folder.");
        }

        return result;
    }

    /**
     * Helper method to apply the cut off
     *
     * @param nodeRef   node to cut off
     */
    private void applyCutoff(final NodeRef nodeRef)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                // Apply the cut off aspect and set cut off date
                Map<QName, Serializable> cutOffProps = new HashMap<QName, Serializable>(1);
                cutOffProps.put(PROP_CUT_OFF_DATE, new Date());
                nodeService.addAspect(nodeRef, ASPECT_CUT_OFF, cutOffProps);

                return null;
            }
        });
    }
}
