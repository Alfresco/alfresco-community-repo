/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.domain.activities.ibatis;

import java.sql.SQLException;

import org.alfresco.repo.domain.activities.ActivitiesDAO;

import com.ibatis.sqlmap.client.SqlMapClient;

public class IBatisSqlMapper implements ActivitiesDAO
{
    private SqlMapClient sqlMapper;
    
    public void setSqlMapClient(SqlMapClient sqlMapper)
    {
        this.sqlMapper = sqlMapper;
    }
    
    public SqlMapClient getSqlMapClient()
    {
        return this.sqlMapper;
    }
    
    public void startTransaction() throws SQLException
    {
        sqlMapper.startTransaction();
    }
    
    public void commitTransaction() throws SQLException
    {
        sqlMapper.commitTransaction();
    }
    
    public void endTransaction() throws SQLException
    {
        sqlMapper.endTransaction();
    }
}
