package org.alfresco.repo.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SpellCheckResult;
import org.alfresco.util.Pair;

/**
 * An empty result set
 * @author andyh
 *
 */
public class EmptyResultSet implements ResultSet
{

    /**
     * Default constructor
     */
    public EmptyResultSet()
    {
        super();
    }

    /**
     * Bulk fetch results in the cache
     * 
     * @param bulkFetch boolean
     */
    public boolean setBulkFetch(boolean bulkFetch)
    {
    	return false;
    }

    /**
     * Do we bulk fetch
     * 
     * @return - true if we do
     */
    public boolean getBulkFetch()
    {
        return false;
    }

    /**
     * Set the bulk fetch size
     * 
     * @param bulkFetchSize int
     */
    public int setBulkFetchSize(int bulkFetchSize)
    {
    	return 0;
    }

    /**
     * Get the bulk fetch size.
     * 
     * @return the fetch size
     */
    public int getBulkFetchSize()
    {
        return 0;
    }
    
    public int length()
    {
        return 0;
    }

    public NodeRef getNodeRef(int n)
    {
        throw new UnsupportedOperationException();
    }

    public float getScore(int n)
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<ResultSetRow> iterator()
    {
        ArrayList<ResultSetRow> dummy = new ArrayList<ResultSetRow>(0);
        return dummy.iterator();
    }

    public void close()
    {

    }

    public ResultSetRow getRow(int i)
    {
        throw new UnsupportedOperationException();
    }

    public List<NodeRef> getNodeRefs()
    {
        return Collections.<NodeRef>emptyList();
    }

    public List<ChildAssociationRef> getChildAssocRefs()
    {
        return Collections.<ChildAssociationRef>emptyList();
    }

    public ChildAssociationRef getChildAssocRef(int n)
    {
        throw new UnsupportedOperationException();
    }

    public ResultSetMetaData getResultSetMetaData()
    {
        return new SimpleResultSetMetaData(LimitBy.UNLIMITED, PermissionEvaluationMode.EAGER, new SearchParameters());
    }

    public int getStart()
    {
        return 0;
    }

    public boolean hasMore()
    {
        return false;
    }
    
    @Override
    public List<Pair<String, Integer>> getFieldFacet(String field)
    {
        return Collections.<Pair<String, Integer>>emptyList();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getNumberFound()
     */
    @Override
    public long getNumberFound()
    {
        return 0;
    }
    
    @Override
    public Map<String, Integer> getFacetQueries()
    {
        return Collections.emptyMap();
    }
    
    @Override
    public SpellCheckResult getSpellCheckResult()
    {
        return new SpellCheckResult(null, null, false);
    }
}
