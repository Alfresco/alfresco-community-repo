package org.alfresco.repo.search.impl.querymodel.impl.db;

import java.util.List;

import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.impl.BaseListArgument;

/**
 * @author Andy
 *
 */
public class DBListArgument extends BaseListArgument
{

    /**
     * @param name String
     * @param arguments List<Argument>
     */
    public DBListArgument(String name, List<Argument> arguments)
    {
        super(name, arguments);
    }

}
