 
package org.alfresco.module.org_alfresco_module_rm.query;

/**
 * Records management query DAO
 * 
 * NOTE:  a place holder that can be extended later when we want to enhance performance with canned queries.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public interface RecordsManagementQueryDAO
{
    /**
     * Get the number of objects with the given identifier value.
     * 
     * Note:  this is provided as an example and is not currently used
     * 
     * @param identifierValue   id value
     * @return int  count
     */
    int getCountRmaIdentifier(String identifierValue);
}
