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
package org.alfresco.service.cmr.remoteticket;

import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.remotecredentials.BaseCredentialsInfo;
import org.alfresco.service.cmr.remotecredentials.RemoteCredentialsService;

/**
 * Service for working with a Remote Alfresco instance, which
 *  holds user credentials for the remote system via the 
 *  {@link RemoteCredentialsService}, and handles ticket 
 *  negotiation for you.
 *  
 * Currently only Username+Password credentials, exchanged for a
 *  regular alf_ticket Alfresco Ticket are supported, but
 *  things like OAuth should be supportable too later.
 *  
 * All Remote Systems must be registered with this service before
 *  use, supplying details of where to find the remote Alfresco
 *  for a given Remote System ID. The Remote System names should
 *  follow the system naming convention from {@link RemoteCredentialsService} 
 *  
 * TODO OAuth support
 * 
 * @author Nick Burch
 * @since 4.0.2
 */
public interface RemoteAlfrescoTicketService
{
    /**
     * Validates and stores the remote credentials for the current user
     * 
     * @param remoteSystemId The ID of the remote system, as registered with the service
     * 
     * @throws AuthenticationException If the credentials are invalid
     * @throws RemoteSystemUnavailableException If the remote system is unavailable
     * @throws NoSuchSystemException If no system has been registered with the given ID
     */
    BaseCredentialsInfo storeRemoteCredentials(String remoteSystemId, String username, String password)
       throws AuthenticationException, RemoteSystemUnavailableException, NoSuchSystemException;

    /**
     * Retrieves the remote credentials (if any) for the current user
     * 
     * @param remoteSystemId The ID of the remote system, as registered with the service
     * @return The current user's remote credentials, or null if they don't have any
     * @throws NoSuchSystemException If no system has been registered with the given ID
     */
    BaseCredentialsInfo getRemoteCredentials(String remoteSystemId) throws NoSuchSystemException;
    
    /**
     * Deletes the remote credentials (if any) for the current user
     * 
     * @param remoteSystemId The ID of the remote system, as registered with the service
     * @return Whether credentials were found to delete
     * @throws NoSuchSystemException If no system has been registered with the given ID
     */
    boolean deleteRemoteCredentials(String remoteSystemId) throws NoSuchSystemException;

    /**
     * Returns the current Alfresco Ticket for the current user on
     *  the remote system, fetching if it isn't already cached.
     * 
     * Note that because tickets are cached, it is possible that a
     *  ticket has become invalid (due to timeout or server restart).
     * If the ticket is rejected by the remote server, you should
     *  call {@link #refetchAlfrescoTicket(String)} to ensure you have
     *  the latest ticket, and re-try the request.
     *  
     * @param remoteSystemId The ID of the remote system, as registered with the service
     * @return The Alfresco Ticket for the current user on the remote system
     * 
     * @throws AuthenticationException If the stored remote credentials are now invalid
     * @throws NoCredentialsFoundException If the user has no stored credentials for the remote system
     * @throws NoSuchSystemException If no system has been registered with the given ID
     * @throws RemoteSystemUnavailableException If it was not possible to talk to the remote system 
     */
    RemoteAlfrescoTicketInfo getAlfrescoTicket(String remoteSystemId)
       throws AuthenticationException, NoCredentialsFoundException, NoSuchSystemException, RemoteSystemUnavailableException;
    
    /**
     * Forces a re-fetch of the Alfresco Ticket for the current user,
     *  if possible, and marks the credentials as failing if not. 
     *
     * Normally {@link #getAlfrescoTicket(String)} should be used initially, with
     *  this only used if the ticket received is rejected by the remote server.
     *  
     * @param remoteSystemId The ID of the remote system, as registered with the service
     * @return The Alfresco Ticket for the current user on the remote system
     * 
     * @throws AuthenticationException If the stored remote credentials are now invalid
     * @throws NoCredentialsFoundException If the user has no stored credentials for the remote system
     * @throws NoSuchSystemException If no system has been registered with the given ID
     * @throws RemoteSystemUnavailableException If it was not possible to talk to the remote system 
     */
    RemoteAlfrescoTicketInfo refetchAlfrescoTicket(String remoteSystemId)
       throws AuthenticationException, NoCredentialsFoundException, NoSuchSystemException, RemoteSystemUnavailableException;
    
    /**
     * Registers the details of a new Remote System with the service.
     * 
     * @param remoteSystemId The ID to be used to identify the system
     * @param baseUrl The base URL of Alfresco Services on the remote system, eg http://localhost:8080/alfresco/service/
     * @param requestHeaders Any HTTP headers that must be sent with the request when talking to the server
     */
    void registerRemoteSystem(String remoteSystemId, String baseUrl, Map<String,String> requestHeaders);
}