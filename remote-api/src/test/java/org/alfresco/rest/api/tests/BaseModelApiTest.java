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

import org.alfresco.rest.api.model.Model;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.junit.Before;

import java.util.HashMap;
import java.util.Map;

public class BaseModelApiTest extends AbstractBaseApiTest
{
    PublicApiClient.ListResponse<org.alfresco.rest.api.tests.client.data.Aspect> aspects = null;
    org.alfresco.rest.api.tests.client.data.Aspect aspect = null, childAspect = null, smartFilter = null, rescanAspect = null, testAspect = null;


    PublicApiClient.ListResponse<org.alfresco.rest.api.tests.client.data.Type> types = null;
    org.alfresco.rest.api.tests.client.data.Type type = null, whitePaperType = null, docType = null, publishableType = null;


    PublicApiClient.Paging paging = getPaging(0, 10);
    Map<String, String> otherParams = new HashMap<>();

    @Before
    public void setup() throws Exception
    {
        super.setup();

        Model myCompanyModel = new Model();
        myCompanyModel.setAuthor("Administrator");
        myCompanyModel.setId("mycompany:model");
        myCompanyModel.setNamespaceUri("http://www.mycompany.com/model/finance/1.0");
        myCompanyModel.setNamespacePrefix("mycompany");

        Model scanModel = new Model();
        scanModel.setAuthor("Administrator");
        scanModel.setId("test:scan");
        scanModel.setNamespaceUri("http://www.test.com/model/account/1.0");
        scanModel.setNamespacePrefix("test");

        testAspect = new org.alfresco.rest.api.tests.client.data.Aspect();
        testAspect.setId("mycompany:testAspect");
        testAspect.setTitle("Test Aspect");
        testAspect.setModel(myCompanyModel);

        childAspect = new org.alfresco.rest.api.tests.client.data.Aspect();
        childAspect.setId("mycompany:childAspect");
        childAspect.setTitle("Child Aspect");
        childAspect.setDescription("Child Aspect Description");
        childAspect.setParentId("smf:smartFolder");
        childAspect.setModel(myCompanyModel);

        rescanAspect = new org.alfresco.rest.api.tests.client.data.Aspect();
        rescanAspect.setId("test:rescan");
        rescanAspect.setTitle("rescan");
        rescanAspect.setDescription("Doc that required to scan ");
        rescanAspect.setModel(scanModel);

        smartFilter = new org.alfresco.rest.api.tests.client.data.Aspect();
        smartFilter.setId("test:smartFilter");
        smartFilter.setTitle("Smart filter");
        smartFilter.setDescription("Smart Filter");
        smartFilter.setParentId("mycompany:testAspect");
        smartFilter.setModel(scanModel);

        whitePaperType = new org.alfresco.rest.api.tests.client.data.Type();
        whitePaperType.setId("mycompany:whitepaper");
        whitePaperType.setTitle("whitepaper");
        whitePaperType.setDescription("Whitepaper");
        whitePaperType.setParentId("mycompany:doc");
        whitePaperType.setModel(myCompanyModel);

        docType = new org.alfresco.rest.api.tests.client.data.Type();
        docType.setId("mycompany:doc");
        docType.setTitle("doc");
        docType.setDescription("Doc");
        docType.setParentId("cm:content");
        docType.setModel(myCompanyModel);

        publishableType = new org.alfresco.rest.api.tests.client.data.Type();
        publishableType.setId("test:publishable");
        publishableType.setParentId("mycompany:doc");
    }

    @Override
    public String getScope()
    {
        return "public";
    }
}
