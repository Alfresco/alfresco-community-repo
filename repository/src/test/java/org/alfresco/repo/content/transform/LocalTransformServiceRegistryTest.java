/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.util.Properties;
import java.util.StringJoiner;

import static org.alfresco.repo.content.transform.LocalTransformServiceRegistry.LOCAL_TRANSFORMER;
import static org.alfresco.repo.content.transform.LocalTransformServiceRegistry.URL;
import static org.junit.Assert.assertEquals;

public class LocalTransformServiceRegistryTest
{
    @Spy
    private Properties properties = new Properties();

    @InjectMocks
    LocalTransformServiceRegistry registry = new LocalTransformServiceRegistry();

    @Rule
    public MockitoRule initRule = MockitoJUnit.rule();

    @Test
    public void testGetTEngineUrlsSortedByName() throws IOException
    {
        properties.put(LOCAL_TRANSFORMER+"aa"+URL,      "aa");
        properties.put(LOCAL_TRANSFORMER+"engine1"+URL, "http_xxxx1");
        properties.put(LOCAL_TRANSFORMER+"engine3"+URL, "http3");
        properties.put(LOCAL_TRANSFORMER+"engine2"+URL, "http_xx2");
        properties.put(LOCAL_TRANSFORMER+"bb"+URL,      "bb");
        properties.put(LOCAL_TRANSFORMER+"b"+URL,       "b");

        StringJoiner orderEngineConfigRead = new StringJoiner(",");
        registry.getTEngineUrlsSortedByName().forEach(name -> orderEngineConfigRead.add(name));
        assertEquals("aa,b,bb,http_xxxx1,http_xx2,http3", orderEngineConfigRead.toString());
    }
}
