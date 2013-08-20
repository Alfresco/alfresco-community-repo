package org.alfresco.rest.framework.resource.parameters;

/**
 * Search sort column 
 */
public class SortColumn
{
    public static final String ASCENDING = "ASC";
    public static final String DESCENDING = "DESC";
    
    /**
     * Constructor
     * 
     * @param column  column to sort on
     * @param asc  sort direction
     */
    public SortColumn(String column, boolean asc)
    {
        this.column = column;
        this.asc = asc;
    }
    
    public String column;
    public boolean asc;
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("SortColumn [column=");
        builder.append(this.column);
        builder.append(", asc=");
        builder.append(this.asc);
        builder.append("]");
        return builder.toString();
    }
}