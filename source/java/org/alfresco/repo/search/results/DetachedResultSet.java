package org.alfresco.repo.search.results;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.search.AbstractResultSet;
import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;

/**
 * Detached result set
 * @author andyh
 *
 */
public class DetachedResultSet extends AbstractResultSet
{
    List<ResultSetRow> rows = null;
    
    ResultSetMetaData rsmd;
    
    long numberFound;
    
    /**
     * Detached result set based on that provided
     * @param resultSet ResultSet
     */
    public DetachedResultSet(ResultSet resultSet)
    {
        super();
        rsmd = resultSet.getResultSetMetaData();
        rows = new ArrayList<ResultSetRow>(resultSet.length());
        for (ResultSetRow row : resultSet)
        {
            rows.add(new DetachedResultSetRow(this, row));
        }
        numberFound = resultSet.getNumberFound();
    }

    public int length()
    {
        return rows.size();
    }

    public NodeRef getNodeRef(int n)
    {
        return rows.get(n).getNodeRef();
    }

    public ResultSetRow getRow(int i)
    {
        return rows.get(i);
    }

    public Iterator<ResultSetRow> iterator()
    {
       return rows.iterator();
    }

    public ChildAssociationRef getChildAssocRef(int n)
    {
        return rows.get(n).getChildAssocRef();
    }

    public ResultSetMetaData getResultSetMetaData()
    {
        return new SimpleResultSetMetaData(rsmd.getLimitedBy(), PermissionEvaluationMode.EAGER, rsmd.getSearchParameters());
    }

    public int getStart()
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasMore()
    {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getNumberFound()
     */
    @Override
    public long getNumberFound()
    {
       return numberFound;
    }

}
