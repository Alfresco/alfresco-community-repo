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
package org.alfresco.repo.domain.hibernate.dialect;

import java.sql.Types;

import org.hibernate.dialect.Oracle9Dialect;

/**
 * Does away with the deprecated LONG datatype.  This extends the deprecated
 * <code>Oracle9Dialect</code> for good reason: Hibernate ceased supporting
 * <b>right outer join</b> in the <code>Oracle9iDialect</code>.
 * 
 * @author Derek Hulley
 * @since 2.2.2
 */
@SuppressWarnings("deprecation")
public class AlfrescoOracle9Dialect extends Oracle9Dialect
{
    public AlfrescoOracle9Dialect()
    {
        super();
        registerColumnType(Types.VARBINARY, "blob");
        registerColumnType(Types.NVARCHAR, 4000, "nvarchar2($l)");
    }
}
