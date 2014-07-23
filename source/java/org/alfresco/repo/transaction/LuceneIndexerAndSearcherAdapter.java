package org.alfresco.repo.transaction;

import org.alfresco.repo.search.impl.lucene.LuceneIndexerAndSearcher;

/* package */class LuceneIndexerAndSearcherAdapter implements TransactionListener
{
	  
	    	protected LuceneIndexerAndSearcher luceneIndexerAndSearcher;
	    	
	    	public LuceneIndexerAndSearcherAdapter (LuceneIndexerAndSearcher luceneIndexerAndSearcher)
	    	{
	    		this.luceneIndexerAndSearcher = luceneIndexerAndSearcher;
	    	}

			@Override
	        public void flush()
	        {
		      // NO-OP  
	        }

			@Override
	        public void beforeCommit(boolean readOnly)
	        {
				luceneIndexerAndSearcher.prepare();   
	        }

			@Override
	        public void beforeCompletion()
	        {
		       // NO-OP
	        }

			@Override
	        public void afterCommit()
	        {
				luceneIndexerAndSearcher.commit();
	        }

			@Override
	        public void afterRollback()
	        {
				luceneIndexerAndSearcher.rollback();
	        }
			
			/**
			 * Return a hashcode for the request
			 * 
			 * @return int
			 */
			public int hashCode() 
			{
				return luceneIndexerAndSearcher.hashCode();
			}
			
			public boolean equals(Object obj) 
			{
				if(obj instanceof LuceneIndexerAndSearcherAdapter)
				{
					LuceneIndexerAndSearcherAdapter other = (LuceneIndexerAndSearcherAdapter)obj;
					return luceneIndexerAndSearcher.equals(other.luceneIndexerAndSearcher);
				}
				return luceneIndexerAndSearcher.equals(obj);
			}
			
		
	    

}
