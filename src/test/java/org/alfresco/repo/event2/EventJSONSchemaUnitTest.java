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

import static org.junit.Assert.fail;

import org.alfresco.error.AlfrescoRuntimeException;
import org.junit.Test;

/**
 * Test event JSON schema mapping.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class EventJSONSchemaUnitTest
{

    @Test
    public void testEventJsonSchema()
    {
        for (EventType type : EventType.values())
        {
            try
            {
                EventJSONSchema.getSchema(type, 1);
            }
            catch (Exception ex)
            {
                fail(ex.getMessage());
            }
        }
    }

    @Test(expected = AlfrescoRuntimeException.class)
    public void testEventJsonSchemaInvalid()
    {
        // Invalid version
        for (EventType type : EventType.values())
        {
            EventJSONSchema.getSchema(type, 5);
        }
    }

    @Test
    public void testEventJsonSchemaV1()
    {
        for (EventType type : EventType.values())
        {
            try
            {
                EventJSONSchema.getSchemaV1(type);
            }
            catch (Exception ex)
            {
                fail(ex.getMessage());
            }
        }
    }
}
