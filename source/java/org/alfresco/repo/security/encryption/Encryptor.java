package org.alfresco.repo.security.encryption;

import java.io.Serializable;
import java.security.AlgorithmParameters;

import javax.crypto.Cipher;

import org.alfresco.util.Pair;


/**
 * Interface providing methods to encrypt and decrypt data. 
 * 
 * @since 4.0
 */
public interface Encryptor
{
    /**
     * Get the basic cipher that must be used for the given use-case
     * 
     * @param keyAlias              the encryption key alias
     * @param params                the parameters for the encryption or decryption
     * @param mode                  the encryption mode
     * @return                      the cipher to use or <tt>null</tt> if there is no
     *                              key associated with the key alias
     */
    Cipher getCipher(String keyAlias, AlgorithmParameters params, int mode);
    
    /**
     * Encrypt some bytes
     * 
     * @param keyAlias              the encryption key alias
     * @param input                 the data to encrypt
     * @return                      the encrypted data and parameters used
     */
    Pair<byte[], AlgorithmParameters> encrypt(String keyAlias, AlgorithmParameters params, byte[] input);
    
    /**
     * Decrypt some bytes
     * 
     * @param keyAlias              the encryption key alias
     * @param input                 the data to decrypt
     * @return                      the unencrypted data
     */
    byte[] decrypt(String keyAlias, AlgorithmParameters params, byte[] input);
    
    /**
     * Encrypt an object
     * 
     * @param keyAlias              the encryption key alias
     * @param input                 the object to write to bytes
     * @return                      the encrypted data and parameters used
     */
    Pair<byte[], AlgorithmParameters> encryptObject(String keyAlias, AlgorithmParameters params, Object input);
    
    /**
     * Decrypt data as an object
     * 
     * @param keyAlias              the encryption key alias
     * @param input                 the data to decrypt
     * @return                      the unencrypted data deserialized
     */
    Object decryptObject(String keyAlias, AlgorithmParameters params, byte[] input);
    
    /**
     * Convenience method to seal on object up cryptographically.
     * <p/>
     * Note that the original object may be returned directly if there is no key associated with
     * the alias.
     * 
     * @param keyAlias              the encryption key alias
     * @param input                 the object to encrypt and seal
     * @return                      the sealed object that can be decrypted with the original key
     */
    Serializable sealObject(String keyAlias, AlgorithmParameters params, Serializable input);
    
    /**
     * Convenience method to unseal on object sealed up cryptographically.
     * <p/>
     * Note that the algorithm parameters not provided on the assumption that a symmetric key
     * algorithm is in use - only the key is required for unsealing.
     * <p/>
     * Note that the original object may be returned directly if there is no key associated with
     * the alias or if the input object is not a <code>SealedObject</code>.
     * 
     * @param keyAlias              the encryption key alias
     * @param input                 the object to decrypt and unseal
     * @return                      the original unsealed object that was encrypted with the original key
     * @throws IllegalStateException    if the key alias is not valid <b>and</b> the input is a
     *                                  <tt>SealedObject</tt>
     */
    Serializable unsealObject(String keyAlias, Serializable input);
}
