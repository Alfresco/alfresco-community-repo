/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.RangedTransferEvent;
import org.alfresco.service.cmr.transfer.TransferEndEvent;
import org.alfresco.service.cmr.transfer.TransferEvent;

/**
 * An abstract implementation of TransferEndEvent.

 * @see TransferEvent
 * @see RangedTransferEvent
 */
public class TransferEndEventImpl extends TransferEventImpl implements TransferEndEvent
{
    private NodeRef sourceReport;
    private NodeRef destinationReport;

    public void setSourceReport(NodeRef sourceReport)
    {
        this.sourceReport = sourceReport;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferEndEvent#getSourceReport()
     */
    @Override
    public NodeRef getSourceReport()
    {
        return sourceReport;
    }

    public void setDestinationReport(NodeRef destinationReport)
    {
        this.destinationReport = destinationReport;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferEndEvent#getDestinationReport()
     */
    @Override
    public NodeRef getDestinationReport()
    {
        return destinationReport;
    }

}
