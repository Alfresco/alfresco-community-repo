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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.UnrecoverableKeyException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Tests {@link KeystoreKeyProvider}
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class KeyStoreKeyProviderTest extends TestCase
{
    private static final String FILE_ONE = "classpath:alfresco/keystore-tests/ks-test-1.jks";
    private static final String FILE_TWO = "classpath:alfresco/keystore-tests/ks-test-2.jks";
    private static final String FILE_THREE = "classpath:alfresco/keystore-tests/ks-test-3.jks";
    private static final String ALIAS_ONE = "mykey1";
    private static final String ALIAS_TWO = "mykey2";
    private static final String ALIAS_THREE = "mykey3";
    
    /**
     * Helper utility to create a two-alias keystore.
     * <p/>
     * TODO: Allow the required aliases and key types to be specified and generate
     *       a keystore on the fly
     */
    /* package */ static KeystoreKeyProvider getTestKeyStoreProvider()
    {
        Map<String, String> passwords = new HashMap<String, String>(5);
        passwords.put(AlfrescoKeyStore.KEY_KEYSTORE_PASSWORD, "ksPwd2");
        passwords.put(ALIAS_ONE, "aliasPwd1");
        passwords.put(ALIAS_TWO, "aliasPwd2");
        KeyStoreParameters encryptionParameters = new KeyStoreParameters("test", "JCEKS", "SunJCE", null, FILE_TWO);
        KeystoreKeyProvider keyProvider = new KeystoreKeyProvider(encryptionParameters, getKeyStoreLoader(passwords));
//                FILE_TWO,
//                getKeyStoreLoader(),
//                "SunJCE",
//                "JCEKS",
//                passwords);
        return keyProvider;
    }

    /* package */ static KeystoreKeyProvider getTestKeyStoreProvider(String keyStoreLocation, Map<String, String> passwords)
    {
//        Map<String, String> passwords = new HashMap<String, String>(5);
//        passwords.put(KeyStoreManager.KEY_KEYSTORE_PASSWORD, "ksPwd2");
//        passwords.put(ALIAS_ONE, "aliasPwd1");
//        passwords.put(ALIAS_TWO, "aliasPwd2");
        KeyStoreParameters encryptionParameters = new KeyStoreParameters("test", "JCEKS", "SunJCE", null, keyStoreLocation);
        KeystoreKeyProvider keyProvider = new KeystoreKeyProvider(encryptionParameters, getKeyStoreLoader(passwords));
//                FILE_TWO,
//                getKeyStoreLoader(),
//                "SunJCE",
//                "JCEKS",
//                passwords);
        return keyProvider;
    }
    
    private static class TestKeyResourceLoader extends SpringKeyResourceLoader
    {
    	private Properties props;

    	TestKeyResourceLoader(Map<String, String> passwords)
    	{
    		StringBuilder aliases = new StringBuilder();
    		props = new Properties();

    		int i = 0;
    		for(Map.Entry<String, String> password : passwords.entrySet())
    		{
    			props.put(password.getKey() + ".password", password.getValue());

    			aliases.append(password.getKey());
    			if(i < passwords.size() - 1)
    			{
    				aliases.append(",");
    				i++;
    			}
    		}

    		props.put("aliases", aliases.toString());
    	}

		@Override
		public Properties loadKeyMetaData(String keyMetaDataFileLocation)
				throws IOException, FileNotFoundException
		{
			return props;
		}
    }
    
    protected static KeyResourceLoader getKeyStoreLoader(Map<String, String> passwords)
    {
    	return new TestKeyResourceLoader(passwords);
    }
    
    public void setUp() throws Exception
    {
    }
    
    public void testNoKeyStorePasswords() throws Exception
    {
    	KeystoreKeyProvider keyProvider = getTestKeyStoreProvider(FILE_ONE, Collections.<String,String>emptyMap());
        
//        KeystoreKeyProvider keyProvider = new KeystoreKeyProvider(
//                FILE_ONE,
//                getKeyStoreLoader(),
//                "SunJCE",
//                "JCEKS",
//                Collections.<String,String>emptyMap());
        // This has succeeded because we have not attempted to access it
        assertNull("Should be no keys available", keyProvider.getKey(ALIAS_ONE));
    }
    
    public void testKeyStoreWithOnlyAliasPasswords() throws Exception
    {
    	KeystoreKeyProvider keyProvider = getTestKeyStoreProvider(FILE_ONE, Collections.singletonMap(ALIAS_ONE, "aliasPwd1"));

//        KeystoreKeyProvider keyProvider = new KeystoreKeyProvider(
//                FILE_TWO,
//                getKeyStoreLoader(),
//                "SunJCE",
//                "JCEKS",
//                Collections.singletonMap(ALIAS_ONE, "aliasPwd1"));
        // This has succeeded because we have not attempted to access it
        assertNotNull("Should be able to key alias with same password", keyProvider.getKey(ALIAS_ONE));
    }
    
    public void testAliasWithIncorrectPassword_One() throws Exception
    {
        try
        {
        	getTestKeyStoreProvider(FILE_ONE, Collections.singletonMap(ALIAS_ONE, "password_fail"));	
        	
//            new KeystoreKeyProvider(
//                    FILE_ONE,
//                    getKeyStoreLoader(),
//                    "SunJCE",
//                    "JCEKS",
//                    Collections.singletonMap(ALIAS_ONE, "password_fail"));
            fail("Expect to fail because password is incorrect");
        }
        catch (AlfrescoRuntimeException e)
        {
            // Expected
            assertTrue(e.getCause() instanceof UnrecoverableKeyException);
        }
    }
    
    public void testAliasWithIncorrectPassword_Two() throws Exception
    {
        try
        {
        	getTestKeyStoreProvider(FILE_TWO, Collections.singletonMap(ALIAS_TWO, "password_fail"));	
//            new KeystoreKeyProvider(
//                    FILE_TWO,
//                    getKeyStoreLoader(),
//                    "SunJCE",
//                    "JCEKS",
//                    Collections.singletonMap(ALIAS_TWO, "password_fail"));
            fail("Expect to fail because password is incorrect");
        }
        catch (AlfrescoRuntimeException e)
        {
            // Expected
            assertTrue(e.getCause() instanceof UnrecoverableKeyException);
        }
    }
    
    public void testAliasWithCorrectPassword_One() throws Exception
    {
    	KeystoreKeyProvider ks = getTestKeyStoreProvider(FILE_ONE, Collections.singletonMap(ALIAS_ONE, "aliasPwd1"));
    	
//        KeystoreKeyProvider ks = new KeystoreKeyProvider(
//                FILE_ONE,
//                getKeyStoreLoader(),
//                "SunJCE",
//                "JCEKS",
//                Collections.singletonMap(ALIAS_ONE, "aliasPwd1"));
        Key keyOne = ks.getKey(ALIAS_ONE);
        assertNotNull(keyOne);
    }
    
    public void testAliasWithCorrectPassword_Two() throws Exception
    {
        Map<String, String> passwords = new HashMap<String, String>(5);
        passwords.put(ALIAS_ONE, "aliasPwd1");
        passwords.put(ALIAS_TWO, "aliasPwd2");

        KeystoreKeyProvider ks = getTestKeyStoreProvider(FILE_TWO, passwords);

//        KeystoreKeyProvider ks = new KeystoreKeyProvider(
//                FILE_TWO,
//                getKeyStoreLoader(),
//                "SunJCE",
//                "JCEKS",
//                passwords);

        assertNotNull(ks.getKey(ALIAS_ONE));
        assertNotNull(ks.getKey(ALIAS_TWO));
    }
    
    public void testAliasWithCorrectPassword_Three() throws Exception
    {
        Map<String, String> passwords = new HashMap<String, String>(5);
        passwords.put(ALIAS_ONE, "aliasPwd1");
        passwords.put(ALIAS_TWO, "aliasPwd2");
        passwords.put(ALIAS_THREE, "aliasPwd3");
        KeystoreKeyProvider ks = getTestKeyStoreProvider(FILE_THREE, passwords);
        
//        KeystoreKeyProvider ks = new KeystoreKeyProvider(
//                FILE_THREE,
//                getKeyStoreLoader(),
//                "SunJCE",
//                "JCEKS",
//                passwords);
        assertNotNull(ks.getKey(ALIAS_ONE));
        assertNotNull(ks.getKey(ALIAS_TWO));
        assertNull(ks.getKey(ALIAS_THREE));
    }
    
    /**
     * TODO: Do we need spring-crypto when it is V1.0?
     */
    public void DISABLED_testSpringCrypto() throws Throwable
    {
        ApplicationContext ctx = ApplicationContextHelper.getApplicationContext(
                new String[] {"alfresco/keystore-tests/encryption-test-context.xml"});
        @SuppressWarnings("unused")
        KeyStore ks1 = (KeyStore) ctx.getBean("ks-test-1");
    }
}
