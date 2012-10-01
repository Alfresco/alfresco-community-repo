/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.remoteticket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.remotecredentials.PasswordCredentialsInfoImpl;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.remoteconnector.RemoteConnectorRequest;
import org.alfresco.service.cmr.remoteconnector.RemoteConnectorService;
import org.alfresco.service.cmr.remotecredentials.BaseCredentialsInfo;
import org.alfresco.service.cmr.remotecredentials.PasswordCredentialsInfo;
import org.alfresco.service.cmr.remotecredentials.RemoteCredentialsService;
import org.alfresco.service.cmr.remoteticket.NoCredentialsFoundException;
import org.alfresco.service.cmr.remoteticket.NoSuchSystemException;
import org.alfresco.service.cmr.remoteticket.RemoteAlfrescoTicketInfo;
import org.alfresco.service.cmr.remoteticket.RemoteAlfrescoTicketService;
import org.alfresco.service.cmr.remoteticket.RemoteSystemUnavailableException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * Service for working with a Remote Alfresco instance, which
 *  holds user credentials for the remote system via the 
 *  {@link RemoteCredentialsService}, and handles ticket 
 *  negotiation for you.
 *  
 * Note - this service will be moved to the Repository Core once
 *  it has stabilised (likely after OAuth support is added)
 * 
 * TODO OAuth support
 * 
 * @author Nick Burch
 * @since 4.0.2
 */
public class RemoteAlfrescoTicketServiceImpl implements RemoteAlfrescoTicketService
{
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(RemoteAlfrescoTicketServiceImpl.class);
            
    private RetryingTransactionHelper retryingTransactionHelper;
    private RemoteCredentialsService remoteCredentialsService;
    private RemoteConnectorService remoteConnectorService;
    private SimpleCache<String, String> ticketsCache;
    
    private Map<String,String> remoteSystemsUrls = new HashMap<String, String>();
    private Map<String,Map<String,String>> remoteSystemsReqHeaders = new HashMap<String, Map<String,String>>();
    
    /**
     * Sets the Remote Credentials Service to use to store and retrieve credentials
     */
    public void setRemoteCredentialsService(RemoteCredentialsService remoteCredentialsService)
    {
        this.remoteCredentialsService = remoteCredentialsService;
    }
    
    /**
     * Sets the Remote Connector Service to use to talk to remote systems with
     */
    public void setRemoteConnectorService(RemoteConnectorService remoteConnectorService)
    {
        this.remoteConnectorService = remoteConnectorService;
    }

    /**
     * Sets the SimpleCache to be used to cache remote tickets in
     */
    public void setTicketsCache(SimpleCache<String, String> ticketsCache)
    {
        this.ticketsCache = ticketsCache;
    }
    
    /**
     * Sets the Retrying Transaction Helper, used to write changes to
     *  Credentials which turn out to be invalid 
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    /**
     * Registers the details of a new Remote System with the service.
     * Any previous details for the system will be overridden
     */
    public synchronized void registerRemoteSystem(String remoteSystemId, String baseUrl, Map<String,String> requestHeaders)
    {
        remoteSystemsUrls.put(remoteSystemId, baseUrl);
        remoteSystemsReqHeaders.put(remoteSystemId, requestHeaders);
        
        if (logger.isDebugEnabled())
            logger.debug("Registered System " + remoteSystemId + " as " + baseUrl);
    }
    
    protected void ensureRemoteSystemKnown(String remoteSystemId) throws NoSuchSystemException
    {
        String baseUrl = remoteSystemsUrls.get(remoteSystemId);
        if (baseUrl == null)
        {
            throw new NoSuchSystemException(remoteSystemId);
        }
    }
    protected PasswordCredentialsInfo ensureCredentialsFound(String remoteSystemId, BaseCredentialsInfo credentails)
    {
        // Check they exist, and are of the right type
        if (credentails == null)
        {
            throw new NoCredentialsFoundException(remoteSystemId);
        }
        if (! (credentails instanceof PasswordCredentialsInfo))
        {
            throw new AlfrescoRuntimeException("Credentials found, but of the wrong type, needed PasswordCredentialsInfo but got " + credentails); 
        }
        return (PasswordCredentialsInfo)credentails;
    }
    protected String toCacheKey(String remoteSystemId, BaseCredentialsInfo credentials)
    {
        // Cache key is system + separator + remote username
        return remoteSystemId + "===" + credentials.getRemoteUsername();
    }
    
