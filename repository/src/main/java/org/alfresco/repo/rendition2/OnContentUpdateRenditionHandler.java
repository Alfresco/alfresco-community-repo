package org.alfresco.repo.rendition2;

import org.springframework.beans.factory.InitializingBean;

import org.alfresco.messaging.TextMessageHandler;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.EventBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.rawevents.TransactionAwareEventProducer;
import org.alfresco.repo.rawevents.types.EventType;
import org.alfresco.repo.rawevents.types.OnContentUpdatePolicyEvent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;

public class OnContentUpdateRenditionHandler implements InitializingBean, TextMessageHandler
{
    private final String endpointUri;
    private final TransactionAwareEventProducer eventProducer;
    private final PolicyComponent policyComponent;
    private final RenditionEventProcessor eventProcessor;

    public OnContentUpdateRenditionHandler(String endpointUri, TransactionAwareEventProducer eventProducer,
            PolicyComponent policyComponent, RenditionEventProcessor eventProcessor)
    {
        this.endpointUri = endpointUri;
        this.eventProducer = eventProducer;
        this.policyComponent = policyComponent;
        this.eventProcessor = eventProcessor;
    }

    @Override
    public void afterPropertiesSet()
    {
        EventBehaviour eventBehaviour = new EventBehaviour(eventProducer, endpointUri, this, "createOnContentUpdateEvent",
                Behaviour.NotificationFrequency.EVERY_EVENT);
        policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentUpdatePolicy.QNAME, RenditionModel.ASPECT_RENDITIONED, eventBehaviour);
    }

    @Override
    public void process(String body)
    {
        eventProcessor.process(body);
    }

    @SuppressWarnings("unused")
    public OnContentUpdatePolicyEvent createOnContentUpdateEvent(NodeRef sourceNodeRef, boolean newContent)
    {
        OnContentUpdatePolicyEvent event = new OnContentUpdatePolicyEvent();
        event.setId(GUID.generate());
        event.setType(EventType.CONTENT_UPDATED.toString());
        event.setAuthenticatedUser(AuthenticationUtil.getFullyAuthenticatedUser());
        event.setExecutingUser(AuthenticationUtil.getRunAsUser());
        event.setTimestamp(System.currentTimeMillis());
        event.setSchema(1);
        event.setNodeRef(sourceNodeRef.toString());
        event.setNewContent(newContent);
        return event;
    }
}