/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.encryption;

import java.io.Serializable;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryBootstrap;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.Pair;
import org.alfresco.util.testing.category.FrequentlyFailingTests;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

public class EncryptionTests extends TestCase
{
	private static final String TEST_MODEL = "org/alfresco/encryption/reencryption_model.xml";
	private static final SecureRandom SECURE_RANDOM = getSecureRandomInstance();

	private static int NUM_PROPERTIES = 500;
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private static QName NODE_TYPE = QName.createQName("http://www.alfresco.org/test/reencryption_test/1.0", "base");
    private static QName PROP = QName.createQName("http://www.alfresco.org/test/reencryption_test/1.0", "prop1");
    
    private NodeRef rootNodeRef;
    
    private TransactionService transactionService;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private MetadataEncryptor metadataEncryptor;
	private ReEncryptor reEncryptor;
	private String cipherAlgorithm = "DESede/CBC/PKCS5Padding";
	private KeyStoreParameters backupKeyStoreParameters;
	private AlfrescoKeyStoreImpl mainKeyStore;
	//private AlfrescoKeyStoreImpl backupKeyStore;
	private KeyResourceLoader keyResourceLoader;
	private EncryptionKeysRegistryImpl encryptionKeysRegistry;
	private KeyStoreChecker keyStoreChecker;
	private DefaultEncryptor mainEncryptor;
	private DefaultEncryptor backupEncryptor;

    private AuthenticationComponent authenticationComponent;
	private DictionaryDAO dictionaryDAO;
	private TenantService tenantService;

	private String keyAlgorithm;
	private KeyMap newKeys = new KeyMap();
	private List<NodeRef> before = new ArrayList<NodeRef>();
	private List<NodeRef> after = new ArrayList<NodeRef>();

	private UserTransaction tx;

	public void setUp() throws Exception
	{
        dictionaryService = (DictionaryService)ctx.getBean("dictionaryService");
        nodeService = (NodeService)ctx.getBean("nodeService");
        transactionService = (TransactionService)ctx.getBean("transactionService");
        tenantService = (TenantService)ctx.getBean("tenantService");
        dictionaryDAO = (DictionaryDAO)ctx.getBean("dictionaryDAO");
        metadataEncryptor = (MetadataEncryptor)ctx.getBean("metadataEncryptor");
        authenticationComponent = (AuthenticationComponent)ctx.getBean("authenticationComponent");
        keyResourceLoader = (KeyResourceLoader)ctx.getBean("springKeyResourceLoader");
        reEncryptor = (ReEncryptor)ctx.getBean("reEncryptor");
        backupKeyStoreParameters = (KeyStoreParameters)ctx.getBean("backupKeyStoreParameters");
        keyStoreChecker = (KeyStoreChecker)ctx.getBean("keyStoreChecker");
        encryptionKeysRegistry = (EncryptionKeysRegistryImpl)ctx.getBean("encryptionKeysRegistry");
        //backupKeyStore = (AlfrescoKeyStoreImpl)ctx.getBean("backupKeyStore");
        mainKeyStore = (AlfrescoKeyStoreImpl)ctx.getBean("keyStore");
        mainEncryptor = (DefaultEncryptor)ctx.getBean("mainEncryptor");
        backupEncryptor = (DefaultEncryptor)ctx.getBean("backupEncryptor");

        // reencrypt in one txn (since we don't commit the model, the qnames won't be available across transactions)
        reEncryptor.setSplitTxns(false);

        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        tx = transactionService.getUserTransaction();
        tx.begin();

        StoreRef storeRef = nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE,
                "ReEncryptor_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);

        keyAlgorithm = "DESede";
        newKeys.setKey(KeyProvider.ALIAS_METADATA, generateSecretKey(keyAlgorithm));

        // Load models
        DictionaryBootstrap bootstrap = new DictionaryBootstrap();
        List<String> bootstrapModels = new ArrayList<String>();
        bootstrapModels.add(TEST_MODEL);
//        List<String> labels = new ArrayList<String>();
//        labels.add(TEST_BUNDLE);
        bootstrap.setModels(bootstrapModels);
//        bootstrap.setLabels(labels);
        bootstrap.setDictionaryDAO(dictionaryDAO);
        bootstrap.setTenantService(tenantService);
        bootstrap.bootstrap();
	}

