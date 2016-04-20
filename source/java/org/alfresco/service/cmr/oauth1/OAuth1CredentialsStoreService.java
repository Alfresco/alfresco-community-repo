package org.alfresco.service.cmr.oauth1;

import java.util.List;

import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.remotecredentials.OAuth1CredentialsInfo;
import org.alfresco.service.cmr.remoteticket.NoSuchSystemException;

/**
 *
 * @author Jared Ottley
 */
public interface OAuth1CredentialsStoreService
{

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth1CredentialsInfo storePersonalOAuth1Credentials(String remoteSystemId,
                String token, String secret)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth1CredentialsInfo storeSharedOAuth1Credentials(String remoteSystemId,
                String token, String secret)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth1CredentialsInfo getPersonalOAuth1Credentials(String remoteSystemId)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth1CredentialsInfo updateSharedOAuth1Credentials(
                OAuth1CredentialsInfo exisitingCredentials, String remoteSystemId,
                String token, String secret)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract List<OAuth1CredentialsInfo> listSharedOAuth1Credentials(String remoteSystemId)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract boolean deletePersonalOAuth1Credentials(String remoteSystemId)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract boolean deleteSharedOAuth1Credentials(String remoteSystemId,
                OAuth1CredentialsInfo credentials) throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth1CredentialsInfo updateCredentialsAuthenticationSucceeded(
                boolean succeeded, OAuth1CredentialsInfo credentials);

}