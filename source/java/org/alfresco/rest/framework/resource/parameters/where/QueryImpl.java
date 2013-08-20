package org.alfresco.rest.framework.resource.parameters.where;

import org.antlr.runtime.tree.CommonTree;

/**
 * Represents a Query defined by a WHERE clause, passed in as a parameter in a GET call to the Rest api.
 * @author Gethin James
 */
public class QueryImpl implements Query
{
    public static final Query EMPTY = new QueryImpl();
    private final CommonTree tree;
    
    private QueryImpl()
    {
        super();
        this.tree = null;
    }

    public QueryImpl(CommonTree tree) {
        super();
        this.tree = tree;
	}
    
    @Override
	public CommonTree getTree() {
		return tree;
	}

	@Override
	public String toString() {
		return "QueryImpl [tree=" + tree + "]";
	}

}
