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

import java.text.MessageFormat;
import java.util.List;

import org.alfresco.rest.core.v0.BaseAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The v0 REST API for copy-to (which supports multi-item copy).
 *
 * @author Tom Page
 * @since 2.6
 */
@Component
public class CopyToAPI extends BaseAPI
{
    /** Logger for the class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CopyToAPI.class);
    /** The URI for the copy-to API. */
    private static final String COPY_TO_API = "{0}doclib/action/copy-to/node/{1}";

    /**
     * Copy a list of nodes to the target container.
     *
     * @param user The username of the user to use.
     * @param password The password of the user.
     * @param targetContainerPath The destination to copy the nodes to. This should be in the format
     * "{site}/{container}/{path}", "{site}/{container}", "{store_type}/{store_id}/{id}/{path}",
     * "{store_type}/{store_id}/{id}" or "{store_type}/{store_id}".
     * @param nodeRefs The list of nodes to copy.
     * @return true if the request was successful.
     */
    public boolean copyTo(String user, String password, String targetContainerPath, List<String> nodeRefs)
    {
        try
        {
            JSONObject requestParams = new JSONObject();
            requestParams.put("nodeRefs", new JSONArray(nodeRefs));

            return doSlingshotPostJsonRequest(user, password, requestParams,
                        MessageFormat.format(COPY_TO_API, "{0}", targetContainerPath));
        }
        catch (JSONException error)
        {
            LOGGER.error("Unable to extract response parameter", error);
        }
        return false;
    }
}
