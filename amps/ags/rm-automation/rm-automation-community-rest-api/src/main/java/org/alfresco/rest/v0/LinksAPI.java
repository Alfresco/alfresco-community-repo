/*-
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import org.alfresco.rest.core.v0.BaseAPI;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;

/**
 * Methods to make API requests using v0 API for Linking Records
 *
 * @author Kavit Shah
 * @since 3.2
 */
@Component
public class LinksAPI extends BaseAPI {

    private static final String LINK_API = "{0}doclib/action/rm-link/site/rm/documentLibrary/{1}";

    /**
     * Creates the Link
     *
     * @param user The username of the user to use.
     * @param password The password of the user.
     * @param expectedStatusCode The expected return status code.
     * @param sourcePath The Source of link the record. This should be in the format
     * "{site}/{container}/{path}", "{site}/{container}", "{store_type}/{store_id}/{id}/{path}",
     * "{store_type}/{store_id}/{id}" or "{store_type}/{store_id}".
     * @param nodeRefs The Node that needs to be linked.
     * @return The HTTP Response.
     * @throws AssertionError If the API didn't return the expected status code.
     */
    public HttpResponse linkRecord(String user, String password, int expectedStatusCode, String sourcePath, List<String> nodeRefs) throws UnsupportedEncodingException {
        JSONObject requestParams = new JSONObject();
        requestParams.put("nodeRefs", new JSONArray(nodeRefs));

        return doSlingshotPostJsonRequest(user, password, expectedStatusCode, requestParams,
            MessageFormat.format(LINK_API, "{0}", sourcePath));
    }

}
