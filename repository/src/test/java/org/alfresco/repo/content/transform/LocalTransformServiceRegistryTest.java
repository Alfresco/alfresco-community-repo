/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.repo.content.transform;

import static org.alfresco.repo.content.transform.LocalTransformServiceRegistry.LOCAL_TRANSFORMER;
import static org.alfresco.repo.content.transform.LocalTransformServiceRegistry.URL;
import static org.junit.Assert.assertEquals;

import java.util.Properties;
import java.util.StringJoiner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LocalTransformServiceRegistryTest
{
    @Spy
    private Properties properties;
    @InjectMocks
    private LocalTransformServiceRegistry registry;

    @Test
    public void testGetTEngineUrlsSortedByName()
    {
        properties.put(LOCAL_TRANSFORMER + "aa" + URL, "aa");
        properties.put(LOCAL_TRANSFORMER + "engine1" + URL, "http_xxxx1");
        properties.put(LOCAL_TRANSFORMER + "engine3" + URL, "http3");
        properties.put(LOCAL_TRANSFORMER + "engine2" + URL, "http_xx2");
        properties.put(LOCAL_TRANSFORMER + "bb" + URL, "bb");
        properties.put(LOCAL_TRANSFORMER + "b" + URL, "b");

        StringJoiner orderEngineConfigRead = new StringJoiner(",");
        registry.getTEngineUrlsSortedByName().forEach(orderEngineConfigRead::add);
        assertEquals("aa,b,bb,http_xxxx1,http_xx2,http3", orderEngineConfigRead.toString());
    }
}
