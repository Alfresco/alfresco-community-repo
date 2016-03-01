 
package org.alfresco.module.org_alfresco_module_rm.email;

import java.util.List;
import java.util.Set;

/**
 * Custom EMail Mapping Service
 */
public interface CustomEmailMappingService
{
    /**
     * Get the list of custom mappings
     *
     * @return  {@link Set}<{@link CustomMapping}>
     */
    Set<CustomMapping> getCustomMappings();

    /**
     * Add custom mapping
     *
     * @param from
     * @param to
     */
    void addCustomMapping(String from, String to);

    /**
     * Delete custom mapping
     *
     * @param from
     * @param to
     */
    void deleteCustomMapping(String from, String to);

    /**
     * Gets the list of email mapping keys
     *
     * @return Email mapping keys
     */
    List<String> getEmailMappingKeys();

    /**
     * Registers an email mapping key with the existing list of email mapping keys
     *
     * @param emailMappingKey  emailMappingKey to register
     */
    void registerEMailMappingKey(String emailMappingKey);
}
