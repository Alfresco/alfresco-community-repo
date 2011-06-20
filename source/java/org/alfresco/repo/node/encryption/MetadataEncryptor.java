package org.alfresco.repo.node.encryption;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SealedObject;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.encryption.Encryptor;
import org.alfresco.repo.security.encryption.KeyProvider;
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
    private Encryptor encryptor;

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
    public void setEncryptor(Encryptor encryptor)
    {
        this.encryptor = encryptor;
    }

    /**
     * @throws AuthenticationException if the thread is not running as 'system'
     */
    private final void checkAuthentication()
    {
        if (!AuthenticationUtil.isRunAsUserTheSystemUser())
        {
            throw new AuthenticationException("Metadata decryption can only be done by the system user.");
        }
    }
    
    /**
     * Encrypt a properties if the data definition (model-specific) requires it.
     * <p/>
     * This method has no specific authentication requirements.
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
        Serializable outbound = encryptor.sealObject(KeyProvider.ALIAS_METADATA, null, inbound);
        // Done
        return outbound;
    }
    
    /**
     * Encrypt properties if their data definition (model-specific) requires it.
     * The values provided can be mixed; values will be encrypted only if required.
     * <p/>
     * This method has no specific authentication requirements.
     * 
     * @param inbound                   the properties to encrypt
     * @return                          a new map of values if some encryption occured
     *                                  otherwise the original inbound map is returned
     */
    public Map<QName, Serializable> encrypt(Map<QName, Serializable> inbound)
    {
        boolean encrypt = false;
        for (Map.Entry<QName, Serializable> entry : inbound.entrySet())
        {
            QName key = entry.getKey();
            PropertyDefinition propertyDef = dictionaryService.getProperty(key);
            if (propertyDef != null && (propertyDef.getDataType().getName().equals(DataTypeDefinition.ENCRYPTED)))
            {
                encrypt = true;
                break;
            }
        }
        if (!encrypt)
        {
            // Nothing to do
            return inbound;
        }
        // Encrypt, in place, using a copied map
        Map<QName, Serializable> outbound = new HashMap<QName, Serializable>(inbound);
        for (Map.Entry<QName, Serializable> entry : inbound.entrySet())
        {
            Serializable value = entry.getValue();
            if (value != null && (value instanceof SealedObject))
            {
                // Straight copy, i.e. do nothing
                continue;
            }
            // Have to decrypt the value
            Serializable encryptedValue = encryptor.sealObject(KeyProvider.ALIAS_METADATA, null, value);
            // Store it back
            outbound.put(entry.getKey(), encryptedValue);
        }
        // Done
        return outbound;
    }
    
    /**
     * Decrypt properties if they are decryptable.  The values provided can be mixed;
     * encrypted values will be sought out and decrypted.
     * <p/>
     * This method can only be called by the 'system' user.
     * 
     * @param inbound                   the properties to decrypt
     * @return                          a new map of values if some decryption occured
     *                                  otherwise the original inbound map is returned
     */
    public Map<QName, Serializable> decrypt(Map<QName, Serializable> inbound)
    {
        checkAuthentication();
        
        boolean decrypt = false;
        for (Map.Entry<QName, Serializable> entry : inbound.entrySet())
        {
            Serializable value = entry.getValue();
            if (value != null && (value instanceof SealedObject))
            {
                decrypt = true;
                break;
            }
        }
        if (!decrypt)
        {
            // Nothing to do
            return inbound;
        }
        // Decrypt, in place, using a copied map
        Map<QName, Serializable> outbound = new HashMap<QName, Serializable>(inbound);
        for (Map.Entry<QName, Serializable> entry : inbound.entrySet())
        {
            Serializable value = entry.getValue();
            if (value != null && (value instanceof SealedObject))
            {
                // Straight copy, i.e. do nothing
                continue;
            }
            // Have to decrypt the value
            Serializable decryptedValue = encryptor.unsealObject(KeyProvider.ALIAS_METADATA, value);
            // Store it back
            outbound.put(entry.getKey(), decryptedValue);
        }
        // Done
        return outbound;
    }
}
