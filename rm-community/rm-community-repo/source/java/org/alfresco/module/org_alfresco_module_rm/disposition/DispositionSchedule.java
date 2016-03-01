 
package org.alfresco.module.org_alfresco_module_rm.disposition;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Disposition schedule interface
 * 
 * @author Roy Wetherall
 */
public interface DispositionSchedule
{
    /**
     * Get the NodeRef that represents the disposition schedule
     * 
     * @return {@link NodeRef} of disposition schedule
     */
    NodeRef getNodeRef();
    
    /**
     * Get the disposition authority
     * 
     * @return  {@link String}  disposition authority
     */
    String getDispositionAuthority();
    
    /**
     * Get the disposition instructions
     * 
     * @return  {@link String}  disposition instructions
     */
    String getDispositionInstructions();
    
    /**
     * Indicates whether the disposal occurs at record level or not
     * 
     * @return  boolean true if at record level, false otherwise
     */
    boolean isRecordLevelDisposition();
    
    /**
     * Gets all the disposition action definitions for the schedule
     * 
     * @return  List<{@link DispositionActionDefinition}>   disposition action definitions
     */
    List<DispositionActionDefinition> getDispositionActionDefinitions();
    
    /**
     * Get the disposition action definition
     * 
     * @param id    the action definition id
     * @return {@link DispositionActionDefinition}  disposition action definition
     */
    DispositionActionDefinition getDispositionActionDefinition(String id);
    
    /**
     * Get the disposition action definition by the name of the disposition action
     * 
     * @param name  disposition action name
     * @return {@link DispositionActionDefinition}  disposition action definition, null if none
     */
    DispositionActionDefinition getDispositionActionDefinitionByName(String name);
}
