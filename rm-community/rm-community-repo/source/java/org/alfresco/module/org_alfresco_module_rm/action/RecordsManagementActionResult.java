package org.alfresco.module.org_alfresco_module_rm.action;

/**
 * Records management action result.
 * 
 * @author Roy Wetherall
 */
public class RecordsManagementActionResult
{
    /** Result value */
    private Object value;
    
    /**
     * Constructor.
     * 
     * @param value result value
     */
    public RecordsManagementActionResult(Object value)
    {
        this.value = value;
    }
    
    /**
     * @return  result value
     */
    public Object getValue()
    {
        return this.value;
    }
}
