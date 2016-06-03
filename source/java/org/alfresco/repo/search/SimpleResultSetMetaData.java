package org.alfresco.repo.search;

import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSetColumn;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetSelector;
import org.alfresco.service.cmr.search.ResultSetType;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Simple implementation of result set meta data.
 * 
 * @author Andy Hind
 */
public class SimpleResultSetMetaData implements ResultSetMetaData
{
    private LimitBy limitedBy; 
    
    private PermissionEvaluationMode permissoinEvaluationMode;
    
    private SearchParameters searchParameters;
    
    
    /**
     * Default properties.
     * 
     * @param limitedBy LimitBy
     * @param permissoinEvaluationMode PermissionEvaluationMode
     * @param searchParameters SearchParameters
     */
    public SimpleResultSetMetaData(LimitBy limitedBy, PermissionEvaluationMode permissoinEvaluationMode, SearchParameters searchParameters)
    {
        super();
        this.limitedBy = limitedBy;
        this.permissoinEvaluationMode = permissoinEvaluationMode;
        this.searchParameters = searchParameters;
    }

    public LimitBy getLimitedBy()
    {
        return limitedBy;
    }

    public PermissionEvaluationMode getPermissionEvaluationMode()
    {
        return permissoinEvaluationMode;
    }

    public SearchParameters getSearchParameters()
    {
        return searchParameters;
    }

    public ResultSetColumn getColumn(String name)
    {
      throw new UnsupportedOperationException();
    }

    public String[] getColumnNames()
    {
        throw new UnsupportedOperationException();
    }

    public ResultSetColumn[] getColumns()
    {
        throw new UnsupportedOperationException();
    }

    public ResultSetType getResultSetType()
    {
        throw new UnsupportedOperationException();
    }

    public ResultSetSelector getSelector(String name)
    {
        throw new UnsupportedOperationException();
    }

    public String[] getSelectorNames()
    {
        throw new UnsupportedOperationException();
    }

    public ResultSetSelector[] getSelectors()
    {
        throw new UnsupportedOperationException();
    }

}
