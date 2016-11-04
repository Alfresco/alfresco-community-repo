package org.alfresco.module.org_alfresco_module_rm.disposition;

import java.util.Date;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionServiceImpl.WriteMode;
import org.alfresco.service.cmr.repository.NodeRef;

public class NextActionFromDisposition
{
    public NextActionFromDisposition(NodeRef dispositionNodeRef, NodeRef nextActionNodeRef, String nextActionName, Date nextActionDateAsOf,
            WriteMode writeMode)
    {
        super();
        this.dispositionNodeRef = dispositionNodeRef;
        this.nextActionNodeRef = nextActionNodeRef;
        this.nextActionName = nextActionName;
        this.nextActionDateAsOf = nextActionDateAsOf;
        this.writeMode = writeMode;
    }

    private NodeRef dispositionNodeRef;

    private NodeRef nextActionNodeRef;

    private String nextActionName;

    private Date nextActionDateAsOf;

    private WriteMode writeMode;

    public WriteMode getWriteMode()
    {
        return writeMode;
    }

    public void setWriteMode(WriteMode writeMode)
    {
        this.writeMode = writeMode;
    }

    public NodeRef getNextActionNodeRef()
    {
        return nextActionNodeRef;
    }

    public void setNextActionNodeRef(NodeRef nextActionNodeRef)
    {
        this.nextActionNodeRef = nextActionNodeRef;
    }

    public NodeRef getDispositionNodeRef()
    {
        return dispositionNodeRef;
    }

    public void setDispositionNodeRef(NodeRef dispositionNodeRef)
    {
        this.dispositionNodeRef = dispositionNodeRef;
    }

    public String getNextActionName()
    {
        return nextActionName;
    }

    public void setNextActionName(String nextActionName)
    {
        this.nextActionName = nextActionName;
    }

    public Date getNextActionDateAsOf()
    {
        return nextActionDateAsOf;
    }

    public void setNextActionDateAsOf(Date nextActionDateAsOf)
    {
        this.nextActionDateAsOf = nextActionDateAsOf;
    }
}
