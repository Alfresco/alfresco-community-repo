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

import java.sql.Types;

/**
 * @since 6.0
 */
public class MySQLInnoDBDialect extends Dialect
{
    public MySQLInnoDBDialect()
    {
        super();
        registerColumnType( Types.BIT, "bit" );
        registerColumnType( Types.BIGINT, "bigint" );
        registerColumnType( Types.SMALLINT, "smallint" );
        registerColumnType( Types.TINYINT, "tinyint" );
        registerColumnType( Types.INTEGER, "integer" );
        registerColumnType( Types.CHAR, "char(1)" );
        registerColumnType( Types.FLOAT, "float" );
        registerColumnType( Types.DOUBLE, "double precision" );
        registerColumnType( Types.DATE, "date" );
        registerColumnType( Types.TIME, "time" );
        registerColumnType( Types.TIMESTAMP, "datetime" );
        registerColumnType( Types.VARBINARY, "longblob" );
        registerColumnType( Types.VARBINARY, 16777215, "mediumblob" );
        registerColumnType( Types.VARBINARY, 65535, "blob" );
        registerColumnType( Types.VARBINARY, 255, "tinyblob" );
        registerColumnType( Types.NUMERIC, "decimal($p,$s)" );
        registerColumnType( Types.BLOB, "longblob" );
        registerColumnType( Types.BLOB, 16777215, "mediumblob" );
        registerColumnType( Types.BLOB, 65535, "blob" );
        registerColumnType( Types.CLOB, "longtext" );
        registerColumnType( Types.CLOB, 16777215, "mediumtext" );
        registerColumnType( Types.CLOB, 65535, "text" );

        registerColumnType( Types.VARCHAR, "longtext" );
        registerColumnType( Types.VARCHAR, 16777215, "mediumtext" );
        registerColumnType( Types.VARCHAR, 65535, "text" );
        registerColumnType( Types.VARCHAR, 255, "varchar($l)" );
    }
}
