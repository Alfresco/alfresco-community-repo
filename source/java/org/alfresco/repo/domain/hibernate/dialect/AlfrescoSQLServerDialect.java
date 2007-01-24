package org.alfresco.repo.domain.hibernate.dialect;

import java.sql.Types;

import org.hibernate.dialect.SQLServerDialect;

public class AlfrescoSQLServerDialect extends SQLServerDialect
{

    public AlfrescoSQLServerDialect()
    {
        super();
        registerColumnType( Types.VARCHAR, "nvarchar($l)" );
    }

}
