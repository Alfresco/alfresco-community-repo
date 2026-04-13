/*
 * Copyright (C) 2005-2020 Alfresco Software Limited.
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.alfresco.error.AlfrescoRuntimeException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AlfrescoKeyStoreTest
{
    @Mock
    private EncryptionKeysRegistry encryptionKeysRegistry;

    @Rule
    public TestName testName = new TestName();

    @Test
    public void testSysPropConfig()
    {
        String keyStoreId = testName.getMethodName() + "-keystore";
        String alias1 = "mykey1";
        String alias2 = "mykey2";

        KeyStoreParameters keyStoreParameters = new KeyStoreParameters();
        keyStoreParameters.setId(keyStoreId);
        keyStoreParameters.setName(testName.getMethodName());
        keyStoreParameters.setType("JCEKS");
        keyStoreParameters.setProvider("SunJCE");
        keyStoreParameters.setLocation("classpath:keystore-tests/ks-test-2.jks");

        System.setProperty(keyStoreId + "." + "password", "ksPwd2");
        System.setProperty(keyStoreId + "." + "aliases", alias1 + "," + alias2);
        System.setProperty(keyStoreId + "." + alias1 + "." + "password", "aliasPwd1");
        System.setProperty(keyStoreId + "." + alias2 + "." + "password", "aliasPwd2");

        try
        {
            AlfrescoKeyStore alfrescoKeyStore = new AlfrescoKeyStoreImpl(keyStoreParameters, new SpringKeyResourceLoader());
            Set<String> expectedAliases = new HashSet<>();
            expectedAliases.add(alias1);
            expectedAliases.add(alias2);
            Assert.assertEquals("The aliases are not correct", expectedAliases, alfrescoKeyStore.getKeyAliases());

            Assert.assertNotNull("Failed to retrieve a key from keystore.", alfrescoKeyStore.getKey(alias1));
            Assert.assertNotNull("Failed to retrieve a key from keystore.", alfrescoKeyStore.getKey(alias2));
        }
        finally
        {
            System.clearProperty(keyStoreId + "." + "password");
            System.clearProperty(keyStoreId + "." + "aliases");
            System.clearProperty(keyStoreId + "." + alias1 + "." + "password");
            System.clearProperty(keyStoreId + "." + alias2 + "." + "password");
        }

    }

    @Test
    public void testSysPropConfigWithoutAliases()
    {
        String keyStoreId = testName.getMethodName() + "-keystore";
        String alias1 = "mykey1";

        KeyStoreParameters keyStoreParameters = new KeyStoreParameters();
        keyStoreParameters.setId(keyStoreId);
        keyStoreParameters.setName(testName.getMethodName());
        keyStoreParameters.setType("JCEKS");
        keyStoreParameters.setProvider("SunJCE");
        keyStoreParameters.setLocation("classpath:keystore-tests/ks-test-1.jks");

        System.setProperty(keyStoreId + "." + "password", "ksPwd1");
        System.setProperty(keyStoreId + "." + alias1 + "." + "password", "aliasPwd1");
        try
        {
            AlfrescoKeyStore keyStore = new AlfrescoKeyStoreImpl(keyStoreParameters, new SpringKeyResourceLoader());
            Assert.assertNull(keyStore.getKey(alias1));
        }
        finally
        {
            System.clearProperty(keyStoreId + "." + "password");
            System.clearProperty(keyStoreId + "." + alias1 + "." + "password");
        }
    }

    @Test
    public void testMetaDataFileConfig()
    {
        String alias1 = "mykey1";

        KeyStoreParameters keyStoreParameters = new KeyStoreParameters();
        keyStoreParameters.setName(testName.getMethodName());
        keyStoreParameters.setType("JCEKS");
        keyStoreParameters.setProvider("SunJCE");
        keyStoreParameters.setLocation("classpath:keystore-tests/ks-test-1.jks");
        keyStoreParameters.setKeyMetaDataFileLocation("classpath:keystore-tests/ks1-metadata.properties");

        AlfrescoKeyStore alfrescoKeyStore = new AlfrescoKeyStoreImpl(keyStoreParameters, new SpringKeyResourceLoader());
        Set<String> expectedAliases = new HashSet<>();
        expectedAliases.add(alias1);
        Assert.assertEquals("The aliases are not correct", expectedAliases, alfrescoKeyStore.getKeyAliases());

        Assert.assertNotNull("Failed to retrieve a key from keystore.", alfrescoKeyStore.getKey(alias1));
    }

    /**
     * Config via System props should be default, but if the metadata file location is set, it will be used instead.
     * This is done to maintain backwards compatibility and simplify testing use cases.
     */
    @Test(expected = AlfrescoRuntimeException.class)
    public void testConfigBothSystemAndFile()
    {
        String keyStoreId = testName.getMethodName() + "-keystore";
        String alias1 = "mykey1";
        String alias2 = "mykey2";

        KeyStoreParameters keyStoreParameters = new KeyStoreParameters();
        keyStoreParameters.setId(keyStoreId);
        keyStoreParameters.setName(testName.getMethodName());
        keyStoreParameters.setType("JCEKS");
        keyStoreParameters.setProvider("SunJCE");
        keyStoreParameters.setLocation("classpath:keystore-tests/ks-test-2.jks");
        // use metadata file from keystore with one key
        keyStoreParameters.setKeyMetaDataFileLocation("classpath:keystore-tests/ks1-metadata.properties");

        System.setProperty(keyStoreId + "." + "password", "ksPwd2");
        System.setProperty(keyStoreId + "." + "aliases", alias1 + "," + alias2);
        System.setProperty(keyStoreId + "." + alias1 + "." + "password", "aliasPwd1");
        System.setProperty(keyStoreId + "." + alias2 + "." + "password", "aliasPwd2");

        try
        {
            new AlfrescoKeyStoreImpl(keyStoreParameters, new SpringKeyResourceLoader());
        }
        finally
        {
            System.clearProperty(keyStoreId + "." + "password");
            System.clearProperty(keyStoreId + "." + "aliases");
            System.clearProperty(keyStoreId + "." + alias1 + "." + "password");
            System.clearProperty(keyStoreId + "." + alias2 + "." + "password");
        }

    }

    @Test(expected = AlfrescoRuntimeException.class)
    public void testValidateKeysWrongAliasMetadataFile() throws Exception
    {
        String alias1 = "mykey1";

        KeyStoreParameters keyStoreParameters = new KeyStoreParameters();
        keyStoreParameters.setName(testName.getMethodName());
        keyStoreParameters.setType("JCEKS");
        keyStoreParameters.setProvider("SunJCE");
        keyStoreParameters.setLocation("classpath:keystore-tests/ks-test-1.jks");
        keyStoreParameters.setKeyMetaDataFileLocation("classpath:keystore-tests/wrong-alias-metadata.properties");

        AlfrescoKeyStoreImpl alfrescoKeyStore = new AlfrescoKeyStoreImpl(keyStoreParameters, new SpringKeyResourceLoader());
        alfrescoKeyStore.setKeysToValidate(Collections.singleton(alias1));
        alfrescoKeyStore.setValidateKeyChanges(true);
        alfrescoKeyStore.setEncryptionKeysRegistry(encryptionKeysRegistry);
        alfrescoKeyStore.validateKeys();
    }

    @Test
    public void testEmptyKeysMetadataFile()
    {
        KeyStoreParameters keyStoreParameters = new KeyStoreParameters();
        keyStoreParameters.setName(testName.getMethodName());
        keyStoreParameters.setType("JCEKS");
        keyStoreParameters.setProvider("SunJCE");
        keyStoreParameters.setLocation("classpath:keystore-tests/ks-test-1.jks");
        keyStoreParameters.setKeyMetaDataFileLocation("classpath:keystore-tests/empty-alias-metadata.properties");

        new AlfrescoKeyStoreImpl(keyStoreParameters, new SpringKeyResourceLoader());
    }

    @Test(expected = AlfrescoRuntimeException.class)
    public void testWrongKeystorePasswordMetadataFile()
    {
        KeyStoreParameters keyStoreParameters = new KeyStoreParameters();
        keyStoreParameters.setName(testName.getMethodName());
        keyStoreParameters.setType("JCEKS");
        keyStoreParameters.setProvider("SunJCE");
        keyStoreParameters.setLocation("classpath:keystore-tests/ks-test-1.jks");
        keyStoreParameters.setKeyMetaDataFileLocation("classpath:keystore-tests/wrong-keystore-password-metadata.properties");

        new AlfrescoKeyStoreImpl(keyStoreParameters, new SpringKeyResourceLoader());
    }

    @Test(expected = AlfrescoRuntimeException.class)
    public void testWrongKeyPasswordMetadataFile()
    {
        KeyStoreParameters keyStoreParameters = new KeyStoreParameters();
        keyStoreParameters.setName(testName.getMethodName());
        keyStoreParameters.setType("JCEKS");
        keyStoreParameters.setProvider("SunJCE");
        keyStoreParameters.setLocation("classpath:keystore-tests/ks-test-1.jks");
        keyStoreParameters.setKeyMetaDataFileLocation("classpath:keystore-tests/wrong-key-password-metadata.properties");

        new AlfrescoKeyStoreImpl(keyStoreParameters, new SpringKeyResourceLoader());
    }

    @Test(expected = AlfrescoRuntimeException.class)
    public void testValidateKeysWrongAliasSysProps() throws Exception
    {
        String keyStoreId = testName.getMethodName() + "-keystore";
        String alias1 = "mykey1";

        KeyStoreParameters keyStoreParameters = new KeyStoreParameters();
        keyStoreParameters.setId(keyStoreId);
        keyStoreParameters.setName(testName.getMethodName());
        keyStoreParameters.setType("JCEKS");
        keyStoreParameters.setProvider("SunJCE");
        keyStoreParameters.setLocation("classpath:keystore-tests/ks-test-1.jks");

        System.setProperty(keyStoreId + "." + "aliases", "wrong-alias,another-wrong-alias");
        System.setProperty(keyStoreId + "." + "password", "ksPwd1");
        System.setProperty(keyStoreId + "." + alias1 + "." + "password", "aliasPwd1");

        try
        {
            AlfrescoKeyStoreImpl alfrescoKeyStore = new AlfrescoKeyStoreImpl(keyStoreParameters, new SpringKeyResourceLoader());
            alfrescoKeyStore.setValidateKeyChanges(true);
            alfrescoKeyStore.setKeysToValidate(Collections.singleton(alias1));
            alfrescoKeyStore.setEncryptionKeysRegistry(encryptionKeysRegistry);
            alfrescoKeyStore.validateKeys();
        }
        finally
        {
            System.clearProperty(keyStoreId + "." + "aliases");
            System.clearProperty(keyStoreId + "." + "password");
            System.clearProperty(keyStoreId + "." + alias1 + "." + "password");
        }
    }

    @Test
    public void testEmptyKeysSysProps()
    {
        String keyStoreId = testName.getMethodName() + "-keystore";

        KeyStoreParameters keyStoreParameters = new KeyStoreParameters();
        keyStoreParameters.setId(keyStoreId);
        keyStoreParameters.setName(testName.getMethodName());
        keyStoreParameters.setType("JCEKS");
        keyStoreParameters.setProvider("SunJCE");
        keyStoreParameters.setLocation("classpath:keystore-tests/ks-test-1.jks");

        System.setProperty(keyStoreId + "." + "aliases", "empty-alias,another-empty-alias");
        System.setProperty(keyStoreId + "." + "password", "ksPwd1");

        try
        {
            new AlfrescoKeyStoreImpl(keyStoreParameters, new SpringKeyResourceLoader());
        }
        finally
        {
            System.clearProperty(keyStoreId + "." + "aliases");
            System.clearProperty(keyStoreId + "." + "password");
        }
    }

    @Test(expected = AlfrescoRuntimeException.class)
    public void testWrongKeystorePasswordSysProps()
    {
        String keyStoreId = testName.getMethodName() + "-keystore";
        String alias1 = "mykey1";

        KeyStoreParameters keyStoreParameters = new KeyStoreParameters();
        keyStoreParameters.setId(keyStoreId);
        keyStoreParameters.setName(testName.getMethodName());
        keyStoreParameters.setType("JCEKS");
        keyStoreParameters.setProvider("SunJCE");
        keyStoreParameters.setLocation("classpath:keystore-tests/ks-test-1.jks");

        System.setProperty(keyStoreId + "." + "aliases", alias1);
        System.setProperty(keyStoreId + "." + "password", "wrong-password");
        System.setProperty(keyStoreId + "." + alias1 + "." + "password", "aliasPwd1");

        try
        {
            new AlfrescoKeyStoreImpl(keyStoreParameters, new SpringKeyResourceLoader());
        }
        finally
        {
            System.clearProperty(keyStoreId + "." + "aliases");
            System.clearProperty(keyStoreId + "." + "password");
            System.clearProperty(keyStoreId + "." + alias1 + "." + "password");
        }
    }

    @Test(expected = AlfrescoRuntimeException.class)
    public void testWrongKeyPasswordSysProps()
    {
        String keyStoreId = testName.getMethodName() + "-keystore";
        String alias1 = "mykey1";

        KeyStoreParameters keyStoreParameters = new KeyStoreParameters();
        keyStoreParameters.setId(keyStoreId);
        keyStoreParameters.setName(testName.getMethodName());
        keyStoreParameters.setType("JCEKS");
        keyStoreParameters.setProvider("SunJCE");
        keyStoreParameters.setLocation("classpath:keystore-tests/ks-test-1.jks");

        System.setProperty(keyStoreId + "." + "aliases", alias1);
        System.setProperty(keyStoreId + "." + "password", "ksPwd1");
        System.setProperty(keyStoreId + "." + alias1 + "." + "password", "wrong-key-password");

        try
        {
            new AlfrescoKeyStoreImpl(keyStoreParameters, new SpringKeyResourceLoader());
        }
        finally
        {
            System.clearProperty(keyStoreId + "." + "aliases");
            System.clearProperty(keyStoreId + "." + "password");
            System.clearProperty(keyStoreId + "." + alias1 + "." + "password");
        }
    }

    /**
     * No exception is expected. An empty keystore can be created.
     */
    @Test
    public void testConfigEmptyKeystore()
    {
        String keyStoreId = testName.getMethodName() + "-keystore";

        KeyStoreParameters keyStoreParameters = new KeyStoreParameters();
        keyStoreParameters.setId(keyStoreId);
        keyStoreParameters.setName(testName.getMethodName());
        keyStoreParameters.setType("JCEKS");
        keyStoreParameters.setProvider("SunJCE");
        keyStoreParameters.setLocation("classpath:non-existing-path/some-keystore.jks");

        new AlfrescoKeyStoreImpl(keyStoreParameters, new SpringKeyResourceLoader());
    }

    @Test(expected = AlfrescoRuntimeException.class)
    public void testValidateKeysEmptyAliasSysProps() throws Exception
    {
        String keyStoreId = testName.getMethodName() + "-keystore";

        KeyStoreParameters keyStoreParameters = new KeyStoreParameters();
        keyStoreParameters.setId(keyStoreId);
        keyStoreParameters.setName(testName.getMethodName());
        keyStoreParameters.setType("JCEKS");
        keyStoreParameters.setProvider("SunJCE");
        keyStoreParameters.setLocation("classpath:keystore-tests/ks-test-1.jks");

        System.setProperty(keyStoreId + "." + "password", "ksPwd1");

        try
        {
            AlfrescoKeyStoreImpl alfrescoKeyStore = new AlfrescoKeyStoreImpl(keyStoreParameters, new SpringKeyResourceLoader());
            alfrescoKeyStore.setValidateKeyChanges(true);
            alfrescoKeyStore.setKeysToValidate(Collections.singleton("non-existing-alias"));
            alfrescoKeyStore.setEncryptionKeysRegistry(encryptionKeysRegistry);
            alfrescoKeyStore.validateKeys();
        }
        finally
        {
            System.clearProperty(keyStoreId + "." + "password");
        }
    }
}
