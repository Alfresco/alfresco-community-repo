package org.alfresco.util.schemacomp;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Results of a validation operation.
 * 
 * @author Matt Ward
 */
public class ValidationResult extends Result
{
    private DbProperty dbProperty;
    private String message;

    
    public ValidationResult(DbProperty dbProperty, String message)
    {
        this.dbProperty = dbProperty;
        this.message = message;
    }

    
    /**
     * @return the dbProperty that was rejected.
     */
    public DbProperty getDbProperty()
    {
        return this.dbProperty;
    }

    /**
     * @param dbProperty the dbProperty to set
     */
    public void setDbProperty(DbProperty dbProperty)
    {
        this.dbProperty = dbProperty;
    }

    
    @Override
    public String describe()
    {
        return I18NUtil.getMessage(
                    "system.schema_comp.validation",
                    getDbProperty().getDbObject().getTypeName(),
                    getDbProperty().getPath(),
                    getValue(),
                    message);
    }

    /**
     * @return the value that was rejected.
     */
    public Object getValue()
    {
        return this.dbProperty.getPropertyValue();
    }
}
