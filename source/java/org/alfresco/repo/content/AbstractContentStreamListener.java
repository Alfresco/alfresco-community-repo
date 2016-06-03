package org.alfresco.repo.content;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentStreamListener;

/**
 * @author Andy
 *
 */
public abstract class AbstractContentStreamListener implements ContentStreamListener
{
    
    /** when set, ensures that listeners are executed within a transaction */
    private RetryingTransactionHelper transactionHelper;
    
    public void setRetryingTransactionHelper(RetryingTransactionHelper helper)
    {
        this.transactionHelper = helper;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.repository.ContentStreamListener#contentStreamClosed()
     */
    public final void contentStreamClosed() throws ContentIOException
    {
        RetryingTransactionCallback<Object> cb = new RetryingTransactionCallback<Object>()
        {
            public Object execute()
            {
                contentStreamClosedImpl();
                return null;
            }
        };
        if (transactionHelper != null)
        {
            // Execute in transaction.
            transactionHelper.doInTransaction(cb, false);
        }
        else
        {
            try
            {
                cb.execute();       
            }
            catch (Throwable e)
            {
                throw new ContentIOException("Failed to executed channel close callbacks", e);
            }
        }

    }
    
    /**
     * ContentStreamListeners must implement this method.
     * The implementation must be idempotent.
     * 
     * The method will be executed inside a retrying transaction helper if one is set.
     * Listeners will not generally require this unless they write to the database.
     * 
     * @throws ContentIOException
     */
    public abstract void  contentStreamClosedImpl() throws ContentIOException;

}
