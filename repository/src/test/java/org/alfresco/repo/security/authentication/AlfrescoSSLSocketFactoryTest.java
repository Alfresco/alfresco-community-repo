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
package org.alfresco.repo.security.authentication;

import static org.junit.Assert.fail;

import java.security.KeyStore;

import org.junit.Test;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * SSL socket factory test
 *
 * @author alex.mukha
 * @since 5.0
 */
public class AlfrescoSSLSocketFactoryTest
{
    private static final String KEYSTORE_TYPE = "JCEKS";

    @Test
    public void testConfiguration() throws Exception
    {
        KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
        // try to use the factory without initialization
        try
        {
            AlfrescoSSLSocketFactory.getDefault();
            fail("An AlfrescoRuntimeException should be thrown as the factory is not initialized.");
        }
        catch (AlfrescoRuntimeException are)
        {
            // Expected
        }

        // initialize and get an instance of AlfrescoSSLSocketFactory
        AlfrescoSSLSocketFactory.initTrustedSSLSocketFactory(ks);
        AlfrescoSSLSocketFactory.getDefault();
    }
}