    /**
     * Validates and stores the remote credentials for the current user
     */
    public BaseCredentialsInfo storeRemoteCredentials(String remoteSystemId, String username, String password)
       throws AuthenticationException, RemoteSystemUnavailableException, NoSuchSystemException
    {
        // Check we know about the system
        ensureRemoteSystemKnown(remoteSystemId);
        
        // Build the initial stub credentials
        PasswordCredentialsInfoImpl credentials = new PasswordCredentialsInfoImpl();
        
        // See if there are existing credentials to update
        BaseCredentialsInfo existing = getRemoteCredentials(remoteSystemId);
        if (existing != null)
        {
            // Update if we can, otherwise delete for re-add
            if (existing instanceof PasswordCredentialsInfoImpl)
            {
                credentials = (PasswordCredentialsInfoImpl)existing;
                if (logger.isDebugEnabled())
                    logger.debug("Updating existing credentials from " + credentials.getNodeRef());
            }
            else
            {
                // Wrong type, delete and use new ones
                if (logger.isDebugEnabled())
                    logger.debug("Unable to update existing credentials from " + existing.getNodeRef() + ", replacing");
                remoteCredentialsService.deleteCredentials(existing);
                existing = null;
            }
        }
        
        // Set the remote system credentials for them
        credentials.setRemoteUsername(username);
        credentials.setRemotePassword(password);
        
        // Validate their credentials are correct, by attempting to get a ticket for them
        refreshTicket(remoteSystemId, credentials);
        
        if (logger.isDebugEnabled())
            logger.debug("Credentials correct for " + username + " on " + remoteSystemId);
        
        // If we get this far, then there credentials are valid, so store them
        credentials.setLastAuthenticationSucceeded(true);
        
        if (credentials.getNodeRef() != null)
        {
            return remoteCredentialsService.updateCredentials(credentials);
        }
        else
        {
            return remoteCredentialsService.createPersonCredentials(remoteSystemId, credentials);
        }
    }

    /**
     * Retrieves the remote credentials (if any) for the current user
     * 
     * @param remoteSystemId The ID of the remote system, as registered with the service
     * @return The current user's remote credentials, or null if they don't have any
     */
    public BaseCredentialsInfo getRemoteCredentials(String remoteSystemId) throws NoSuchSystemException
    {
        // Check we know about the system
        ensureRemoteSystemKnown(remoteSystemId);
        
        // Retrieve, if available, and return
        return remoteCredentialsService.getPersonCredentials(remoteSystemId);
    }
    
    /**
     * Deletes the remote credentials (if any) for the current user
     */
    public boolean deleteRemoteCredentials(String remoteSystemId) throws NoSuchSystemException
    {
        // Try to retrieve
        BaseCredentialsInfo credentials = getRemoteCredentials(remoteSystemId);
        
        // If there are none, nothing to do
        if (credentials == null)
        {
            if (logger.isDebugEnabled())
                logger.debug("No credentials found to delete on " + remoteSystemId);
            return false;
        }
        
        // Log that we're going to delete
        if (logger.isDebugEnabled())
            logger.debug("Deleting credentials for " + credentials.getRemoteUsername() + " on " + remoteSystemId);
        
        // Delete the credentials
        remoteCredentialsService.deleteCredentials(credentials);
        
        // Zap the cached ticket, if there is one
        String cacheKey = toCacheKey(remoteSystemId, credentials);
        ticketsCache.remove(cacheKey);
        
        // Indicate the delete worked
        return true;
    }

    /**
     * Returns the current Alfresco Ticket for the current user on
     *  the remote system, fetching if it isn't already cached.
     */
    public RemoteAlfrescoTicketInfo getAlfrescoTicket(String remoteSystemId)
       throws AuthenticationException, NoCredentialsFoundException, NoSuchSystemException, RemoteSystemUnavailableException
    {
        // Check we know about the system
        ensureRemoteSystemKnown(remoteSystemId);
        
        // Grab the user's details
        BaseCredentialsInfo creds = getRemoteCredentials(remoteSystemId);
        PasswordCredentialsInfo credentials = ensureCredentialsFound(remoteSystemId, creds);
        
        // Is there a cached ticket?
        String cacheKey = toCacheKey(remoteSystemId, credentials);
        String ticket = ticketsCache.get(cacheKey);
        
        // Refresh if if isn't cached
        if (ticket == null)
        {
            return refreshTicket(remoteSystemId, credentials);
        }
        else
        {
            if (logger.isDebugEnabled())
                logger.debug("Cached ticket found for " + creds.getRemoteUsername() + " on " + remoteSystemId);
                
            // Wrap and return
            return new AlfTicketRemoteAlfrescoTicketImpl(ticket);
        }
    }
    
    /**
     * Forces a re-fetch of the Alfresco Ticket for the current user,
     *  if possible, and marks the credentials as failing if not. 
     */
    public RemoteAlfrescoTicketInfo refetchAlfrescoTicket(String remoteSystemId)
       throws AuthenticationException, NoCredentialsFoundException, NoSuchSystemException, RemoteSystemUnavailableException
    {
        // Check we know about the system
        ensureRemoteSystemKnown(remoteSystemId);
        
        // Grab the user's details
        BaseCredentialsInfo creds = getRemoteCredentials(remoteSystemId);
        PasswordCredentialsInfo credentials = ensureCredentialsFound(remoteSystemId, creds);
        
        // Trigger the refresh
        return refreshTicket(remoteSystemId, credentials);
    }
    
