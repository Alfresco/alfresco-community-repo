/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.domain.solr;

import java.util.Date;
import java.util.List;

/**
 * Holds parameters for SOLR DAO calls
 * 
 * @since 4.0
 */
public class SOLRTransactionParameters {
    private Long minTxnId;
    private Long txnFromCommitTime;
    private List<Long> transactionIds;
    private Long fromNodeId;
    private Long toNodeId;

    public SOLRTransactionParameters()
    {
    }

    public void setMinTxnId(Long minTxnId)
    {
        this.minTxnId = minTxnId;
    }

    public Long getMinTxnId()
    {
        return minTxnId;
    }
    
    public void setTxnFromCommitTime(Long txnFromCommitTime) {
		this.txnFromCommitTime = txnFromCommitTime;
	}

	public Long getTxnFromCommitTime() {
		return txnFromCommitTime;
	}
    
	public void setTransactionIds(List<Long> txnIds) {
		this.transactionIds = txnIds;
	}

	public List<Long> getTransactionIds() {
		return transactionIds;
	}
	
	public Long getFromNodeId() {
		return fromNodeId;
	}

	public void setFromNodeId(Long fromNodeId) {
		this.fromNodeId = fromNodeId;
	}

	public Long getToNodeId() {
		return toNodeId;
	}

	public void setToNodeId(Long toNodeId) {
		this.toNodeId = toNodeId;
	}

	@Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("SOLRTransactionParameters")
          .append(", txnFromCommitTime").append(txnFromCommitTime == null ? null : new Date(txnFromCommitTime))
          .append(", fromNodeId").append(fromNodeId == null ? null : fromNodeId)
          .append(", toNodeId").append(toNodeId == null ? null : toNodeId)
          .append(", txnIds").append(transactionIds == null ? null : transactionIds.size())
          .append("]");
        return sb.toString();
    }
}
