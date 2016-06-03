package org.alfresco.repo.search;

import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;


/**
 * Iterate over the rows in a ResultSet
 * 
 * @author andyh
 * 
 */
public abstract class AbstractResultSetRowIterator implements ResultSetRowIterator
{
    /**
     * The result set
     */
    private ResultSet resultSet;

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
     * @param resultSet ResultSet
     */
    public AbstractResultSetRowIterator(ResultSet resultSet)
    {
        super();
        this.resultSet = resultSet;
        this.max = resultSet.length();
    }

    
    
    public ResultSet getResultSet()
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

    abstract public ResultSetRow next();
    
    protected int moveToNextPosition()
    {
        return ++position;
    }

    abstract public ResultSetRow previous();
    
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

    public void set(ResultSetRow o)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void add(ResultSetRow o)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

}
