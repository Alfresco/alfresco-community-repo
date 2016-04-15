package org.alfresco.repo.search.impl.querymodel.impl.db;

/**
 * @author Andy
 */
public class DBQueryBuilderJoinCommand
{
    String alias = null;

    DBQueryBuilderJoinCommandType type;

    Long qnameId = null;

    boolean outer = false;

    /**
     * @return the alias
     */
    public String getAlias()
    {
        return alias;
    }

    /**
     * @param alias the alias to set
     */
    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return type.toString();
    }

    /**
     * @param type the type to set
     */
    public void setType(DBQueryBuilderJoinCommandType type)
    {
        this.type = type;
    }

    /**
     * @return the qnameId
     */
    public Long getQnameId()
    {
        return qnameId;
    }

    /**
     * @param qnameId the qnameId to set
     */
    public void setQnameId(Long qnameId)
    {
        this.qnameId = qnameId;
    }

    /**
     * @return the outer
     */
    public boolean isOuter()
    {
        return outer;
    }

    /**
     * @param outer the outer to set
     */
    public void setOuter(boolean outer)
    {
        this.outer = outer;
    }
    
    
}
