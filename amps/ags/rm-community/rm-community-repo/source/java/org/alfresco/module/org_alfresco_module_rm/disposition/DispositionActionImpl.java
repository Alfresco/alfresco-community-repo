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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.module.org_alfresco_module_rm.event.EventCompletionDetails;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.script.slingshot.RMSearchGet;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Disposition action implementation.
 *
 * @author Roy Wetherall
 * @since 1.0
 */
public class DispositionActionImpl implements DispositionAction,
                                              RecordsManagementModel
{
    /** logger */
    private static Log logger = LogFactory.getLog(DispositionActionImpl.class);

    /** records management service registry */
    private RecordsManagementServiceRegistry services;

    /** disposition node reference */
    private NodeRef dispositionNodeRef;

    /** disposition action definition */
    private DispositionActionDefinition dispositionActionDefinition;

    /**
     * Constructor
     *
     * @param services                  records management service registry
     * @param dispositionActionNodeRef  disposition action node reference
     */
    public DispositionActionImpl(RecordsManagementServiceRegistry services, NodeRef dispositionActionNodeRef)
    {
        this.services = services;
        this.dispositionNodeRef = dispositionActionNodeRef;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getDispositionActionDefinition()
     */
    public DispositionActionDefinition getDispositionActionDefinition()
    {
        if (dispositionActionDefinition == null)
        {
            // Get the current action
            String id = (String)services.getNodeService().getProperty(this.dispositionNodeRef, PROP_DISPOSITION_ACTION_ID);

            // Get the disposition instructions for the owning node
            NodeRef recordNodeRef = services.getNodeService().getPrimaryParent(this.dispositionNodeRef).getParentRef();
            if (recordNodeRef != null)
            {
                DispositionSchedule ds = services.getDispositionService().getDispositionSchedule(recordNodeRef);

                if (ds != null)
                {
                    // Get the disposition action definition
                    dispositionActionDefinition = ds.getDispositionActionDefinition(id);
                }
            }
        }

        return dispositionActionDefinition;

    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getNodeRef()
     */
    public NodeRef getNodeRef()
    {
       return this.dispositionNodeRef;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getLabel()
     */
    public String getLabel()
    {
        String name = getName();
        String label = name;

        // get the disposition action from the RM action service
        RecordsManagementAction action = services.getRecordsManagementActionService().getDispositionAction(name);
        if (action != null)
        {
            label = action.getLabel();
        }

        return label;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getId()
     */
    public String getId()
    {
        return (String)services.getNodeService().getProperty(this.dispositionNodeRef, PROP_DISPOSITION_ACTION_ID);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getName()
     */
    public String getName()
    {
        return (String)services.getNodeService().getProperty(this.dispositionNodeRef, PROP_DISPOSITION_ACTION);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getAsOfDate()
     */
    public Date getAsOfDate()
    {
        return (Date)services.getNodeService().getProperty(this.dispositionNodeRef, PROP_DISPOSITION_AS_OF);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#isEventsEligible()
     */
    public boolean isEventsEligible()
    {
        return ((Boolean)services.getNodeService().getProperty(this.dispositionNodeRef, PROP_DISPOSITION_EVENTS_ELIGIBLE)).booleanValue();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getCompletedAt()
     */
    public Date getCompletedAt()
    {
        return (Date)services.getNodeService().getProperty(this.dispositionNodeRef, PROP_DISPOSITION_ACTION_COMPLETED_AT);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getCompletedBy()
     */
    public String getCompletedBy()
    {
        return (String)services.getNodeService().getProperty(this.dispositionNodeRef, PROP_DISPOSITION_ACTION_COMPLETED_BY);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getStartedAt()
     */
    public Date getStartedAt()
    {
        return (Date)services.getNodeService().getProperty(this.dispositionNodeRef, PROP_DISPOSITION_ACTION_STARTED_AT);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getStartedBy()
     */
    public String getStartedBy()
    {
        return (String)services.getNodeService().getProperty(this.dispositionNodeRef, PROP_DISPOSITION_ACTION_STARTED_BY);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getEventCompletionDetails()
     */
    public List<EventCompletionDetails> getEventCompletionDetails()
    {
        List<ChildAssociationRef> assocs = services.getNodeService().getChildAssocs(
                                                        this.dispositionNodeRef,
                                                        ASSOC_EVENT_EXECUTIONS,
                                                        RegexQNamePattern.MATCH_ALL);
        List<EventCompletionDetails> result = new ArrayList<>(assocs.size());
        for (ChildAssociationRef assoc : assocs)
        {
            result.add(getEventCompletionDetailsFromNodeRef(assoc.getChildRef()));
        }

        return result;
    }

    /**
     * Helper method to create object representation of event completed details from
     * node reference.
     *
     * @param nodeRef                           node reference
     * @return {@link EventCompletionDetails}   event completion details
     */
    private EventCompletionDetails getEventCompletionDetailsFromNodeRef(NodeRef nodeRef)
    {
        // get the properties
        Map<QName, Serializable> props = this.services.getNodeService().getProperties(nodeRef);

        // get the event name
        String eventName = (String)props.get(PROP_EVENT_EXECUTION_NAME);

        // create event completion details
        return new EventCompletionDetails(
                nodeRef,
                eventName,
                services.getRecordsManagementEventService().getEvent(eventName).getDisplayLabel(),
                getBooleanValue(props.get(PROP_EVENT_EXECUTION_AUTOMATIC), false),
                getBooleanValue(props.get(PROP_EVENT_EXECUTION_COMPLETE), false),
                (Date) props.get(PROP_EVENT_EXECUTION_COMPLETED_AT),
                (String) props.get(PROP_EVENT_EXECUTION_COMPLETED_BY));
    }

    /**
     * Helper method to deal with boolean values
     *
     * @param value
     * @param defaultValue
     * @return
     */
    private boolean getBooleanValue(Object value, boolean defaultValue)
    {
        boolean result = defaultValue;
        if (value instanceof Boolean)
        {
            result = ((Boolean)value).booleanValue();
        }
        return result;
    }

    /**
     * Gets the event completion details for the named event.
     * <p>
     * Returns null if event can not be found.
     *
     * @param  eventName   name of the event
     * @return {@link EventCompletionDetails}   event completion details for named event, null otherwise
     *
     * @since 2.2
     */
    @Override
    public EventCompletionDetails getEventCompletionDetails(String eventName)
    {
        EventCompletionDetails result = null;
        List<ChildAssociationRef> assocs = services.getNodeService().getChildAssocsByPropertyValue(dispositionNodeRef, PROP_EVENT_EXECUTION_NAME, eventName);

        if (!assocs.isEmpty())
        {
            if (assocs.size() != 1)
            {
                throw new AlfrescoRuntimeException("Unable to get event completion details, because more than one child was found for event " + eventName);
            }

            result = getEventCompletionDetailsFromNodeRef(assocs.get(0).getChildRef());
        }

        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#completeEvent(java.lang.String, java.util.Date, java.lang.String)
     */
    @Override
    public void completeEvent(final String eventName, final Date completedAt, final String completedBy)
    {
        final EventCompletionDetails event = getEventCompletionDetails(eventName);
        if (event != null && !event.isEventComplete())
        {
            AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    // use "now" if no completed date set
                    Date completedAtValue = completedAt;
                    if (completedAt == null)
                    {
                        completedAtValue = new Date();
                    }

                    // use the currently authenticated user if none set
                    String completedByValue = completedBy;
                    if (completedBy == null)
                    {
                        completedByValue = AuthenticationUtil.getFullyAuthenticatedUser();
                    }

                    // Update the event so that it is complete
                    NodeRef eventNodeRef = event.getNodeRef();
                    Map<QName, Serializable> props = services.getNodeService().getProperties(eventNodeRef);
                    props.put(PROP_EVENT_EXECUTION_COMPLETE, true);
                    props.put(PROP_EVENT_EXECUTION_COMPLETED_AT, completedAtValue);
                    props.put(PROP_EVENT_EXECUTION_COMPLETED_BY, completedByValue);
                    services.getNodeService().setProperties(eventNodeRef, props);

                    // check a specific event from rmEventConfigBootstrap.json
                    if (eventName.equals("declassification_review"))
                    {
                        setDeclassificationReview(eventNodeRef, completedAtValue, completedByValue);
                    }

                    // Check to see if the events eligible property needs to be updated
                    updateEventEligible();

                    return null;
                }
            });
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#undoEvent(java.lang.String)
     */
    @Override
    public void undoEvent(final String eventName)
    {
        final EventCompletionDetails event = getEventCompletionDetails(eventName);
        if (event != null && event.isEventComplete())
        {
            AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    // Update the event so that it is undone
                    NodeRef eventNodeRef = event.getNodeRef();
                    Map<QName, Serializable> props = services.getNodeService().getProperties(eventNodeRef);
                    props.put(PROP_EVENT_EXECUTION_COMPLETE, false);
                    props.put(PROP_EVENT_EXECUTION_COMPLETED_AT, null);
                    props.put(PROP_EVENT_EXECUTION_COMPLETED_BY, null);
                    services.getNodeService().setProperties(eventNodeRef, props);

                    // Check to see if the events eligible property needs to be updated
                    updateEventEligible();

                    return null;
                }
            });
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#refreshEvents()
     */
    @SuppressWarnings("unchecked")
    @Override
    public void refreshEvents()
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                // go through the current events on the next action and remove any that are not present any more
                List<String> stepEvents = (List<String>) services.getNodeService().getProperty(getDispositionActionDefinition().getNodeRef(), PROP_DISPOSITION_EVENT);

                List<EventCompletionDetails> eventsList = getEventCompletionDetails();
                List<String> nextActionEvents = new ArrayList<>(eventsList.size());

                for (EventCompletionDetails event : eventsList)
                {
                    // take note of the event names present on the next action
                    String eventName = event.getEventName();
                    nextActionEvents.add(eventName);

                    // if the event has been removed delete from next action
                    if (stepEvents != null && !stepEvents.contains(event.getEventName()))
                    {
                        // remove the child association representing the event
                        services.getNodeService().removeChild(getNodeRef(), event.getNodeRef());

                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Removed '" + eventName + "' from next action '" + getName() +
                                         "' (" + getNodeRef() + ")");
                        }
                    }
                }

                // go through the disposition action definition step events and add any new ones
                if (stepEvents != null)
                {
                    for (String eventName : stepEvents)
                    {
                        if (!nextActionEvents.contains(eventName))
                        {
                            // add the details of the new event
                            addEventCompletionDetails(services.getRecordsManagementEventService().getEvent(eventName));

                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Added '" + eventName + "' to next action '" + getName() +
                                             "' (" + getNodeRef() + ")");
                            }
                        }
                    }
                }

                // NOTE: eventsList contains all the events that have been updated!
                // TODO: manually update the search properties for the parent node!

                // finally since events may have changed re-calculate the events eligible flag
                boolean eligible = updateEventEligible();

                if (logger.isDebugEnabled())
                {
                    logger.debug("Set events eligible flag to '" + eligible + "' for next action '" + getName() +
                                 "' (" + getNodeRef() + ")");
                }

                return null;
            }
        });
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#addEventCompletionDetails(org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent)
     */
    @Override
    public void addEventCompletionDetails(RecordsManagementEvent event)
    {
        Map<QName, Serializable> eventProps = new HashMap<>(7);
        eventProps.put(PROP_EVENT_EXECUTION_NAME, event.getName());
        // TODO display label
        eventProps.put(PROP_EVENT_EXECUTION_AUTOMATIC, event.getRecordsManagementEventType().isAutomaticEvent());
        eventProps.put(PROP_EVENT_EXECUTION_COMPLETE, false);

        // Create the event execution object
        services.getNodeService().createNode(getNodeRef(),
                                             ASSOC_EVENT_EXECUTIONS,
                                             ASSOC_EVENT_EXECUTIONS,
                                             TYPE_EVENT_EXECUTION,
                                             eventProps);
    }


    /**
     * Calculates and updates the <code>rma:dispositionEventsEligible</code>
     * property for the given next disposition action.
     *
     * @param nextAction The next disposition action
     * @return The result of calculation
     *
     * @since 2.2
     */
    private boolean updateEventEligible()
    {
        boolean eligible = false;

        // get the events for the next disposition action
        List<EventCompletionDetails> events = getEventCompletionDetails();

        if (!events.isEmpty())
        {
            // get the disposition action definition
            DispositionActionDefinition dispositionActionDefinition = getDispositionActionDefinition();
            if (dispositionActionDefinition != null)
            {            
                if (!dispositionActionDefinition.eligibleOnFirstCompleteEvent())
                {
                    // if one event is complete then the disposition action is eligible
                    eligible = true;
                    for (EventCompletionDetails event : events)
                    {
                        if (!event.isEventComplete())
                        {
                            eligible = false;
                            break;
                        }
                    }
                }
                else
                {
                    // all events must be complete for the disposition action to be eligible
                    for (EventCompletionDetails event : events)
                    {
                        if (event.isEventComplete())
                        {
                            eligible = true;
                            break;
                        }
                    }
                }
            }
        }

        // Update the property with the eligible value
        services.getNodeService().setProperty(getNodeRef(), PROP_DISPOSITION_EVENTS_ELIGIBLE, eligible);

        return eligible;
    }

    /**
     * Sets declassification review authority and date on records and record folder
     *
     * @param eventNodeRef Declassification review event node ref
     * @param completedAtValue Declassification review authority
     * @param completedByValue Declassification review date
     */
    private void setDeclassificationReview(NodeRef eventNodeRef, Date completedAtValue, String completedByValue)
    {
        NodeRef nextDispositionActionNodeRef = services.getNodeService().getPrimaryParent(eventNodeRef).getParentRef();
        NodeRef nodeRef = services.getNodeService().getPrimaryParent(nextDispositionActionNodeRef).getParentRef();
        setPropsOnContent(nodeRef, completedAtValue, completedByValue);

        // check if the node is a record folder then set the declassification review on the records also
        if (services.getNodeService().getType(nodeRef).equals(RecordsManagementModel.TYPE_RECORD_FOLDER))
        {
            // get all the records inside the record folder
            List<ChildAssociationRef> records = services.getNodeService().getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef child : records)
            {
                NodeRef recordNodeRef = child.getChildRef();
                setPropsOnContent(recordNodeRef, completedAtValue, completedByValue);
            }
        }
    }

    private void setPropsOnContent(NodeRef nodeRef, Date completedAtValue, String completedByValue)
    {
        Map<QName, Serializable> nodeProps = services.getNodeService().getProperties(nodeRef);
        nodeProps.put(PROP_RS_DECLASSIFICATION_REVIEW_COMPLETED_AT, completedAtValue);
        nodeProps.put(PROP_RS_DECLASSIFICATION_REVIEW_COMPLETED_BY, completedByValue);
        services.getNodeService().setProperties(nodeRef, nodeProps);
    }
}
