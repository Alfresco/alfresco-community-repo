/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.facebook;


/**
 * Facebook Application
 * 
 * @author davidc
 */
public class FacebookAppModel
{
    String appId;
    String apiKey;    
    String secretKey;
    
    /**
     * Construct
     * 
     * @param apiKey
     */
    public FacebookAppModel(String apiKey)
    {
        this.apiKey = apiKey;
    }
    
    /**
     * @return  application apiKey
     */
    public String getApiKey()
    {
        return apiKey;
    }

    /**
     * @return  application secret
     */
    public String getSecret()
    {
        return secretKey;
    }

    /**
     * @param secretKey  application secret
     */
    public void setSecret(String secretKey)
    {
        this.secretKey = secretKey;
    }

    /**
     * @return  application id
     */
    public String getId()
    {
        return appId;
    }

    /**
     * @param appId  application id
     */
    public void setId(String appId)
    {
        this.appId = appId;
    }
    
}
