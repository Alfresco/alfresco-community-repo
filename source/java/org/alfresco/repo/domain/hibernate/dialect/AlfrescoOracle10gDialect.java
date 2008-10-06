package org.alfresco.repo.domain.hibernate.dialect;

import java.sql.Types;

import org.hibernate.dialect.Oracle10gDialect;

/**
 * Does away with the deprecated LONG datatype.
 * 
 * @author Derek Hulley
 * @since 2.2.2
 */
public class AlfrescoOracle10gDialect extends Oracle10gDialect
{
    public AlfrescoOracle10gDialect()
    {
        super();
        registerColumnType( Types.VARCHAR, "blob" );
    }
}
