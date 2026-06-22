/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertNotNull;

import java.util.Objects;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

import org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl;
import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.CMISDispatcherRegistry;
import org.alfresco.opencmis.CMISPropertyBasedVirtualRepository;
import org.alfresco.opencmis.CMISVirtualRepository;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Person;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class TestCMISVirtualRepository extends EnterpriseTestApi
{
    private NodeService nodeService;
    private RepoService.TestNetwork testNetwork;

    public TestCMISVirtualRepository()
    {
        System.err.println("CTOR");
    }

    @Override
    public void setup() throws Exception
    {
        final ApplicationContext ctx = getTestFixture().getApplicationContext();
        final NamespaceService namespaceService = ctx.getBean("NamespaceService", NamespaceService.class);

        this.nodeService = ctx.getBean("NodeService", NodeService.class);

        final CMISVirtualRepository virtualRepository = new CMISPropertyBasedVirtualRepository(nodeService, namespaceService,
                QName.createQName("http://www.alfresco.org/test/virtcmis/1.0", "VirtualRepository"),
                QName.createQName("http://www.alfresco.org/test/virtcmis/1.0", "repositoryId"),
                "exposed");

        ctx.getBean("CMISConnector", CMISConnector.class).setVirtualRepository(virtualRepository);

        testNetwork = getTestFixture().getRandomNetwork();
    }

    public void tearDown()
    {}

    @Override
    protected String[] getCustomConfigLocations()
    {
        return new String[]{"classpath:org/alfresco/rest/api/tests/cmis-virtual-repository-test-context.xml"};
    }

    @Test
    public void testVirtualRepository()
    {
        givenCmisSession();

    }

    @Test
    public void test2()
    {
        givenCmisSession();
    }

    @Test
    public void test3()
    {
        givenCmisSession();
    }

    private PublicApiClient.CmisSession givenCmisSession()
    {
        final String personId = testNetwork.getPersonIds().stream().filter(Objects::nonNull).findFirst().orElse(null);
        assertNotNull(personId);

        Person person = repoService.getPerson(personId);
        assertNotNull(person);

        publicApiClient.setRequestContext(new RequestContext(testNetwork.getId(), personId));
        return publicApiClient.createPublicApiCMISSession(CMISDispatcherRegistry.Binding.browser, "1.1", AlfrescoObjectFactoryImpl.class.getName());
    }
}
