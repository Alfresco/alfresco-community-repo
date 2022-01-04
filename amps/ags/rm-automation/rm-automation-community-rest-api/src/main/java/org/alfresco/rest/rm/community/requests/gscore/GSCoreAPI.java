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
package org.alfresco.rest.rm.community.requests.gscore;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;

import io.restassured.RestAssured;

import org.alfresco.rest.core.RMRestProperties;
import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.requests.gscore.api.ActionsExecutionAPI;
import org.alfresco.rest.rm.community.requests.RMModelRequest;
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

/**
 * Defines the entire GS Core API
 * {@link http://host:port/gs-api-explorer} select "GS Core API"
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public class GSCoreAPI extends RMModelRequest
{
    /**
     * Constructor
     *
     * @param rmRestWrapper RM REST Wrapper
     * @param rmRestProperties RM REST Properties
     */
    public GSCoreAPI(RMRestWrapper rmRestWrapper, RMRestProperties rmRestProperties)
    {
        super(rmRestWrapper);
        RestAssured.baseURI = format("%s://%s", rmRestProperties.getScheme(), rmRestProperties.getServer());
        RestAssured.port = parseInt(rmRestProperties.getPort());
        RestAssured.basePath = rmRestProperties.getRestRmPath();
        restWrapper.configureRequestSpec().setBasePath(RestAssured.basePath);
    }

    /**
     * Provides DSL on all REST calls under <code>ig-sites/rm/...</code> API path
     *
     * @return {@link RMSiteAPI}
     */
    public RMSiteAPI usingRMSite()
    {
      return new RMSiteAPI(getRmRestWrapper());
    }

    /**
     * Provides DSL on all REST calls under <code>file-plans/...</code> API path
     *
     * @return {@link FilePlanAPI}
     */
    public FilePlanAPI usingFilePlans()
    {
        return new FilePlanAPI(getRmRestWrapper());
    }

    /**
     * Provides DSL on all REST calls under <code>record-categories/...</code> API path
     *
     * @return {@link RecordCategoryAPI}
     */
    public RecordCategoryAPI usingRecordCategory()
    {
        return new RecordCategoryAPI(getRmRestWrapper());
    }

    /**
     * Provides DSL on all REST calls under <code>record-folders/...</code> API path
     *
     * @return {@link RecordFolderAPI}
     */
    public RecordFolderAPI usingRecordFolder()
    {
        return new RecordFolderAPI(getRmRestWrapper());
    }

    /**
     * Provides DSL on all REST calls under <code>records/...</code> API path
     *
     * @return {@link RecordsAPI}
     */
    public RecordsAPI usingRecords()
    {
        return new RecordsAPI(getRmRestWrapper());
    }

    /**
     * Provides DSL on all REST calls under <code>files/...</code> API path
     *
     * @return {@link FilesAPI}
     */
    public FilesAPI usingFiles()
    {
        return new FilesAPI(getRmRestWrapper());
    }

    /**
     * Provides DSL on all REST calls under <code>transfer-containers/...</code> API path
     *
     * @return {@link TransferContainerAPI}
     */
    public TransferContainerAPI usingTransferContainer()
    {
        return new TransferContainerAPI(getRmRestWrapper());
    }

    /**
     * Provides DSL on all REST calls under <code>transfers/...</code> API path
     *
     * @return {@link TransferAPI}
     */
    public TransferAPI usingTransfer()
    {
        return new TransferAPI(getRmRestWrapper());
    }

    /**
     * Provides DSL for RM unfiled container API
     *
     * @return {@link UnfiledContainerAPI}
     */
    public UnfiledContainerAPI usingUnfiledContainers()
    {
        return new UnfiledContainerAPI(getRmRestWrapper());
    }

    /**
     * Provides DSL for RM unfiled record folders API
     *
     * @return {@link UnfiledRecordFolderAPI}
     */
    public UnfiledRecordFolderAPI usingUnfiledRecordFolder()
    {
        return new UnfiledRecordFolderAPI(getRmRestWrapper());
    }

    /**
     * Provides DSL for RM user management API
     *
     * @return {@link RMUserAPI}
     */
    public RMUserAPI usingRMUser()
    {
        return new RMUserAPI(getRmRestWrapper());
    }

    /**
     * Provides DSL for ActionExecution API
     *
     * @return {@link ActionsExecutionAPI}
     */
    public ActionsExecutionAPI usingActionsExecutionsAPI()
    {
        return new ActionsExecutionAPI(getRmRestWrapper());
    }
}
