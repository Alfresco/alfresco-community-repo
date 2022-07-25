/*-
 * #%L
 * alfresco-tas-restapi
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
package org.alfresco.rest.demo.workshop;

import org.alfresco.rest.RestTest;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.Test;

public class RestApiDemoTests extends RestTest
{    
    /*
     * Test steps:
     * 1. create a user
     * 2. create a site
     * 3. create a second user
     * 4. add the second user to site with a user role
     * 5. call rest api call " GET sites/{siteId}/members" with first user authenticated
     * Expected: the response contains the user added as a member to the site
     */
    @Test(groups = { "demo" })
    public void verifyGetSiteMembersRestApiCall()
    {
        UserModel user = dataUser.createRandomTestUser();
        SiteModel site = dataSite.usingUser(user).createPublicRandomSite();
        UserModel member = dataUser.createRandomTestUser();
        dataUser.usingUser(user).addUserToSite(member, site, UserRole.SiteCollaborator);
        
        //add here code for step 5 

    }

    /*
     * Test steps:
     * 1. create a user
     * 2. create a site
     * 3. create a second user
     * 4. add the second user to site with a user role
     * 5. call rest api call " GET sites/{siteId}/members/{personId}" with first user authenticated
     * Expected: the response contains the user added as a member to the site
     */

    @Test(groups = { "demo" })
    public void verifyGetASiteMemberApiCall()
    {
        UserModel user = dataUser.createRandomTestUser();
        SiteModel site = dataSite.usingUser(user).createPublicRandomSite();
        UserModel member = dataUser.createRandomTestUser();
        dataUser.usingUser(user).addUserToSite(member, site, UserRole.SiteCollaborator);

        //add here code for step 5
    }
}
