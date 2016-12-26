/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.rm.community.base;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.requests.igCoreAPI.FilePlanComponentAPI;
import org.alfresco.rest.rm.community.requests.igCoreAPI.RMSiteAPI;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 * Abstract class to hold the REST APIs
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public abstract class BaseRESTAPI extends RestTest
{
    @Autowired
    private RMRestWrapper rmRestWrapper;

    @Autowired
    private DataUser dataUser;

    /**
     * The RM REST Wrapper
     *
     * @return the rmRestWrapper
     */
    protected RMRestWrapper getRmRestWrapper()
    {
        return this.rmRestWrapper;
    }

    /**
     * The data user service
     *
     * @return the dataUser
     */
    protected DataUser getDataUser()
    {
        return this.dataUser;
    }

    /**
     * Compares the given {@link HttpStatus} code with the response status code
     *
     * @param httpStatus The {@link HttpStatus} to assert
     */
    protected void assertStatusCode(HttpStatus httpStatus)
    {
        getRmRestWrapper().assertStatusCodeIs(httpStatus);
    }

    /**
     * Gets the admin user
     *
     * @return The admin user
     */
    protected UserModel getAdminUser()
    {
        return getDataUser().getAdminUser();
    }

    /**
     * Gets the {@link RMSiteAPI} as the admin user
     *
     * @return {@link RMSiteAPI} with the admin credentials
     */
    protected RMSiteAPI getRMSiteAPI()
    {
        return getRMSiteAPI(getAdminUser());
    }

    /**
     * Gets the {@link RMSiteAPI} as the given user
     *
     * @param userModel The user model whose credentials will be used to get the API
     * @return {@link RMSiteAPI} with user credentials
     */
    protected RMSiteAPI getRMSiteAPI(UserModel userModel)
    {
        getRmRestWrapper().authenticateUser(userModel);
        return getRmRestWrapper().withIGCoreAPI().usingRMSite();
    }

    /**
     * Gets the {@link FilePlanComponentAPI} as the admin user
     *
     * @return {@link FilePlanComponentAPI} with the admin credentials
     */
    protected FilePlanComponentAPI getFilePlanComponentsAPI()
    {
        return getFilePlanComponentsAPI(getAdminUser());
    }

    /**
     * Get the {@link FilePlanComponentAPI} as the given user
     *
     * @param userModel The user model whose credentials will be used to get the API
     * @return {@link FilePlanComponentAPI} with the user credentials
     */
    protected FilePlanComponentAPI getFilePlanComponentsAPI(UserModel userModel)
    {
        getRmRestWrapper().authenticateUser(userModel);
        return getRmRestWrapper().withIGCoreAPI().usingFilePlanComponents();
    }
}
