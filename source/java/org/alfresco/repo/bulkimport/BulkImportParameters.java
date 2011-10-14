package org.alfresco.repo.bulkimport;

import org.alfresco.service.cmr.repository.NodeRef;

public class BulkImportParameters
{
	private NodeRef target;
	private boolean replaceExisting = false;
	private Integer batchSize;
	private Integer numThreads;
	private Integer loggingInterval;
	private boolean disableRulesService = false;
	
	public boolean isDisableRulesService()
	{
		return disableRulesService;
	}
	public void setDisableRulesService(boolean disableRulesService)
	{
		this.disableRulesService = disableRulesService;
	}
	public Integer getLoggingInterval()
	{
		return loggingInterval;
	}
	public void setLoggingInterval(Integer loggingInterval)
	{
		this.loggingInterval = loggingInterval;
	}
	public NodeRef getTarget()
	{
		return target;
	}
	public void setTarget(NodeRef target)
	{
		this.target = target;
	}
	public boolean isReplaceExisting()
	{
		return replaceExisting;
	}
	public void setReplaceExisting(boolean replaceExisting)
	{
		this.replaceExisting = replaceExisting;
	}
	public Integer getBatchSize()
	{
		return batchSize;
	}
	public void setBatchSize(Integer batchSize)
	{
		this.batchSize = batchSize;
	}
	public Integer getNumThreads()
	{
		return numThreads;
	}
	public void setNumThreads(Integer numThreads)
	{
		this.numThreads = numThreads;
	}
	
}
