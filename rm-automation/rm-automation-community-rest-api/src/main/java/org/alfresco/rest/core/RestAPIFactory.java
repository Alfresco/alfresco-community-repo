/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.rest.core;

import javax.annotation.Resource;

import org.alfresco.rest.rm.community.requests.igCoreAPI.FilePlanComponentAPI;
import org.alfresco.rest.rm.community.requests.igCoreAPI.FilesAPI;
import org.alfresco.rest.rm.community.requests.igCoreAPI.RMSiteAPI;
import org.alfresco.rest.rm.community.requests.igCoreAPI.RecordsAPI;
import org.alfresco.rest.rm.community.requests.igCoreAPI.RestIGCoreAPI;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * REST API Factory Implementation
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
@Service
@Scope(value = "prototype")
public class RestAPIFactory
{
    @Autowired
    private DataUser dataUser;

    @Resource(name = "RMRestWrapper")
    private RMRestWrapper rmRestWrapper;

    /**
     * @return the rmRestWrapper
     */
    public RMRestWrapper getRmRestWrapper()
    {
        return this.rmRestWrapper;
    }

    private RestIGCoreAPI getRestIGCoreAPI(UserModel userModel)
    {
        getRmRestWrapper().authenticateUser(userModel != null ? userModel : dataUser.getAdminUser());
        return getRmRestWrapper().withIGCoreAPI();
    }

    public RMSiteAPI getRMSiteAPI()
    {
        return getRestIGCoreAPI(null).usingRMSite();
    }

    public RMSiteAPI getRMSiteAPI(UserModel userModel)
    {
        return getRestIGCoreAPI(userModel).usingRMSite();
    }

    public FilePlanComponentAPI getFilePlanComponentsAPI()
    {
        return getRestIGCoreAPI(null).usingFilePlanComponents();
    }

    public FilePlanComponentAPI getFilePlanComponentsAPI(UserModel userModel)
    {
        return getRestIGCoreAPI(userModel).usingFilePlanComponents();
    }

    public RecordsAPI getRecordsAPI()
    {
        return getRestIGCoreAPI(null).usingRecords();
    }

    public RecordsAPI getRecordsAPI(UserModel userModel)
    {
        return getRestIGCoreAPI(userModel).usingRecords();
    }

    public FilesAPI getFilesAPI()
    {
        return getRestIGCoreAPI(null).usingFiles();
    }

    public FilesAPI getFilesAPI(UserModel userModel)
    {
        return getRestIGCoreAPI(userModel).usingFiles();
    }
}
