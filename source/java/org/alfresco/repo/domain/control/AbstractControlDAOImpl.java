package org.alfresco.repo.domain.control;

import java.sql.Savepoint;

/**
 * Abstract implementation for connection controlling DAO.
 * <p>
 * Provides any basic logic.
 * 
 * @author Derek Hulley
 * @since 3.2SP1
 */
public abstract class AbstractControlDAOImpl implements ControlDAO
{
    /**
     * @return              Returns <tt>null</tt> by default i.e. not supported
     */
    @Override
    public Savepoint createSavepoint(String savepoint)
    {
        return null;
    }

    /** No-op */
    @Override
    public void rollbackToSavepoint(Savepoint savepoint)
    {
    }

    /** No-op */
    @Override
    public void releaseSavepoint(Savepoint savepoint)
    {
    }

    @Override
    public int setTransactionIsolationLevel(int isolationLevel)
    {
        throw new UnsupportedOperationException("Method not implemented by the DAO");
    }
}
