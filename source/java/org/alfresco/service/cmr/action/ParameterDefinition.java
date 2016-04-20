package org.alfresco.service.cmr.action;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.namespace.QName;

/**
 * Parameter definition interface.
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface ParameterDefinition 
{
	/**
	 * Get the name of the parameter.
	 * <p>
	 * This is unique and is used to identify the parameter.
	 * 
	 * @return	the parameter name
	 */
	public String getName();
	
	/**
	 * Get the type of parameter
	 * 
	 * @return	the parameter type qname
	 */
	public QName getType();
	
    /**
     * Is multi-valued?
     */
    public boolean isMultiValued();
    
	/**
	 * Indicates whether the parameter is mandatory or not.
	 * <p>
	 * If a parameter is mandatory it means that the value can not be null.
	 * 
	 * @return	true if the parameter is mandatory, false otherwise
	 */
	public boolean isMandatory();
	
	/**
	 * Get the display label of the parameter.
	 * 
	 * @return	the parameter display label
	 */
	public String getDisplayLabel();
	
	/**
	 * Gets the parameter constraint name, null if none set.
	 * 
	 * @return   the parameter constraint name
	 */
	public String getParameterConstraintName();
	
}