	private static SecureRandom getSecureRandomInstance(){
		try
		{
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(System.nanoTime());
			return random;
		} catch (NoSuchAlgorithmException e)
		{
			throw new AlfrescoRuntimeException(e.getMessage());
		}
	}
	
	protected KeyProvider getKeyProvider(KeyStoreParameters keyStoreParameters)
	{
		KeyProvider backupKeyProvider = new KeystoreKeyProvider(keyStoreParameters, keyResourceLoader);
		return backupKeyProvider;
	}

    public void setBackupKeyStoreParameters(KeyStoreParameters backupKeyStoreParameters)
	{
		this.backupKeyStoreParameters = backupKeyStoreParameters;
	}

	@Override
    protected void tearDown() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        tx.rollback();
        super.tearDown();
    }

	protected KeyProvider getKeyProvider(final KeyMap keys)
	{
		KeyProvider keyProvider = new KeyProvider()
		{
			@Override
			public Key getKey(String keyAlias)
			{
				return keys.getCachedKey(keyAlias).getKey();
			}
		};
		return keyProvider;
	}

	protected void createEncryptedProperties(List<NodeRef> nodes)
	{
		for(int i = 0; i < NUM_PROPERTIES; i++)
		{
			NodeRef nodeRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("assoc1"), NODE_TYPE).getChildRef();
			nodes.add(nodeRef);

			Map<QName, Serializable> props = new HashMap<QName, Serializable>();
			props.put(PROP, nodeRef.toString());
			props = metadataEncryptor.encrypt(props);
            nodeService.setProperties(nodeRef, props);
		}
	}

	public byte[] generateKeyData()
	{
		byte[] bytes = new byte[DESedeKeySpec.DES_EDE_KEY_LEN];
		SECURE_RANDOM.nextBytes(bytes);
		return bytes;
	}

	protected Key generateSecretKey(String keyAlgorithm) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException
	{
		DESedeKeySpec keySpec = new DESedeKeySpec(generateKeyData());
		SecretKeyFactory kf = SecretKeyFactory.getInstance(keyAlgorithm);
		return kf.generateSecret(keySpec);
	}

	public void testReEncrypt()
	{
		KeyProvider backupKeyProvider = backupEncryptor.getKeyProvider();
		KeyProvider mainKeyProvider = mainEncryptor.getKeyProvider();
		try
		{
			// Create encrypted properties using the configured encryptor and key provider
	        createEncryptedProperties(before);
	        
	        // Create encrypted properties using the new encryptor and key provider
	        KeyProvider newKeyProvider = getKeyProvider(newKeys);

	        // set backup encryptor key provider to main encryptor key provider and drop in
	        // new key provider for main encryptor
	        backupEncryptor.setKeyProvider(mainEncryptor.getKeyProvider());
	        mainEncryptor.setKeyProvider(newKeyProvider);
	        
	        createEncryptedProperties(after);

	        // re-encrypt
	        long start = System.currentTimeMillis();
	        System.out.println(reEncryptor.reEncrypt() + " properties re-encrypted");
			System.out.println("Re-encrypted " + NUM_PROPERTIES*2 + " properties in " + (System.currentTimeMillis() - start) + "ms");
	
			// check that the nodes have been re-encrypted properly i.e. check that the properties
			// decrypted using the new keys match the expected values.
			for(NodeRef nodeRef : before)
			{
				Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
				props = metadataEncryptor.decrypt(props);
				assertNotNull("", props.get(PROP));
				assertEquals("", nodeRef.toString(), props.get(PROP));
			}
	
			for(NodeRef nodeRef : after)
			{
				Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
				props = metadataEncryptor.decrypt(props);
				assertNotNull("", props.get(PROP));
				assertEquals("", nodeRef.toString(), props.get(PROP));			
			}
		}
		catch(MissingKeyException e)
		{
			fail(e.getMessage());
		}
		catch(AlfrescoRuntimeException e)
		{
			if(e.getCause() instanceof InvalidKeyException)
			{
				e.printStackTrace();
				fail();
			}
		}
		finally
		{
			backupEncryptor.setKeyProvider(backupKeyProvider);
			mainEncryptor.setKeyProvider(mainKeyProvider);
		}
	}

	protected void testChangeKeysImpl(boolean cacheCiphers) throws Throwable
	{
		// on a single thread
		// create an encryptor, encrypt a string, change encryptor keys, decrypt -> should result in Invalid Key
		
		Pair<byte[], AlgorithmParameters> pair = null;
		DefaultEncryptor encryptor = null;
		Key secretKey1 = null;
		Key secretKey2 = null;
		String test = "hello world";
		final KeyMap keys = new KeyMap();
		byte[] decrypted = null;
		String testDecrypted = null;
		
		secretKey1 = generateSecretKey("DESede");
		keys.setKey("test", secretKey1);
		KeyProvider keyProvider = keyAlias -> keys.getCachedKey(keyAlias).getKey();

		encryptor = new DefaultEncryptor();
		encryptor.setCipherAlgorithm("DESede/CBC/PKCS5Padding");
		encryptor.setCipherProvider(null);
		encryptor.setKeyProvider(keyProvider);
		encryptor.setCacheCiphers(cacheCiphers);
		pair = encryptor.encrypt("test", null, test.getBytes("UTF-8"));

		decrypted = encryptor.decrypt("test", pair.getSecond(), pair.getFirst());
		testDecrypted = new String(decrypted, "UTF-8");
		
		assertEquals("Expected encrypt,decrypt to end up with the original value", test, testDecrypted);
		System.out.println("1:" + new String(decrypted, "UTF-8"));
		
		secretKey2 = generateSecretKey("DESede");
		keys.setKey("test", secretKey2);
		
		assertNotNull(encryptor);
		assertNotNull(pair);

		try
		{
			decrypted = encryptor.decrypt("test", pair.getSecond(), pair.getFirst());
			fail("Decryption should have failed");
		}
		catch(AlfrescoRuntimeException e)
		{
			// ok - decryption failed expected.
		}
	}
	
	public void testChangeKeys() throws Throwable
	{
		testChangeKeysImpl(false);
	}

	public void testChangeKeysCachedCiphers() throws Throwable
	{
		testChangeKeysImpl(true);
	}

	public void testFailedEncryptionWithCachedCiphers() throws Throwable
	{
		Pair<byte[], AlgorithmParameters> pair = null;
		DefaultEncryptor encryptor = null;
		Key secretKey1 = null;
		Key secretKey2 = null;
		String test = "hello world";
		final KeyMap keys = new KeyMap();
		byte[] decrypted = null;
		String testDecrypted = null;
		
		secretKey1 = generateSecretKey("DESede");
		keys.setKey("test", secretKey1);
		KeyProvider keyProvider = keyAlias -> keys.getCachedKey(keyAlias).getKey();

		encryptor = new DefaultEncryptor();
		encryptor.setCipherAlgorithm("DESede/CBC/PKCS5Padding");
		encryptor.setCipherProvider(null);
		encryptor.setKeyProvider(keyProvider);
		encryptor.setCacheCiphers(true);
		pair = encryptor.encrypt("test", null, test.getBytes("UTF-8"));

		secretKey2 = generateSecretKey("DESede");
		keys.setKey("test", secretKey2);
		
		assertNotNull(encryptor);
		assertNotNull(pair);

		try
		{
			decrypted = encryptor.decrypt("test", pair.getSecond(), pair.getFirst());
			fail("Decryption should have failed");
		}
		catch(AlfrescoRuntimeException e)
		{
			// ok - decryption failed expected - changed key
		}

		keys.setKey("test", secretKey1);
		try
		{
			decrypted = encryptor.decrypt("test", pair.getSecond(), pair.getFirst());
			testDecrypted = new String(decrypted, "UTF-8");
			assertEquals(test, testDecrypted);
		}
		catch(AlfrescoRuntimeException e)
		{
			fail("Expected decryption to work ok");
		}
	}
}