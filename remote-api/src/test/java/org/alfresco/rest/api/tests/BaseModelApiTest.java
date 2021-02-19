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

import org.alfresco.rest.api.model.Association;
import org.alfresco.rest.api.model.AssociationSource;
import org.alfresco.rest.api.model.Model;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.junit.Before;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Arrays;

public class BaseModelApiTest extends AbstractBaseApiTest
{
    PublicApiClient.ListResponse<org.alfresco.rest.api.tests.client.data.Aspect> aspects = null;
    org.alfresco.rest.api.tests.client.data.Aspect aspect = null, childAspect = null, smartFilterAspect = null,
            rescanAspect = null, testAspect = null, testAllAspect = null;

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
        testAspect.setContainer(false);
        testAspect.setIncludedInSupertypeQuery(true);
        testAspect.setArchive(true);

        childAspect = new org.alfresco.rest.api.tests.client.data.Aspect();
        childAspect.setId("mycompany:childAspect");
        childAspect.setTitle("Child Aspect");
        childAspect.setDescription("Child Aspect Description");
        childAspect.setParentId("smf:smartFolder");
        childAspect.setModel(myCompanyModel);
        childAspect.setContainer(false);
        childAspect.setIncludedInSupertypeQuery(true);

        rescanAspect = new org.alfresco.rest.api.tests.client.data.Aspect();
        rescanAspect.setId("test:rescan");
        rescanAspect.setTitle("rescan");
        rescanAspect.setDescription("Doc that required to scan ");
        rescanAspect.setModel(scanModel);
        rescanAspect.setContainer(false);
        rescanAspect.setIncludedInSupertypeQuery(true);

        smartFilterAspect = new org.alfresco.rest.api.tests.client.data.Aspect();
        smartFilterAspect.setId("test:smartFilter");
        smartFilterAspect.setTitle("Smart filter");
        smartFilterAspect.setDescription("Smart Filter");
        smartFilterAspect.setParentId("mycompany:testAspect");
        smartFilterAspect.setModel(scanModel);
        smartFilterAspect.setContainer(false);
        smartFilterAspect.setArchive(true);
        smartFilterAspect.setIncludedInSupertypeQuery(true);

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

        Model testModel = new Model();
        testModel.setAuthor("Administrator");
        testModel.setId("api:apiModel");
        testModel.setNamespaceUri("http://www.api.t2/model/1.0");
        testModel.setNamespacePrefix("test2");
        AssociationSource source =  new AssociationSource(null, "test2:aspect-all", true, true, null);
        AssociationSource target =  new AssociationSource(null, "api:referenceable", false, false, false);
        Association expectedAssociation = new Association("api:assoc-all", null, null, false, null, source, target);
        testAllAspect = new org.alfresco.rest.api.tests.client.data.Aspect();
        testAllAspect.setId("test2:aspect-all");
        testAllAspect.setTitle("Aspect derived from other namespace");
        testAllAspect.setArchive(false);
        testAllAspect.setIncludedInSupertypeQuery(false);
        testAllAspect.setContainer(false);
        testAllAspect.setModel(testModel);
        testAllAspect.setAssociations(Collections.singletonList(expectedAssociation));
        testAllAspect.setMandatoryAspects(Arrays.asList("test2:aspect-three", "api:aspect-one", "api:aspect-two"));
    }

    @Override
    public String getScope()
    {
        return "public";
    }
}
