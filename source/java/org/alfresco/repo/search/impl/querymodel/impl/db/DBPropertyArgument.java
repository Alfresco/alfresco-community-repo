package org.alfresco.repo.search.impl.querymodel.impl.db;

import org.alfresco.repo.search.impl.querymodel.impl.BasePropertyArgument;

/**
 * @author Andy
 *
 */
public class DBPropertyArgument extends BasePropertyArgument
{

    /**
     * @param name String
     * @param queryable boolean
     * @param orderable boolean
     * @param selector String
     * @param propertyName String
     */
    public DBPropertyArgument(String name, boolean queryable, boolean orderable, String selector, String propertyName)
    {
        super(name, queryable, orderable, selector, propertyName);
    }

}
