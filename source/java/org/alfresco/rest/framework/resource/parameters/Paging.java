package org.alfresco.rest.framework.resource.parameters;

import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;

/**
 * Represents paging of collections of resources. Set by the client request.<br/>
 * skipCount - How many entries exist in the entire collection before those included in the list<br/>
 * maxItems - The maximum number of items the client requires. Defaults to 100.
 * 
 * @author Gethin James
 */
public class Paging
{
    
    public static final int DEFAULT_SKIP_COUNT = 0;
    public static final int DEFAULT_MAX_ITEMS = 100;
    public static final Paging DEFAULT = Paging.valueOf(DEFAULT_SKIP_COUNT, DEFAULT_MAX_ITEMS);
        
    private final int skipCount;
    private final int maxItems;

    private Paging(int skipCount, int maxItems)
    {
        super();
        if(skipCount < 0)
        {
        	throw new InvalidArgumentException();
        }
        if(maxItems < 1)
        {
        	throw new InvalidArgumentException();
        }
        this.skipCount = skipCount;
        this.maxItems = maxItems;
    }

    /**
     * How many entries exist in the entire collection before those included in the list
     * @return Integer
     */
    public int getSkipCount()
    {
        return this.skipCount;
    }

    /**
     * The maximum number of items the client requires. Defaults to 100.
     * @return Integer
     */
    public int getMaxItems()
    {
        return this.maxItems;
    }

    public static Paging valueOf(int skipCount, int maxItems)
    {
        return new Paging(skipCount,maxItems);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Paging [skipCount=");
        builder.append(this.skipCount);
        builder.append(", maxItems=");
        builder.append(this.maxItems);
        builder.append("]");
        return builder.toString();
    }

}
