/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
/*
 * Copyright (C) 2018 Alfresco Software Limited.
 * This file is part of Alfresco
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.rest.search;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.alfresco.utility.model.TestModel;
import org.alfresco.utility.model.UserModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Search SQL Query object for JDBC connection.
 * 
 * @author Meenal Bhave
 * 
 * Request POST
 * End point: /sql
 * PostBody:
 * {
 *   "stmt":"Select SITE from alfresco where SITE = 'swsdp' limit 2"
 * }
 */
public class SearchSqlJDBCRequest extends TestModel
{
    @JsonProperty(required = true)

    String sql;
    Connection connection;
    Statement stmt;
    ResultSet resultSet;
    UserModel authUser;
    String errorDetails;

    public String getSql()
    {
        return sql;
    }

    public void setSql(String sql)
    {
        this.sql = sql;
    }
    
    public Statement getStmt()
    {
        return stmt;
    }

    public void setStmt(Statement stmt)
    {
        this.stmt = stmt;
    }

    public Connection getConnection()
    {
        return connection;
    }

    public void setConnection(Connection con)
    {
        this.connection = con;
    }

    public ResultSet getResultSet()
    {
        return resultSet;
    }

    public void setResultSet(ResultSet rs)
    {
        this.resultSet = rs;
    }

    public UserModel getAuthUser()
    {
        return authUser;
    }

    public void setAuthUser(UserModel usermodel)
    {
        this.authUser = usermodel;
    }  

    public String getErrorDetails()
    {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails)
    {
        this.errorDetails = errorDetails;
    }

    public SearchSqlJDBCRequest()
    {
    }
}
