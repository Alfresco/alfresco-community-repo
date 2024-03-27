/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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

import java.util.concurrent.TimeUnit;

import org.alfresco.model.ContentModel;
import org.awaitility.Awaitility;
import org.junit.Test;

public class EventGeneratorDisabledTest extends EventGeneratorTest
{
    @Test
    public void shouldNotReceiveEvent2EventsOnNodeCreation()
    {
        if (eventGenerator.isEnabled())
        {
            eventGenerator.disable();
        }
        
        createNode(ContentModel.TYPE_CONTENT);

        Awaitility.await().pollDelay(6, TimeUnit.SECONDS).until(() -> receivedEvents.size() == 0);
        
        assertTrue(EVENT_CONTAINER.getEvents().size() == 0);
        assertTrue(receivedEvents.size() == 0);

        eventGenerator.enable();
    }
    
    @Test
    @Override
    public void shouldReceiveEvent2EventsOnNodeCreation()
    {
        if (!eventGenerator.isEnabled())
        {
            eventGenerator.enable();
        }

        super.shouldReceiveEvent2EventsOnNodeCreation();
    }

    @Test
    @Override
    public void shouldReceiveEvent2EventsInOrder()
    {
        if (!eventGenerator.isEnabled())
        {
            eventGenerator.enable();
        }

        super.shouldReceiveEvent2EventsInOrder();
    }
}
