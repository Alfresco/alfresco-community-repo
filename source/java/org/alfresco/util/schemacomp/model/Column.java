package org.alfresco.util.schemacomp.model;

import org.alfresco.util.schemacomp.DbObjectVisitor;
import org.alfresco.util.schemacomp.DbProperty;
import org.alfresco.util.schemacomp.DiffContext;

/**
 * Represents a column in a database table.
 * 
 * @author Matt Ward
 */
public class Column extends AbstractDbObject
{
    private String type;
    private boolean nullable;
    private boolean autoIncrement;
    private int order;
    private boolean compareOrder = true;
    
    public Column(String name)
    {
        super(null, name);
    }
    
    /**
     * Construct a Column.
     * 
     * @param table the parent table
     * @param name String
     * @param type String
     * @param nullable boolean
     */
    public Column(Table table, String name, String type, boolean nullable)
    {
        super(table, name);
        this.type = type;
        this.nullable = nullable;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return this.type;
    }
    
    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }
    
    /**
     * @return the nullable
     */
    public boolean isNullable()
    {
        return this.nullable;
    }
    
    /**
     * @param nullable the nullable to set
     */
    public void setNullable(boolean nullable)
    {
        this.nullable = nullable;
    }
    
    
    /**
     * @return the order
     */
    public int getOrder()
    {
        return this.order;
    }

    /**
     * @param order the order to set
     */
    public void setOrder(int order)
    {
        this.order = order;
    }

    
    /**
     * @return whether the column has an auto-increment flag set.
     */
    public boolean isAutoIncrement()
    {
        return this.autoIncrement;
    }

    /**
     * @param autoIncrement whether this column has the auto-increment flag set.
     */
    public void setAutoIncrement(boolean autoIncrement)
    {
        this.autoIncrement = autoIncrement;
    }


    /**
     * @return the compareOrder
     */
    public boolean isCompareOrder()
    {
        return this.compareOrder;
    }

    /**
     * @param compareOrder the compareOrder to set
     */
    public void setCompareOrder(boolean compareOrder)
    {
        this.compareOrder = compareOrder;
    }


    

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (this.autoIncrement ? 1231 : 1237);
        result = prime * result + (this.compareOrder ? 1231 : 1237);
        result = prime * result + (this.nullable ? 1231 : 1237);
        result = prime * result + this.order;
        result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        Column other = (Column) obj;
        if (this.autoIncrement != other.autoIncrement) return false;
        if (this.compareOrder != other.compareOrder) return false;
        if (this.nullable != other.nullable) return false;
        if (this.order != other.order) return false;
        if (this.type == null)
        {
            if (other.type != null) return false;
        }
        else if (!this.type.equals(other.type)) return false;
        return true;
    }

    @Override
    protected void doDiff(DbObject right, DiffContext ctx)
    {
        DbProperty thisTypeProp = new DbProperty(this, "type");
        DbProperty thisNullableProp = new DbProperty(this, "nullable");
        DbProperty thisOrderProp = new DbProperty(this, "order");
        DbProperty thisAutoIncProp = new DbProperty(this, "autoIncrement");
        
        Column thatColumn = (Column) right;
        DbProperty thatTypeProp = new DbProperty(thatColumn, "type");
        DbProperty thatNullableProp = new DbProperty(thatColumn, "nullable");
        DbProperty thatOrderProp = new DbProperty(thatColumn, "order");
        DbProperty thatAutoIncProp = new DbProperty(thatColumn, "autoIncrement");
        
        comparisonUtils.compareSimple(thisTypeProp, thatTypeProp, ctx);
        comparisonUtils.compareSimple(thisNullableProp, thatNullableProp, ctx);
        if (compareOrder)
        {
            comparisonUtils.compareSimple(thisOrderProp, thatOrderProp, ctx);
        }
        comparisonUtils.compareSimple(thisAutoIncProp, thatAutoIncProp, ctx);        
    }

    @Override
    public void accept(DbObjectVisitor visitor)
    {
        visitor.visit(this);
    }
}
