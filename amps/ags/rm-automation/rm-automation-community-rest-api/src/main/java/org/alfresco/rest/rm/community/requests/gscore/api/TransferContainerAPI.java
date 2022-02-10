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

package org.alfresco.rest.rm.community.requests.gscore.api;

import static org.alfresco.rest.core.RestRequest.requestWithBody;
import static org.alfresco.rest.core.RestRequest.simpleRequest;
import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryObject;
import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryString;
import static org.alfresco.rest.rm.community.util.PojoUtility.toJson;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;

import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.model.transfer.TransferCollection;
import org.alfresco.rest.rm.community.model.transfercontainer.TransferContainer;
import org.alfresco.rest.rm.community.requests.RMModelRequest;

/**
 * Transfer Container REST API Wrapper
 *
 * @author Silviu Dinuta
 * @since 2.6
 */
public class TransferContainerAPI extends RMModelRequest
{
    /**
     * @param rmRestWrapper RM REST Wrapper
     */
    public TransferContainerAPI(RMRestWrapper rmRestWrapper)
    {
        super(rmRestWrapper);
    }

    /**
     * see {@link #getTransferContainer(String, String)}
     */
    public TransferContainer getTransferContainer(String transferContainerId)
    {
        mandatoryString("transferContainerId", transferContainerId);

        return getTransferContainer(transferContainerId, EMPTY);
    }

    /**
     * Gets a transfer container.
     *
     * @param transferContainerId The identifier of a transfer container
     * @param parameters The URL parameters to add
     * @return The {@link TransferContainer} for the given {@code transferContainerId}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>{@code transferContainerId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to read {@code transferContainerId}</li>
     *  <li>{@code transferContainerId} does not exist</li>
     * </ul>
     */
    public TransferContainer getTransferContainer(String transferContainerId, String parameters)
    {
        mandatoryString("transferContainerId", transferContainerId);

        return getRmRestWrapper().processModel(TransferContainer.class, simpleRequest(
                GET,
                "/transfer-containers/{transferContainerId}?{parameters}",
                transferContainerId,
                parameters
        ));
    }

    /**
     * see {@link #updateTransferContainer(TransferContainer, String, String)
     */
    public TransferContainer updateTransferContainer(TransferContainer transferContainerModel, String transferContainerId)
    {
        mandatoryObject("transferContainerModel", transferContainerModel);
        mandatoryString("transferContainerId", transferContainerId);

        return updateTransferContainer(transferContainerModel, transferContainerId, EMPTY);
    }

    /**
     * Updates a transfer container.
     *
     * @param transferContainerModel The transfer container model which holds the information
     * @param transferContainerId The identifier of a transfer container
     * @param parameters The URL parameters to add
     * @param returns The updated {@link TransferContainer}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>the update request is invalid or {@code transferContainerId} is not a valid format or {@code transferContainerModel} is invalid</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to update {@code transferContainerId}</li>
     *  <li>{@code transferContainerId} does not exist</li>
     *  <li>the updated name clashes with an existing transfer container in the current file plan</li>
     *  <li>model integrity exception, including transfer container name with invalid characters</li>
     * </ul>
     */
    public TransferContainer updateTransferContainer(TransferContainer transferContainerModel, String transferContainerId, String parameters)
    {
        mandatoryObject("transferContainerModel", transferContainerModel);
        mandatoryString("transferContainerId", transferContainerId);

        return getRmRestWrapper().processModel(TransferContainer.class, requestWithBody(
                PUT,
                toJson(transferContainerModel),
                "transfer-containers/{transferContainerId}?{parameters}",
                transferContainerId,
                parameters
        ));
    }

    /**
     * see {@link #getTransfers(String, String)}
     */
    public TransferCollection getTransfers(String transferContainerId)
    {
        mandatoryString("transferContainerId", transferContainerId);

        return getTransfers(transferContainerId, EMPTY);
    }

    /**
     * Gets the children (transfers) of a transfer container.
     *
     * @param transferContainerId The identifier of a transfer container
     * @param parameters The URL parameters to add
     * @return The {@link TransferCollection} for the given {@code transferContainerId}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to read {@code transferContainerId}</li>
     *  <li>{@code filePlanId} does not exist</li>
     *</ul>
     */
    public TransferCollection getTransfers(String transferContainerId, String parameters)
    {
        mandatoryString("transferContainerId", transferContainerId);

        return getRmRestWrapper().processModels(TransferCollection.class, simpleRequest(
            GET,
            "transfer-containers/{filePlanId}/transfers?{parameters}",
            transferContainerId,
            parameters
        ));
    }
}
