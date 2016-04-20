package org.alfresco.repo.search.impl.querymodel.impl.db;

import java.io.Serializable;

import org.alfresco.repo.search.impl.querymodel.impl.BaseLiteralArgument;
import org.alfresco.service.namespace.QName;

/**
 * @author Andy
 *
 */
public class DBLiteralArgument extends BaseLiteralArgument
{

    /**
     * @param name String
     * @param type QName
     * @param value Serializable
     */
    public DBLiteralArgument(String name, QName type, Serializable value)
    {
        super(name, type, value);
    }

}
