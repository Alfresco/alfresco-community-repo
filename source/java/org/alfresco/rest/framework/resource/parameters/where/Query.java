package org.alfresco.rest.framework.resource.parameters.where;

import org.antlr.runtime.tree.CommonTree;

/**
 * Represents a Query defined by a WHERE clause, passed in as a parameter in a GET call to the Rest api.
 * 
 * @author Gethin James
 */
public interface Query
{
    
    /**
     * Returns the list of operations
     */
    CommonTree getTree();

}
