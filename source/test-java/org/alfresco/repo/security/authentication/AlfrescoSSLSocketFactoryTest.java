package org.alfresco.repo.security.authentication;

import org.alfresco.error.AlfrescoRuntimeException;
import org.junit.Test;

import java.security.KeyStore;

import static org.junit.Assert.fail;

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
