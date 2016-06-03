package org.alfresco.repo.domain.hibernate.dialect;

import java.sql.Types;

import org.hibernate.dialect.SybaseAnywhereDialect;

public class AlfrescoSybaseAnywhereDialect extends SybaseAnywhereDialect
{

    public AlfrescoSybaseAnywhereDialect()
    {
        super();
        registerColumnType( Types.VARCHAR, "text" );
    }

}
