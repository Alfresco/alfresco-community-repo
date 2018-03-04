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
public class PostgreSQLDialect extends Dialect
{
    public PostgreSQLDialect()
    {
        super();
        registerColumnType( Types.BIT, "bool" );
        registerColumnType( Types.BIGINT, "int8" );
        registerColumnType( Types.SMALLINT, "int2" );
        registerColumnType( Types.TINYINT, "int2" );
        registerColumnType( Types.INTEGER, "int4" );
        registerColumnType( Types.CHAR, "char(1)" );
        registerColumnType( Types.VARCHAR, "varchar($l)" );
        registerColumnType( Types.FLOAT, "float4" );
        registerColumnType( Types.DOUBLE, "float8" );
        registerColumnType( Types.DATE, "date" );
        registerColumnType( Types.TIME, "time" );
        registerColumnType( Types.TIMESTAMP, "timestamp" );
        registerColumnType( Types.VARBINARY, "bytea" );
        registerColumnType( Types.CLOB, "text" );
        registerColumnType( Types.BLOB, "oid" );
        registerColumnType( Types.NUMERIC, "numeric($p, $s)" );
    }
}
