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
package org.alfresco.httpclient;

/**
 * 
 * @since 4.0
 *
 */
public class MD5EncryptionParameters
{
    private String cipherAlgorithm;
    private long messageTimeout;
    private String macAlgorithm;

    public MD5EncryptionParameters()
    {
        
    }

    public MD5EncryptionParameters(String cipherAlgorithm,
            Long messageTimeout, String macAlgorithm)
    {
        this.cipherAlgorithm = cipherAlgorithm;
        this.messageTimeout = messageTimeout;
        this.macAlgorithm = macAlgorithm;
    }
    
    public String getCipherAlgorithm()
    {
        return cipherAlgorithm;
    }

    public void setCipherAlgorithm(String cipherAlgorithm)
    {
        this.cipherAlgorithm = cipherAlgorithm;
    }
    
    public long getMessageTimeout()
    {
        return messageTimeout;
    }

    public String getMacAlgorithm()
    {
        return macAlgorithm;
    }
    
    public void setMessageTimeout(long messageTimeout)
    {
        this.messageTimeout = messageTimeout;
    }

    public void setMacAlgorithm(String macAlgorithm)
    {
        this.macAlgorithm = macAlgorithm;
    }
}
