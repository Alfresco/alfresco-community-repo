package org.alfresco.repo.security.encryption;

import java.io.UnsupportedEncodingException;
import java.security.Security;

import javax.crypto.Cipher;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.crypto.params.KeyParameter;

public class DefaultEncryptionEngine implements EncryptionEngine
{
    private static final Log logger = LogFactory.getLog(EncryptionEngine.class);

    //private String encryptionProvider;
    private KeyProvider keyProvider;
    
    private BufferedBlockCipher cipher;
    private AESEngine engine;

    //private Cipher cipher;
    //private byte[] key;

    public DefaultEncryptionEngine(/*byte[] key*/)
    {
        // TODO check that this hasn't already been done
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//
//        this.key = key;
    }

    public void setKeyProvider(KeyProvider keyProvider)
    {
        this.keyProvider = keyProvider;
    }
    
//    public void setEncryptionProvider(String encryptionProvider)
//    {
//        this.encryptionProvider = encryptionProvider;
//    }
    
    public void init()
    {
        //cipher = Cipher.getInstance("AES");
        this.engine = new AESEngine();

        /*
         * Paddings available (http://www.bouncycastle.org/docs/docs1.6/org/bouncycastle/crypto/paddings/BlockCipherPadding.html):
         *   - ISO10126d2Padding
         *   - ISO7816d4Padding
         *   - PKCS7Padding
         *   - TBCPadding
         *   - X923Padding
         *   - ZeroBytePadding
         */
        BlockCipherPadding blockCipherPadding = new ZeroBytePadding();
        this.cipher = new PaddedBufferedBlockCipher(engine, blockCipherPadding);

//        logger.debug("Encryption cipher: " + cipher.getProvider().getInfo());
    }
    
    protected byte[] process(boolean toEncrypt, byte[] input)
    {
        try
        {
            //CipherParameters param = new KeyParameter(keyProvider.getKey());
            //cipher.init(toEncrypt, param);

            int inputLength = input.length;
            int maximumOutputLength = cipher.getOutputSize(inputLength);
            byte[] output = new byte[maximumOutputLength];
    
            int outputOffset = 0;
            int outputLength = 0;
            int bytesProcessed = cipher.processBytes(input, 0, input.length, output, 0);
            outputOffset += bytesProcessed;
            outputLength += bytesProcessed;
            bytesProcessed = cipher.doFinal(output, outputOffset);
            outputOffset += bytesProcessed;
            outputLength += bytesProcessed;
    
            if(outputLength == output.length)
            {
                return output;
            }
            else
            {
                byte[] truncatedOutput = new byte[outputLength];
                System.arraycopy(output, 0, truncatedOutput, 0, outputLength);
                return truncatedOutput;
            }
        }
        catch(InvalidCipherTextException ex)
        {
            throw new AlfrescoRuntimeException("Unexpected encryption error", ex);
        }
    }

    public byte[] encrypt(byte[] input)
    {
        return process(true, input);
    }

    public byte[] decrypt(byte[] input)
    {
        return process(false, input);
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
