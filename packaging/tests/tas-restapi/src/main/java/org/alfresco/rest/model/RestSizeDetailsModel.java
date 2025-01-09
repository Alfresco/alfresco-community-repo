/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

public class RestSizeDetailsModel extends TestModel implements IRestModel<RestSizeDetailsModel>
{
    @JsonProperty(value = "entry")
    RestSizeDetailsModel model;

    private String id;
    private Long sizeInBytes;
    private Date calculatedAt;
    private Integer numberOfFiles;
    private String jobId;
    private STATUS status;

    public enum STATUS
    {
        NOT_INITIATED, PENDING, IN_PROGRESS, COMPLETED, FAILED
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Long getSizeInBytes()
    {
        return sizeInBytes;
    }

    public void setSizeInBytes(Long sizeInBytes)
    {
        this.sizeInBytes = sizeInBytes;
    }

    public Date getCalculatedAt()
    {
        return calculatedAt;
    }

    public void setCalculatedAt(Date calculatedAt)
    {
        this.calculatedAt = calculatedAt;
    }

    public Integer getNumberOfFiles()
    {
        return numberOfFiles;
    }

    public void setNumberOfFiles(Integer numberOfFiles)
    {
        this.numberOfFiles = numberOfFiles;
    }

    public String getJobId()
    {
        return jobId;
    }

    public void setJobId(String jobId)
    {
        this.jobId = jobId;
    }

    public STATUS getStatus()
    {
        return status;
    }

    public void setStatus(STATUS status)
    {
        this.status = status;
    }

    @Override
    public RestSizeDetailsModel onModel()
    {
        return model;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        RestSizeDetailsModel that = (RestSizeDetailsModel) o;
        return Objects.equals(id, that.id) && Objects.equals(sizeInBytes, that.sizeInBytes) && Objects.equals(
                calculatedAt, that.calculatedAt) && Objects.equals(numberOfFiles, that.numberOfFiles)
                && Objects.equals(jobId, that.jobId) && status == that.status;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, sizeInBytes, calculatedAt, numberOfFiles, jobId, status);
    }
}