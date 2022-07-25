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
import org.testng.annotations.Test;

/**
 * 
 * Demo workshop for RestAPI test
 *
 */
public class RestApiWorkshopTests extends RestTest
{    
    @Test(groups = { "demo" })
    public void verifyGetSitesRestApiCall()
    {
        // creating a random user in repository
       
        // create a new random site using your UserModel from above

        // using "siteApi", call get "/sites" Rest API and verify created site is present
        
        // verify status is OK 

    }
    
    @Test(groups = { "demo" })
    public void verifyGetASiteRestApiCall()
    {
        // creating a random user in repository
       
        // create a new random site using your UserModel from above

        // using "siteApi", call get "/sites/{siteId}" Rest API
        // using "siteApi", verify created site is present

        // verify status is OK 
    }
}
