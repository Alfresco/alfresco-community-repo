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

import org.alfresco.rest.api.tests.AbstractBaseApiTest;
import org.alfresco.rest.api.tests.util.JacksonUtil;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.junit.After;
import org.junit.Before;

/**
 * @author Gethin James
 * @author janv
 */
public class AbstractSingleNetworkSiteTest extends AbstractBaseApiTest
{
    protected String tSiteId;
    protected String tDocLibNodeId;

    protected JacksonUtil jacksonUtil;

    // TODO make this a runtime option to allow creation of non-default network
    protected final static boolean useDefaultNetwork = true;
    
    @Override
    public String getScope()
    {
        return "public";
    }
    
    @Override
    @Before
    public void setup() throws Exception
    {
        jacksonUtil = new JacksonUtil(applicationContext.getBean("jsonHelper", JacksonHelper.class));

        // createTestData=false
        getTestFixture(false);
        
        if (! useDefaultNetwork)
        {
            networkOne = getRepoService().createNetwork(this.getClass().getName().toLowerCase(), true);
            networkOne.create();
        }
        else 
        {
            networkOne = getRepoService().getSystemNetwork();
        }
        
        user1 = createUser("user1-" + RUNID, "user1Password", networkOne);
        user2 = createUser("user2-" + RUNID, "user2Password", networkOne);

        // to enable admin access via test calls - eg. via PublicApiClient -> AbstractTestApi -> findUserByUserName
        getOrCreateUser("admin", "admin");

        // used-by teardown to cleanup
        authenticationService = applicationContext.getBean("authenticationService", MutableAuthenticationService.class);
        personService = applicationContext.getBean("personService", PersonService.class);
        
        users.add(user1);
        users.add(user2);
        
        setRequestContext(networkOne.getId(), user1, null);
        
        tSiteId = createSite("Test Site - " + System.currentTimeMillis(), SiteVisibility.PRIVATE).getId();
        tDocLibNodeId = getSiteContainerNodeId(tSiteId, "documentLibrary");

        setRequestContext(null);
    }

    @Override
    @After
    public void tearDown() throws Exception
    {
        setRequestContext(networkOne.getId(), user1, null);
        deleteSite(tSiteId, 204); // TODO permanent=true
        
        super.tearDown();
    }
}
