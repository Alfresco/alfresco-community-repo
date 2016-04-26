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
