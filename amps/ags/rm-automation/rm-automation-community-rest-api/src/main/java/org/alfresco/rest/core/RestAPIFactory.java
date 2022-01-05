/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

import static lombok.AccessLevel.PROTECTED;

import javax.annotation.Resource;

import lombok.Getter;
import lombok.Setter;
import org.alfresco.rest.requests.Node;
import org.alfresco.rest.requests.coreAPI.RestCoreAPI;
import org.alfresco.rest.requests.search.SearchAPI;
import org.alfresco.rest.rm.community.requests.gscore.GSCoreAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.ActionsExecutionAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.FilePlanAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.FilesAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RMSiteAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RMUserAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordCategoryAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordsAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.TransferAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.TransferContainerAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledContainerAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledRecordFolderAPI;
import org.alfresco.utility.data.DataUserAIS;
import org.alfresco.utility.model.RepoTestModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * REST API Factory which provides access to the APIs
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
@Service
@Scope(value = "prototype")
public class RestAPIFactory
{
    @Autowired
    @Getter (value = PROTECTED)
    private DataUserAIS dataUser;

    @Resource(name = "RMRestWrapper")
    @Getter
    @Setter
    private RMRestWrapper rmRestWrapper;

    private GSCoreAPI getGSCoreAPI(UserModel userModel)
    {
        getRmRestWrapper().authenticateUser(userModel != null ? userModel : getDataUser().getAdminUser());
        return getRmRestWrapper().withGSCoreAPI();
    }

    private RestCoreAPI getCoreAPI(UserModel userModel)
    {
        getRmRestWrapper().authenticateUser(userModel != null ? userModel : getDataUser().getAdminUser());
        return getRmRestWrapper().withCoreAPI();
    }

    public SearchAPI getSearchAPI(UserModel userModel)
    {
        getRmRestWrapper().authenticateUser(userModel != null ? userModel : getDataUser().getAdminUser());
        return getRmRestWrapper().withSearchAPI();
    }

    /**
     * When no user is given the default is set to admin
     */
    public SearchAPI getSearchAPI()
    {
        return getSearchAPI(null);
    }

    public Node getNodeAPI(RepoTestModel model) throws RuntimeException
    {
        try
        {
            return getCoreAPI(null).usingNode(model);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to load nodeAPI.", e);
        }
    }

    public Node getNodeAPI(UserModel userModel, RepoTestModel model) throws RuntimeException
    {
        try
        {
            return getCoreAPI(userModel).usingNode(model);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to load nodeAPI.", e);
        }
    }

    public RMSiteAPI getRMSiteAPI()
    {
        return getGSCoreAPI(null).usingRMSite();
    }

    public RMSiteAPI getRMSiteAPI(UserModel userModel)
    {
        return getGSCoreAPI(userModel).usingRMSite();
    }

    public FilePlanAPI getFilePlansAPI()
    {
        return getGSCoreAPI(null).usingFilePlans();
    }

    public FilePlanAPI getFilePlansAPI(UserModel userModel)
    {
        return getGSCoreAPI(userModel).usingFilePlans();
    }

    public RecordCategoryAPI getRecordCategoryAPI()
    {
        return getGSCoreAPI(null).usingRecordCategory();
    }

    public RecordCategoryAPI getRecordCategoryAPI(UserModel userModel)
    {
        return getGSCoreAPI(userModel).usingRecordCategory();
    }

    public RecordFolderAPI getRecordFolderAPI()
    {
        return getGSCoreAPI(null).usingRecordFolder();
    }

    public RecordFolderAPI getRecordFolderAPI(UserModel userModel)
    {
        return getGSCoreAPI(userModel).usingRecordFolder();
    }

    public RecordsAPI getRecordsAPI()
    {
        return getGSCoreAPI(null).usingRecords();
    }

    public RecordsAPI getRecordsAPI(UserModel userModel)
    {
        return getGSCoreAPI(userModel).usingRecords();
    }

    public FilesAPI getFilesAPI()
    {
        return getGSCoreAPI(null).usingFiles();
    }

    public FilesAPI getFilesAPI(UserModel userModel)
    {
        return getGSCoreAPI(userModel).usingFiles();
    }

    public TransferContainerAPI getTransferContainerAPI()
    {
        return getGSCoreAPI(null).usingTransferContainer();
    }

    public TransferContainerAPI getTransferContainerAPI(UserModel userModel)
    {
        return getGSCoreAPI(userModel).usingTransferContainer();
    }

    public TransferAPI getTransferAPI()
    {
        return getGSCoreAPI(null).usingTransfer();
    }

    public TransferAPI getTransferAPI(UserModel userModel)
    {
        return getGSCoreAPI(userModel).usingTransfer();
    }

    public RMUserAPI getRMUserAPI()
    {
        return getGSCoreAPI(null).usingRMUser();
    }

    public RMUserAPI getRMUserAPI(UserModel userModel)
    {
        return getGSCoreAPI(userModel).usingRMUser();
    }

    public UnfiledContainerAPI getUnfiledContainersAPI()
    {
        return getGSCoreAPI(null).usingUnfiledContainers();
    }

    public UnfiledContainerAPI getUnfiledContainersAPI(UserModel userModel)
    {
        return getGSCoreAPI(userModel).usingUnfiledContainers();
    }

    public UnfiledRecordFolderAPI getUnfiledRecordFoldersAPI()
    {
        return getGSCoreAPI(null).usingUnfiledRecordFolder();
    }

    public UnfiledRecordFolderAPI getUnfiledRecordFoldersAPI(UserModel userModel)
    {
        return getGSCoreAPI(userModel).usingUnfiledRecordFolder();
    }

    public ActionsExecutionAPI getActionsAPI(UserModel userModel)
    {
        return getGSCoreAPI(userModel).usingActionsExecutionsAPI();
    }

    public ActionsExecutionAPI getActionsAPI()
    {
        return getGSCoreAPI(null).usingActionsExecutionsAPI();
    }
}
