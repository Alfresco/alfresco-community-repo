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
package org.alfresco.rest.v0;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.rest.core.v0.BaseAPI;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

/**
 * Methods to make API requests using v0 API for Exporting Items
 *
 * @author Shubham Jain
 * @since 7.1.0
 */

@Component
public class ExportAPI extends BaseAPI
{
    /**
     * The URI to export an item
     */
    private static final String EXPORT_API = "{0}rma/admin/export";

    /**
     * Export a single Record/Record Folder/Record Category using V0 Export API
     *
     * @param user               User performing the export
     * @param password           User's Password
     * @param expectedStatusCode Expected Response Code
     * @param nodeID             ID of the Node(Record/RecordFolder) to be exported
     * @return HTTP Response
     */
    public HttpResponse exportRMNode(String user, String password, int expectedStatusCode, String nodeID)
    {
        return export(user, password, expectedStatusCode, Collections.singletonList(getNodeRefSpacesStore() + nodeID));
    }

    /**
     * Export a list of nodes using V0 Export API
     *
     * @param user               User performing the export
     * @param password           User's Password
     * @param expectedStatusCode Expected Response Code
     * @param nodeIDList         List of the nodes to be exported
     * @return HTTP Response
     */
    public HttpResponse exportRMNodes(String user, String password, int expectedStatusCode, List<String> nodeIDList)
    {

        List<String> nodeRefs =
                nodeIDList.stream().map(nodeID -> getNodeRefSpacesStore() + nodeID).collect(Collectors.toList());

        return export(user, password, expectedStatusCode, nodeRefs);
    }

    /**
     * Export API function to perform Export Operation on items with given noderefs using V0 Export Rest API
     *
     * @param user               User performing the export
     * @param password           User's Password
     * @param expectedStatusCode Expected Response Code
     * @param nodeRefs           list of the noderefs for the items to be exported
     * @return Rest API Post Request
     */
    public HttpResponse export(String user, String password, int expectedStatusCode, List<String> nodeRefs)
    {
        final JSONObject requestParams = new JSONObject();

        requestParams.put("nodeRefs", new JSONArray(nodeRefs));

        return doPostJsonRequest(user, password, expectedStatusCode, requestParams, EXPORT_API);
    }
}
