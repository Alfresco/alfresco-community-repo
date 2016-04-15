package org.alfresco.repo.domain.activities.ibatis;

import java.sql.SQLException;

import org.alfresco.repo.domain.activities.ActivitiesDAO;
import org.mybatis.spring.SqlSessionTemplate;

public class ActivitiesDAOImpl implements ActivitiesDAO
{
    protected SqlSessionTemplate template;
    
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }
    
    public void startTransaction() throws SQLException
    {
        // NOOP
    }
    
    public void commitTransaction() throws SQLException
    {
        // NOOP
    }
    
    public void rollbackTransaction() throws SQLException
    {
        // NOOP
    }
    
    public void endTransaction() throws SQLException
    {
        // NOOP
    }
}
