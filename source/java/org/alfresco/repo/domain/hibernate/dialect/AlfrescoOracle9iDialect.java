package org.alfresco.repo.domain.hibernate.dialect;

import java.sql.Types;

import org.hibernate.dialect.Oracle9iDialect;

/**
 * Does away with the deprecated LONG datatype.
 * 
 * @author Derek Hulley
 * @since 2.2.2
 */
public class AlfrescoOracle9iDialect extends Oracle9iDialect
{
    public AlfrescoOracle9iDialect()
    {
        super();
        registerColumnType( Types.VARBINARY, "blob" );
    }
}
