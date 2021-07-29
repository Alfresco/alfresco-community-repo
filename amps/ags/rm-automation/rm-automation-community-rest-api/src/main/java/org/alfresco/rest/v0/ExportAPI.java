/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.rest.v0;

import static org.alfresco.utility.report.log.Step.STEP;
import static org.apache.http.HttpStatus.SC_OK;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.core.v0.BaseAPI;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolder;
import org.alfresco.utility.report.log.Step;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

/**
 * Methods to make API requests using v0 API for Exporting a file
 *
 * @author Shubham Jain
 * @since 3.5.0
 */

@Component
public class ExportAPI extends BaseAPI
{
    /**
     * The URI to export an item
     */
    private static final String EXPORT_API = "{0}rma/admin/export";

    /**
     * List to store the Node References of items to be exported
     */
    private static List<String> nodeRefs = new ArrayList<>();

    /**
     * Export a single Record using V0 Export API
     *
     * @param user               User performing the export
     * @param password           User's Password
     * @param expectedStatusCode Expected Response Code
     * @param recordName         Name of the Record to be exported
     * @return exportAPI function
     */
    public HttpResponse exportRecord(String user, String password, int expectedStatusCode, String recordName)
    {
        final String recNodeRef = getNodeRefSpacesStore() + contentService.getNodeRef(user, password, RM_SITE_ID,
                recordName);

        nodeRefs.add(recNodeRef);

        return export(user, password, expectedStatusCode, nodeRefs);
    }

    /**
     * Export an array of records using V0 Export API
     *
     * @param user               User performing the export
     * @param password           User's Password
     * @param expectedStatusCode Expected Response Code
     * @param recordList         List of the records to be exported
     * @return exportAPI function
     */
    public HttpResponse exportRecords(String user, String password, int expectedStatusCode, List<Record> recordList)
    {

        for (int i = 0; i < recordList.size(); i++)
        {
            String recNodeRef = getNodeRefSpacesStore() + contentService.getNodeRef(user, password, RM_SITE_ID,
                    recordList.get(i).getName());

            nodeRefs.add(recNodeRef);
        }

        return export(user, password, expectedStatusCode, nodeRefs);
    }

    /**
     * Export a Record Folder containing records using V0 Export API
     *
     * @param user               User performing the export
     * @param password           User's Password
     * @param expectedStatusCode Expected Response Code
     * @param folderName         Name of the Record Folder to be exported
     * @return exportAPI function
     */
    public HttpResponse exportRecordFolder(String user, String password, int expectedStatusCode, String folderName)
    {

        final String recNodeRef = getNodeRefSpacesStore() + contentService.getNodeRef(user, password, RM_SITE_ID,
                folderName);

        nodeRefs.add(recNodeRef);

        return export(user, password, expectedStatusCode, nodeRefs);
    }

    /**
     * Export API function to perform Export Operation on items with given noderefs using V0 Export Rest API
     * @param user User performing the export
     * @param password User's Password
     * @param expectedStatusCode Expected Response Code
     * @param nodeRefs list of the noderefs for the items to be exported
     * @return  Rest API Post Request
     */
    public HttpResponse export(String user, String password, int expectedStatusCode, List nodeRefs)
    {

        final JSONObject requestParams = new JSONObject();

        requestParams.put("nodeRefs", new JSONArray(nodeRefs));

        nodeRefs.clear();

        return doPostJsonRequest(user, password, expectedStatusCode, requestParams, EXPORT_API);


    }


}
