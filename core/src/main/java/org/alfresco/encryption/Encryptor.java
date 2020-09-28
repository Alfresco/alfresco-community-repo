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

import java.io.InputStream;
import java.io.Serializable;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;

import org.alfresco.util.Pair;

/**
 * Interface providing methods to encrypt and decrypt data. 
 * 
 * @since 4.0
 */
public interface Encryptor
{
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
     * Decrypt an input stream
     * 
     * @param keyAlias              the encryption key alias
     * @param in                    the data to decrypt
     * @return                      the unencrypted data
     */
    InputStream decrypt(String keyAlias, AlgorithmParameters params, InputStream in);
    
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
    Serializable unsealObject(String keyAlias, Serializable input) throws InvalidKeyException;

    /**
     * Decodes encoded cipher algorithm parameters
     * 
     * @param encoded the encoded cipher algorithm parameters
     * @return the decoded cipher algorithmParameters
     */
    AlgorithmParameters decodeAlgorithmParameters(byte[] encoded);
    
    boolean keyAvailable(String keyAlias);
}
