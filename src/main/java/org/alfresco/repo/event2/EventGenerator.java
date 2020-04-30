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
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event2.filter.EventFilterRegistry;
import org.alfresco.repo.event2.filter.EventUserFilter;
import org.alfresco.repo.event2.filter.NodeTypeFilter;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnRemoveAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
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
public class EventGenerator extends AbstractLifecycleBean implements InitializingBean, EventSupportedPolicies
{
    private static final Log LOGGER = LogFactory.getLog(EventGenerator.class);

    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private DescriptorService descriptorService;
    private EventFilterRegistry eventFilterRegistry;
    private Event2MessageProducer event2MessageProducer;
    private TransactionService transactionService;
    private PersonService personService;

    private NodeTypeFilter nodeTypeFilter;
    private EventUserFilter userFilter;
    private NodeResourceHelper nodeResourceHelper;
    private final EventTransactionListener transactionListener = new EventTransactionListener();

    @Override
    public void afterPropertiesSet()
    {
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "descriptorService", descriptorService);
        PropertyCheck.mandatory(this, "eventFilterRegistry", eventFilterRegistry);
        PropertyCheck.mandatory(this, "event2MessageProducer", event2MessageProducer);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "personService", personService);

        this.nodeTypeFilter = eventFilterRegistry.getNodeTypeFilter();
        this.userFilter = eventFilterRegistry.getEventUserFilter();
        this.nodeResourceHelper = new NodeResourceHelper(nodeService, namespaceService, dictionaryService,
                                                         personService,
                                                         eventFilterRegistry);
    }

    private void bindBehaviours()
    {
        policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, this,
                                           new JavaBehaviour(this, "onCreateNode"));
        policyComponent.bindClassBehaviour(BeforeDeleteNodePolicy.QNAME, this,
                                           new JavaBehaviour(this, "beforeDeleteNode"));
        policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, this,
                                           new JavaBehaviour(this, "onUpdateProperties"));
        policyComponent.bindClassBehaviour(OnAddAspectPolicy.QNAME, this,
                                           new JavaBehaviour(this, "onAddAspect"));
        policyComponent.bindClassBehaviour(OnRemoveAspectPolicy.QNAME, this,
                                           new JavaBehaviour(this, "onRemoveAspect"));
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

    public void setEventFilterRegistry(EventFilterRegistry eventFilterRegistry)
    {
        this.eventFilterRegistry = eventFilterRegistry;
    }

    public void setEvent2MessageProducer(Event2MessageProducer event2MessageProducer)
    {
        this.event2MessageProducer = event2MessageProducer;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        getEventConsolidator(childAssocRef.getChildRef()).onCreateNode(childAssocRef);
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        getEventConsolidator(nodeRef).onUpdateProperties(nodeRef, before, after);
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

    /**
     * @return the {@link EventConsolidator} for the supplied {@code nodeRef} from
     * the current transaction context.
     */
    private EventConsolidator getEventConsolidator(NodeRef nodeRef)
    {
        Map<NodeRef, EventConsolidator> nodeEvents = getTxnResourceMap(transactionListener);
        if (nodeEvents.isEmpty())
        {
            AlfrescoTransactionSupport.bindListener(transactionListener);
        }

        EventConsolidator eventConsolidator = nodeEvents.get(nodeRef);
        if (eventConsolidator == null)
        {
            eventConsolidator = new EventConsolidator(nodeResourceHelper);
            nodeEvents.put(nodeRef, eventConsolidator);
        }
        return eventConsolidator;
    }

    private Map<NodeRef, EventConsolidator> getTxnResourceMap(Object resourceKey)
    {
        Map<NodeRef, EventConsolidator> map = AlfrescoTransactionSupport.getResource(resourceKey);
        if (map == null)
        {
            map = new LinkedHashMap<>(29);
            AlfrescoTransactionSupport.bindResource(resourceKey, map);
        }
        return map;
    }

    private boolean isFiltered(QName nodeType, String user)
    {
        return (nodeTypeFilter.isExcluded(nodeType) || (userFilter.isExcluded(user)));
    }

    private EventInfo getEventInfo(String user)
    {
        return new EventInfo().setTimestamp(ZonedDateTime.now())
                              .setId(UUID.randomUUID().toString())
                              .setTxnId(AlfrescoTransactionSupport.getTransactionId())
                              .setPrincipal(user)
                              .setSource(URI.create("/" + descriptorService.getCurrentRepositoryDescriptor().getId()));
    }

    private void sendEvent(NodeRef nodeRef, EventConsolidator consolidator)
    {
        if (consolidator.isTemporaryNode())
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Ignoring temporary node: " + nodeRef);
            }
            return;
        }

        final String user = AuthenticationUtil.getFullyAuthenticatedUser();
        // Get the repo event before the filtering,
        // so we can take the latest node info into account
        final RepoEvent<?> event = consolidator.getRepoEvent(getEventInfo(user));

        final QName nodeType = consolidator.getNodeType();
        if (isFiltered(nodeType, user))
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("EventFilter - Excluding node: '" + nodeRef + "' of type: '"
                                         + ((nodeType == null) ? "Unknown' " : nodeType.toPrefixString())
                                         + "' created by: " + user);
            }
            return;
        }

        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("List of Events:" + consolidator.getEventTypes());
            LOGGER.trace("Sending event:" + event);
        }
        // Need to execute this in another read txn because Camel expects it
        transactionService.getRetryingTransactionHelper().doInTransaction((RetryingTransactionCallback<Void>) () -> {
            event2MessageProducer.send(event);

            return null;
        }, true, false);
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

    private class EventTransactionListener extends TransactionListenerAdapter
    {
        @Override
        public void afterCommit()
        {
            try
            {
                final Map<NodeRef, EventConsolidator> changedNodes = getTxnResourceMap(this);
                for (Map.Entry<NodeRef, EventConsolidator> entry : changedNodes.entrySet())
                {
                    EventConsolidator eventConsolidator = entry.getValue();
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
}
