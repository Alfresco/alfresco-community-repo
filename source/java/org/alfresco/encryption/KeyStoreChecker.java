package org.alfresco.encryption;

/**
 * Checks the repository key stores.
 * 
 * @since 4.0
 *
 */
public class KeyStoreChecker
{
    private AlfrescoKeyStore mainKeyStore;

    public KeyStoreChecker()
    {
    }
        
    public void setMainKeyStore(AlfrescoKeyStore mainKeyStore)
    {
        this.mainKeyStore = mainKeyStore;
    }

    public void validateKeyStores() throws InvalidKeystoreException, MissingKeyException
    {
        mainKeyStore.validateKeys();
        if(!mainKeyStore.exists())
        {
            mainKeyStore.create();
        }
    }
}
