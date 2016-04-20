package org.alfresco.service.cmr.oauth2;

import java.util.Date;
import java.util.List;

import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.remotecredentials.OAuth2CredentialsInfo;
import org.alfresco.service.cmr.remoteticket.NoSuchSystemException;

/**
 *
 * @author Jared Ottley
 */
public interface OAuth2CredentialsStoreService
{

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth2CredentialsInfo storePersonalOAuth2Credentials(String remoteSystemId,
                String accessToken, String refreshToken, Date expiresAt, Date issuedAt)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth2CredentialsInfo storeSharedOAuth2Credentials(String remoteSystemId,
                String accessToken, String refreshToken, Date expiresAt, Date issuedAt)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth2CredentialsInfo getPersonalOAuth2Credentials(String remoteSystemId)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth2CredentialsInfo updateSharedOAuth2Credentials(
                OAuth2CredentialsInfo exisitingCredentials, String remoteSystemId,
                String accessToken, String refreshToken, Date expiresAt, Date issuedAt)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract List<OAuth2CredentialsInfo> listSharedOAuth2Credentials(String remoteSystemId)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract boolean deletePersonalOAuth2Credentials(String remoteSystemId)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract boolean deleteSharedOAuth2Credentials(String remoteSystemId,
                OAuth2CredentialsInfo credentials) throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth2CredentialsInfo updateCredentialsAuthenticationSucceeded(
                boolean succeeded, OAuth2CredentialsInfo credentials);

}