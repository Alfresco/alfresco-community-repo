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

    public void init() throws Exception
    {
        if(!isInitialized)
        {
            isInitialized = true;
            initialization();
        }
    }

    public void initialization() throws Exception
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
