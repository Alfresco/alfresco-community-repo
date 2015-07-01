/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.service.cmr.repository;

import java.util.HashMap;

/**
 * This class is used by DocumentLinkService to encapsulate the status of
 * deleting the links of a document.
 * 
 * @author Ana Bozianu
 * @since 5.1
 */
public class DeleteLinksStatusReport
{
    /* the count of all links to the documents that were found */
    private int totalLinksFoundCount = 0;
    /* the count of links that were successfully deleted */
    private int deletedLinksCount = 0;
    /* detailed information about the nodes that could not be deleted */
    private HashMap<NodeRef, Throwable> errorDetails = new HashMap<NodeRef, Throwable>();

    public int getTotalLinksFoundCount()
    {
        return totalLinksFoundCount;
    }

    public int getDeletedLinksCount()
    {
        return deletedLinksCount;
    }

    public HashMap<NodeRef, Throwable> getErrorDetails()
    {
        return errorDetails;
    }

    public void setTotalLinksFoundCount(int totalLinksFoundCount)
    {
        this.totalLinksFoundCount = totalLinksFoundCount;
    }
    
    public void addTotalLinksFoundCount(int totalLinksFoundCount)
    {
        this.totalLinksFoundCount += totalLinksFoundCount;
    }

    public void setDeletedLinksCount(int deletedLinksCount)
    {
        this.deletedLinksCount = deletedLinksCount;
    }

    public void incrementDeletedLinksCount()
    {
        this.deletedLinksCount++;
    }

    public void addErrorDetail(NodeRef nodeRef, Throwable th)
    {
        errorDetails.put(nodeRef, th);
    }

}
