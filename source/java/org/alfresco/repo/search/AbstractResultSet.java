package org.alfresco.repo.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SpellCheckResult;
import org.alfresco.util.Pair;

/**
 * Common result set implementation.
 * 
 * @author andyh
 */
public abstract class AbstractResultSet implements ResultSet
{
    /**
     * Default constructor
     */
    public AbstractResultSet()
    {
        super();

    }

    public float getScore(int n)
    {
        // All have equal weight by default
        return 1.0f;
    }

    public void close()
    {
        // default to do nothing
    }
    
    public List<NodeRef> getNodeRefs()
    {
        ArrayList<NodeRef> nodeRefs = new ArrayList<NodeRef>(length());
        for (ResultSetRow row : this)
        {
            nodeRefs.add(row.getNodeRef());
        }
        return nodeRefs;
    }

    public List<ChildAssociationRef> getChildAssocRefs()
    {
        ArrayList<ChildAssociationRef> cars = new ArrayList<ChildAssociationRef>(length());
        for (ResultSetRow row : this)
        {
            cars.add(row.getChildAssocRef());
        }
        return cars;
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

    @Override
    public List<Pair<String, Integer>> getFieldFacet(String field)
    {
        return Collections.<Pair<String, Integer>>emptyList();
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
