package org.alfresco.util.schemacomp;

/**
 * Base class for the result of a differencing or validation operation.
 *  
 * @author Matt Ward
 */
public abstract class Result
{    
    /**
     * A loggable message to describe the comparison result. Default implementation
     * delegates to toString() but this should generally be overridden as toString()
     * is used in a multitude of contexts.
     * 
     * @return String
     */
    public String describe()
    {
        return toString();
    }
}
