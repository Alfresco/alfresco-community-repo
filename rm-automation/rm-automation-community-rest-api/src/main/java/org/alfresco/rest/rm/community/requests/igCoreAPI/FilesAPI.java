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
package org.alfresco.rest.rm.community.requests.igCoreAPI;

import static org.alfresco.rest.core.RestRequest.simpleRequest;
import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryString;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpMethod.POST;

import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.community.requests.RMModelRequest;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Files REST API Wrapper
 *
 * @author Kristijan Conkas
 * @since 2.6
 */
@Component
@Scope (value = "prototype")
public class FilesAPI extends RMModelRequest
{
    /**
     * @param rmRestWrapper
     */
    public FilesAPI(RMRestWrapper rmRestWrapper)
    {
        super(rmRestWrapper);
    }

    /**
     * Declare file as record
     * @param fileId The Id of a file to declare as record
     * @param parameters Request parameters, refer to API documentation for more details
     * @return The {@link FilePlanComponent} for created record
     * @throws Exception for malformed JSON responses
     */
    public FilePlanComponent declareAsRecord(String fileId, String parameters) throws Exception
    {
        mandatoryString("fileId", fileId);

        return getRMRestWrapper().processModel(FilePlanComponent.class, simpleRequest(
            POST,
            "/files/{fileId}/declare?{parameters}",
            fileId,
            parameters
        ));
    }

    /**
     * A no-parameter version of {@link FilesAPI#declareAsRecord}
     * @param fileId The Id of a file to declare as record
     * @return The {@link FilePlanComponent} for created record
     * @throws Exception for malformed JSON responses
     */
    public FilePlanComponent declareAsRecord(String fileId) throws Exception
    {
        return declareAsRecord(fileId, EMPTY);
    }
}

