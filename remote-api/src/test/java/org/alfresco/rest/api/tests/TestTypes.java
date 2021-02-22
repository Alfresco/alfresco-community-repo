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
import org.alfresco.rest.api.model.Association;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.ExpectedComparison;
import org.alfresco.rest.api.tests.client.data.Type;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.util.Arrays;

import static org.junit.Assert.*;

public class TestTypes extends BaseModelApiTest
{
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

        otherParams.put("where", "(parentId in ('cm:content'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        int total = types.getPaging().getTotalItems();

        otherParams.put("where", "(parentId in ('cm:content') AND namespaceUri matches('http://www.mycompany.com/model.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        types.getList().get(0).expected(docType);
        types.getList().get(1).expected(whitePaperType);
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(2));

        otherParams.put("where", "(parentId in ('cm:content') AND not namespaceUri matches('http://www.mycompany.com/model.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(total - 2));

        // match everything
        otherParams.put("where", "(parentId in ('cm:content') AND namespaceUri matches('.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(total));

        // match nothing
        otherParams.put("where", "(parentId in ('cm:content') AND not namespaceUri matches('.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(0));
    }

    @Test
    public void filterTypesByModelId() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(modelId in ('mycompany:model','test:scan'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(4));

        otherParams.put("where", "(modelId in ('mycompany:model','test:scan') AND namespaceUri matches('http://www.mycompany.com/model.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        types.getList().get(0).expected(docType);
        types.getList().get(1).expected(whitePaperType);
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(2));

        otherParams.put("where", "(modelId in ('mycompany:model','test:scan') AND not namespaceUri matches('http://www.mycompany.com/model.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(2));

        // match everything
        otherParams.put("where", "(modelId in ('mycompany:model','test:scan') AND namespaceUri matches('.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(4));

        // match nothing
        otherParams.put("where", "(modelId in ('mycompany:model','test:scan') AND not namespaceUri matches('.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(0));
    }

    @Test
    public void testIncludeProperty() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(modelId in ('mycompany:model','test:scan'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(4));
        assertNull(types.getList().get(0).getProperties());
        assertNull(types.getList().get(1).getProperties());
        assertNull(types.getList().get(2).getProperties());

        otherParams.put("where", "(modelId in ('mycompany:model','test:scan') AND namespaceUri matches('http://www.mycompany.com/model.*'))");
        otherParams.put("include", "properties");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        types.getList().get(0).expected(docType);
        types.getList().get(1).expected(whitePaperType);
        assertNotNull(types.getList().get(0).getProperties());
        assertNotNull(types.getList().get(1).getProperties());
    }

    @Test
    public void testIncludeAssociation() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(modelId in ('api:apiModel'))");
        otherParams.put("include", "associations");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(10));


        for (int i = 0; i < types.getList().size(); i++)
        {
            Type type = types.getList().get(i);

            assertNotNull(type.getAssociations());
            assertNull(type.getProperties());
            assertNull(type.getMandatoryAspects());

            type.expected(allTypes.get(i));

            for (int j = 0; j < type.getAssociations().size(); j++)
            {
                ExpectedComparison association = (ExpectedComparison) type.getAssociations().get(j);
                association.expected(allTypes.get(i).getAssociations().get(j));
            }
        }
    }

    @Test
    public void testIncludeMandatoryAspect() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(modelId in ('api:apiModel'))");
        otherParams.put("include", "mandatoryAspects");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));

        for (int i = 0; i < types.getList().size(); i++)
        {
            Type type = types.getList().get(i);

            assertNotNull(type.getMandatoryAspects());
            assertNull(type.getProperties());
            assertNull(type.getAssociations());

            type.expected(allTypes.get(i));
            assertEquals(type.getMandatoryAspects(), allTypes.get(i).getMandatoryAspects());
        }
    }

    @Test
    public void testIncludes() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(modelId in ('api:apiModel'))");
        otherParams.put("include", "associations,mandatoryAspects");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(10));

        for (int i = 0; i < types.getList().size(); i++)
        {
            Type type = types.getList().get(i);

            assertNotNull(type.getAssociations());
            assertNull(type.getProperties());
            assertNull(type.getMandatoryAspects());

            type.expected(allTypes.get(i));
            assertEquals(type.getMandatoryAspects(), allTypes.get(i).getMandatoryAspects());

            for (int j = 0; j < type.getAssociations().size(); j++)
            {
                ExpectedComparison association = (ExpectedComparison) type.getAssociations().get(j);
                association.expected(allTypes.get(i).getAssociations().get(j));
            }
        }
    }

    @Test
    public void testSubTypes() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(modelId in ('mycompany:model'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(2));
        types.getList().get(0).expected(docType);
        types.getList().get(1).expected(whitePaperType);

        otherParams.put("where", "(modelId in ('mycompany:model INCLUDESUBTYPES'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(3));
        types.getList().get(0).expected(docType);
        types.getList().get(1).expected(whitePaperType);
        types.getList().get(2).expected(publishableType);

        otherParams.put("where", "(modelId in ('mycompany:model INCLUDESUBTYPES') AND namespaceUri matches('http://www.test.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        assertEquals(types.getPaging().getTotalItems(), Integer.valueOf(1));
        types.getList().get(0).expected(publishableType);

        otherParams.put("where", "(modelId in ('mycompany:model INCLUDESUBTYPES') AND not namespaceUri matches('http://www.test.*'))");
        types = publicApiClient.types().getTypes(createParams(paging, otherParams));
        types.getList().get(0).expected(docType);
        types.getList().get(1).expected(whitePaperType);
    }

    @Test
    public void testTypesById() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        type = publicApiClient.types().getType("mycompany:whitepaper");
        type.expected(whitePaperType);

        type = publicApiClient.types().getType("api:base");
        type.expected(apiBaseType);
        assertNull(type.getProperties());
        assertEquals(type.getMandatoryAspects(), apiBaseType.getMandatoryAspects());
        assertEquals(type.getAssociations(), apiBaseType.getAssociations());
    }

    @Test
    public void testListTypeByInvalidValue() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        testListTypeException("(modelId in ('mycompany:model','unknown:model'))");
        testListTypeException("(modelId in ('unknown:model','unknown1:another'))");
        testListTypeException("(modelId in (' ', '')");
        testListTypeException("(parentId in ('cm:content','unknown:type')");
        testListTypeException("(parentId in ('unknown:type','cm:content'))");
        testListTypeException("(parentId in ('unknown:type','unknown:types'))");
        testListTypeException("(parentId in (' ',' ',' '))");
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
}
