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
