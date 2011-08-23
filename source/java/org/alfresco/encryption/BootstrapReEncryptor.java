/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.encryption;

import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * 
 * @since 4.0
 *
 */
public class BootstrapReEncryptor extends AbstractLifecycleBean
{
	private boolean enabled;
	private ReEncryptor reEncryptor;
	private KeyStoreParameters oldKeyStoreParameters;
	
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public void setReEncryptor(ReEncryptor reEncryptor)
	{
		this.reEncryptor = reEncryptor;
	}

	public void setOldKeyStoreParameters(KeyStoreParameters oldKeyStoreParameters)
	{
		this.oldKeyStoreParameters = oldKeyStoreParameters;
	}
	
	public void reEncrypt()
	{
		reEncryptor.execute(oldKeyStoreParameters);
	}

    @Override
    protected void onBootstrap(ApplicationEvent event)
	{
    	if(enabled)
    	{
    		reEncrypt();
    	}
	}
    
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    }
}