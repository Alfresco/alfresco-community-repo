/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.requests.gscore.api;

import com.google.common.collect.ImmutableMap;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.requests.ModelRequest;
import org.alfresco.rest.rm.community.model.rules.ActionsOnRule;
import org.alfresco.utility.model.RepoTestModel;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

/**
 * Produces processed results from Core Actions API calls
 *
 * @author Claudia Agache
 * @since 3.1
 */
@Component
public class ActionsExecutionAPI extends ModelRequest
{

    /**
     * @param rmRestWrapper
     */
    public ActionsExecutionAPI(RestWrapper rmRestWrapper)
    {
        super(rmRestWrapper);
    }


    /**
     * Declares and files a document as record to a record folder using v1 actions api
     *
     * @param targetNode      the node on which the action is executed
     * @param destinationPath the path to the record folder
     * @throws Exception
     */
    public JSONObject declareAndFile(RepoTestModel targetNode, String destinationPath) throws Exception
    {
       return restWrapper.withCoreAPI().usingActions()
                     .executeAction(ActionsOnRule.DECLARE_AS_RECORD.getActionValue(), targetNode,
                             ImmutableMap.of("path", destinationPath));

    }

    /**
     * Declares a document as record using v1 actions api
     *
     * @param targetNode the node on which the action is executed
     * @throws Exception
     */
    public JSONObject declareAsRecord(RepoTestModel targetNode) throws Exception
    {
        return restWrapper.withCoreAPI().usingActions()
                                 .executeAction(ActionsOnRule.DECLARE_AS_RECORD.getActionValue(), targetNode);
    }
}
