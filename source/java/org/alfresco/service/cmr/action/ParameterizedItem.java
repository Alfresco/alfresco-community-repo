package org.alfresco.service.cmr.action;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Rule item interface
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface ParameterizedItem
{
	/**
	 * Unique identifier for the parameterized item
	 * 
	 * @return	the id string
	 */
	public String getId();
	
	/**
	 * Get the parameter values
	 * 
	 * @return	get the parameter values
	 */
	public Map<String, Serializable> getParameterValues();
	
	/**
	 * Get value of a named parameter.
	 * 
	 * @param name	the parameter name
	 * @return		the value of the parameter
	 */
	public Serializable getParameterValue(String name);
	
	/**
	 * Sets the parameter values
	 * 
	 * @param parameterValues	the parameter values
	 */
	public void setParameterValues(
            Map<String, Serializable> parameterValues);
	
	/**
	 * Sets the value of a parameter.
	 * 
	 * @param name		the parameter name
	 * @param value		the parameter value
	 */
	public void setParameterValue(String name, Serializable value);
}
