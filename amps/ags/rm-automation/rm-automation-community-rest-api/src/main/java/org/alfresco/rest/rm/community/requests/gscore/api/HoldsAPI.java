/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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

import static org.alfresco.rest.core.RestRequest.requestWithBody;
import static org.alfresco.rest.core.RestRequest.simpleRequest;
import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryObject;
import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryString;
import static org.alfresco.rest.rm.community.util.PojoUtility.toJson;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.model.hold.Hold;
import org.alfresco.rest.rm.community.model.hold.HoldChild;
import org.alfresco.rest.rm.community.model.hold.HoldChildCollection;
import org.alfresco.rest.rm.community.model.hold.HoldDeletionReason;
import org.alfresco.rest.rm.community.model.hold.HoldDeletionReasonEntry;
import org.alfresco.rest.rm.community.requests.RMModelRequest;

public class HoldsAPI extends RMModelRequest
{

    /**
     * @param rmRestWrapper
     */
    public HoldsAPI(RMRestWrapper rmRestWrapper)
    {
        super(rmRestWrapper);
    }


    public Hold getHold(String holdId)
    {
        mandatoryString("holdId", holdId);

        return getHold(holdId, EMPTY);
    }

    public Hold getHold(String holdId, String parameters)
    {
        mandatoryString("holdId", holdId);

        return getRmRestWrapper().processModel(Hold.class, simpleRequest(
            GET,
            "holds/{holdId}?{parameters}",
            holdId,
            parameters
                                                                        ));
    }

    public Hold updateHold(Hold holdModel, String holdId)
    {
        mandatoryObject("holdModel", holdModel);
        mandatoryString("holdId", holdId);

        return updateHold(holdModel, holdId, EMPTY);
    }

    public Hold updateHold(Hold holdModel, String holdId, String parameters)
    {
        mandatoryObject("holdModel", holdModel);
        mandatoryString("holdId", holdId);

        return getRmRestWrapper().processModel(Hold.class, requestWithBody(
            PUT,
            toJson(holdModel),
            "holds/{holdId}?{parameters}",
            holdId,
            parameters
                                                                            ));
    }

    public void deleteHold(String holdId)
    {
        mandatoryString("holdId", holdId);

        getRmRestWrapper().processEmptyModel(simpleRequest(
            DELETE,
            "holds/{holdId}",
            holdId
                                                          ));
    }

    public HoldDeletionReasonEntry deleteHoldWithReason(HoldDeletionReason reason, String holdId)
    {
        mandatoryObject("reason", reason);
        mandatoryString("holdId", holdId);

        return getRmRestWrapper().processModel(HoldDeletionReasonEntry.class, requestWithBody(
            PUT,
            toJson(reason),
            "holds/{holdId}/delete",
            holdId
                                                          ));
    }

    public HoldChild addChildToHold(HoldChild holdChild, String holdId)
    {
        mandatoryObject("holdId", holdId);

        return getRmRestWrapper().processModel(HoldChild.class, requestWithBody(
            POST,
            toJson(holdChild),
            "holds/{holdId}/children",
            holdId
                                                                          ));
    }

    public HoldChildCollection getChildren(String holdId, String parameters)
    {
        mandatoryString("holdId", holdId);

        return getRmRestWrapper().processModels(HoldChildCollection.class, simpleRequest(
            GET,
            "holds/{holdId}/children",
            holdId,
            parameters
                                                                                   ));
    }

    public void deleteHoldChild(String holdId, String holdChildId)
    {
        mandatoryString("holdId", holdId);
        mandatoryString("holdChildId", holdChildId);

        getRmRestWrapper().processEmptyModel(simpleRequest(
            DELETE,
            "holds/{holdId}/{holdChildId}",
            holdId
                                                          ));
    }
}
