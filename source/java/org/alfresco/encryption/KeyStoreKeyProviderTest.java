package org.alfresco.encryption;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.UnrecoverableKeyException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.encryption.KeyStoreLoader;
import org.alfresco.encryption.KeystoreKeyProvider;
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
        passwords.put(KeystoreKeyProvider.KEY_KEYSTORE_PASSWORD, "ksPwd2");
        passwords.put(ALIAS_ONE, "aliasPwd1");
        passwords.put(ALIAS_TWO, "aliasPwd2");
        KeystoreKeyProvider ks = new KeystoreKeyProvider(
                FILE_TWO,
                getKeyStoreLoader(),
                "SunJCE",
                "JCEKS",
                passwords);
        return ks;
    }
    
    protected static KeyStoreLoader getKeyStoreLoader()
    {
    	return new SpringKeyStoreLoader();
    }
    
    public void setUp() throws Exception
    {
    }
    
    public void testNoKeyStorePasswords() throws Exception
    {
        KeystoreKeyProvider keyProvider = new KeystoreKeyProvider(
                FILE_ONE,
                getKeyStoreLoader(),
                "SunJCE",
                "JCEKS",
                Collections.<String,String>emptyMap());
        // This has succeeded because we have not attempted to access it
        assertNull("Should be no keys available", keyProvider.getKey(ALIAS_ONE));
    }
    
    public void testKeyStoreWithOnlyAliasPasswords() throws Exception
    {
        KeystoreKeyProvider keyProvider = new KeystoreKeyProvider(
                FILE_TWO,
                getKeyStoreLoader(),
                "SunJCE",
                "JCEKS",
                Collections.singletonMap(ALIAS_ONE, "aliasPwd1"));
        // This has succeeded because we have not attempted to access it
        assertNotNull("Should be able to key alias with same password", keyProvider.getKey(ALIAS_ONE));
    }
    
    public void testAliasWithIncorrectPassword_One() throws Exception
    {
        try
        {
            new KeystoreKeyProvider(
                    FILE_ONE,
                    getKeyStoreLoader(),
                    "SunJCE",
                    "JCEKS",
                    Collections.singletonMap(ALIAS_ONE, "password_fail"));
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
            new KeystoreKeyProvider(
                    FILE_TWO,
                    getKeyStoreLoader(),
                    "SunJCE",
                    "JCEKS",
                    Collections.singletonMap(ALIAS_TWO, "password_fail"));
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
        KeystoreKeyProvider ks = new KeystoreKeyProvider(
                FILE_ONE,
                getKeyStoreLoader(),
                "SunJCE",
                "JCEKS",
                Collections.singletonMap(ALIAS_ONE, "aliasPwd1"));
        Key keyOne = ks.getKey(ALIAS_ONE);
        assertNotNull(keyOne);
    }
    
    public void testAliasWithCorrectPassword_Two() throws Exception
    {
        Map<String, String> passwords = new HashMap<String, String>(5);
        passwords.put(ALIAS_ONE, "aliasPwd1");
        passwords.put(ALIAS_TWO, "aliasPwd2");
        KeystoreKeyProvider ks = new KeystoreKeyProvider(
                FILE_TWO,
                getKeyStoreLoader(),
                "SunJCE",
                "JCEKS",
                passwords);
        assertNotNull(ks.getKey(ALIAS_ONE));
        assertNotNull(ks.getKey(ALIAS_TWO));
    }
    
    public void testAliasWithCorrectPassword_Three() throws Exception
    {
        Map<String, String> passwords = new HashMap<String, String>(5);
        passwords.put(ALIAS_ONE, "aliasPwd1");
        passwords.put(ALIAS_TWO, "aliasPwd2");
        passwords.put(ALIAS_THREE, "aliasPwd3");
        KeystoreKeyProvider ks = new KeystoreKeyProvider(
                FILE_THREE,
                getKeyStoreLoader(),
                "SunJCE",
                "JCEKS",
                passwords);
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
