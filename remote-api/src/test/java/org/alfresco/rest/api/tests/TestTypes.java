/*
 * #%L
 * Alfresco Remote API
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

package org.alfresco.rest.api.tests;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class TestTypes extends AbstractBaseApiTest
{

    private PublicApiClient.Paging paging = getPaging(0, 10);
    PublicApiClient.ListResponse<org.alfresco.rest.api.tests.client.data.Type> types = null;
    org.alfresco.rest.api.tests.client.data.Type type = null, whitePaperType = null, docType = null;
    Map<String, String> otherParams = new HashMap<>();

    @Before
    public void setup() throws Exception
    {
        super.setup();
        whitePaperType = new org.alfresco.rest.api.tests.client.data.Type();
        whitePaperType.setId("mycompany:whitepaper");
        whitePaperType.setTitle("whitepaper");
        whitePaperType.setDescription("Whitepaper");
        whitePaperType.setParentId("mycompany:doc");

        docType = new org.alfresco.rest.api.tests.client.data.Type();
        docType.setId("mycompany:doc");
        docType.setTitle("doc");
        docType.setDescription("Doc");
        docType.setParentId("cm:content");
    }

    @Test
    public void testAllTypes() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertTrue(types.getPaging().getTotalItems() > 135);
        assertTrue(types.getPaging().getHasMoreItems());

        paging.setSkipCount(130);
        paging.setMaxItems(50);
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertFalse(types.getPaging().getHasMoreItems());
    }

    @Test
    public void filterTypesByNamespace() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(namespaceUri matches('http://www.mycompany.com/model.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        types.getList().get(0).expected(docType);
        types.getList().get(1).expected(whitePaperType);
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(2));

        otherParams.put("where", "(not namespaceUri matches('http://www.mycompany.com/model.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertTrue(types.getPaging().getTotalItems() > 130);
    }

    @Test
    public void filterTypesByParentId() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(parentIds in ('cm:content'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        int total = types.getPaging().getTotalItems();

        otherParams.put("where", "(parentIds in ('cm:content') AND namespaceUri matches('http://www.mycompany.com/model.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        types.getList().get(0).expected(docType);
        types.getList().get(1).expected(whitePaperType);
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(2));

        otherParams.put("where", "(parentIds in ('cm:content') AND not namespaceUri matches('http://www.mycompany.com/model.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(total - 2));

        // match everything
        otherParams.put("where", "(parentIds in ('cm:content') AND namespaceUri matches('.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(total));

        // match nothing
        otherParams.put("where", "(parentIds in ('cm:content') AND not namespaceUri matches('.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(0));
    }

    @Test
    public void filterTypesByModelId() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(modelIds in ('mycompany:model','test:scan'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(3));

        otherParams.put("where", "(modelIds in ('mycompany:model','test:scan') AND namespaceUri matches('http://www.mycompany.com/model.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        types.getList().get(0).expected(docType);
        types.getList().get(1).expected(whitePaperType);
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(2));

        otherParams.put("where", "(modelIds in ('mycompany:model','test:scan') AND not namespaceUri matches('http://www.mycompany.com/model.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(1));

        // match everything
        otherParams.put("where", "(modelIds in ('mycompany:model','test:scan') AND namespaceUri matches('.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(3));

        // match nothing
        otherParams.put("where", "(modelIds in ('mycompany:model','test:scan') AND not namespaceUri matches('.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(0));
    }

    @Test
    public void testTypesById() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        type = publicApiClient.types().getType("mycompany:whitepaper");
        type.expected(whitePaperType);
    }

    @Test
    public void testListTypeByInvalidValue() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        testListTypeException("(modelIds in ('mycompany:model','unknown:model'))");
        testListTypeException("(modelIds in ('unknown:model','unknown1:another'))");
        testListTypeException("(modelIds=' , , ')");
        testListTypeException("(parentIds in ('cm:content','unknown:type')");
        testListTypeException("(parentIds in ('unknown:type','cm:content'))");
        testListTypeException("(parentIds in ('unknown:type','unknown:types'))");
        testListTypeException("(parentIds in (' ',' ',' '))");
        testListTypeException("");
        testListTypeException("(namespaceUri matches('*'))"); // wrong pattern
    }

    @Test
    public void testGetTypeByInvalidValue() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        testGetTypeExceptions("unknown:childType");
        testGetTypeExceptions("type:");
        testGetTypeExceptions("type");
    }


    private void testGetTypeExceptions(String typeId)
    {
        try
        {
            publicApiClient.types().getType(typeId);
            fail("type not found expected");
        }
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
        }
    }

    private void testListTypeException(String query)
    {
        try
        {
            otherParams.put("where", query);
            publicApiClient.types().getTypes(createParams(paging, otherParams));
            fail("Bad request expected");
        }
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
        }
    }


    @Override
    public String getScope()
    {
        return "public";
    }
}
