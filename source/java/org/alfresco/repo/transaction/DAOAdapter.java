package org.alfresco.repo.transaction;

/* package scope */ class DAOAdapter implements TransactionListener
{
	protected TransactionalDao daoService;
	
	public DAOAdapter (TransactionalDao daoService)
	{
		this.daoService =  daoService;
	}

	@Override
    public void flush()
    {
      // NO-OP  
    }

	@Override
    public void beforeCommit(boolean readOnly)
    {
		daoService.beforeCommit(readOnly);
    }

	@Override
    public void beforeCompletion()
    {
       // NO-OP
    }

	@Override
    public void afterCommit()
    {
		// NO-OP
    }

	@Override
    public void afterRollback()
    {
		// NO-OP
    }
	
	public TransactionalDao getService()
	{
		return daoService;
	}
	
	public boolean equals(Object obj) 
	{
		if(obj instanceof DAOAdapter)
		{
			DAOAdapter other = (DAOAdapter)obj;
			return daoService.equals(other.daoService);
		}
		return daoService.equals(obj);
	}

	/**
	 * Return a hashcode for the request
	 * 
	 * @return int
	 */
	public int hashCode() 
	{
		return daoService.hashCode();
	}
}

