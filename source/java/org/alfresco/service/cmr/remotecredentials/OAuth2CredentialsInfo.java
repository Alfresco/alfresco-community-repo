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
package org.alfresco.service.cmr.remotecredentials;

import java.util.Date;

/**
 * This class represents an OAuth 2.0 based set of credentials
 * 
 * @author Nick Burch
 * @since 4.0.2
 */
public interface OAuth2CredentialsInfo extends BaseCredentialsInfo 
{
   /**
    * @return the OAuth Access Token
    */
   String getOAuthAccessToken();
   
   /**
    * @return the OAuth Refresh
    */
   String getOAuthRefreshToken();
   
   /**
    * @return When the Access Token was Issued
    */
   Date getOAuthTicketIssuedAt();
   
   /**
    * @return When the Access Token will Expire
    */
   Date getOAuthTicketExpiresAt();
}
