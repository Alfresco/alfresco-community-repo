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

package org.alfresco.rest;

import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsStringNonNull;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.rest.api.tests.AbstractBaseApiTest;
import org.alfresco.rest.api.tests.RepoService;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.data.ContentInfo;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.util.JacksonUtil;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.junit.After;
import org.junit.Before;

/**
 * @author Gethin James
 */
public class AbstractSingleNetworkSiteTest extends AbstractBaseApiTest
{
    protected MutableAuthenticationService authenticationService;
    protected PersonService personService;

    protected RepoService.TestNetwork networkOne;
    protected RepoService.TestPerson u1;
    protected RepoService.TestSite tSite;
    protected NodeRef docLibNodeRef;

    protected JacksonUtil jacksonUtil;

    @Override
    public String getScope()
    {
        return "public";
    }

    @Before
    public void setup() throws Exception
    {
        authenticationService = applicationContext.getBean("authenticationService", MutableAuthenticationService.class);
        personService = applicationContext.getBean("personService", PersonService.class);

        jacksonUtil = new JacksonUtil(applicationContext.getBean("jsonHelper", JacksonHelper.class));

        networkOne = getTestFixture().getRandomNetwork();
        u1 = networkOne.createUser();
        tSite = createSite(networkOne, u1, SiteVisibility.PRIVATE);

        AuthenticationUtil.setFullyAuthenticatedUser(u1.getId());
        docLibNodeRef = tSite.getContainerNodeRef("documentLibrary");
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    @After
    public void tearDown() throws Exception
    {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                if (personService.personExists(u1.getId()))
                {
                    authenticationService.deleteAuthentication(u1.getId());
                    personService.deletePerson(u1.getId());
                }
                return null;
            }
        });
        AuthenticationUtil.clearCurrentSecurityContext();
    }


    protected Document createDocument(Folder parentFolder, String docName) throws Exception
    {
        Document d1 = new Document();
        d1.setName(docName);
        d1.setNodeType("cm:content");
        ContentInfo ci = new ContentInfo();
        ci.setMimeType("text/plain");
        d1.setContent(ci);

        // create empty file
        HttpResponse response = post(getNodeChildrenUrl(parentFolder.getId()), u1.getId(), toJsonAsStringNonNull(d1), 201);
        return RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
    }
}
