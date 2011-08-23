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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * The EncryptionChecker checks the state of the repository's encryption system.
 * In particular it checks:
 * <ul>
 *    <li> that the keystore exists and, if not, creates one.
 *    <li> that the encryption keys have not been changed. If so, the bootstrap will be halted.
 * </ul>
 * 
 * @since 4.0
 *
 */
public class EncryptionChecker extends AbstractLifecycleBean
{
    private static Log logger = LogFactory.	getLog(EncryptionChecker.class);
    
    private KeyStoreChecker keyStoreChecker;
    private KeyStoreParameters keyStoreParameters;
    private KeyResourceLoader keyResourceLoader;

	public void setkeyStoreParameters(KeyStoreParameters keyStoreParameters)
	{
		this.keyStoreParameters = keyStoreParameters;
	}

	public void setKeyStoreChecker(KeyStoreChecker keyStoreChecker)
	{
		this.keyStoreChecker = keyStoreChecker;
	}
	
	public void setKeyResourceLoader(KeyResourceLoader keyResourceLoader)
	{
		this.keyResourceLoader = keyResourceLoader;
	}

	@Override
	protected void onBootstrap(ApplicationEvent event)
	{
		AlfrescoKeyStore mainKeyStore = new CachingKeyStore(keyStoreParameters, keyResourceLoader);
		keyStoreChecker.checkKeyStore(mainKeyStore);
	}

	@Override
	protected void onShutdown(ApplicationEvent event)
	{
		
	}
}
