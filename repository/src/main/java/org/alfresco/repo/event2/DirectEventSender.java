/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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

import java.util.Optional;
import java.util.concurrent.Callable;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.util.PropertyCheck;
import org.gytheio.messaging.MessagingException;
import org.springframework.beans.factory.InitializingBean;

/**
 * Sender allows to send events directly to topic.
 */
public class DirectEventSender implements EventSender, InitializingBean
{
    protected Event2MessageProducer event2MessageProducer;

    @Override
    public void afterPropertiesSet()
    {
        PropertyCheck.mandatory(this, "event2MessageProducer", event2MessageProducer);
    }

    public void setEvent2MessageProducer(Event2MessageProducer event2MessageProducer)
    {
        this.event2MessageProducer = event2MessageProducer;
    }

    @Override
    public void accept(Callable<Optional<RepoEvent<?>>> eventProducer)
    {
        try
        {
            eventProducer.call().ifPresent(event -> event2MessageProducer.send(event));
        }
        catch (MessagingException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException("Unexpected error while executing maker function for repository event", e);
        }
    }
}
