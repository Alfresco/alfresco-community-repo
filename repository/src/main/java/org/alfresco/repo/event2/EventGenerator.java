/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.event2;

import java.io.Serializable;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.Transaction;
import org.alfresco.repo.event.v1.model.EventType;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event2.filter.ChildAssociationTypeFilter;
import org.alfresco.repo.event2.filter.EventFilterRegistry;
import org.alfresco.repo.event2.filter.EventUserFilter;
import org.alfresco.repo.event2.filter.NodeTypeFilter;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnRemoveAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnSetNodeTypePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Generates events and sends them to an event topic.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class EventGenerator extends AbstractLifecycleBean implements InitializingBean, EventSupportedPolicies,
                                                                     ChildAssociationEventSupportedPolicies,
                                                                     PeerAssociationEventSupportedPolicies
{
    private static final Log LOGGER = LogFactory.getLog(EventGenerator.class);

    protected PolicyComponent policyComponent;
    protected NodeService nodeService;
    private NamespaceService namespaceService;
    protected DictionaryService dictionaryService;
    private DescriptorService descriptorService;
    private EventFilterRegistry eventFilterRegistry;
    private TransactionService transactionService;
    private PersonService personService;
    protected NodeResourceHelper nodeResourceHelper;
    protected NodeDAO nodeDAO;

    private EventGeneratorQueue eventGeneratorQueue;
    private NodeTypeFilter nodeTypeFilter;
    private ChildAssociationTypeFilter childAssociationTypeFilter;
    private EventUserFilter userFilter;
    protected final EventTransactionListener transactionListener = new EventTransactionListener();

    @Override
    public void afterPropertiesSet()
    {
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "descriptorService", descriptorService);
        PropertyCheck.mandatory(this, "eventFilterRegistry", eventFilterRegistry);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "personService", personService);
        PropertyCheck.mandatory(this, "nodeResourceHelper", nodeResourceHelper);
        PropertyCheck.mandatory(this, "nodeDAO", nodeDAO);
        PropertyCheck.mandatory(this, "eventGeneratorQueue", eventGeneratorQueue);

        this.nodeTypeFilter = eventFilterRegistry.getNodeTypeFilter();
        this.childAssociationTypeFilter = eventFilterRegistry.getChildAssociationTypeFilter();
        this.userFilter = eventFilterRegistry.getEventUserFilter();
    }

    private void bindBehaviours()
    {
        policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, this,
                                           new JavaBehaviour(this, "onCreateNode"));
        policyComponent.bindClassBehaviour(BeforeDeleteNodePolicy.QNAME, this,
                                           new JavaBehaviour(this, "beforeDeleteNode"));
        policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, this,
                                           new JavaBehaviour(this, "onUpdateProperties"));
        policyComponent.bindClassBehaviour(OnSetNodeTypePolicy.QNAME, this,
                                           new JavaBehaviour(this, "onSetNodeType"));
        policyComponent.bindClassBehaviour(OnAddAspectPolicy.QNAME, this,
                                           new JavaBehaviour(this, "onAddAspect"));
        policyComponent.bindClassBehaviour(OnRemoveAspectPolicy.QNAME, this,
                                           new JavaBehaviour(this, "onRemoveAspect"));
        policyComponent.bindClassBehaviour(OnMoveNodePolicy.QNAME, this,
                                           new JavaBehaviour(this, "onMoveNode"));
        policyComponent.bindAssociationBehaviour(OnCreateChildAssociationPolicy.QNAME, this,
                                           new JavaBehaviour(this, "onCreateChildAssociation"));
        policyComponent.bindAssociationBehaviour(BeforeDeleteChildAssociationPolicy.QNAME, this,
                                           new JavaBehaviour(this, "beforeDeleteChildAssociation"));
        policyComponent.bindAssociationBehaviour(OnCreateAssociationPolicy.QNAME, this,
                                           new JavaBehaviour(this, "onCreateAssociation"));
        policyComponent.bindAssociationBehaviour(BeforeDeleteAssociationPolicy.QNAME, this,
                                           new JavaBehaviour(this, "beforeDeleteAssociation"));
    }

    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    // To make IntelliJ stop complaining about unused method!
    @SuppressWarnings("unused")
    public void setEventFilterRegistry(EventFilterRegistry eventFilterRegistry)
    {
        this.eventFilterRegistry = eventFilterRegistry;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    public void setNodeResourceHelper(NodeResourceHelper nodeResourceHelper)
    {
        this.nodeResourceHelper = nodeResourceHelper;
    }

    public void setEventGeneratorQueue(EventGeneratorQueue eventGeneratorQueue)
    {
        this.eventGeneratorQueue = eventGeneratorQueue;
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        getEventConsolidator(childAssocRef.getChildRef()).onCreateNode(childAssocRef);
    }

    @Override
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        getEventConsolidator(newChildAssocRef.getChildRef()).onMoveNode(oldChildAssocRef, newChildAssocRef);
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        getEventConsolidator(nodeRef).onUpdateProperties(nodeRef, before, after);
    }

    @Override
    public void onSetNodeType(NodeRef nodeRef, QName before, QName after)
    {
        getEventConsolidator(nodeRef).onSetNodeType(nodeRef, before, after);
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        getEventConsolidator(nodeRef).beforeDeleteNode(nodeRef);
    }

    @Override
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        getEventConsolidator(nodeRef).onAddAspect(nodeRef, aspectTypeQName);
    }

    @Override
    public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        getEventConsolidator(nodeRef).onRemoveAspect(nodeRef, aspectTypeQName);
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssociationRef, boolean isNewNode)
    {
        getEventConsolidator(childAssociationRef).onCreateChildAssociation(childAssociationRef, isNewNode);
    }

    @Override
    public void beforeDeleteChildAssociation(ChildAssociationRef childAssociationRef)
    {
        getEventConsolidator(childAssociationRef).beforeDeleteChildAssociation(childAssociationRef);
    }

    @Override
    public void onCreateAssociation(AssociationRef associationRef)
    {
        getEventConsolidator(associationRef).onCreateAssociation(associationRef);
    }

    @Override
    public void beforeDeleteAssociation(AssociationRef associationRef)
    {
        getEventConsolidator(associationRef).beforeDeleteAssociation(associationRef);
    }

    protected EventConsolidator createEventConsolidator()
    {
        return new EventConsolidator(nodeResourceHelper);
    }

    protected ChildAssociationEventConsolidator createChildAssociationEventConsolidator(
                ChildAssociationRef childAssociationRef)
    {
        return new ChildAssociationEventConsolidator(childAssociationRef, nodeResourceHelper);
    }

    protected PeerAssociationEventConsolidator createPeerAssociationEventConsolidator(AssociationRef peerAssociationRef)
    {
        return new PeerAssociationEventConsolidator(peerAssociationRef, nodeResourceHelper);
    }

    /**
     * @return the {@link EventConsolidator} for the supplied {@code nodeRef} from
     * the current transaction context.
     */
    protected EventConsolidator getEventConsolidator(NodeRef nodeRef)
    {
        Consolidators consolidators = getTxnConsolidators(transactionListener);
        Map<NodeRef, EventConsolidator> nodeEvents = consolidators.getNodes();
        if (nodeEvents.isEmpty())
        {
            AlfrescoTransactionSupport.bindListener(transactionListener);
        }

        EventConsolidator eventConsolidator = nodeEvents.get(nodeRef);
        if (eventConsolidator == null)
        {
            eventConsolidator = createEventConsolidator();
            nodeEvents.put(nodeRef, eventConsolidator);
        }
        return eventConsolidator;
    }


    protected Consolidators getTxnConsolidators(Object resourceKey)
    {
        Consolidators consolidators = AlfrescoTransactionSupport.getResource(resourceKey);
        if (consolidators == null)
        {
            consolidators = new Consolidators();
            AlfrescoTransactionSupport.bindResource(resourceKey, consolidators);
        }
        return consolidators;
    }

    /**
     * @return the {@link EventConsolidator} for the supplied {@code childAssociationRef} from
     * the current transaction context.
     */
    private ChildAssociationEventConsolidator getEventConsolidator(ChildAssociationRef childAssociationRef)
    {
        Consolidators consolidators = getTxnConsolidators(transactionListener);
        Map<ChildAssociationRef, ChildAssociationEventConsolidator> assocEvents = consolidators.getChildAssocs();
        if (assocEvents.isEmpty())
        {
            AlfrescoTransactionSupport.bindListener(transactionListener);
        }

        ChildAssociationEventConsolidator eventConsolidator = assocEvents.get(childAssociationRef);
        if (eventConsolidator == null)
        {
            eventConsolidator = createChildAssociationEventConsolidator(childAssociationRef);
            assocEvents.put(childAssociationRef, eventConsolidator);
        }
        return eventConsolidator;
    }

    /**
     * @return the {@link EventConsolidator} for the supplied {@code peerAssociationRef} from
     * the current transaction context.
     */
    private PeerAssociationEventConsolidator getEventConsolidator(AssociationRef peerAssociationRef)
    {
        Consolidators consolidators = getTxnConsolidators(transactionListener);
        Map<AssociationRef, PeerAssociationEventConsolidator> assocEvents = consolidators.getPeerAssocs();
        if (assocEvents.isEmpty())
        {
            AlfrescoTransactionSupport.bindListener(transactionListener);
        }

        PeerAssociationEventConsolidator eventConsolidator = assocEvents.get(peerAssociationRef);
        if (eventConsolidator == null)
        {
            eventConsolidator = createPeerAssociationEventConsolidator(peerAssociationRef);
            assocEvents.put(peerAssociationRef, eventConsolidator);
        }
        return eventConsolidator;
    }

    private boolean isFiltered(QName nodeType, String user)
    {
        return (nodeTypeFilter.isExcluded(nodeType) || (userFilter.isExcluded(user)));
    }

    private boolean isFilteredChildAssociation(QName childAssocType, String user)
    {
        return (childAssociationTypeFilter.isExcluded(childAssocType) || (userFilter.isExcluded(user)));
    }

    protected EventInfo getEventInfo(String user)
    {
        return new EventInfo().setTimestamp(getCurrentTransactionTimestamp())
                              .setId(UUID.randomUUID().toString())
                              .setTxnId(AlfrescoTransactionSupport.getTransactionId())
                              .setPrincipal(user)
                              .setSource(URI.create("/" + descriptorService.getCurrentRepositoryDescriptor().getId()));
    }

    private ZonedDateTime getCurrentTransactionTimestamp()
    {
        Long currentTransactionCommitTime = nodeDAO.getCurrentTransactionCommitTime();
        ZonedDateTime timestamp;
        if(currentTransactionCommitTime != null)
        {
            Instant commitTimeMs = Instant.ofEpochMilli(currentTransactionCommitTime);
            timestamp = ZonedDateTime.ofInstant(commitTimeMs, ZoneOffset.UTC);
        }
        else 
        {
            // Sometimes the transaction entry is not created because no nodes were added to the transaction, but we are producing events anywhere.
            if(log.isDebugEnabled()){
                log.debug("Unable to retrieve the commit time for transaction " + AlfrescoTransactionSupport.getTransactionId() + " the current timestamp will be used as event timestamp.");
            }
                
            timestamp = ZonedDateTime.now();
        }
        
        return timestamp;
    }

    @Override
    protected void onBootstrap(ApplicationEvent applicationEvent)
    {
        bindBehaviours();
    }

    @Override
    protected void onShutdown(ApplicationEvent applicationEvent)
    {
        //NOOP
    }

    protected class EventTransactionListener extends TransactionListenerAdapter
    {
        @Override
        public void afterCommit()
        {
            try
            {
                final Consolidators consolidators = getTxnConsolidators(this);

                // Node events
                for (Map.Entry<NodeRef, EventConsolidator> entry : consolidators.getNodes().entrySet())
                {
                    EventConsolidator eventConsolidator = entry.getValue();
                    sendEvent(entry.getKey(), eventConsolidator);
                }

                // Child assoc events
                for (Map.Entry<ChildAssociationRef, ChildAssociationEventConsolidator> entry : consolidators.getChildAssocs().entrySet())
                {
                    ChildAssociationEventConsolidator eventConsolidator = entry.getValue();
                    sendEvent(entry.getKey(), eventConsolidator);
                }

                // Peer assoc events
                for (Map.Entry<AssociationRef, PeerAssociationEventConsolidator> entry : consolidators.getPeerAssocs().entrySet())
                {
                    PeerAssociationEventConsolidator eventConsolidator = entry.getValue();
                    sendEvent(entry.getKey(), eventConsolidator);
                }
            }
            catch (Exception e)
            {
                // Must consume the exception to protect other TransactionListeners
                LOGGER.error("Unexpected error while sending repository events", e);
            }
        }

        protected void sendEvent(NodeRef nodeRef, EventConsolidator consolidator)
        {
            EventInfo eventInfo = getEventInfo(AuthenticationUtil.getFullyAuthenticatedUser());
            eventGeneratorQueue.accept(()-> createEvent(nodeRef, consolidator, eventInfo));
        }

        private RepoEvent<?> createEvent(NodeRef nodeRef, EventConsolidator consolidator, EventInfo eventInfo)
        {
            String user = eventInfo.getPrincipal();

            if (consolidator.isTemporaryNode())
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Ignoring temporary node: " + nodeRef);
                }
                return null;
            }

            // Get the repo event before the filtering,
            // so we can take the latest node info into account
            final RepoEvent<?> event = consolidator.getRepoEvent(eventInfo);

            final QName nodeType = consolidator.getNodeType();
            if (isFiltered(nodeType, user))
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("EventFilter - Excluding node: '" + nodeRef + "' of type: '"
                            + ((nodeType == null) ? "Unknown' " : nodeType.toPrefixString())
                            + "' created by: " + user);
                }
                return null;
            }

            if (event.getType().equals(EventType.NODE_UPDATED.getType()) && consolidator.isResourceBeforeAllFieldsNull())
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Ignoring node updated event as no fields have been updated: " + nodeRef);
                }
                return null;
            }

            logEvent(event, consolidator.getEventTypes());
            return event;
        }

        protected void sendEvent(ChildAssociationRef childAssociationRef, ChildAssociationEventConsolidator consolidator)
        {
            EventInfo eventInfo = getEventInfo(AuthenticationUtil.getFullyAuthenticatedUser());
            eventGeneratorQueue.accept(()-> createEvent(eventInfo, childAssociationRef, consolidator));
        }

        private RepoEvent<?> createEvent(EventInfo eventInfo, ChildAssociationRef childAssociationRef, ChildAssociationEventConsolidator consolidator)
        {
            String user = eventInfo.getPrincipal();
            if (consolidator.isTemporaryChildAssociation())
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Ignoring temporary child association: " + childAssociationRef);
                }
                return null;
            }

            // Get the repo event before the filtering,
            // so we can take the latest association info into account
            final RepoEvent<?> event = consolidator.getRepoEvent(eventInfo);

            final QName childAssocType = consolidator.getChildAssocType();
            if (isFilteredChildAssociation(childAssocType, user))
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("EventFilter - Excluding child association: '" + childAssociationRef + "' of type: '"
                            + ((childAssocType == null) ? "Unknown' " : childAssocType.toPrefixString())
                            + "' created by: " + user);
                }
                return null;
            } else if (childAssociationRef.isPrimary())
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("EventFilter - Excluding primary child association: '" + childAssociationRef + "' of type: '"
                            + ((childAssocType == null) ? "Unknown' " : childAssocType.toPrefixString())
                            + "' created by: " + user);
                }
                return null;
            }

            logEvent(event, consolidator.getEventTypes());
            return event;
        }

        protected void sendEvent(AssociationRef peerAssociationRef, PeerAssociationEventConsolidator consolidator)
        {
            EventInfo eventInfo = getEventInfo(AuthenticationUtil.getFullyAuthenticatedUser());
            eventGeneratorQueue.accept(()-> createEvent(eventInfo, peerAssociationRef, consolidator));
        }

        private RepoEvent<?> createEvent(EventInfo eventInfo, AssociationRef peerAssociationRef, PeerAssociationEventConsolidator consolidator)
        {
            if (consolidator.isTemporaryPeerAssociation())
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Ignoring temporary peer association: " + peerAssociationRef);
                }
                return null;
            }

            RepoEvent<?> event = consolidator.getRepoEvent(eventInfo);
            logEvent(event, consolidator.getEventTypes());
            return event;
        }

        private void logEvent(RepoEvent<?> event, Deque<EventType> listOfEvents)
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("List of Events:" + listOfEvents);
                LOGGER.trace("Sending event:" + event);
            }
        }
    }


    protected static class Consolidators
    {
        private Map<NodeRef, EventConsolidator> nodes;
        private Map<ChildAssociationRef, ChildAssociationEventConsolidator> childAssocs;
        private Map<AssociationRef, PeerAssociationEventConsolidator> peerAssocs;

        public Map<NodeRef, EventConsolidator> getNodes()
        {
            if (nodes == null)
            {
                nodes = new LinkedHashMap<>(29);
            }
            return nodes;
        }

        public Map<ChildAssociationRef, ChildAssociationEventConsolidator> getChildAssocs()
        {
            if (childAssocs == null)
            {
                childAssocs = new LinkedHashMap<>(29);
            }
            return childAssocs;
        }

        public Map<AssociationRef, PeerAssociationEventConsolidator> getPeerAssocs()
        {
            if (peerAssocs == null)
            {
                 peerAssocs = new LinkedHashMap<>(29);
            }
            return peerAssocs;
        }
    }
}
