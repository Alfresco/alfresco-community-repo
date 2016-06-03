package org.alfresco.util.schemacomp.model;

import org.alfresco.util.schemacomp.DbObjectVisitor;
import org.alfresco.util.schemacomp.DbProperty;
import org.alfresco.util.schemacomp.DiffContext;


/**
 * Represents a foreign key on a database table (<code>localColumn</code>) that references
 * <code>targetTable.targetColumn</code>
 * 
 * @author Matt Ward
 */
public class ForeignKey extends AbstractDbObject
{
    private String localColumn;
    private String targetTable;
    private String targetColumn;
    
    
    public ForeignKey(String name)
    {
        super(null, name);
    }
    
    /**
     * Constructor.
     * 
     * @param table the parent table
     * @param fkName String
     * @param localColumn String
     * @param targetTable String
     * @param targetColumn String
     */
    public ForeignKey(Table table, String fkName, String localColumn, String targetTable, String targetColumn)
    {
        super(table, fkName);
        this.localColumn = localColumn;
        this.targetTable = targetTable;
        this.targetColumn = targetColumn;
    }
    
    /**
     * @return the localColumn
     */
    public String getLocalColumn()
    {
        return this.localColumn;
    }
    
    /**
     * @param localColumn the localColumn to set
     */
    public void setLocalColumn(String localColumn)
    {
        this.localColumn = localColumn;
    }
    
    /**
     * @return the targetTable
     */
    public String getTargetTable()
    {
        return this.targetTable;
    }
    
    /**
     * @param targetTable the targetTable to set
     */
    public void setTargetTable(String targetTable)
    {
        this.targetTable = targetTable;
    }
    
    /**
     * @return the targetColumn
     */
    public String getTargetColumn()
    {
        return this.targetColumn;
    }
    
    /**
     * @param targetColumn the targetColumn to set
     */
    public void setTargetColumn(String targetColumn)
    {
        this.targetColumn = targetColumn;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.localColumn == null) ? 0 : this.localColumn.hashCode());
        result = prime * result + ((this.targetColumn == null) ? 0 : this.targetColumn.hashCode());
        result = prime * result + ((this.targetTable == null) ? 0 : this.targetTable.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        ForeignKey other = (ForeignKey) obj;
        if (this.localColumn == null)
        {
            if (other.localColumn != null) return false;
        }
        else if (!this.localColumn.equals(other.localColumn)) return false;
        if (this.targetColumn == null)
        {
            if (other.targetColumn != null) return false;
        }
        else if (!this.targetColumn.equals(other.targetColumn)) return false;
        if (this.targetTable == null)
        {
            if (other.targetTable != null) return false;
        }
        else if (!this.targetTable.equals(other.targetTable)) return false;
        return true;
    }
    
    
    @Override
    protected void doDiff(DbObject right, DiffContext ctx)
    {
        ForeignKey thatFK = (ForeignKey) right;
        comparisonUtils.compareSimple(
                    new DbProperty(this, "localColumn"),
                    new DbProperty(thatFK, "localColumn"),
                    ctx);
        comparisonUtils.compareSimple(
                    new DbProperty(this, "targetTable"),
                    new DbProperty(thatFK, "targetTable"),
                    ctx);
        comparisonUtils.compareSimple(
                    new DbProperty(this, "targetColumn"),
                    new DbProperty(thatFK, "targetColumn"),
                    ctx);        
    }

    @Override
    public void accept(DbObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public String getTypeName()
    {
        return "foreign key";
    }

    @Override
    public boolean sameAs(DbObject other)
    {
        if (other == null)
        {
            return false;
        }
        if (!getClass().equals(other.getClass()))
        {
            return false;
        }
        
        if ((getParent() != null && getParent().sameAs(other.getParent())))
        {
            ForeignKey otherFK = (ForeignKey) other;
            if (!getLocalColumn().equals(otherFK.getLocalColumn()))
            {
                return false;
            }
            // ALF-14129 fix, make table names case insensitive 
            if (!getTargetTable().equalsIgnoreCase(otherFK.getTargetTable()))
            {
                return false;
            }
            if (!getTargetColumn().equals(otherFK.getTargetColumn()))
            {
                return false;
            }
            return true;
        }
        
        return false;
    }
}
