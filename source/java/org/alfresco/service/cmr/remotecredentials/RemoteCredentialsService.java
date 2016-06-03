/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.remotecredentials;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.namespace.QName;

/**
 * The core Remote Credentials service.
 * 
 * This provides low level support for storing, retrieving
 *  and finding remote credentials. Most users will want 
 *  something built on top of this, eg to do the OAuth Dance.
 * 
 * The "Remote System" name chosen by systems built on top of
 *  this need to be unique, to avoid clashes. Where there is
 *  only one thing that is talked to (eg Twitter, Flickr,
 *  Alfresco Cloud), then the "Remote System Name" should be
 *  the name of the system. Where one service can talk to
 *  multiple systems, the system hostname should be used as
 *  a suffix, such as "OpenID-livejournal.com" and 
 *  "OpenID-stackexchange.net", so they can be differentiated.
 * 
 * @author Nick Burch
 * @since 4.0.2
 */
public interface RemoteCredentialsService 
{
   /**
    * Stores a new {@link BaseCredentialsInfo} for the current user
    */
   BaseCredentialsInfo createPersonCredentials(String remoteSystem, BaseCredentialsInfo credentials); 
    
   /**
    * Stores a new {@link BaseCredentialsInfo} for shared use.
    * Permissions should then be set to control access to these.
    */
   BaseCredentialsInfo createSharedCredentials(String remoteSystem, BaseCredentialsInfo credentials);
     
   /**
    * Updates an existing {@link BaseCredentialsInfo}. The type
    *  must not change.
    */
   BaseCredentialsInfo updateCredentials(BaseCredentialsInfo credentials);
   
   /**
    * Records if the most recent Authentication attempt with a given
    *  set of credentials worked or not.
    */
   BaseCredentialsInfo updateCredentialsAuthenticationSucceeded(boolean succeeded, BaseCredentialsInfo credentialsInfo);
     
   /**
    * Deletes an existing {@link BaseCredentialsInfo} from the repository
    */
   void deleteCredentials(BaseCredentialsInfo credentialsInfo);

   
   /**
    * Lists all Remote Systems for which credentials are
    *  stored for the current user
    */
   PagingResults<String> listPersonRemoteSystems(PagingRequest paging);
   
   /**
    * Lists all Remote Systems for which the user has access
    *  to shared credentials
    */
   PagingResults<String> listSharedRemoteSystems(PagingRequest paging);
   
   /**
    * Lists all the Remote Systems for which the user has credentials,
    *  either personal ones or shared ones
    */
   PagingResults<String> listAllRemoteSystems(PagingRequest paging);
   
   
   /**
    * Fetches the credentials for the current user for the specified
    *  System. If multiple credentials exist, the first is returned, so
    *  this should only be used for systems where a user is restricted
    *  to only one set of credentials per system.
    * @return The Credentials, or Null if none exist for the current user
    */
   BaseCredentialsInfo getPersonCredentials(String remoteSystem);

   
   /**
    * Lists all Credentials for the current user for the given Remote System 
    * 
    * @param remoteSystem The Remote System to return credentials for
    * @param credentialsType Optional type (including child subtypes) of the credentials to filter by
    */
   PagingResults<? extends BaseCredentialsInfo> listPersonCredentials(String remoteSystem, QName credentialsType, PagingRequest paging);

   /**
    * Lists all Credentials that are shared with the current user for
    *  the given Remote System 
    * 
    * @param remoteSystem The Remote System to return credentials for
    * @param credentialsType Optional type (including child subtypes) of the credentials to filter by
    */
   PagingResults<? extends BaseCredentialsInfo> listSharedCredentials(String remoteSystem, QName credentialsType, PagingRequest paging);

   /**
    * Lists all Credentials that the user has access to
    *  for the given Remote System 
    * 
    * @param remoteSystem The Remote System to return credentials for
    * @param credentialsType Optional type (including child subtypes) of the credentials to filter by
    */
   PagingResults<? extends BaseCredentialsInfo> listAllCredentials(String remoteSystem, QName credentialsType, PagingRequest paging);
}
