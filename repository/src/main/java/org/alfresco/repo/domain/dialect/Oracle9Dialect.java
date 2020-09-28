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
 * Does away with the deprecated LONG datatype.  This extends the deprecated
 * <code>Oracle9Dialect</code> for good reason: Hibernate ceased supporting
 * <b>right outer join</b> in the <code>Oracle9iDialect</code>.
 * 
 * @since 6.0
 */
public class Oracle9Dialect extends Dialect
{
    public Oracle9Dialect()
    {
        super();
        registerColumnType( Types.BIT, "number(1,0)" );
        registerColumnType( Types.BIGINT, "number(19,0)" );
        registerColumnType( Types.SMALLINT, "number(5,0)" );
        registerColumnType( Types.TINYINT, "number(3,0)" );
        registerColumnType( Types.INTEGER, "number(10,0)" );
        registerColumnType( Types.CHAR, "char(1 char)" );
        registerColumnType( Types.VARCHAR, 4000, "varchar2($l char)" );
        registerColumnType( Types.VARCHAR, "long" );
        registerColumnType( Types.FLOAT, "float" );
        registerColumnType( Types.DOUBLE, "double precision" );
        registerColumnType( Types.DATE, "date" );
        registerColumnType( Types.TIME, "date" );
        registerColumnType( Types.TIMESTAMP, "timestamp" );
        registerColumnType( Types.VARBINARY, 2000, "raw($l)" );
        registerColumnType( Types.VARBINARY, "long raw" );
        registerColumnType( Types.NUMERIC, "number($p,$s)" );
        registerColumnType( Types.DECIMAL, "number($p,$s)" );
        registerColumnType( Types.BLOB, "blob" );
        registerColumnType( Types.CLOB, "clob" );

        registerColumnType( Types.VARBINARY, "blob");
        registerColumnType( Types.NVARCHAR, 4000, "nvarchar2($l)");
    }
}
