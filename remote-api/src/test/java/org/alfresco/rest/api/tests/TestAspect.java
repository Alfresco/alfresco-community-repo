/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TestAspect extends AbstractBaseApiTest {

    private PublicApiClient.Paging paging = getPaging(0, 10);
    PublicApiClient.ListResponse<org.alfresco.rest.api.tests.client.data.Aspect> aspects = null;
    Map<String, String> otherParams = new HashMap<>();

    @Before
    public void setup() throws Exception {
        super.setup();
    }


    @Test
    public void testListAllAspects() throws PublicApiException {
        try
        {
            AuthenticationUtil.setRunAsUser(user1);
            publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

            aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
            assertTrue(aspects.getPaging().getTotalItems() > 135);
            assertTrue(aspects.getPaging().getHasMoreItems());


            otherParams.put("where", "()");
            aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
            assertFalse(aspects.getPaging().getHasMoreItems());
        }
        finally
        {
        }

    }

    @Override
    public String getScope() {
        return "public";
    }
}
