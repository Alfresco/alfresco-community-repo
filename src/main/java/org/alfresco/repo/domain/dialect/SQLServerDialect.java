/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
public class SQLServerDialect extends Dialect
{
    public SQLServerDialect()
    {
        super();
        registerColumnType( Types.BIT, "tinyint" ); //Sybase BIT type does not support null values
        registerColumnType( Types.BIGINT, "numeric(19,0)" );
        registerColumnType( Types.SMALLINT, "smallint" );
        registerColumnType( Types.TINYINT, "tinyint" );
        registerColumnType( Types.INTEGER, "int" );
        registerColumnType( Types.CHAR, "char(1)" );
        registerColumnType( Types.VARCHAR, "varchar($l)" );
        registerColumnType( Types.FLOAT, "float" );
        registerColumnType( Types.DOUBLE, "double precision" );
        registerColumnType( Types.DATE, "datetime" );
        registerColumnType( Types.TIME, "datetime" );
        registerColumnType( Types.TIMESTAMP, "datetime" );
        registerColumnType( Types.VARBINARY, "varbinary($l)" );
        registerColumnType( Types.NUMERIC, "numeric($p,$s)" );
        registerColumnType( Types.BLOB, "image" );
        registerColumnType( Types.CLOB, "text" );

        registerColumnType( Types.VARBINARY, "image" );
        registerColumnType( Types.VARBINARY, 8000, "varbinary($l)" );

        registerColumnType( Types.VARCHAR, "nvarchar($l)" );
        registerColumnType( Types.CLOB, "nvarchar(max)");
    }

}
