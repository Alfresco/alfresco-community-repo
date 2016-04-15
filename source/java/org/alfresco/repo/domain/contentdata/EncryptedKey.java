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
package org.alfresco.repo.domain.contentdata;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class EncryptedKey implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String masterKeystoreId;
    private String masterKeyAlias;
    private final String algorithm;
    private final ByteBuffer encryptedKeyBytes;
    
    public EncryptedKey(String masterKeystoreId, String masterKeyAlias, String algorithm, ByteBuffer encryptedKeyBytes)
    {
        this.masterKeyAlias = masterKeyAlias;
        this.masterKeystoreId = masterKeystoreId;
        this.algorithm = algorithm;
        this.encryptedKeyBytes = encryptedKeyBytes.asReadOnlyBuffer();
    }

    public String getMasterKeystoreId()
    {
        return masterKeystoreId;
    }

    public String getMasterKeyAlias()
    {
        return masterKeyAlias;
    }

    public ByteBuffer getEncryptedKeyBytes()
    {
        return encryptedKeyBytes;
    }

    public String getAlgorithm()
    {
        return this.algorithm;
    }
    
    public ByteBuffer getByteBuffer()
    {
        return this.encryptedKeyBytes.asReadOnlyBuffer();
    }

    public int keySize()
    {
        byte[] eKey = new byte[getByteBuffer().remaining()];
        getByteBuffer().get(eKey);
        return eKey.length * 8;
    }

}
