/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.repo.domain.Server;
import org.alfresco.repo.domain.Transaction;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

/**
 * Bean containing all the persistence data representing a <b>Transaction</b>.
 * <p>
 * This implementation of the {@link org.alfresco.repo.domain.Transaction Transaction} interface is
 * Hibernate specific.
 * 
 * @author Derek Hulley
 */
public class TransactionImpl extends LifecycleAdapter implements Transaction, Serializable
{
    private static final long serialVersionUID = -8264339795578077552L;

    private Long id;
    private Long version;
    private String changeTxnId;
    private Long commitTimeMs;
    private Server server;
    
    public TransactionImpl()
    {
        this.commitTimeMs = Long.valueOf(0);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(50);
        sb.append("Transaction")
          .append("[id=").append(id)
          .append(", txnTimeMs=").append(commitTimeMs == null ? "---" : ISO8601DateFormat.format(new Date(commitTimeMs)))
          .append(", changeTxnId=").append(changeTxnId)
          .append("]");
        return sb.toString();
    }
    
    public Long getId()
    {
        return id;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setId(Long id)
    {
        this.id = id;
    }

    public Long getVersion()
    {
        return version;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setVersion(Long version)
    {
        this.version = version;
    }

    public String getChangeTxnId()
    {
        return changeTxnId;
    }

    public void setChangeTxnId(String changeTransactionId)
    {
        this.changeTxnId = changeTransactionId;
    }

    public Long getCommitTimeMs()
    {
        return commitTimeMs;
    }

    public void setCommitTimeMs(Long commitTimeMs)
    {
        this.commitTimeMs = commitTimeMs;
    }

    public Server getServer()
    {
        return server;
    }

    public void setServer(Server server)
    {
        this.server = server;
    }
}
