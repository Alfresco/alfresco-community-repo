package org.alfresco.repo.transaction;

/**
 * NO-OP listener.
 * 
 * @deprecated              Since 5.0, use {@link org.alfresco.util.transaction.TransactionListenerAdapter}
 * @author Derek Hulley
 */
@Deprecated
public abstract class TransactionListenerAdapter implements TransactionListener
{
    /**
     * {@inheritDoc}
     */
    public void flush()
    {
    }

    /**
     * {@inheritDoc}
     */
    public void beforeCommit(boolean readOnly)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void beforeCompletion()
    {
    }

    /**
     * {@inheritDoc}
     */
    public void afterCommit()
    {
    }

    /**
     * {@inheritDoc}
     */
    public void afterRollback()
    {
    }
}
