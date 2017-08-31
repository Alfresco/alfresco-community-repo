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
