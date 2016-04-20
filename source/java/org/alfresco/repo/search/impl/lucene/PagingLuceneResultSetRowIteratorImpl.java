package org.alfresco.repo.search.impl.lucene;

import java.util.ListIterator;

import org.alfresco.service.cmr.search.ResultSetRow;

/**
 * @author andyh
 */
public class PagingLuceneResultSetRowIteratorImpl implements ListIterator<ResultSetRow>
{
    /**
     * The result set
     */
    private PagingLuceneResultSet resultSet;

    /**
     * The current position
     */
    private int position = -1;

    /**
     * The maximum position
     */
    private int max;

    /**
     * Create an iterator over the result set. Follows stadard ListIterator conventions
     * 
     * @param resultSet PagingLuceneResultSet
     */
    public PagingLuceneResultSetRowIteratorImpl(PagingLuceneResultSet resultSet)
    {
        this.resultSet = resultSet;
        this.max = resultSet.getLength();
    }

    public PagingLuceneResultSet getResultSet()
    {
        return resultSet;
    }

    /*
     * ListIterator implementation
     */
    public boolean hasNext()
    {
        return position < (max - 1);
    }

    public boolean allowsReverse()
    {
        return true;
    }

    public boolean hasPrevious()
    {
        return position > 0;
    }

    public ResultSetRow next()
    {
        return resultSet.getRow(moveToNextPosition());
    }

    protected int moveToNextPosition()
    {
        return ++position;
    }

    public ResultSetRow previous()
    {
        return resultSet.getRow(moveToPreviousPosition());
    }

    protected int moveToPreviousPosition()
    {
        return --position;
    }

    public int nextIndex()
    {
        return position + 1;
    }

    public int previousIndex()
    {
        return position - 1;
    }

    /*
     * Mutation is not supported
     */

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    public void set(ResultSetRow o)
    {
        throw new UnsupportedOperationException();
    }

    public void add(ResultSetRow o)
    {
        throw new UnsupportedOperationException();
    }

}
