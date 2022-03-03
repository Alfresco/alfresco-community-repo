/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.tests;

import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsStringNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class NodeApiVersioningJsonParameterizedTest extends AbstractSingleNetworkSiteTest
{
    private static final String TYPE_CM_CONTENT = "cm:content";
    private static final String TYPE_CUSTOM_DOCUMENT = "custom:document";

    private static final String VERSIONING_ENABLED_TRUE = "true";
    private static final String VERSIONING_ENABLED_FALSE = "false";
    private static final String VERSIONING_ENABLED_UNSET = null;

    private static final Boolean MAJOR_VERSION_ENABLED_TRUE = true;
    private static final Boolean MAJOR_VERSION_ENABLED_FALSE = false;
    private static final Boolean MAJOR_VERSION_ENABLED_UNSET = null;

    private static final String VERSION_NOT_EXPECTED = null;
    private static final String EXPECTED_VERSION_0_1 = "0.1";
    private static final String EXPECTED_VERSION_1_0 = "1.0";

    private static final String EXPECTED_ASPECT_VERSIONABLE = "cm:versionable";
    private static final String EXPECTED_ASPECT_NONE = null;

    protected PermissionService permissionService;
    protected AuthorityService authorityService;
    private NodeService nodeService;
    private NamespaceService namespaceService;


    @Parameterized.Parameter(value = 0)
    public String type;

    @Parameterized.Parameter(value = 1)
    public String versioningEnabled;

    @Parameterized.Parameter(value = 2)
    public Boolean majorVersion;

    @Parameterized.Parameter(value = 3)
    public String expectedVersion;

    @Parameterized.Parameter(value = 4)
    public String expectedAspect;

    @Parameterized.Parameters //parameters source - MMT-22462 comments
    public static Collection<Object[]> data()
    {
        Collection<Object[]> params = new ArrayList();
        params.add(new Object[]{TYPE_CM_CONTENT, VERSIONING_ENABLED_UNSET, MAJOR_VERSION_ENABLED_UNSET, VERSION_NOT_EXPECTED, EXPECTED_ASPECT_NONE});
        params.add(new Object[]{TYPE_CM_CONTENT, VERSIONING_ENABLED_UNSET, MAJOR_VERSION_ENABLED_TRUE, EXPECTED_VERSION_1_0, EXPECTED_ASPECT_NONE});
        params.add(new Object[]{TYPE_CM_CONTENT, VERSIONING_ENABLED_UNSET, MAJOR_VERSION_ENABLED_FALSE, EXPECTED_VERSION_0_1, EXPECTED_ASPECT_NONE});

        params.add(new Object[]{TYPE_CM_CONTENT, VERSIONING_ENABLED_FALSE, MAJOR_VERSION_ENABLED_UNSET, VERSION_NOT_EXPECTED, EXPECTED_ASPECT_NONE});
        params.add(new Object[]{TYPE_CM_CONTENT, VERSIONING_ENABLED_FALSE, MAJOR_VERSION_ENABLED_TRUE, VERSION_NOT_EXPECTED, EXPECTED_ASPECT_NONE});
        params.add(new Object[]{TYPE_CM_CONTENT, VERSIONING_ENABLED_FALSE, MAJOR_VERSION_ENABLED_FALSE, VERSION_NOT_EXPECTED, EXPECTED_ASPECT_NONE});

        params.add(new Object[]{TYPE_CM_CONTENT, VERSIONING_ENABLED_TRUE, MAJOR_VERSION_ENABLED_UNSET, EXPECTED_VERSION_1_0, EXPECTED_ASPECT_VERSIONABLE});
        params.add(new Object[]{TYPE_CM_CONTENT, VERSIONING_ENABLED_TRUE, MAJOR_VERSION_ENABLED_TRUE, EXPECTED_VERSION_1_0, EXPECTED_ASPECT_VERSIONABLE});
        params.add(new Object[]{TYPE_CM_CONTENT, VERSIONING_ENABLED_TRUE, MAJOR_VERSION_ENABLED_FALSE, EXPECTED_VERSION_0_1, EXPECTED_ASPECT_VERSIONABLE});

        params.add(new Object[]{TYPE_CUSTOM_DOCUMENT, VERSIONING_ENABLED_UNSET, MAJOR_VERSION_ENABLED_UNSET, EXPECTED_VERSION_1_0, EXPECTED_ASPECT_VERSIONABLE});
        params.add(new Object[]{TYPE_CUSTOM_DOCUMENT, VERSIONING_ENABLED_UNSET, MAJOR_VERSION_ENABLED_TRUE, EXPECTED_VERSION_1_0, EXPECTED_ASPECT_VERSIONABLE});
        params.add(new Object[]{TYPE_CUSTOM_DOCUMENT, VERSIONING_ENABLED_UNSET, MAJOR_VERSION_ENABLED_FALSE, EXPECTED_VERSION_0_1, EXPECTED_ASPECT_VERSIONABLE});

        params.add(new Object[]{TYPE_CUSTOM_DOCUMENT, VERSIONING_ENABLED_TRUE, MAJOR_VERSION_ENABLED_UNSET, EXPECTED_VERSION_1_0, EXPECTED_ASPECT_VERSIONABLE});
        params.add(new Object[]{TYPE_CUSTOM_DOCUMENT, VERSIONING_ENABLED_TRUE, MAJOR_VERSION_ENABLED_TRUE, EXPECTED_VERSION_1_0, EXPECTED_ASPECT_VERSIONABLE});
        params.add(new Object[]{TYPE_CUSTOM_DOCUMENT, VERSIONING_ENABLED_TRUE, MAJOR_VERSION_ENABLED_FALSE, EXPECTED_VERSION_0_1, EXPECTED_ASPECT_VERSIONABLE});

        params.add(new Object[]{TYPE_CUSTOM_DOCUMENT, VERSIONING_ENABLED_FALSE, MAJOR_VERSION_ENABLED_UNSET, EXPECTED_VERSION_1_0, EXPECTED_ASPECT_VERSIONABLE});
        params.add(new Object[]{TYPE_CUSTOM_DOCUMENT, VERSIONING_ENABLED_FALSE, MAJOR_VERSION_ENABLED_TRUE, EXPECTED_VERSION_1_0, EXPECTED_ASPECT_VERSIONABLE});
        params.add(new Object[]{TYPE_CUSTOM_DOCUMENT, VERSIONING_ENABLED_FALSE, MAJOR_VERSION_ENABLED_FALSE, EXPECTED_VERSION_0_1, EXPECTED_ASPECT_VERSIONABLE});

        return params;
    }

    @Before
    public void setup() throws Exception
    {
        super.setup();

        permissionService = applicationContext.getBean("permissionService", PermissionService.class);
        authorityService = (AuthorityService) applicationContext.getBean("AuthorityService");
        nodeService = applicationContext.getBean("NodeService", NodeService.class);
        namespaceService= (NamespaceService) applicationContext.getBean("NamespaceService");
    }

    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    @Test
    public void versionableDocumentJsonNodeCreationTest() throws Exception
    {
        setRequestContext(user1);
        String myNodeId = getMyNodeId();

        Document d1 = new Document();
        Map<String, String> params = new HashMap<>();
        d1.setName("testDoc" + UUID.randomUUID());
        d1.setNodeType(type);

        if(versioningEnabled != null)
        {
            params.put("versioningEnabled", versioningEnabled);
        }
        if(majorVersion != null)
        {
            params.put("majorVersion", majorVersion.toString());
        }

        HttpResponse response = post(getNodeChildrenUrl(myNodeId), toJsonAsStringNonNull(d1), params, null, null, 201);
        Document documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertExpectedVersion(expectedVersion, documentResponse.getProperties());
        assertContainsAspect(expectedAspect, documentResponse);
    }

    private void assertExpectedVersion(String expectedVersion, Map<String, Object> documentProperties)
    {
        if(documentProperties != null) {
            assertEquals(expectedVersion, documentProperties.get("cm:versionLabel"));
        }
    }

    private void assertContainsAspect(String expectedAspect, Document documentResponse)
    {
        if(expectedAspect != null) {
            assertTrue(!documentResponse.getAspectNames().isEmpty());
            assertTrue(documentResponse.getAspectNames().contains(expectedAspect));
        }
    }
}
