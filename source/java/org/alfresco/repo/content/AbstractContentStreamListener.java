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
