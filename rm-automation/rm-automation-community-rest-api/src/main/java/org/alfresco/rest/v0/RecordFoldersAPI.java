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
package org.alfresco.rest.v0;

import org.alfresco.dataprep.ContentService;
import org.alfresco.rest.core.v0.BaseAPI;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Methods to make API requests using v0 API on records folders
 *
 * @author Oana Nechiforescu
 * @since 2.5
 */
@Component
public class RecordFoldersAPI extends BaseAPI
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordFoldersAPI.class);

    @Autowired
    private ContentService contentService;

    /**
     * Close the record folder
     *
     * @param user         the user closing the folder
     * @param password     the user's password
     * @param recordFolder the record folder name
     * @return true if the action completed successfully
     */
    public boolean closeRecordFolder(String user, String password, String recordFolder)
    {
        String recNodeRef = NODE_REF_WORKSPACE_SPACES_STORE + contentService.getNodeRef(user, password, RM_SITE_ID, recordFolder);

        try
        {
            JSONObject requestParams = new JSONObject();
            requestParams.put("name", "closeRecordFolder");
            requestParams.put("nodeRef", recNodeRef);

            return doPostJsonRequest(user, password, requestParams, RM_ACTIONS_API);
        }
        catch (JSONException error)
        {
            LOGGER.error("Unable to extract response parameter", error);
        }
        return false;
    }
}