    /**
     * Fetches a new ticket for the given user, and caches it
     */
    @SuppressWarnings("unchecked")
    protected RemoteAlfrescoTicketInfo refreshTicket(final String remoteSystemId, final PasswordCredentialsInfo credentials)
       throws AuthenticationException, NoSuchSystemException, RemoteSystemUnavailableException
    {
        // Check we know about the system
        String baseUrl = remoteSystemsUrls.get(remoteSystemId);
        if (baseUrl == null)
        {
            throw new NoSuchSystemException(remoteSystemId);
        }
        
        if (logger.isDebugEnabled())
            logger.debug("Fetching new ticket for " + credentials.getRemoteUsername() + " on " + remoteSystemId);
        
        // Build up the JSON for the ticket request
        JSONObject json = new JSONObject();
        json.put("username", credentials.getRemoteUsername());
        json.put("password", credentials.getRemotePassword());
        
        // Build the URL
        String url = baseUrl + "api/login";
        
        // Turn this into a remote request
        RemoteConnectorRequest request = remoteConnectorService.buildRequest(url, "POST");
        request.setRequestBody(json.toJSONString());
        
        Map<String,String> reqHeaders = remoteSystemsReqHeaders.get(remoteSystemId);
        if (reqHeaders != null)
        {
            for (Map.Entry<String, String> reqHeader : reqHeaders.entrySet())
            {
                request.addRequestHeader(reqHeader.getKey(), reqHeader.getValue());
            }
        }
        
        // Work out what key we'll use to cache on
        String cacheKey = toCacheKey(remoteSystemId, credentials);
        
        // Perform the request
        String ticket = null;
        try {
            JSONObject response = remoteConnectorService.executeJSONRequest(request);
            if (logger.isDebugEnabled())
                logger.debug("JSON Ticket Response Received: " + response);

            // Pull out the ticket, validating the JSON along the way
            Object data = response.get("data");
            if (data == null)
            {
                throw new RemoteSystemUnavailableException("Invalid JSON received: " + response);
            }
            if (! (data instanceof JSONObject))
            {
                throw new RemoteSystemUnavailableException("Invalid JSON part received: " + data.getClass() + " - from: " + response);
            }

            Object ticketJSON = ((JSONObject)data).get("ticket");
            if (ticketJSON == null)
            {
                throw new RemoteSystemUnavailableException("Invalid JSON received, ticket missing: " + response);
            }
            if (! (ticketJSON instanceof String))
            {
                throw new RemoteSystemUnavailableException("Invalid JSON part received: " + ticketJSON.getClass() + " from: " + response);
            }
            ticket = (String)ticketJSON;
        }
        catch (IOException ioEx)
        {
            if (logger.isDebugEnabled())
                logger.debug("Problem communicating with remote Alfresco instance " + remoteSystemId, ioEx);
            
            throw new RemoteSystemUnavailableException("Error talking to remote system", ioEx);
        }
        catch (ParseException jsonEx)
        {
            if (logger.isDebugEnabled())
                logger.debug("Invalid JSON from remote Alfresco instance " + remoteSystemId, jsonEx);
            
            throw new RemoteSystemUnavailableException("Invalid JSON response from remote system", jsonEx);
        }
        catch (AuthenticationException authEx)
        {
            // Record the credentials as now failing, if they're persisted ones
            // Do this in a read-write transaction (most ticket stuff is read only)
            if (credentials.getNodeRef() != null && credentials.getLastAuthenticationSucceeded())
            {
                retryingTransactionHelper.doInTransaction(
                        new RetryingTransactionCallback<Void>()
                        {
                            public Void execute()
                            {
                                remoteCredentialsService.updateCredentialsAuthenticationSucceeded(false, credentials);
                                return null;
                            }
                        }, false, true
                );
            }
            
            // Clear old the old, invalid ticket from the cache, if it was there
            ticketsCache.remove(cacheKey);
            
            // Propagate up the problem
            throw authEx;
        }
        
        // Cache the new ticket
        ticketsCache.put(cacheKey, ticket);
        
        // If the credentials indicate the previous attempt failed, record as now working
        if (! credentials.getLastAuthenticationSucceeded())
        {
            retryingTransactionHelper.doInTransaction(
                    new RetryingTransactionCallback<Void>()
                    {
                        public Void execute()
                        {
                            remoteCredentialsService.updateCredentialsAuthenticationSucceeded(true, credentials);
                            return null;
                        }
                    }, false, true
            );
        }
        
        // Wrap and return
        return new AlfTicketRemoteAlfrescoTicketImpl(ticket);
    }
}
