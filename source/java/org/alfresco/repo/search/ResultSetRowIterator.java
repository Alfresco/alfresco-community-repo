package org.alfresco.repo.search;

import java.util.ListIterator;

import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;

/**
 * A typed ListIterator over Collections containing ResultSetRow elements
 * 
 * @author andyh
 */
public interface ResultSetRowIterator extends ListIterator<ResultSetRow>
{
    /**
     * Get the underlying result set
     * 
     * @return - the result set
     */
    public ResultSet getResultSet();

    /**
     * Does the result set allow reversal?
     * 
     * @return - true if the result set can be navigated in reverse.
     */
    public boolean allowsReverse();
}
