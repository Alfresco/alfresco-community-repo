package org.alfresco.opencmis.search;

import java.util.ListIterator;

/**
 * @author andyh
 */
public class CMISResultSetRowIterator implements ListIterator<CMISResultSetRow>
{
    /**
     * The result set
     */
    private CMISResultSet resultSet;

    /**
     * The current position
     */
    private int position = -1;

    /**
     * The maximum position
     */
    private int max;

    /**
     * Create an iterator over the result set. Follows stadard ListIterator
     * conventions
     * 
     * @param resultSet CMISResultSet
     */
    public CMISResultSetRowIterator(CMISResultSet resultSet)
    {
        this.resultSet = resultSet;
        this.max = resultSet.getLength();
    }

    public CMISResultSet getResultSet()
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

    public CMISResultSetRow next()
    {
        return resultSet.getRow(moveToNextPosition());
    }

    protected int moveToNextPosition()
    {
        return ++position;
    }

    public CMISResultSetRow previous()
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void set(CMISResultSetRow o)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void add(CMISResultSetRow o)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

}
