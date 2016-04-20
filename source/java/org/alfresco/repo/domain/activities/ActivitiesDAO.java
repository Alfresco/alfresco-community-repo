package org.alfresco.repo.domain.activities;

import java.sql.SQLException;

/**
 * Common interface for activity DAO service
 */
public interface ActivitiesDAO
{
    public static final String KEY_ACTIVITY_NULL_VALUE = "@@NULL@@";
    
    public void startTransaction() throws SQLException;
    
    public void commitTransaction() throws SQLException;
    
    public void rollbackTransaction() throws SQLException;
    
    public void endTransaction() throws SQLException;
}
