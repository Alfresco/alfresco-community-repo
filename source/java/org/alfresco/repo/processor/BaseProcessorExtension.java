/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
