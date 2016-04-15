package org.alfresco.repo.remotecredentials;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.service.cmr.remotecredentials.BaseCredentialsInfo;
import org.alfresco.service.cmr.remotecredentials.PasswordCredentialsInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * The Factory for building {@link PasswordCredentialsInfo} objects
 * 
 * @author Nick Burch
 * @since Odin
 */
public class PasswordCredentialsFactory implements RemoteCredentialsInfoFactory
{
    private MetadataEncryptor metadataEncryptor;
    
    public void setMetadataEncryptor(MetadataEncryptor metadataEncryptor)
    {
        this.metadataEncryptor = metadataEncryptor;
    }
    
    /**
     * Creates a new {@link PasswordCredentialsInfo} based on the details of the underlying node.
     */
    public PasswordCredentialsInfo createCredentials(QName type, NodeRef nodeRef, String remoteSystemName, 
            NodeRef remoteSystemContainerNodeRef, Map<QName,Serializable> properties)
    {
        // Decrypt the password
        String password = (String)metadataEncryptor.decrypt(
                RemoteCredentialsModel.PROP_REMOTE_PASSWORD, properties.get(RemoteCredentialsModel.PROP_REMOTE_PASSWORD));
        
        // Build the object
        PasswordCredentialsInfoImpl credentials = 
            new PasswordCredentialsInfoImpl(nodeRef, remoteSystemName, remoteSystemContainerNodeRef);
        
        // Populate
        RemoteCredentialsInfoFactory.FactoryHelper.setCoreCredentials(credentials, properties);
        credentials.setRemotePassword(password);
        
        // All done
        return credentials;
    }

    /**
     * Serializes the given {@link BaseCredentialsInfo} object to node properties.
     * 
     * @param info The Credentials object to serialize
     * @return The final set of properties to be serialized for the node
     */
    public Map<QName,Serializable> serializeCredentials(BaseCredentialsInfo info)
    {
        if (! (info instanceof PasswordCredentialsInfo))
        {
            throw new IllegalStateException("Incorrect registration, info must be a PasswordCredentialsInfo");
        }
        
        // Encrypt the password
        PasswordCredentialsInfo credentials = (PasswordCredentialsInfo)info;
        Serializable passwordEncrypted = metadataEncryptor.encrypt(
                RemoteCredentialsModel.PROP_REMOTE_PASSWORD, credentials.getRemotePassword());

        // Store our specific types and return
        Map<QName,Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(RemoteCredentialsModel.PROP_REMOTE_PASSWORD, passwordEncrypted);
        return properties;
    }
}