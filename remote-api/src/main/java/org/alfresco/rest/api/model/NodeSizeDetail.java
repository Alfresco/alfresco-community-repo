/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.rest.api.model;

import java.util.Objects;

public class NodeSizeDetail
{
    private String nodeId;
    private Long size;
    private String calculatedAt;
    private Integer numberOfFiles;
    private String status;
    private String jobId;

    public NodeSizeDetail(String jobId) {
        this.jobId = jobId;
    }

    public NodeSizeDetail(String nodeId, String status) {
        this.nodeId = nodeId;
        this.status = status;
    }

    public NodeSizeDetail(String nodeId, Long size, String calculatedAt, Integer numberOfFiles, String status, String jobId)
    {
        this.nodeId = nodeId;
        this.size = size;
        this.calculatedAt = calculatedAt;
        this.numberOfFiles = numberOfFiles;
        this.status = status;
        this.jobId = jobId;
    }

    public String getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(String nodeId)
    {
        this.nodeId = nodeId;
    }

    public Long getSize()
    {
        return size;
    }

    public void setSize(Long size)
    {
        this.size = size;
    }

    public String getCalculatedAt()
    {
        return calculatedAt;
    }

    public void setCalculatedAt(String calculatedAt)
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

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getJobId() { return jobId; }

    public void setJobId(String jobId) { this.jobId = jobId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeSizeDetail that = (NodeSizeDetail) o;
        return Objects.equals(nodeId, that.nodeId) && Objects.equals(size, that.size) && Objects.equals(calculatedAt, that.calculatedAt) && Objects.equals(numberOfFiles, that.numberOfFiles) && Objects.equals(status, that.status) && Objects.equals(jobId, that.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, size, calculatedAt, numberOfFiles, status, jobId);
    }

    @Override
    public String toString() {
        return "NodeSizeDetail{" +
                "nodeId='" + nodeId + '\'' +
                ", size=" + size +
                ", calculatedAt='" + calculatedAt + '\'' +
                ", numberOfFiles=" + numberOfFiles +
                ", status='" + status + '\'' +
                ", jobId='" + jobId + '\'' +
                '}';
    }
}
