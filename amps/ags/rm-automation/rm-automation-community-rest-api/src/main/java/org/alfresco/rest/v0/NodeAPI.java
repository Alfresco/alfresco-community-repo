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
import java.text.MessageFormat;

import org.alfresco.dataprep.AlfrescoHttpClient;
import org.alfresco.dataprep.AlfrescoHttpClientFactory;
import org.alfresco.rest.core.v0.BaseAPI;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The v0 REST API for nodes
 * 
 * @author jcule
 * @since 2.7EA1
 */
@Component
public class NodeAPI extends BaseAPI
{
    /** Logger for the class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeAPI.class);

    /** The URI for the get node API. */
    private static final String GET_NODE_API = "{0}alfresco/s/slingshot/doclib2/node/{1}";

    @Autowired
    private AlfrescoHttpClientFactory alfrescoHttpClientFactory;

    /**
     * Get the node metadata using the using the node data webscript:  Document List v2 Component
     * 
     * @param username
     * @param password
     * @param nodeId
     * @return
     */
    public JSONObject getNode(String username, String password, String nodeId)
    {
        String requestURL;
        AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();
        requestURL = MessageFormat.format(GET_NODE_API, client.getAlfrescoUrl(), NODE_PREFIX + nodeId);
        client.close();
        return doGetRequest(username, password, requestURL);
    }
    
}
