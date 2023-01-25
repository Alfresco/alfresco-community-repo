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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.TransactionEntity;
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
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.BehaviourDefinition;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.ServiceBehaviourBinding;
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
    protected boolean enabled;
    private Set<Behaviour> behaviours;

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public void afterPropertiesSet()
    {
        if (!isEnabled())
        {
            return;
        }

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
        setClassBehaviour(OnCreateNodePolicy.QNAME, "onCreateNode");
        setClassBehaviour(BeforeDeleteNodePolicy.QNAME, "beforeDeleteNode");
        setClassBehaviour(OnUpdatePropertiesPolicy.QNAME, "onUpdateProperties");
        setClassBehaviour(OnSetNodeTypePolicy.QNAME, "onSetNodeType");
        setClassBehaviour(OnAddAspectPolicy.QNAME, "onAddAspect");
        setClassBehaviour(OnRemoveAspectPolicy.QNAME, "onRemoveAspect");
        setClassBehaviour(OnMoveNodePolicy.QNAME, "onMoveNode");
        setAssociationBehaviour(OnCreateChildAssociationPolicy.QNAME, "onCreateChildAssociation");
        setAssociationBehaviour(BeforeDeleteChildAssociationPolicy.QNAME, "beforeDeleteChildAssociation");
        setAssociationBehaviour(OnCreateAssociationPolicy.QNAME, "onCreateAssociation");
        setAssociationBehaviour(BeforeDeleteAssociationPolicy.QNAME, "beforeDeleteAssociation");
    }

    /**
     * Disable Events2 generated events
     */
    public void disable()
    {
        if (!isEnabled())
        {
            return;
        }
        setEnabled(false);
        disableBehaviours();
    }

    /**
     * Enable Events2 generated events
     */
    public void enable()
    {
        if (isEnabled())
        {
            return;
        }

        setEnabled(true);

        if (behaviours == null)
        {
            behaviours = new HashSet<Behaviour>();
            afterPropertiesSet();
            bindBehaviours();

        }
        else
        {
            enableBehaviours();
        }

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

    private void setClassBehaviour(QName policyQName, String method)
    {
        Behaviour behaviour = bindClassBehaviour(policyQName, method);
        behaviours.add(behaviour);
    }

    private void setAssociationBehaviour(QName policyQName, String method)
    {
        Behaviour behaviour = bindAssociationBehaviour(policyQName, method);
        behaviours.add(behaviour);
    }

    protected Behaviour bindClassBehaviour(QName policyQName, String method)
    {
        BehaviourDefinition<ServiceBehaviourBinding> behaviourDef = policyComponent.bindClassBehaviour(policyQName, this,
                new JavaBehaviour(this, method));
        return behaviourDef.getBehaviour();
    }

    protected Behaviour bindAssociationBehaviour(QName policyQName, String method)
    {
        BehaviourDefinition<ServiceBehaviourBinding> behaviourDef = policyComponent.bindAssociationBehaviour(policyQName, this,
                new JavaBehaviour(this, method));
        return behaviourDef.getBehaviour();
    }

    private void disableBehaviours()
    {
        disableBehaviours(behaviours);
    }

    protected void disableBehaviours(Set<Behaviour> bindedBehaviours)
    {
        if (bindedBehaviours != null)
        {
            bindedBehaviours.forEach(behaviour -> {
                behaviour.disable();
            });
        }
    }

    private void enableBehaviours()
    {
        enableBehaviours(behaviours);
    }

    protected void enableBehaviours(Set<Behaviour> bindedBehaviours)
    {
        if (bindedBehaviours != null)
        {
            bindedBehaviours.forEach(behaviour -> {
                behaviour.enable();
            });
        }
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
        Instant commitTimeMs = Instant.ofEpochMilli(currentTransactionCommitTime);
        return ZonedDateTime.ofInstant(commitTimeMs, ZoneOffset.UTC);
    }

    @Override
    protected void onBootstrap(ApplicationEvent applicationEvent)
    {
        if (!isEnabled())
        {
            return;
        }
        behaviours = new HashSet<Behaviour>();
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
            if(isTransactionCommitted())
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
        }

        protected void sendEvent(NodeRef nodeRef, EventConsolidator consolidator)
        {
            EventInfo eventInfo = getEventInfo(AuthenticationUtil.getFullyAuthenticatedUser());
            eventGeneratorQueue.accept(()-> createEvent(nodeRef, consolidator, eventInfo));
        }

        /**
         * @return true if a node transaction is not only active, but also committed with modifications.
         * This means that a {@link TransactionEntity} object was created.
         */
        protected boolean isTransactionCommitted()
        {
            return nodeDAO.getCurrentTransactionCommitTime() != null;
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
