package org.alfresco.repo.security.encryption;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.Security;

import javax.crypto.Cipher;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class AESEncryptionEngine implements EncryptionEngine
{
    private static String ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final Log logger = LogFactory.getLog(AESEncryptionEngine.class);

    private KeyProvider keyProvider;
    //private Key key;

    private ThreadLocal<Cipher> cipher;

    public AESEncryptionEngine()
    {
    }

    public void setKeyProvider(KeyProvider keyProvider)
    {
        this.keyProvider = keyProvider;
    }
    
    public void init()
    {
        this.cipher = new ThreadLocal<Cipher>();
//        key = keyProvider.getKey();
//        if(key == null)
//        {
//            throw new AlfrescoRuntimeException("Secret key is null.");
//        }
    }
    
    protected byte[] process(int cipherMode, byte[] input)
    {
        Cipher cipher = this.cipher.get();

        if(cipher == null)
        {
            try
            {
                cipher = Cipher.getInstance(ALGORITHM);
            }
            catch(Exception e)
            {
                Security.addProvider(new BouncyCastleProvider());
                try
                {
                    cipher = Cipher.getInstance(ALGORITHM);
                }
                catch(Exception e1)
                {
                    throw new AlfrescoRuntimeException("Unable to initialise encryption engine", e1);
                }
            }
        
            if(cipher == null)
            {
                throw new AlfrescoRuntimeException("Unable to initialise encryption engine");
            }

            this.cipher.set(cipher);

            logger.debug("Initialised thread local cipher");
        }

        try
        {
            cipher.init(cipherMode, keyProvider.getKey());

            // do the encryption/decryption in one go
            return cipher.doFinal(input);
        }
        catch(Exception e)
        {
            throw new AlfrescoRuntimeException("Unexpected exception during encryption/decryption", e);
        }
    }
    
    public byte[] encrypt(byte[] input)
    {
        return process(Cipher.ENCRYPT_MODE, input);
    }

    public byte[] decrypt(byte[] input)
    {
        return process(Cipher.DECRYPT_MODE, input);
    }

    public byte[] encryptString(String input) throws UnsupportedEncodingException
    {
        byte[] in = input.getBytes("UTF-8");
        return encrypt(in);
    }
    
    public String decryptAsString(byte[] input) throws UnsupportedEncodingException
    {
        return new String(decrypt(input), "UTF-8").trim();
    }
}
