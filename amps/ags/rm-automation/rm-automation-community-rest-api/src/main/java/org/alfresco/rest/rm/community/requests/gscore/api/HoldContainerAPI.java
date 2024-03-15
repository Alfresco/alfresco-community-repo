/*-
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
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.model.hold.Hold;
import org.alfresco.rest.rm.community.model.hold.HoldCollection;
import org.alfresco.rest.rm.community.requests.RMModelRequest;

public class HoldContainerAPI extends RMModelRequest
{
    /**
     * @param rmRestWrapper
     */
    public HoldContainerAPI(RMRestWrapper rmRestWrapper)
    {
        super(rmRestWrapper);
    }

    public Hold createHold(Hold hold, String holdContainerId)
    {
        mandatoryObject("holdContainerId", holdContainerId);

        return getRmRestWrapper().processModel(Hold.class, requestWithBody(
            POST,
            toJson(hold),
            "holds-containers/{holdContainerId}/holds",
            holdContainerId
                                                                               ));
    }

    public HoldCollection getHolds(String holdContainerId, String parameters)
    {
        mandatoryString("holdContainerId", holdContainerId);

        return getRmRestWrapper().processModels(HoldCollection.class, simpleRequest(
            GET,
            "holds-containers/{holdContainerId}/holds?{parameters}",
            holdContainerId,
            parameters
                                                                                       ));
    }
}
