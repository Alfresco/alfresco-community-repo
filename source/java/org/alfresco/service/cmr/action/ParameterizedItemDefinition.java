package org.alfresco.service.cmr.action;

import java.util.List;

import org.alfresco.api.AlfrescoPublicApi;

@AlfrescoPublicApi
public interface ParameterizedItemDefinition 
{
	/**
	 * Get the name of the rule item.
	 * <p>
	 * The name is unique and is used to identify the rule item.
	 * 
	 * @return	the name of the rule action
	 */
	public String getName();
	
	/**
	 * The title of the parameterized item definition
	 * 
	 * @return	the title
	 */
	public String getTitle();
	
	/**
	 * The description of the parameterized item definition
	 * 
	 * @return	the description
	 */
	public String getDescription();
	
	/**
	 * Indicates whether the parameterized item allows adhoc properties to be set
	 * 
	 * @return	true if ashoc properties are allowed, false otherwise
	 */
	public boolean getAdhocPropertiesAllowed();
    
    /**
     * Indicates whether the parameterized item has any parameter definitions
     * 
     * @return true if the parameterized item has any parameter definitions, false otherwise
     */
    public boolean hasParameterDefinitions();
	
	/**
	 * A list containing the parmameter defintions for this rule item.
	 * 
	 * @return	a list of parameter definitions
	 */
	public List<ParameterDefinition> getParameterDefinitions();
    
    /**
     * Get the parameter definition by name
     * 
     * @param name  the name of the parameter
     * @return      the parameter definition, null if none found
     */
    public ParameterDefinition getParameterDefintion(String name);
}
