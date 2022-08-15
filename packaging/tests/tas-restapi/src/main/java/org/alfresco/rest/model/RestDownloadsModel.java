/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

/*
 * Handles responses for GET /downloads/{downloadId} and POST /downloads rest calls
 * E.g
 * {
 *   "entry": {
 *      "filesAdded": 1,
 *      "bytesAdded": 4,
 *      "totalBytes": 4,
 *      "id": "6ab1357c-bf13-4ecc-9c6e-3af0b6a7bb34",
 *      "totalFiles": 1,
 *      "status": "DONE"
 *   }
 * }
*/

public class RestDownloadsModel extends TestModel implements IRestModel<RestDownloadsModel>
{
    @JsonProperty(value = "entry")
    RestDownloadsModel model;

    @Override
    public RestDownloadsModel onModel()
    {
        return model;
    }

    @JsonProperty(required = true)
    private String filesAdded;

    @JsonProperty(required = true)
    private String bytesAdded;

    @JsonProperty(required = true)
    private String totalBytes;

    @JsonProperty(required = true)
    private String id;

    @JsonProperty(required = true)
    private String totalFiles;

    @JsonProperty(required = true)
    private String status;

    public RestDownloadsModel getModel()
    {
        return model;
    }

    public void setModel(RestDownloadsModel model)
    {
        this.model = model;
    }

    public String getFilesAdded()
    {
        return filesAdded;
    }

    public void setFilesAdded(String filesAdded)
    {
        this.filesAdded = filesAdded;
    }

    public String getBytesAdded()
    {
        return bytesAdded;
    }

    public void setBytesAdded(String bytesAdded)
    {
        this.bytesAdded = bytesAdded;
    }

    public String getTotalBytes()
    {
        return totalBytes;
    }

    public void setTotalBytes(String totalBytes)
    {
        this.totalBytes = totalBytes;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getTotalFiles()
    {
        return totalFiles;
    }

    public void setTotalFiles(String totalFiles)
    {
        this.totalFiles = totalFiles;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}

