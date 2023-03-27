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

package org.alfresco.repo.security.mtls;


import org.alfresco.repo.rendition2.LocalTransformClientIntegrationTest;
import org.alfresco.repo.rendition2.RenditionService2;
import org.junit.BeforeClass;

/**
 * Integration tests for {@link RenditionService2} with mtls enabled
 */
public class LocalTransformClientWithMTLSIntegrationTest extends LocalTransformClientIntegrationTest
{
    @BeforeClass
    public static void before()
    {
        local();

        System.setProperty("localTransform.core-aio.url", "https://localhost:8090/");
        System.setProperty("httpclient.config.transform.mTLSEnabled", "true");
        System.setProperty("ssl-keystore.password", "password");
        System.setProperty("ssl-truststore.password", "password");
        System.setProperty("metadata-keystore.password", "password");
        System.setProperty("metadata-keystore.aliases", "metadata");
        System.setProperty("metadata-keystore.metadata.password", "password");
    }

}
