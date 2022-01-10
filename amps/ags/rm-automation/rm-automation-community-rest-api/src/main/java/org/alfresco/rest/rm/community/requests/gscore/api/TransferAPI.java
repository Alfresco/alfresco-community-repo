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

import static org.alfresco.rest.core.RestRequest.simpleRequest;
import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryString;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpMethod.GET;

import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.model.transfer.Transfer;
import org.alfresco.rest.rm.community.model.transfer.TransferChildCollection;
import org.alfresco.rest.rm.community.requests.RMModelRequest;

/**
 * Transfer REST API Wrapper
 *
 * @author Silviu Dinuta
 * @since 2.6
 */
public class TransferAPI extends RMModelRequest
{
    /**
     * @param rmRestWrapper RM REST Wrapper
     */
    public TransferAPI(RMRestWrapper rmRestWrapper)
    {
        super(rmRestWrapper);
    }

    /**
     * see {@link #getTransfer(String, String)}
     */
    public Transfer getTransfer(String transferId)
    {
        mandatoryString("transferId", transferId);

        return getTransfer(transferId, EMPTY);
    }

    /**
     * Gets a transfer.
     *
     * @param transferId The identifier of a transfer
     * @param parameters The URL parameters to add
     * @return The {@link Transfer} for the given {@code transferId}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>{@code transferId} is not a valid format</li>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to read {@code transferId}</li>
     *  <li>{@code transferId} does not exist</li>
     * </ul>
     */
    public Transfer getTransfer(String transferId, String parameters)
    {
        mandatoryString("transferId", transferId);

        return getRmRestWrapper().processModel(Transfer.class, simpleRequest(
                GET,
                "/transfers/{transferId}?{parameters}",
                transferId,
                parameters
        ));
    }
    /**
     * see {@link #getTransfersChildren(String, String)}
     */
    public TransferChildCollection getTransfersChildren(String transferId)
    {
        mandatoryString("transferId", transferId);

        return getTransfersChildren(transferId, EMPTY);
    }

    /**
     * Gets the children (record folder or record) of a transfer.
     *
     * @param transferId The identifier of a transfer
     * @param parameters The URL parameters to add
     * @return The {@link TransferChildCollection} for the given {@code transferId}
     * @throws RuntimeException for the following cases:
     * <ul>
     *  <li>authentication fails</li>
     *  <li>current user does not have permission to read {@code transferId}</li>
     *  <li>{@code filePlanId} does not exist</li>
     *</ul>
     */
    public TransferChildCollection getTransfersChildren(String transferId, String parameters)
    {
        mandatoryString("transferId", transferId);

        return getRmRestWrapper().processModels(TransferChildCollection.class, simpleRequest(
            GET,
            "transfers/{filePlanId}/children?{parameters}",
            transferId,
            parameters
        ));
    }
}
