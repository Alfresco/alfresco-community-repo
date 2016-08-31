/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.hibernate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * Factory for the Hibernate dialect. Allows dialect detection logic to be centralized and the dialect to be injected
 * where required as a singleton from the container.
 * 
 * @author dward
 */
public class DialectFactoryBean implements FactoryBean<Dialect>
{

    /** The local session factory. */
    private LocalSessionFactoryBean localSessionFactory;

    /**
     * Sets the local session factory.
     * 
     * @param localSessionFactory
     *            the new local session factory
     */
    public void setLocalSessionFactory(LocalSessionFactoryBean localSessionFactory)
    {
        this.localSessionFactory = localSessionFactory;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Dialect getObject() throws SQLException
    {
        Session session = ((SessionFactory) this.localSessionFactory.getObject()).openSession();
        Configuration cfg = this.localSessionFactory.getConfiguration();
        Connection con = null;
        try
        {
            // make sure that we AUTO-COMMIT
            con = session.connection();
            con.setAutoCommit(true);
            DatabaseMetaData meta = con.getMetaData();
            Dialect dialect = DialectFactory.buildDialect(cfg.getProperties(), meta.getDatabaseProductName(), meta
                    .getDatabaseMajorVersion());
            dialect = changeDialect(cfg, dialect);
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

    /**
     * Substitute the dialect with an alternative, if possible.
     * 
     * @param cfg
     *            the configuration
     * @param dialect
     *            the dialect
     * @return the dialect
     */
    private Dialect changeDialect(Configuration cfg, Dialect dialect)
    {
        String dialectName = cfg.getProperty(Environment.DIALECT);
        if (dialectName == null || dialectName.length() == 0)
        {
            // Fix the dialect property to match the detected dialect
            cfg.setProperty(Environment.DIALECT, dialect.getClass().getName());
        }
        return dialect;
        // TODO: https://issues.alfresco.com/jira/browse/ETHREEOH-679
        // else if (dialectName.equals(Oracle9Dialect.class.getName()))
        // {
        // String subst = AlfrescoOracle9Dialect.class.getName();
        // LogUtil.warn(logger, WARN_DIALECT_SUBSTITUTING, dialectName, subst);
        // cfg.setProperty(Environment.DIALECT, subst);
        // }
        // else if (dialectName.equals(MySQLDialect.class.getName()))
        // {
        // String subst = MySQLInnoDBDialect.class.getName();
        // LogUtil.warn(logger, WARN_DIALECT_SUBSTITUTING, dialectName, subst);
        // cfg.setProperty(Environment.DIALECT, subst);
        // }
        // else if (dialectName.equals(MySQL5Dialect.class.getName()))
        // {
        // String subst = MySQLInnoDBDialect.class.getName();
        // LogUtil.warn(logger, WARN_DIALECT_SUBSTITUTING, dialectName, subst);
        // cfg.setProperty(Environment.DIALECT, subst);
        // }
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
