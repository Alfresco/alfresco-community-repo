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

package org.alfresco.module.org_alfresco_module_rm.model.behaviour;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionImpl;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionScheduleImpl;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.event.EventCompletionDetails;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.rma.aspect.FrozenAspect;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordDefinition;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordService;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Search behaviour class.
 *
 * Manages the collapse of data onto the supporting aspect on the record/record folder.
 *
 * @author Roy Wetherall
 * @since 1.0
 */
public class RecordsManagementSearchBehaviour implements RecordsManagementModel,
                                                         NodeServicePolicies.OnMoveNodePolicy
{
    /** logger */
    private static Log logger = LogFactory.getLog(RecordsManagementSearchBehaviour.class);

    /** Policy component */
    private PolicyComponent policyComponent;

    /** Node service */
    private NodeService nodeService;

    /** Disposition service */
    private DispositionService dispositionService;

    /** Records management service registry */
    private RecordsManagementServiceRegistry recordsManagementServiceRegistry;

    /** Vital record service */
    private VitalRecordService vitalRecordService;

    /** Record folder service */
    private RecordFolderService recordFolderService;

    /** Record service*/
    private RecordService recordService;

    /**
     * Frozen aspect
     */
    private FrozenAspect frozenAspect;

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param dispositionService    the disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * @param policyComponent the policyComponent to set
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @param recordsManagementServiceRegistry  the records management service registry
     */
    public void setRecordsManagementServiceRegistry(RecordsManagementServiceRegistry recordsManagementServiceRegistry)
    {
        this.recordsManagementServiceRegistry = recordsManagementServiceRegistry;
    }

    /**
     * @param vitalRecordService    vital record service
     */
    public void setVitalRecordService(VitalRecordService vitalRecordService)
    {
        this.vitalRecordService = vitalRecordService;
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
        this.recordService = recordService;
    }

    /**
     * @param frozenAspect frozen aspect
     */
    public void setFrozenAspect(FrozenAspect frozenAspect)
    {
        this.frozenAspect = frozenAspect;
    }


    /** on add search aspect behaviour */
    private JavaBehaviour onAddSearchAspect = new JavaBehaviour(this, "rmSearchAspectAdd", NotificationFrequency.TRANSACTION_COMMIT);
    
    /** disposition action behaviours */
    private JavaBehaviour jbDispositionActionCreate = new JavaBehaviour(this, "dispositionActionCreate", NotificationFrequency.TRANSACTION_COMMIT);
    private JavaBehaviour jbDispositionActionPropertiesUpdate = new JavaBehaviour(this, "dispositionActionPropertiesUpdate", NotificationFrequency.TRANSACTION_COMMIT);
    
    /** disposition lifecycle behaviours */
    private JavaBehaviour jbDispositionLifeCycleAspect = new JavaBehaviour(this, "onAddDispositionLifecycleAspect", NotificationFrequency.TRANSACTION_COMMIT);
    
    /** disposition schedule behaviours */
    private JavaBehaviour jbDispositionSchedulePropertiesUpdate = new JavaBehaviour(this, "dispositionSchedulePropertiesUpdate", NotificationFrequency.TRANSACTION_COMMIT);
    
    /** event update behaviours */
    private JavaBehaviour jbEventExecutionUpdate = new JavaBehaviour(this, "eventExecutionUpdate", NotificationFrequency.TRANSACTION_COMMIT);
    private JavaBehaviour jbEventExecutionDelete = new JavaBehaviour(this, "eventExecutionDelete", NotificationFrequency.TRANSACTION_COMMIT);

    /** on move record or record folder behavior */
    private JavaBehaviour jbMoveNode = new JavaBehaviour(this, "onMoveNode", NotificationFrequency.TRANSACTION_COMMIT);

    /** Array of behaviours related to disposition schedule artifacts */
    private JavaBehaviour[] jbDispositionBehaviours =
    {
            jbDispositionActionCreate,
            jbDispositionActionPropertiesUpdate,
            jbDispositionSchedulePropertiesUpdate,
            jbEventExecutionUpdate,
            jbEventExecutionDelete,
            jbDispositionLifeCycleAspect
    };

    /**
     * Initialisation method
     */
    public void init()
    {
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
                TYPE_DISPOSITION_ACTION,
                jbDispositionActionCreate);

        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
                TYPE_DISPOSITION_ACTION,
                jbDispositionActionPropertiesUpdate);

        this.policyComponent.bindClassBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
                    TYPE_DISPOSITION_SCHEDULE,
                    jbDispositionSchedulePropertiesUpdate);

        this.policyComponent.bindAssociationBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateChildAssociation"),
                TYPE_DISPOSITION_ACTION,
                ASSOC_EVENT_EXECUTIONS,
                jbEventExecutionUpdate);

        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"),
                TYPE_EVENT_EXECUTION,
                jbEventExecutionDelete);

        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"),
                ASPECT_RM_SEARCH,
                onAddSearchAspect);
        
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"),
                ASPECT_DISPOSITION_LIFECYCLE,
                jbDispositionLifeCycleAspect);        

        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"),
                ASPECT_RECORD,
                new JavaBehaviour(this, "onAddRecordAspect", NotificationFrequency.TRANSACTION_COMMIT));

        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
                TYPE_RECORD_FOLDER,
                new JavaBehaviour(this, "recordFolderCreate", NotificationFrequency.TRANSACTION_COMMIT));

        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onSetNodeType"),
                TYPE_RECORD_FOLDER,
                new JavaBehaviour(this, "convertedToOrFromRecordFolder", NotificationFrequency.TRANSACTION_COMMIT));

        // Vital Records Review Details Rollup
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"),
                ASPECT_VITAL_RECORD_DEFINITION,
                new JavaBehaviour(this, "vitalRecordDefintionAddAspect", NotificationFrequency.TRANSACTION_COMMIT));
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
                ASPECT_VITAL_RECORD_DEFINITION,
                new JavaBehaviour(this, "vitalRecordDefintionUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT));

        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onMoveNode"),
                ASPECT_FILE_PLAN_COMPONENT,
                jbMoveNode);
    }

    /**
     * Disabled disposition schedule behaviour
     */
    public void disableDispositionScheduleBehaviour()
    {
        for (JavaBehaviour jb : jbDispositionBehaviours)
        {
            jb.disable();
        }
    }

    /**
     * Enables disposition schedule behaviour
     */
    public void enableDispositionScheduleBehaviour()
    {
        for (JavaBehaviour jb : jbDispositionBehaviours)
        {
            jb.enable();
        }
    }

    /**
     * Ensures the search aspect for the given node is present, complete and correct.
     *
     * @param recordOrFolder    node reference to record or record folder
     */
    public void fixupSearchAspect(NodeRef recordOrFolder)
    {
        // for now only deal with record folders
        if (recordFolderService.isRecordFolder(recordOrFolder))
        {
            // ensure the search aspect is applied
            applySearchAspect(recordOrFolder);

            // setup the properties relating to the disposition schedule
            setupDispositionScheduleProperties(recordOrFolder);

            // setup the properties relating to the disposition lifecycle
            DispositionAction da = dispositionService.getNextDispositionAction(recordOrFolder);
            if (da != null)
            {
                updateDispositionActionProperties(recordOrFolder, da.getNodeRef());
                setupDispositionActionEvents(recordOrFolder, da);
            }

            // setup the properties relating to the vital record indicator
            setVitalRecordDefintionDetails(recordOrFolder);
        }
    }

    /**
     * Updates the disposition action properties
     *
     * @param nodeRef   node reference
     * @param before    value of properties before
     * @param after     value of properties after
     */
    public void dispositionActionPropertiesUpdate(final NodeRef nodeRef, final Map<QName, Serializable> before, final Map<QName, Serializable> after)
    {
        if (nodeService.exists(nodeRef))
        {
            AuthenticationUtil.runAs(new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    ChildAssociationRef assoc = nodeService.getPrimaryParent(nodeRef);
                    if (assoc.getTypeQName().equals(ASSOC_NEXT_DISPOSITION_ACTION))
                    {
                        // Get the record (or record folder)
                        NodeRef record = assoc.getParentRef();

                        // Apply the search aspect
                        applySearchAspect(record);

                        // Update disposition properties
                        updateDispositionActionProperties(record, nodeRef);
                    }

                    return null;

                }}, AuthenticationUtil.getSystemUserName());
        }
    }

    /**
     * Helper method to apply the search aspect
     *
     * @param nodeRef   node reference
     */
    private void applySearchAspect(NodeRef nodeRef)
    {
        onAddSearchAspect.disable();
        try
        {
            if (!nodeService.hasAspect(nodeRef, ASPECT_RM_SEARCH))
            {
                nodeService.addAspect(nodeRef, ASPECT_RM_SEARCH , null);

                if (logger.isDebugEnabled())
                {
                    logger.debug("Added search aspect to node: " + nodeRef);
                }
            }
        }
        finally
        {
            onAddSearchAspect.enable();
        }
    }

    /**
     * On add record aspect behaviour implementation
     *
     * @param nodeRef           node reference
     * @param aspectTypeQName   aspect type qname
     */
    public void onAddRecordAspect(final NodeRef nodeRef, final QName aspectTypeQName)
    {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                if (nodeService.exists(nodeRef) && nodeService.hasAspect(nodeRef, ASPECT_RECORD))
                {
                    applySearchAspect(nodeRef);
                    setupDispositionScheduleProperties(nodeRef);
                }

                return null;
            }
        });
    }
    
    /**
     * On addition of the disposition lifecycle aspect
     * @param nodeRef
     * @param aspectTypeQName
     */
    public void onAddDispositionLifecycleAspect(final NodeRef nodeRef, final QName aspectTypeQName)
    {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                if (nodeService.exists(nodeRef) && nodeService.hasAspect(nodeRef, ASPECT_RECORD))
                {
                    applySearchAspect(nodeRef);
                    setupDispositionScheduleProperties(nodeRef);
                }

                return null;
            }
        });
    }
    
    /**
     * On create record folder behaviour implmentation
     *
     * @param childAssocRef child association reference
     */
    public void recordFolderCreate(final ChildAssociationRef childAssocRef)
    {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                NodeRef nodeRef = childAssocRef.getChildRef();
                if (nodeService.exists(nodeRef))
                {
                    applySearchAspect(nodeRef);
                    setupDispositionScheduleProperties(nodeRef);
                }

                return null;
            }
        });
    }

    /**
     * On update type to or from record folder behaviour implementation
     * @param nodeRef the updated node
     * @param oldType the type the node had before update
     * @param newType the type the node has after update
     */
    public void convertedToOrFromRecordFolder(final NodeRef nodeRef, final QName oldType, final QName newType)
    {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                // If the node has been updated to a record folder
                if (newType.equals(TYPE_RECORD_FOLDER) && nodeService.exists(nodeRef))
                {
                    applySearchAspect(nodeRef);
                    setupDispositionScheduleProperties(nodeRef);
                }

                return null;
            }
        });
    }
    /**
     * Helper method to setup the disposition schedule properties
     *
     * @param recordOrFolder    node reference of record or record folder
     */
    private void setupDispositionScheduleProperties(NodeRef recordOrFolder)
    {
        if (!methodCached("setupDispositionScheduleProperties", recordOrFolder))
        {        
            DispositionSchedule ds = dispositionService.getDispositionSchedule(recordOrFolder);
            if (ds == null)
            {
                nodeService.setProperty(recordOrFolder, PROP_RS_HAS_DISPOITION_SCHEDULE, false);
            }
            else
            {
                nodeService.setProperty(recordOrFolder, PROP_RS_HAS_DISPOITION_SCHEDULE, true);
                setDispositionScheduleProperties(recordOrFolder, ds);
            }
    
            if (logger.isDebugEnabled())
            {
                logger.debug("Set rma:recordSearchHasDispositionSchedule for node " + recordOrFolder +
                            " to: " + (ds != null));
            }
        }
    }

    /**
     * On disposition action create behaviour implementation
     *
     * @param childAssocRef child association reference
     */
    public void dispositionActionCreate(final ChildAssociationRef childAssocRef)
    {
    	AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
    	{
			public Void doWork() throws Exception 
			{
				NodeRef child = childAssocRef.getChildRef();
		        if (nodeService.exists(child) &&
		            childAssocRef.getTypeQName().equals(ASSOC_NEXT_DISPOSITION_ACTION))
		        {
		            // Get the record (or record folder)
		            NodeRef record = childAssocRef.getParentRef();

		            // Apply the search aspect
		            applySearchAspect(record);

		            // Update disposition properties
		            updateDispositionActionProperties(record, childAssocRef.getChildRef());

		            // Clear the events
		            nodeService.setProperty(record, PROP_RS_DISPOSITION_EVENTS, null);
		        }
		        
		        return null;
			}
    	});        
    }
    
    /**
     * Helper method to determine whether a method has been called in this transaction 
     * already, or not.
     * <P>
     * Prevents work if we get unexpected behaviours firing.
     * 
     * @param method    method name (can be any unique string)
     * @return boolean  true if already called in this transaction, false otherwise
     */
    private boolean methodCached(String method, NodeRef nodeRef)
    {
        boolean result = true;
        Set<String> methods = TransactionalResourceHelper.getSet("rm.seachrollup.methodCache");
        String key = method + "|" + nodeRef;
        if (!methods.contains(key))
        {
            result = false;
            methods.add(key);
        }
        return result;
    }

    /**
     * On update disposition action properties behaviour implementation
     *
     * @param record            record node reference
     * @param dispositionAction disposition action
     */
    private void updateDispositionActionProperties(NodeRef record, NodeRef dispositionAction)
    {
        Map<QName, Serializable> props = nodeService.getProperties(record);

        DispositionAction da = new DispositionActionImpl(recordsManagementServiceRegistry, dispositionAction);

        props.put(PROP_RS_DISPOSITION_ACTION_NAME, da.getName());
        props.put(PROP_RS_DISPOSITION_ACTION_AS_OF, da.getAsOfDate());
        props.put(PROP_RS_DISPOSITION_EVENTS_ELIGIBLE, nodeService.getProperty(dispositionAction, PROP_DISPOSITION_EVENTS_ELIGIBLE));

        DispositionActionDefinition daDefinition = da.getDispositionActionDefinition();
        if (daDefinition != null)
        {
            Period period = daDefinition.getPeriod();
            if (period != null)
            {
                props.put(PROP_RS_DISPOSITION_PERIOD, period.getPeriodType());
                props.put(PROP_RS_DISPOSITION_PERIOD_EXPRESSION, period.getExpression());
            }
            else
            {
                props.put(PROP_RS_DISPOSITION_PERIOD, null);
                props.put(PROP_RS_DISPOSITION_PERIOD_EXPRESSION, null);
            }
        }

        try
        {
            //disable on properties update policy for the frozen aspect
            frozenAspect.disableOnPropUpdateFrozenAspect();
            nodeService.setProperties(record, props);
        }
        finally
        {
            frozenAspect.enableOnPropUpdateFrozenAspect();
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Set rma:recordSearchDispositionActionName for node " + record + " to: " +
                        props.get(PROP_RS_DISPOSITION_ACTION_NAME));
            logger.debug("Set rma:recordSearchDispositionActionAsOf for node " + record + " to: " +
                        props.get(PROP_RS_DISPOSITION_ACTION_AS_OF));
            logger.debug("Set rma:recordSearchDispositionEventsEligible for node " + record + " to: " +
                        props.get(PROP_RS_DISPOSITION_EVENTS_ELIGIBLE));
            logger.debug("Set rma:recordSearchDispositionPeriod for node " + record + " to: " +
                        props.get(PROP_RS_DISPOSITION_PERIOD));
            logger.debug("Set rma:recordSearchDispositionPeriodExpression for node " + record + " to: " +
                        props.get(PROP_RS_DISPOSITION_PERIOD_EXPRESSION));
        }
    }

    /**
     * On update of event execution information behaviour\
     *
     * @param childAssocRef child association reference
     * @param isNewNode     true if a new node, false otherwise
     */
    @SuppressWarnings("unchecked")
    public void eventExecutionUpdate(ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        NodeRef dispositionAction = childAssocRef.getParentRef();
        NodeRef eventExecution = childAssocRef.getChildRef();

        if (nodeService.exists(dispositionAction) &&
            nodeService.exists(eventExecution))
        {
            ChildAssociationRef assoc = nodeService.getPrimaryParent(dispositionAction);
            if (assoc.getTypeQName().equals(ASSOC_NEXT_DISPOSITION_ACTION))
            {
                // Get the record (or record folder)
                NodeRef record = assoc.getParentRef();

                // Apply the search aspect
                applySearchAspect(record);

                Collection<String> events = (Collection<String>)nodeService.getProperty(record, PROP_RS_DISPOSITION_EVENTS);
                if (events == null)
                {
                    events = new ArrayList<>(1);
                }
                events.add((String)nodeService.getProperty(eventExecution, PROP_EVENT_EXECUTION_NAME));
                nodeService.setProperty(record, PROP_RS_DISPOSITION_EVENTS, (Serializable)events);
            }
        }
    }

    /**
     * On event execution delete behaviour implementation.
     *
     * @param childAssocRef     child association reference
     * @param isNodeArchived    true if node is archived on delete, false otherwise
     */
    public void eventExecutionDelete(ChildAssociationRef childAssocRef, boolean isNodeArchived)
    {
        NodeRef dispositionActionNode = childAssocRef.getParentRef();

        if (nodeService.exists(dispositionActionNode))
        {
            ChildAssociationRef assoc = nodeService.getPrimaryParent(dispositionActionNode);
            if (assoc.getTypeQName().equals(ASSOC_NEXT_DISPOSITION_ACTION))
            {
                // Get the record (or record folder)
                NodeRef record = assoc.getParentRef();

                // Apply the search aspect
                applySearchAspect(record);

                // make sure the list of events match the action definition
                setupDispositionActionEvents(record, dispositionService.getNextDispositionAction(record));
            }
        }
    }

    /**
     * Helper method to setup disposition action events.
     *
     * @param nodeRef   node reference
     * @param da        disposition action
     */
    private void setupDispositionActionEvents(NodeRef nodeRef, DispositionAction da)
    {
        if (!methodCached("setupDispositionActionEvents", nodeRef))
        {
            if (da != null)
            {
                List<String> eventNames = null;
                List<EventCompletionDetails> eventsList = da.getEventCompletionDetails();
                if (eventsList.size() > 0)
                {
                    eventNames = new ArrayList<>(eventsList.size());
                    for (EventCompletionDetails event : eventsList)
                    {
                        eventNames.add(event.getEventName());
                    }
                }
    
                // set the property
                nodeService.setProperty(nodeRef, PROP_RS_DISPOSITION_EVENTS, (Serializable)eventNames);
    
                if (logger.isDebugEnabled())
                {
                    logger.debug("Set rma:recordSearchDispositionEvents for node " + nodeRef + " to: " + eventNames);
                }
            }
        }
    }

    /**
     * On add search aspect behaviour implementation.
     *
     * @param nodeRef           node reference
     * @param aspectTypeQName   aspect type qname
     */
    public void rmSearchAspectAdd(final NodeRef nodeRef, final QName aspectTypeQName)
    {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                if (nodeService.exists(nodeRef))
                {
                    // Initialise the search parameteres as required
                    setVitalRecordDefintionDetails(nodeRef);
                }

                return null;
            }
        });
    }

    /**
     * On add aspect vital record defintion behaviour implementation.
     *
     * @param nodeRef           node reference
     * @param aspectTypeQName   aspect tyep qname
     */
    public void vitalRecordDefintionAddAspect(final NodeRef nodeRef, final QName aspectTypeQName)
    {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                // Only care about record folders
                if (nodeService.exists(nodeRef) && recordFolderService.isRecordFolder(nodeRef))
                {
                    updateVitalRecordDefinitionValues(nodeRef);
                }

                return null;
            }
        });
    }

    /**
     * On update vital record definition properties behaviour implementation.
     *
     * @param nodeRef   node reference
     * @param before    before properties
     * @param after     after properties
     */
    public void vitalRecordDefintionUpdateProperties(final NodeRef nodeRef, final Map<QName, Serializable> before, final Map<QName, Serializable> after)
    {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                // Only care about record folders
                if (nodeService.exists(nodeRef) && recordFolderService.isRecordFolder(nodeRef))
                {
                    Set<QName> props = new HashSet<>(1);
                    props.add(PROP_REVIEW_PERIOD);
                    Set<QName> changed = determineChangedProps(before, after);
                    changed.retainAll(props);
                    if (!changed.isEmpty())
                    {
                        updateVitalRecordDefinitionValues(nodeRef);
                    }
                }

                return null;
            }
        });
    }

    /**
     * Helper method to update the vital record defintion values
     *
     * @param nodeRef   node reference
     */
    private void updateVitalRecordDefinitionValues(NodeRef nodeRef)
    {
        if (!methodCached("updateVitalRecordDefinitionValues", nodeRef))
        {
            // ensure the folder itself reflects the correct details
            applySearchAspect(nodeRef);
            setVitalRecordDefintionDetails(nodeRef);
    
            List<NodeRef> records = recordService.getRecords(nodeRef);
            for (NodeRef record : records)
            {
                // Apply the search aspect
                applySearchAspect(record);
    
                // Set the vital record definition details
                setVitalRecordDefintionDetails(record);
            }
        }
    }

    /**
     * Helper method to set vital record definition details.
     *
     * @param nodeRef   node reference
     */
    private void setVitalRecordDefintionDetails(NodeRef nodeRef)
    {
        if (!methodCached("setVitalRecordDefinitionDetails", nodeRef))
        {
            VitalRecordDefinition vrd = vitalRecordService.getVitalRecordDefinition(nodeRef);
            try
            {
               frozenAspect.disableOnPropUpdateFrozenAspect();
                if (vrd != null && vrd.isEnabled() && vrd.getReviewPeriod() != null)
                {
                    // Set the property values
                    nodeService.setProperty(nodeRef, PROP_RS_VITAL_RECORD_REVIEW_PERIOD, vrd.getReviewPeriod().getPeriodType());
                    nodeService.setProperty(nodeRef, PROP_RS_VITAL_RECORD_REVIEW_PERIOD_EXPRESSION, vrd.getReviewPeriod().getExpression());

                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Set rma:recordSearchVitalRecordReviewPeriod for node " + nodeRef + " to: " +
                                vrd.getReviewPeriod().getPeriodType());
                        logger.debug("Set rma:recordSearchVitalRecordReviewPeriodExpression for node " + nodeRef + " to: " +
                                vrd.getReviewPeriod().getExpression());
                    }
                }
                else
                {
                    // Clear the vital record properties
                    nodeService.setProperty(nodeRef, PROP_RS_VITAL_RECORD_REVIEW_PERIOD, null);
                    nodeService.setProperty(nodeRef, PROP_RS_VITAL_RECORD_REVIEW_PERIOD_EXPRESSION, null);
                }
            }
            finally
            {
               frozenAspect.enableOnPropUpdateFrozenAspect();

            }
        }
    }

    /**
     * Updates the disposition schedule properties
     *
     * @param nodeRef   node reference
     * @param before    properties before
     * @param after     properties after
     */
    public void dispositionSchedulePropertiesUpdate(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        if (nodeService.exists(nodeRef))
        {
            // create the schedule object and get the record category for it
            DispositionSchedule schedule = new DispositionScheduleImpl(recordsManagementServiceRegistry, nodeService, nodeRef);
            NodeRef recordCategoryNode = nodeService.getPrimaryParent(schedule.getNodeRef()).getParentRef();

            if (schedule.isRecordLevelDisposition())
            {
                for (NodeRef recordFolder : getRecordFolders(recordCategoryNode))
                {
                    for (NodeRef record : recordService.getRecords(recordFolder))
                    {
                        applySearchAspect(record);
                        setDispositionScheduleProperties(record, schedule);
                    }
                }
            }
            else
            {
                for (NodeRef recordFolder : getRecordFolders(recordCategoryNode))
                {
                    applySearchAspect(recordFolder);
                    setDispositionScheduleProperties(recordFolder, schedule);
                }
            }
        }
    }

    /**
     * Helper method to set disposition schedule properties
     *
     * @param recordOrFolder    node reference
     * @param schedule          dispostion schedule
     */
    private void setDispositionScheduleProperties(NodeRef recordOrFolder, DispositionSchedule schedule)
    {
        if (schedule != null)
        {
            try
            {
                frozenAspect.disableOnPropUpdateFrozenAspect();
                nodeService.setProperty(recordOrFolder, PROP_RS_DISPOITION_AUTHORITY, schedule.getDispositionAuthority());
                nodeService.setProperty(recordOrFolder, PROP_RS_DISPOITION_INSTRUCTIONS, schedule.getDispositionInstructions());

                if (logger.isDebugEnabled())
                {
                    logger.debug("Set rma:recordSearchDispositionAuthority for node " + recordOrFolder + " to: " + schedule.getDispositionAuthority());
                    logger.debug("Set rma:recordSearchDispositionInstructions for node " + recordOrFolder + " to: " + schedule.getDispositionInstructions());
                }
            }
            finally
            {
                frozenAspect.enableOnPropUpdateFrozenAspect();
            }
        }
    }

    /**
     * This method compares the oldProps map against the newProps map and returns
     * a set of QNames of the properties that have changed. Changed here means one of
     * <ul>
     * <li>the property has been removed</li>
     * <li>the property has had its value changed</li>
     * <li>the property has been added</li>
     * </ul>
     */
    private Set<QName> determineChangedProps(Map<QName, Serializable> oldProps, Map<QName, Serializable> newProps)
    {
        Set<QName> result = new HashSet<>();
        for (Map.Entry<QName, Serializable> entry : oldProps.entrySet())
        {
            QName qn = entry.getKey();
            if (newProps.get(qn) == null || !newProps.get(qn).equals(entry.getValue()))
            {
                result.add(qn);
            }
        }
        for (QName qn : newProps.keySet())
        {
            if (oldProps.get(qn) == null)
            {
                result.add(qn);
            }
        }

        return result;
    }

    /**
     * Helper method to get the record folders contained in the provided record category.
     *
     * @param recordCategoryNode    record category node reference
     * @return  List<NodeRef>       contained record folders
     */
    private List<NodeRef> getRecordFolders(NodeRef recordCategoryNode)
    {
        List<NodeRef> results = new ArrayList<>(8);

        List<ChildAssociationRef> folderAssocs = nodeService.getChildAssocs(recordCategoryNode,
                    ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef folderAssoc : folderAssocs)
        {
            NodeRef folder = folderAssoc.getChildRef();
            if (recordFolderService.isRecordFolder(folder))
            {
                results.add(folder);
            }
        }

        return results;
    }

    /**
     * Record and record folder move behavior
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy#onMoveNode(org.alfresco.service.cmr.repository.ChildAssociationRef, org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    @Behaviour
            (
                    kind = BehaviourKind.CLASS,
                    notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
            )
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        // check the parent has actually changed
        if (!oldChildAssocRef.getParentRef().equals(newChildAssocRef.getParentRef()))
        {
            final NodeRef recordOrFolder = newChildAssocRef.getChildRef();
            final boolean isRecordOrFolder = recordService.isRecord(recordOrFolder) || recordFolderService.isRecordFolder(recordOrFolder);
            AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    if (nodeService.exists(recordOrFolder) && isRecordOrFolder)
                    {
                        applySearchAspect(recordOrFolder);
                    }
                    return null;
                }
            });
        }
    }
}
