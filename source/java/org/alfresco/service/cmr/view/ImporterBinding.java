package org.alfresco.service.cmr.view;

import org.alfresco.service.namespace.QName;


/**
 * Encapsulation of Import binding parameters
 * 
 * @author David Caruana
 */
public interface ImporterBinding
{

    /**
     * UUID Binding 
     */
    public enum UUID_BINDING
    {
        CREATE_NEW, CREATE_NEW_WITH_UUID, REMOVE_EXISTING, REPLACE_EXISTING, UPDATE_EXISTING, THROW_ON_COLLISION
    }

    /**
     * Gets the Node UUID Binding
     * 
     * @return  UUID_BINDING
     */
    public UUID_BINDING getUUIDBinding();

    /**
     * Gets whether the search for imported node references should search within the import
     * transaction or not.
     * 
     * @return true => search within import transaction;  false => only search existing committed items 
     */
    public boolean allowReferenceWithinTransaction();
    
    /**
     * Gets a value for the specified name - to support simple name / value substitution
     * 
     * @param key   the value name
     * @return  the value
     */
    public String getValue(String key);

    /**
     * Gets the list of content model classes to exclude from import
     * 
     * @return  list of model class qnames to exclude (return null to indicate use of default list)
     */
    public QName[] getExcludedClasses();

    public ImporterContentCache getImportConentCache();
}
