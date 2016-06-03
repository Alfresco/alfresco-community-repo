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
