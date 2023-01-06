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
package org.alfresco.rest;

import org.alfresco.rest.model.RestNetworkModel;
import org.alfresco.utility.model.UserModel;

public abstract class NetworkDataPrep extends RestTest
{
    protected static UserModel adminUserModel;
    protected static UserModel adminTenantUser, secondAdminTenantUser;
    protected static UserModel tenantUser, secondTenantUser, differentNetworkTenantUser;
    protected static UserModel tenantUserWithBad;
    protected static UserModel userModel;
    protected static RestNetworkModel restNetworkModel;
    protected static String tenantDomain;
    private static boolean isInitialized = false;

    public void init()
    {
        if(!isInitialized)
        {
            isInitialized = true;
            initialization();
        }
    }

    public void initialization()
    {
        adminUserModel = dataUser.getAdminUser();
        //create first tenant Admin User.
        adminTenantUser = UserModel.getAdminTenantUser();
        restClient.authenticateUser(adminUserModel);
            restClient.usingTenant().createTenant(adminTenantUser);

        tenantUser = dataUser.usingUser(adminTenantUser).createUserWithTenant("uTenant");
        secondTenantUser = dataUser.usingUser(adminTenantUser).createUserWithTenant("sTenant");
        //create second tenant Admin User.
        secondAdminTenantUser = UserModel.getAdminTenantUser();
            restClient.usingTenant().createTenant(secondAdminTenantUser);

        tenantDomain = tenantUser.getDomain();
        differentNetworkTenantUser = dataUser.usingUser(secondAdminTenantUser).createUserWithTenant("dTenant");

        userModel = dataUser.createRandomTestUser();
    }
}
