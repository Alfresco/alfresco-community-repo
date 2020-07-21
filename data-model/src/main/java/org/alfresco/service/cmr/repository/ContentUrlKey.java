/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.service.cmr.repository;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * 
 * @author sglover
 *
 */
public class ContentUrlKey implements Serializable
{
	private static final long serialVersionUID = -2943112451758281764L;

	private ByteBuffer encryptedKeyBytes;
    private Integer keySize;
    private String algorithm;
    private String masterKeystoreId;
    private String masterKeyAlias;
    private Long unencryptedFileSize;

    public ContentUrlKey()
    {
    }

	public ByteBuffer getEncryptedKeyBytes() 
	{
		return encryptedKeyBytes;
	}

	public void setEncryptedKeyBytes(ByteBuffer encryptedKeyBytes)
	{
		this.encryptedKeyBytes = encryptedKeyBytes;
	}

	public Long getUnencryptedFileSize()
	{
		return unencryptedFileSize;
	}

	public void setUnencryptedFileSize(Long unencryptedFileSize)
	{
		this.unencryptedFileSize = unencryptedFileSize;
	}

	public void setKeySize(Integer keySize)
	{
		this.keySize = keySize;
	}

	public Integer getKeySize()
	{
		return keySize;
	}

	public String getAlgorithm()
	{
		return algorithm;
	}

	public void setAlgorithm(String algorithm)
	{
		this.algorithm = algorithm;
	}

	public String getMasterKeystoreId()
	{
		return masterKeystoreId;
	}

	public void setMasterKeystoreId(String masterKeystoreId)
	{
		this.masterKeystoreId = masterKeystoreId;
	}

	public String getMasterKeyAlias()
	{
		return masterKeyAlias;
	}

	public void setMasterKeyAlias(String masterKeyAlias) 
	{
		this.masterKeyAlias = masterKeyAlias;
	}
}
