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
import static org.springframework.http.HttpMethod.POST;

import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.requests.RMModelRequest;

/**
 * Files REST API Wrapper
 *
 * @author Kristijan Conkas
 * @since 2.6
 */
public class FilesAPI extends RMModelRequest<FilesAPI>
{
    public static final String PARENT_ID_PARAM = "parentId";

    /**
     * @param rmRestWrapper RM REST Wrapper
     */
    public FilesAPI(RMRestWrapper rmRestWrapper)
    {
        super(rmRestWrapper);
    }

    /**
     * Declare file as record
     *
     * @param fileId The Id of a file to declare as record
     * @return The {@link Record} for created record
     * @throws RuntimeException for malformed JSON responses
     */
    public Record declareAsRecord(String fileId)
    {
        mandatoryString("fileId", fileId);

        return getRmRestWrapper().processModel(Record.class, simpleRequest(
            POST,
            "/files/{fileId}/declare?{parameters}",
            fileId,
            getRmRestWrapper().getParameters()
        ));
    }
}

