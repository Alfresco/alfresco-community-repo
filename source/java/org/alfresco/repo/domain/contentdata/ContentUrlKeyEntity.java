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

import org.alfresco.service.cmr.repository.ContentUrlKey;
import org.apache.commons.codec.DecoderException;

/**
 * 
 * @author sglover
 *
 */
public class ContentUrlKeyEntity implements Serializable
{
    private static final long serialVersionUID = -6594309522849585169L;

    private Long id;
    private Long contentUrlId;
    private byte[] encryptedKeyAsBytes;
    private Integer keySize;
    private String algorithm;
    private String masterKeystoreId;
    private String masterKeyAlias;
    private Long unencryptedFileSize;

    public ContentUrlKeyEntity()
    {
    }

    public ContentUrlKey getContentUrlKey() throws DecoderException
    {
        ContentUrlKey contentUrlKey = new ContentUrlKey();
        contentUrlKey.setAlgorithm(algorithm);
        contentUrlKey.setKeySize(keySize);
          contentUrlKey.setEncryptedKeyBytes(ByteBuffer.wrap(encryptedKeyAsBytes));
        contentUrlKey.setMasterKeyAlias(masterKeyAlias);
        contentUrlKey.setMasterKeystoreId(masterKeystoreId);
        contentUrlKey.setUnencryptedFileSize(unencryptedFileSize);
        return contentUrlKey;
    }

    public Long getContentUrlId()
    {
        return contentUrlId;
    }

    public void setContentUrlId(Long contentUrlId)
    {
        this.contentUrlId = contentUrlId;
    }

    public void setEncryptedKeyAsBytes(byte[] encryptedKeyAsBytes)
    {
        this.encryptedKeyAsBytes = encryptedKeyAsBytes;
    }

    public byte[] getEncryptedKeyAsBytes()
    {
        return encryptedKeyAsBytes;
    }

    public void setEncryptedKey(EncryptedKey encryptedKey)
    {
        byte[] encryptedKeyAsBytes = new byte[encryptedKey.getByteBuffer().remaining()];
        encryptedKey.getByteBuffer().get(encryptedKeyAsBytes);

        this.encryptedKeyAsBytes = encryptedKeyAsBytes;
        this.keySize = encryptedKeyAsBytes.length*8;
        this.algorithm = encryptedKey.getAlgorithm();
        this.masterKeyAlias = encryptedKey.getMasterKeyAlias();
        this.masterKeystoreId = encryptedKey.getMasterKeystoreId();
    }

    public static ContentUrlKeyEntity setEncryptedKey(ContentUrlKeyEntity existing, EncryptedKey encryptedKey)
    {
        ContentUrlKeyEntity newContentUrlKeyEntity = new ContentUrlKeyEntity();

        byte[] encryptedKeyAsBytes = new byte[encryptedKey.getByteBuffer().remaining()];
        encryptedKey.getByteBuffer().get(encryptedKeyAsBytes);
        newContentUrlKeyEntity.setEncryptedKeyAsBytes(encryptedKeyAsBytes);
        newContentUrlKeyEntity.setKeySize(encryptedKeyAsBytes.length*8);
        newContentUrlKeyEntity.setAlgorithm(encryptedKey.getAlgorithm());
        newContentUrlKeyEntity.setMasterKeyAlias(encryptedKey.getMasterKeyAlias());
        newContentUrlKeyEntity.setMasterKeystoreId(encryptedKey.getMasterKeystoreId());
        newContentUrlKeyEntity.setContentUrlId(existing.getContentUrlId());
        newContentUrlKeyEntity.setUnencryptedFileSize(existing.getUnencryptedFileSize());
        newContentUrlKeyEntity.setId(existing.getId());

        return newContentUrlKeyEntity;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }
    
    public EncryptedKey getEncryptedKey() throws DecoderException
    {
        EncryptedKey encryptedKey = new EncryptedKey(getMasterKeystoreId(), getMasterKeyAlias(),
                getAlgorithm(), ByteBuffer.wrap(this.encryptedKeyAsBytes));
        return encryptedKey;
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

	@Override
    public int hashCode()
	{
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((algorithm == null) ? 0 : algorithm.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((masterKeyAlias == null) ? 0 : masterKeyAlias.hashCode());
        result = prime
                * result
                + ((masterKeystoreId == null) ? 0 : masterKeystoreId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ContentUrlKeyEntity other = (ContentUrlKeyEntity) obj;
        if (algorithm == null) {
            if (other.algorithm != null)
                return false;
        } else if (!algorithm.equals(other.algorithm))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (masterKeyAlias == null) {
            if (other.masterKeyAlias != null)
                return false;
        } else if (!masterKeyAlias.equals(other.masterKeyAlias))
            return false;
        if (masterKeystoreId == null) {
            if (other.masterKeystoreId != null)
                return false;
        } else if (!masterKeystoreId.equals(other.masterKeystoreId))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "ContentUrlKeyEntity [id=" + id + ", encryptedKeyAsBytes="
                + encryptedKeyAsBytes+ ", keySize=" + keySize + ", algorithm="
                + algorithm + ", masterKeystoreId=" + masterKeystoreId
                + ", masterKeyAlias=" + masterKeyAlias
                + ", unencryptedFileSize=" + unencryptedFileSize + "]";
    }
}
