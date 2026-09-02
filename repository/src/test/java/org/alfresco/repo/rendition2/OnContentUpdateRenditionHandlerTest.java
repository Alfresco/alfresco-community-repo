package org.alfresco.repo.rendition2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import org.alfresco.model.RenditionModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.policy.EventBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.rawevents.TransactionAwareEventProducer;

public class OnContentUpdateRenditionHandlerTest
{
    @Test
    public void registersContentUpdatePolicyAndDelegatesMessages()
    {
        PolicyComponent policyComponent = mock(PolicyComponent.class);
        RenditionEventProcessor eventProcessor = mock(RenditionEventProcessor.class);
        OnContentUpdateRenditionHandler handler = new OnContentUpdateRenditionHandler("jms:renditions",
                mock(TransactionAwareEventProducer.class), policyComponent, eventProcessor);

        handler.afterPropertiesSet();
        handler.process("payload");

        verify(policyComponent).bindClassBehaviour(eq(ContentServicePolicies.OnContentUpdatePolicy.QNAME),
            eq(RenditionModel.ASPECT_RENDITIONED), any(EventBehaviour.class));
        verify(eventProcessor).process("payload");
    }
}