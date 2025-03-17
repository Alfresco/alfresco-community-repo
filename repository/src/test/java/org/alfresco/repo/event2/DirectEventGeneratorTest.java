/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import java.util.Collection;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"repo.event2.queue.skip=true"})
public class DirectEventGeneratorTest extends EventGeneratorTest
{
    @Autowired
    private EventSender eventSender;
    @Autowired
    private Collection<EventSender> allEventSenderBeans;

    @Test
    public void testIfOnlyRequiredEventSenderIsInstantiated()
    {
        assertEquals(1, allEventSenderBeans.size());
        assertTrue(allEventSenderBeans.contains(eventSender));
    }

    @Test
    public void testIfDirectSenderIsSetInEventGenerator()
    {
        assertEquals(DirectEventSender.class, eventSender.getClass());
        assertEquals(eventSender, eventGenerator.getEventSender());
    }
}
