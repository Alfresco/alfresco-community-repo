/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.domain.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * Factory for the DB dialect. Allows dialect detection logic to be centralized and the dialect to be injected
 * where required as a singleton from the container.
 * 
 * @author dward
 * @since 6.0
 */
public class DialectFactoryBean implements FactoryBean<Dialect>
{
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    @Override
    public Dialect getObject() throws SQLException
    {
        Connection con = null;
        try
        {
            // make sure that we AUTO-COMMIT
            con = DataSourceUtils.getConnection(dataSource);
            con.setAutoCommit(true);
            DatabaseMetaData meta = con.getMetaData();
            Dialect dialect = DialectFactory.buildDialect(meta.getDatabaseProductName(), meta.getDatabaseMajorVersion(), meta.getDriverName());
            return dialect;
        }
        finally
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
            }
        }
    }

    @Override
    public Class<?> getObjectType()
    {
        return Dialect.class;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }
}
