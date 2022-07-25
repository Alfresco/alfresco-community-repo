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
package org.alfresco.rest.requests.search;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.requests.ModelRequest;
import org.alfresco.rest.search.SearchSqlJDBCRequest;
import io.restassured.RestAssured;


/**
 * Wrapper for Search SQL using JDBC connection.
 * 
 * @author Meenal Bhave
 * 
 * Request POST JDBC
 * End point: /sql
 */
public class SearchSQLJDBC extends ModelRequest<SearchSQLJDBC>
{
    public SearchSQLJDBC(RestWrapper restWrapper)
    {
        super(restWrapper);
        RestAssured.basePath = "alfresco/api/-default-/public/search/versions/1/sql";
        restWrapper.configureRequestSpec().setBasePath(RestAssured.basePath);
    }

    public ResultSet executeQueryViaJDBC(SearchSqlJDBCRequest query) throws SQLException
    {
        
        StringBuilder connectionString = new StringBuilder()
                .append(RestAssured.baseURI
                        .replaceAll("http", "jdbc:alfresco")
                        .replaceAll("https", "jdbc:alfresco"))
                .append(":")
                .append(RestAssured.port)
                .append("/")
                .append(RestAssured.basePath)
                .append("/");

        String sql = (null == query.getSql() || query.getSql().isEmpty()) ? "" : query.getSql();

        Properties auth = new Properties();
        auth.put("user", query.getAuthUser().getUsername());
        auth.put("password", query.getAuthUser().getPassword());

        try
        {
            query.setConnection(DriverManager.getConnection(connectionString.toString(), auth));
            query.setStmt(query.getConnection().createStatement());
            query.setResultSet(query.getStmt().executeQuery(sql));
        }
        catch (SQLException eSql)
        {
            query.setErrorDetails(eSql.getMessage());
        }

        return query.getResultSet();
    }

    public void clearSearchQuery(SearchSqlJDBCRequest query) throws SQLException
    {
        if (query.getResultSet() != null)
        {
            query.getResultSet().close();
        }

        if (query.getStmt() != null)
        {
            query.getStmt().close();
        }

        if (query.getConnection() != null)
        {
            query.getConnection().close();
        }
    }
}
