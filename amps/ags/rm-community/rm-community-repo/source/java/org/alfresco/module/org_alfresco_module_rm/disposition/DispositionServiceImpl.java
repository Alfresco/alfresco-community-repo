/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.disposition;

import static org.apache.commons.lang3.BooleanUtils.isNotTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry;
import org.alfresco.module.org_alfresco_module_rm.disposition.property.DispositionProperty;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.dictionary.types.period.Immediately;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(DispositionServiceImpl.class);

    /** Transaction mode for setting next action */
    public enum WriteMode
    {
        /** Do not update any data. */
        READ_ONLY,
        /** Only set the "disposition as of" date. */
        DATE_ONLY,
        /**
         * Set the "disposition as of" date and the name of the next action. This only happens during the creation of a
         * disposition schedule impl node under a record or folder.
         */
        DATE_AND_NAME
    }

    /** Behaviour filter */
    private BehaviourFilter behaviourFilter;

    /** Records management service registry */
    private RecordsManagementServiceRegistry serviceRegistry;

    /** File plan service */
    private FilePlanService filePlanService;

    /** Record Folder Service */
    private RecordFolderService recordFolderService;

    /** Record Service */
    private RecordService recordService;

    /** Transaction service */
    private TransactionService transactionService;

    /** Disposition properties */
    private Map<QName, DispositionProperty> dispositionProperties = new HashMap<>(4);

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
     * @param dictionaryService  the dictionary service
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
     * @param transactionService transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
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
    }

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
        List<DispositionProperty> result = new ArrayList<>(values.size());
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
    public DispositionSchedule getDispositionSchedule(final NodeRef nodeRef)
    {
        DispositionSchedule ds = null;
        NodeRef dsNodeRef = null;
        if (isRecord(nodeRef))
        {
            // calculate disposition schedule without taking into account the user
            DispositionSchedule originDispositionSchedule = AuthenticationUtil.runAsSystem(new RunAsWork<DispositionSchedule>()
            {
                @Override
                public DispositionSchedule doWork()
                {
                    return getOriginDispositionSchedule(nodeRef);
                }
            });
            // if the initial disposition schedule of the record is folder based
            if (originDispositionSchedule == null || 
                    isNotTrue(originDispositionSchedule.isRecordLevelDisposition()))
            {
                return null;
            }
            
            final NextActionFromDisposition dsNextAction = getDispositionActionByNameForRecord(nodeRef);

            if (dsNextAction != null)
            {
                final NodeRef action = dsNextAction.getNextActionNodeRef();
                if (isNotTrue((Boolean)nodeService.getProperty(action, PROP_MANUALLY_SET_AS_OF)))
                {
                    if (!dsNextAction.getWriteMode().equals(WriteMode.READ_ONLY))
                    {
                        final String dispositionActionName = dsNextAction.getNextActionName();
                        final Date dispositionActionDate = dsNextAction.getNextActionDateAsOf();

                        RunAsWork<Void> runAsWork = () -> {
                            nodeService.setProperty(action, PROP_DISPOSITION_AS_OF, dispositionActionDate);
                            return null;
                        };

                        // if the current transaction is READ ONLY set the property on the node
                        // in a READ WRITE transaction
                        if (AlfrescoTransactionSupport.getTransactionReadState().equals(TxnReadState.TXN_READ_ONLY))
                        {
                            transactionService.getRetryingTransactionHelper().doInTransaction((RetryingTransactionCallback<Void>) () -> {
                                AuthenticationUtil.runAsSystem(runAsWork);
                                return null;
                            }, false, true);
                        }
                        else
                        {
                            AuthenticationUtil.runAsSystem(runAsWork);
                        }

                        if (dsNextAction.getWriteMode().equals(WriteMode.DATE_AND_NAME))
                        {
                            nodeService.setProperty(action, PROP_DISPOSITION_ACTION_NAME, dispositionActionName);
                        }
                    }
                }
                
                dsNodeRef = dsNextAction.getDispositionNodeRef();
            }
        }
        else
        {
            // Get the disposition instructions for the node reference provided
            dsNodeRef = getDispositionScheduleImpl(nodeRef);
        }

        if (dsNodeRef != null)
        {
            ds = new DispositionScheduleImpl(serviceRegistry, nodeService, dsNodeRef);
        }

        return ds;
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
    
    public DispositionSchedule getOriginDispositionSchedule(NodeRef nodeRef)
    {
        NodeRef parent = this.nodeService.getPrimaryParent(nodeRef).getParentRef();
        if (parent != null)
        {
            if (filePlanService.isRecordCategory(parent)) 
            {
                NodeRef result = getAssociatedDispositionScheduleImpl(parent);
                if (result == null)
                {
                    return getOriginDispositionSchedule(parent);
                }
                return new DispositionScheduleImpl(serviceRegistry, nodeService, result);
            }
            else
            {
                return getOriginDispositionSchedule(parent);
            }
        } 
        return null;
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
                // Create disposition schedule object
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
            throw new AlfrescoRuntimeException("Can not find the associated retention schedule for a non records management component. (nodeRef=" + nodeRef.toString() + ")");
        }
        if (getInternalNodeService().hasAspect(nodeRef, ASPECT_SCHEDULED))
        {
            List<ChildAssociationRef> childAssocs = getInternalNodeService().getChildAssocs(nodeRef, ASSOC_DISPOSITION_SCHEDULE, RegexQNamePattern.MATCH_ALL);
            if (!childAssocs.isEmpty())
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
            if (!assocs.isEmpty())
            {
                if (assocs.size() != 1)
                {
                    // TODO in the future we should be able to support disposition schedule reuse, but for now just warn that
                    //      only the first disposition schedule will be considered
                    if (LOGGER.isWarnEnabled())
                    {
                        LOGGER.warn("Retention schedule has more than one associated records management container.  " +
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
        ParameterCheck.mandatory("dispositionSchedule", dispositionSchdule);

        // Get the associated container
        NodeRef rmContainer = getAssociatedRecordsManagementContainer(dispositionSchdule);

        return hasDisposableItemsImpl(dispositionSchdule.isRecordLevelDisposition(), rmContainer);
    }

    /**
     * Method that provides a boolean if given Records Management Container has disposable items.
     * This method is similar to getDisposableItemsImpl(boolean isRecordLevelDisposition, NodeRef rmContainer) but with improved performance:
     * For RecordLevelDisposition it will limit Record retrieval to 1.
     * Early returns once the first occurrence is found.
     * @param isRecordLevelDisposition
     * @param rmContainer
     * @return
     */
    private boolean hasDisposableItemsImpl(boolean isRecordLevelDisposition, NodeRef rmContainer)
    {
        List<NodeRef> items = filePlanService.getAllContained(rmContainer);
        for (NodeRef item : items)
        {
            if (recordFolderService.isRecordFolder(item))
            {
                if (isRecordLevelDisposition)
                {
                    List<ChildAssociationRef> assocs = nodeService.getChildAssocs(item, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL, 1, true);
                    if (!assocs.isEmpty())
                    {
                        return true;
                    }
                }
                else
                {
                    return true;
                }
            }
            else if (filePlanService.isRecordCategory(item) && getAssociatedDispositionScheduleImpl(item) == null)
            {
                if (hasDisposableItemsImpl(isRecordLevelDisposition, item));
                {
                    return true;
                }
            }
        }
        return false;
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
     * @return
     */
    private List<NodeRef> getDisposableItemsImpl(boolean isRecordLevelDisposition, NodeRef rmContainer)
    {
        List<NodeRef> items = filePlanService.getAllContained(rmContainer);
        List<NodeRef> result = new ArrayList<>(items.size());
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
            throw new AlfrescoRuntimeException("Unable to create retention schedule, because node does not exist. (nodeRef=" + nodeRef.toString() + ")");
        }

        // Check is sub-type of rm:recordCategory
        QName nodeRefType = nodeService.getType(nodeRef);
        if (!TYPE_RECORD_CATEGORY.equals(nodeRefType) &&
            !dictionaryService.isSubClass(nodeRefType, TYPE_RECORD_CATEGORY))
        {
            throw new AlfrescoRuntimeException("Unable to create retention schedule on a node that is not a records management container.");
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
            if (assocs.isEmpty())
            {
            	DispositionSchedule currentDispositionSchdule = getDispositionSchedule(nodeRef);
            	if (currentDispositionSchdule != null)
            	{
            		List<NodeRef> items = getDisposableItemsImpl(currentDispositionSchdule.isRecordLevelDisposition(), nodeRef);
            		if (!items.isEmpty())
            		{
            			throw new AlfrescoRuntimeException("Can not create a retention schedule if there are disposable items already under the control of an other retention schedule");
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
                throw new AlfrescoRuntimeException("Unable to create retention schedule on node that already has a retention schedule.");
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
     *  @param nodeRef node reference
     *  @param dispositionActionDefinition disposition action definition
     */
    private DispositionAction initialiseDispositionAction(final NodeRef nodeRef, DispositionActionDefinition dispositionActionDefinition)
    {
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeRef, ASSOC_NEXT_DISPOSITION_ACTION, ASSOC_NEXT_DISPOSITION_ACTION, 1, true);
        if (childAssocs != null && !childAssocs.isEmpty())
        {
            return new DispositionActionImpl(serviceRegistry, childAssocs.get(0).getChildRef());
        }

        // Create the properties
        final Map<QName, Serializable> props = new HashMap<>(10);

        Date asOfDate = calculateAsOfDate(nodeRef, dispositionActionDefinition);

        // Set the property values
        props.put(PROP_DISPOSITION_ACTION_ID, dispositionActionDefinition.getId());
        props.put(PROP_DISPOSITION_ACTION, dispositionActionDefinition.getName());
        if (asOfDate != null)
        {
            props.put(PROP_DISPOSITION_AS_OF, asOfDate);
        }

        DispositionAction da;
        // check if current transaction is a READ ONLY one and if true create the node in a READ WRITE transaction
        if (AlfrescoTransactionSupport.getTransactionReadState().equals(TxnReadState.TXN_READ_ONLY))
        {
            da =
                    transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<DispositionAction>()
                    {
                        public DispositionAction execute() throws Throwable
                        {
                            return createDispositionAction(nodeRef, props);
                        }
                    }, false, true);
        }
        else
        {
            da = createDispositionAction(nodeRef, props);
        }

        // Create the events
        List<RecordsManagementEvent> events = dispositionActionDefinition.getEvents();
        for (RecordsManagementEvent event : events)
        {
            // For every event create an entry on the action
            da.addEventCompletionDetails(event);
        }
        return da;
    }

    /** Creates a new disposition action object
     *
     * @param nodeRef node reference
     * @param props properties of the disposition action to be created
     * @return the disposition action object
     */
    private DispositionAction createDispositionAction(final NodeRef nodeRef, Map<QName, Serializable> props)
    {
        NodeRef dispositionActionNodeRef = nodeService.createNode(
                nodeRef,
                ASSOC_NEXT_DISPOSITION_ACTION,
                ASSOC_NEXT_DISPOSITION_ACTION,
                TYPE_DISPOSITION_ACTION,
                props).getChildRef();

        return new DispositionActionImpl(serviceRegistry, dispositionActionNodeRef);
    }

    /**
     * Compute the "disposition as of" date (if necessary) for a disposition action and a node.
     *
     * @param nodeRef The node which the schedule applies to.
     * @param dispositionActionDefinition The definition of the disposition action.
     * @return The new "disposition as of" date.
     */
    @Override
    public Date calculateAsOfDate(NodeRef nodeRef, DispositionActionDefinition dispositionActionDefinition)
    {
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
                if (RecordsManagementModel.PROP_DISPOSITION_AS_OF.equals(periodProperty))
                {
                    DispositionAction lastCompletedDispositionAction = getLastCompletedDispostionAction(nodeRef);
                    if (lastCompletedDispositionAction != null)
                    {
                        contextDate = lastCompletedDispositionAction.getCompletedAt();
                    }
                    else
                    {
                        contextDate = (Date)this.nodeService.getProperty(nodeRef, periodProperty);
                    }

                }
                else
                {
                    // doesn't matter if the period property isn't set ... the asOfDate will get updated later
                    // when the value of the period property is set
                    contextDate = (Date)this.nodeService.getProperty(nodeRef, periodProperty);
                }
            }
            else
            {
                if (period.getPeriodType().equals(Immediately.PERIOD_TYPE))
                {
                    contextDate = (Date)nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED);
                }
                else
                {
                    // for now use 'NOW' as the default context date
                    // TODO set the default period property ... cut off date or last disposition date depending on context
                    contextDate = new Date();
                }
            }

            // Calculate the as of date
            if (contextDate != null)
            {
                asOfDate = period.getNextDate(contextDate);
            }
        }
        return asOfDate;
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
        DispositionAction nextDa = getNextDispositionAction(nodeRef);

        if (di != null &&
                nodeService.hasAspect(nodeRef, ASPECT_DISPOSITION_LIFECYCLE) &&
                nextDa != null)
        {
            // for accession step we can have also AND between step conditions
            boolean combineSteps = false;
            if (nextDa.getName().equals("accession"))
            {
                NodeRef accessionNodeRef = di.getDispositionActionDefinitionByName("accession").getNodeRef();
                if (accessionNodeRef != null)
                {
                    Boolean combineStepsProp = (Boolean) getInternalNodeService().getProperty(accessionNodeRef, PROP_COMBINE_DISPOSITION_STEP_CONDITIONS);
                    if (combineStepsProp != null)
                    {
                        combineSteps = combineStepsProp;
                    }
                }
            }
            Date asOf = (Date) getInternalNodeService().getProperty(nextDa.getNodeRef(), PROP_DISPOSITION_AS_OF);
            boolean asOfDateInPast = false;
            if (asOf != null)
            {
                asOfDateInPast = asOf.before(new Date());
            }
            if (asOfDateInPast && !combineSteps)
            {
                return true;
            }
            else if (!asOfDateInPast && combineSteps)
            {
                return false;
            }
            DispositionAction da = new DispositionActionImpl(serviceRegistry, nextDa.getNodeRef());
            DispositionActionDefinition dad = da.getDispositionActionDefinition();
            if (dad != null)
            {
                boolean firstComplete =  authenticationUtil.runAsSystem(() -> dad.eligibleOnFirstCompleteEvent());

                List<ChildAssociationRef> assocs = getInternalNodeService().getChildAssocs(nextDa.getNodeRef(), ASSOC_EVENT_EXECUTIONS,
                        RegexQNamePattern.MATCH_ALL);
                for (ChildAssociationRef assoc : assocs)
                {
                    NodeRef eventExecution = assoc.getChildRef();
                    Boolean isCompleteValue = (Boolean) getInternalNodeService().getProperty(eventExecution, PROP_EVENT_EXECUTION_COMPLETE);
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
        if (!assocs.isEmpty())
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
        List<DispositionAction> result = new ArrayList<>(assocs.size());
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
                DispositionSchedule dispositionSchedule = getDispositionSchedule(nodeRef);

                updateNextDispositionAction(nodeRef, dispositionSchedule);

                return null;
            }

        };

        AuthenticationUtil.runAsSystem(runAsWork);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService#updateNextDispositionAction(NodeRef)
     */
    @Override
    public void updateNextDispositionAction(final NodeRef nodeRef, final DispositionSchedule dispositionSchedule)
    {


        RunAsWork<Void> runAsWork = new RunAsWork<Void>()
        {
            /**
             * @see org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork#doWork()
             */
            @Override
            public Void doWork()
            {

                if (dispositionSchedule != null)
                {
                    // Get the current action node
                    NodeRef currentDispositionAction = null;
                    if (nodeService.hasAspect(nodeRef, ASPECT_DISPOSITION_LIFECYCLE))
                    {
                        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ASSOC_NEXT_DISPOSITION_ACTION, ASSOC_NEXT_DISPOSITION_ACTION);
                        if (!assocs.isEmpty())
                        {
                            currentDispositionAction = assocs.get(0).getChildRef();
                        }
                    }

                    if (currentDispositionAction != null)
                    {
                        // Move it to the history association
                        nodeService.moveNode(currentDispositionAction, nodeRef, ASSOC_DISPOSITION_ACTION_HISTORY, ASSOC_DISPOSITION_ACTION_HISTORY);
                    }

                    List<DispositionActionDefinition> dispositionActionDefinitions = dispositionSchedule.getDispositionActionDefinitions();
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
                        currentDispositionActionDefinition = dispositionSchedule.getDispositionActionDefinition(currentADId);

                        // When the record has multiple disposition schedules the current disposition action may not be found by id
                        // In this case it will be searched by name
                        if(currentDispositionActionDefinition == null)
                        {
                            String currentADName = (String) nodeService.getProperty(currentDispositionAction, PROP_DISPOSITION_ACTION);
                            currentDispositionActionDefinition = dispositionSchedule.getDispositionActionDefinitionByName(currentADName);
                        }

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

                        initialiseDispositionAction(nodeRef, nextDispositionActionDefinition);
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
    public void cutoffDisposableItem(final NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        // check that the node ref is a filed record or record folder
        if (FilePlanComponentKind.RECORD_FOLDER.equals(filePlanService.getFilePlanComponentKind(nodeRef)) ||
            FilePlanComponentKind.RECORD.equals(filePlanService.getFilePlanComponentKind(nodeRef)))
        {
            if (!isDisposableItemCutoff(nodeRef))
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
                    // runAs system so that we can close a record that has already been cutoff
                    authenticationUtil.runAsSystem(new RunAsWork<Void>()
                    {
                        public Void doWork() throws Exception
                        {
                            recordFolderService.closeRecordFolder(nodeRef);
                            return null;
                        }
                    });
                }
            }
            else
            {
                throw new AlfrescoRuntimeException("unable to perform cutoff, because node is frozen or has frozen children");
            }
        }
        else
        {
            throw new AlfrescoRuntimeException("Unable to peform cutoff, because node is not a disposible item. (nodeRef=" + nodeRef.toString() + ")");
        }
    }

    public Date getDispositionActionDate(NodeRef record, NodeRef dispositionSchedule, String dispositionActionName)
    {
        DispositionSchedule ds = new DispositionScheduleImpl(serviceRegistry, nodeService, dispositionSchedule);
        List<ChildAssociationRef> assocs = getInternalNodeService().getChildAssocs(dispositionSchedule);

        if (assocs != null && !assocs.isEmpty())
        {
            for (ChildAssociationRef assoc : assocs)
            {
                if (assoc != null && assoc.getQName().getLocalName().contains(dispositionActionName))
                {
                    DispositionActionDefinition actionDefinition = ds.getDispositionActionDefinition(assoc.getChildRef().getId());

                    return authenticationUtil.runAsSystem(() -> calculateAsOfDate(record, actionDefinition));
                }
            }
        }
        return null;
    }
    
    public void recalculateNextDispositionStep(NodeRef record)
    {
        List<NodeRef> recordFolders = recordFolderService.getRecordFolders(record);
        
        DispositionAction nextDispositionAction = getNextDispositionAction(record);
        
        if (nextDispositionAction != null)
        {
            NextActionFromDisposition dsNextAction = getNextDispositionAction(record, recordFolders, nextDispositionAction);
            if (dsNextAction != null)
            {
                final NodeRef action = dsNextAction.getNextActionNodeRef();
                final Date dispositionActionDate = dsNextAction.getNextActionDateAsOf();
                AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
                {
                    @Override
                    public Void doWork()
                    {
                        nodeService.setProperty(action, PROP_DISPOSITION_AS_OF, dispositionActionDate);
                        return null;
                    }
                });
            }
        }
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
                Map<QName, Serializable> cutOffProps = new HashMap<>(1);
                cutOffProps.put(PROP_CUT_OFF_DATE, new Date());
                nodeService.addAspect(nodeRef, ASPECT_CUT_OFF, cutOffProps);

                return null;
            }
        });
    }



   /**
    * Calculate next disposition action for a record
    *
    * @param record
    * @return next disposition action (name, date) and the disposition associated
    */

    protected NextActionFromDisposition getDispositionActionByNameForRecord(NodeRef record)
    {
        List<NodeRef> recordFolders = recordFolderService.getRecordFolders(record);
        DispositionAction nextDispositionAction = getNextDispositionAction(record);

        if (nextDispositionAction == null)
        {
            DispositionAction lastCompletedDispositionAction = getLastCompletedDispostionAction(record);
            if (lastCompletedDispositionAction != null)
            {
                // all disposition actions upon the given record were completed
                return null;
            }

            return getFirstDispositionAction(record, recordFolders);
        }
        else
        {
            return getNextDispositionAction(record, recordFolders, nextDispositionAction);
        }
    }

    /**
     * Calculate next disposition action when the record already has one
     * @param recordFolders
     * @param nextDispositionAction
     * @return next disposition action and the associated disposition schedule
     */
    private NextActionFromDisposition getNextDispositionAction(NodeRef record, List<NodeRef> recordFolders, DispositionAction nextDispositionAction)
    {
        String recordNextDispositionActionName = nextDispositionAction.getName();
        Date recordNextDispositionActionDate = nextDispositionAction.getAsOfDate();
        // We're looking for the latest date, so initially start with a very early one.
        Date nextDispositionActionDate = new Date(Long.MIN_VALUE);
        NodeRef dispositionNodeRef = null;

        // Find the latest "disposition as of" date from all the schedules this record is subject to.
        for (NodeRef folder : recordFolders)
        {
            NodeRef dsNodeRef = getDispositionScheduleImpl(folder);
            if (dsNodeRef != null)
            {
                Date dispActionDate = getDispositionActionDate(record, dsNodeRef, recordNextDispositionActionName);
                if (dispActionDate == null || (nextDispositionActionDate != null
                                && nextDispositionActionDate.before(dispActionDate)))
                {
                    nextDispositionActionDate = dispActionDate;
                    dispositionNodeRef = dsNodeRef;
                    if (dispActionDate == null)
                    {
                        // Treat null as the latest date possible (so stop searching further).
                        break;
                    }
                }
            }
        }
        if (dispositionNodeRef == null)
        {
           return null;
        }
        WriteMode mode = determineWriteMode(recordNextDispositionActionDate, nextDispositionActionDate);

        return new NextActionFromDisposition(dispositionNodeRef, nextDispositionAction.getNodeRef(),
                recordNextDispositionActionName, nextDispositionActionDate, mode);
    }

    /**
     * Determine what should be updated for an existing disposition schedule impl. We only update the date if the
     * existing date is earlier than the calculated one.
     *
     * @param recordNextDispositionActionDate The next action date found on the record node (or folder node).
     * @param nextDispositionActionDate The next action date calculated from the current disposition schedule(s)
     *            affecting the node.
     * @return READ_ONLY if nothing should be updated, or DATE_ONLY if the date needs updating.
     */
    private WriteMode determineWriteMode(Date recordNextDispositionActionDate, Date nextDispositionActionDate)
    {
        // Treat null dates as being the latest possible date.
        Date maxDate = new Date(Long.MAX_VALUE);
        Date recordDate = (recordNextDispositionActionDate != null ? recordNextDispositionActionDate : maxDate);
        Date calculatedDate = (nextDispositionActionDate != null ? nextDispositionActionDate : maxDate);

        // We only need to update the date if the current one is too early.
        if (recordDate.before(calculatedDate))
        {
            return WriteMode.DATE_ONLY;
        }
        else
        {
            return WriteMode.READ_ONLY;
        }
    }

    /**
     * Calculate first disposition action when the record doesn't have one
     * @param recordFolders
     * @return next disposition action and the associated disposition schedule
     */
    private NextActionFromDisposition getFirstDispositionAction(NodeRef record, List<NodeRef> recordFolders)
    {
        NodeRef newAction = null;
        String newDispositionActionName = null;
        // We're looking for the latest date, so start with a very early one.
        Date newDispositionActionDateAsOf = new Date(Long.MIN_VALUE);
        NodeRef dispositionNodeRef = null;
        for (NodeRef folder : recordFolders)
        {
            NodeRef folderDS = getDispositionScheduleImpl(folder);
            if (folderDS != null)
            {
                DispositionSchedule ds = new DispositionScheduleImpl(serviceRegistry, nodeService, folderDS);
                List<DispositionActionDefinition> dispositionActionDefinitions = ds.getDispositionActionDefinitions();

                if (dispositionActionDefinitions != null && dispositionActionDefinitions.size() > 0)
                {
                    DispositionActionDefinition firstDispositionActionDef = dispositionActionDefinitions.get(0);
                    dispositionNodeRef = folderDS;

                    if (newAction == null)
                    {
                        NodeRef recordOrFolder = record;
                        if (!ds.isRecordLevelDisposition()) 
                        {
                            recordOrFolder = folder;
                        }
                        DispositionAction firstDispositionAction = initialiseDispositionAction(recordOrFolder, firstDispositionActionDef);
                        newAction = firstDispositionAction.getNodeRef();
                        newDispositionActionName = (String)nodeService.getProperty(newAction, PROP_DISPOSITION_ACTION_NAME);
                        newDispositionActionDateAsOf = firstDispositionAction.getAsOfDate();
                    }
                    else if (firstDispositionActionDef.getPeriod() != null)
                    {
                        Date firstActionDate = calculateAsOfDate(record, firstDispositionActionDef);
                        if (firstActionDate == null || (newDispositionActionDateAsOf != null
                                        && newDispositionActionDateAsOf.before(firstActionDate)))
                        {
                            newDispositionActionName = firstDispositionActionDef.getName();
                            newDispositionActionDateAsOf = firstActionDate;
                            if (firstActionDate == null)
                            {
                                // Treat null as the latest date possible, so there's no point searching further.
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (newDispositionActionName == null || dispositionNodeRef == null || newAction == null)
        {
            return null;
        }
        return new NextActionFromDisposition(dispositionNodeRef, newAction,
                newDispositionActionName, newDispositionActionDateAsOf, WriteMode.DATE_AND_NAME);
    }
}
