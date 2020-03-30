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

import java.util.HashMap;
import java.util.Map;
import org.alfresco.error.AlfrescoRuntimeException;

/**
 * A factory for generating Dialect instances.
 * <p>
 * This class is an Alfresco-specific version of the Hibernate class with the same name.
 *
 * @author Steve Ebersole
 * @author Alfresco
 * @since 6.0
 */
public class DialectFactory
{
    /**
     * Builds an appropriate Dialect instance.
     * <p/>
     * The JDBC driver, the database name and version
     * (obtained from connection metadata) are used to make the determination.
     * <p/>
     * An exception is thrown if a dialect was not explicitly set and the database name is not known.
     *

     * @param databaseName The name of the database product (obtained from metadata).
     * @param databaseMajorVersion The major version of the database product (obtained from metadata).
     * @param driverName THe name of the JDBC driver
     *
     * @return The appropriate dialect.
     */
    public static Dialect buildDialect(String databaseName, int databaseMajorVersion, String driverName)
    {
        if ( databaseName == null ) {
            throw new IllegalArgumentException("Database name must be explicitly set");
        }

        DatabaseDialectMapper mapper = MAPPERS.get(driverName) != null ?
                MAPPERS.get(driverName) : MAPPERS.get(databaseName);
        if ( mapper == null )
        {
            throw new IllegalArgumentException( "Dialect must be explicitly set for database: " + databaseName );
        }

        String dialectName = mapper.getDialectClass( databaseMajorVersion );
        return buildDialect( dialectName );
    }

    /**
     * Returns a dialect instance given the name of the class to use.
     *
     * @param dialectName The name of the dialect class.
     *
     * @return The dialect instance.
     */
    public static Dialect buildDialect(String dialectName)
    {
        try
        {
            return ( Dialect ) Class.forName( dialectName ).newInstance();
        }
        catch ( ClassNotFoundException cnfe )
        {
            throw new IllegalArgumentException( "Dialect class not found: " + dialectName );
        }
        catch ( Exception e )
        {
            throw new AlfrescoRuntimeException( "Could not instantiate dialect class", e );
        }
    }

    /**
     * For a given database product name, instances of
     * DatabaseDialectMapper know which Dialect to use for different versions.
     */
    public static interface DatabaseDialectMapper {
        public String getDialectClass(int majorVersion);
    }

    /**
     * A simple DatabaseDialectMapper for dialects which are independent
     * of the underlying database product version.
     */
    public static class VersionInsensitiveMapper implements DatabaseDialectMapper {
        private String dialectClassName;

        public VersionInsensitiveMapper(String dialectClassName) {
            this.dialectClassName = dialectClassName;
        }

        public String getDialectClass(int majorVersion) {
            return dialectClassName;
        }
    }

    // TODO : this is the stuff it'd be nice to move to a properties file or some other easily user-editable place
    private static final Map<String, VersionInsensitiveMapper> MAPPERS = new HashMap<String, DialectFactory.VersionInsensitiveMapper>();
    static {
        // detectors...
        MAPPERS.put( "PostgreSQL", new VersionInsensitiveMapper( "org.alfresco.repo.domain.dialect.PostgreSQLDialect" ) );
        MAPPERS.put( "MariaDB", new VersionInsensitiveMapper( "org.alfresco.repo.domain.dialect.MySQLInnoDBDialect" ) );
        MAPPERS.put( "MySQL", new VersionInsensitiveMapper( "org.alfresco.repo.domain.dialect.MySQLInnoDBDialect" ) );
        MAPPERS.put( "Microsoft SQL Server Database", new VersionInsensitiveMapper( "org.alfresco.repo.domain.dialect.SQLServerDialect" ) );
        MAPPERS.put( "Microsoft SQL Server", new VersionInsensitiveMapper( "org.alfresco.repo.domain.dialect.SQLServerDialect" ) );
        MAPPERS.put( "Oracle", new VersionInsensitiveMapper( "org.alfresco.repo.domain.dialect.Oracle9Dialect" ) );
    }
}
