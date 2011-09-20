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
package org.alfresco.repo.node.encryption;

import java.io.Serializable;
import java.security.KeyException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.crypto.SealedObject;

import org.alfresco.encryption.FallbackEncryptor;
import org.alfresco.encryption.KeyProvider;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

/**
 * Component to convert encrypt/decrypt properties.
 * <p/>
 * This is a helper; it is up to the client how and when encryption and decryption is done,
 * but metadata integrity enforcement will expect that encrypted properties are already
 * encrypted.
 * <p/>
 * This class must <b>always</b> be used 
 * {@link AuthenticationUtil#runAs(org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork, String) running as 'system'}.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class MetadataEncryptor
{
    private DictionaryService dictionaryService;
    private FallbackEncryptor encryptor;

    /**
     * @param dictionaryService service to check if properties need encrypting
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param encryptor         the class that does the encryption/decryption
     */
    public void setEncryptor(FallbackEncryptor encryptor)
    {
        this.encryptor = encryptor;
    }

    /**
     * Encrypt a properties if the data definition (model-specific) requires it.
     * 
     * @param propertyQName             the property qualified name
     * @param inbound                   the property to encrypt
     * @return                          the encrypted property or the original if encryption is not required
     */
    public Serializable encrypt(QName propertyQName, Serializable inbound)
    {
        PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
        if (inbound == null || propertyDef == null || !(propertyDef.getDataType().getName().equals(DataTypeDefinition.ENCRYPTED)))
        {
            return inbound;
        }
        if (inbound instanceof SealedObject)
        {
            return inbound;
        }
        Serializable outbound = encryptor.sealObject(KeyProvider.ALIAS_METADATA, null, inbound);
        // Done
        return outbound;
    }
    
    /**
     * Decrypt a property if the data definition (model-specific) requires it.
     * 
     * @param propertyQName             the property qualified name
     * @param inbound                   the property to decrypt
     * @return                          the decrypted property or the original if it wasn't encrypted
     */
    public Serializable decrypt(QName propertyQName, Serializable inbound)
    {
        PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
        if (inbound == null || propertyDef == null || !(propertyDef.getDataType().getName().equals(DataTypeDefinition.ENCRYPTED)))
        {
            return inbound;
        }
        if (!(inbound instanceof SealedObject))
        {
            return inbound;
        }
        try
        {
	        Serializable outbound = encryptor.unsealObject(KeyProvider.ALIAS_METADATA, inbound);
	        // Done
	        return outbound;
        }
        catch(KeyException e)
        {
        	throw new AlfrescoRuntimeException("Invalid metadata decryption key", e);
        }
    }
    
    /**
     * Encrypt properties if their data definition (model-specific) requires it.
     * The values provided can be mixed; values will be encrypted only if required.
     * 
     * @param inbound                   the properties to encrypt
     * @return                          a new map of values if some encryption occured
     *                                  otherwise the original inbound map is returned
     */
    public Map<QName, Serializable> encrypt(Map<QName, Serializable> inbound)
    {
        Set<QName> encryptedProperties = new HashSet<QName>(5);
        for (Map.Entry<QName, Serializable> entry : inbound.entrySet())
        {
            QName qname = entry.getKey();
            Serializable value = entry.getValue();
            PropertyDefinition propertyDef = dictionaryService.getProperty(qname);
            if (propertyDef != null && (propertyDef.getDataType().getName().equals(DataTypeDefinition.ENCRYPTED)))
            {
                if (value != null && !(value instanceof SealedObject))
                {
                    encryptedProperties.add(qname);
                }
            }
        }
        if (encryptedProperties.isEmpty())
        {
            // Nothing to do
            return inbound;
        }
        // Encrypt, in place, using a copied map
        Map<QName, Serializable> outbound = new HashMap<QName, Serializable>(inbound);
        for (QName propertyQName : encryptedProperties)
        {
            // We have already checked for nulls and conversions
            Serializable value = inbound.get(propertyQName);
            // Have to encrypt the value
            Serializable encryptedValue = encryptor.sealObject(KeyProvider.ALIAS_METADATA, null, value);
            // Store it back
            outbound.put(propertyQName, encryptedValue);
        }
        // Done
        return outbound;
    }
    
    /**
     * Decrypt properties if they are decryptable.  The values provided can be mixed;
     * encrypted values will be sought out and decrypted.
     * 
     * @param inbound                   the properties to decrypt
     * @return                          a new map of values if some decryption occured
     *                                  otherwise the original inbound map is returned
     */
    public Map<QName, Serializable> decrypt(Map<QName, Serializable> inbound)
    {
        Set<QName> encryptedProperties = new HashSet<QName>(5);
        for (Map.Entry<QName, Serializable> entry : inbound.entrySet())
        {
            QName qname = entry.getKey();
            Serializable value = entry.getValue();
            PropertyDefinition propertyDef = dictionaryService.getProperty(qname);
            if (propertyDef != null && (propertyDef.getDataType().getName().equals(DataTypeDefinition.ENCRYPTED)))
            {
                if (value != null && (value instanceof SealedObject))
                {
                    encryptedProperties.add(qname);
                }
            }
        }
        if (encryptedProperties.isEmpty())
        {
            // Nothing to do
            return inbound;
        }
        // Decrypt, in place, using a copied map
        Map<QName, Serializable> outbound = new HashMap<QName, Serializable>(inbound);
        for (QName propertyQName : encryptedProperties)
        {
            // We have already checked for nulls and conversions
            Serializable value = inbound.get(propertyQName);
            // Have to decrypt the value
            try
            {
	            Serializable unencryptedValue = encryptor.unsealObject(KeyProvider.ALIAS_METADATA, value);
	            // Store it back
	            outbound.put(propertyQName, unencryptedValue);
            }
            catch(KeyException e)
            {
            	throw new AlfrescoRuntimeException("Invalid metadata decryption key", e);
            }
        }
        // Done
        return outbound;
    }
    
    public boolean keyAvailable(String keyAlias)
    {
    	return encryptor.keyAvailable(keyAlias);
    }
    
    public boolean backupKeyAvailable(String keyAlias)
    {
    	return encryptor.backupKeyAvailable(keyAlias);
    }
}