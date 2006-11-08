/**
 * 
 */
package org.alfresco.repo.jscript;

import org.alfresco.service.cmr.repository.ScriptImplementation;
import org.alfresco.service.cmr.repository.ScriptService;

/**
 * Abstract base class for a script implementation
 * 
 * @author Roy Wetherall
 */
public abstract class BaseScriptImplementation implements ScriptImplementation
{
	/** The script service */
	private ScriptService scriptService;
	
	/** The name of the script */
	private String scriptName;
	
	/**
	 * Sets the script service
	 * 
	 * @param scriptService		the script service
	 */
	public void setScriptService(ScriptService scriptService)
	{
		this.scriptService = scriptService;
	}
	
	/**
	 * Registers this script with the script service
	 */
	public void register()
	{
		this.scriptService.registerScript(this);
	}
	
	/**
	 * Sets the script name
	 * 
	 * @param scriptName the script name
	 */
	public void setScriptName(String scriptName)
	{
		this.scriptName = scriptName;
	}
    
    /**
     * @see org.alfresco.service.cmr.repository.ScriptImplementation#getScriptName()
     */
    public String getScriptName()
    {
    	return this.scriptName;
    }
}
