package org.alfresco.repo.processor;

import org.alfresco.api.AlfrescoPublicApi;    
import org.alfresco.processor.Processor;
import org.alfresco.processor.ProcessorExtension;

/**
 * Abstract base class for a processor extension
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public abstract class BaseProcessorExtension implements ProcessorExtension
{
	/** The processor */
	private Processor processor;
	
	/** The name of the extension */
	private String extensionName;
	
	/**
	 * Sets the processor
	 * 
	 * @param processor		the processor
	 */
	public void setProcessor(Processor processor)
	{
		this.processor = processor;
	}
	
	/**
	 * Registers this script with the script service
	 */
	public void register()
	{
		this.processor.registerProcessorExtension(this);
	}
	
	/**
	 * Sets the extension name
	 * 
	 * @param extension the extension name
	 */
	public void setExtensionName(String extension)
	{
		this.extensionName = extension;
	}
    
    /**
     * @see org.alfresco.processor.ProcessorExtension#getExtensionName()
     */
    public String getExtensionName()
    {
    	return this.extensionName;
    }
}
