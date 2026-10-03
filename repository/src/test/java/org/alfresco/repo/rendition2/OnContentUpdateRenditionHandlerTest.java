/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
