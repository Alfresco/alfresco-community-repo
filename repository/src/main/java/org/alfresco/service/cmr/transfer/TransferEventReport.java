/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.service.cmr.transfer;

import org.alfresco.repo.transfer.TransferEventImpl;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This event is that the destination transfer report has been written.
 * 
 * @author mrogers
 *
 */
public class TransferEventReport extends  TransferEventImpl implements TransferEvent
{
    public enum ReportType { SOURCE, DESTINATION };
    
    private NodeRef nodeRef;
    
    private ReportType reportType;
    
    public String toString()
    {
        return "TransferEventReportWritten: " + nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }

    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public void setReportType(ReportType reportType)
    {
        this.reportType = reportType;
    }

    public ReportType getReportType()
    {
        return reportType;
    }

}
