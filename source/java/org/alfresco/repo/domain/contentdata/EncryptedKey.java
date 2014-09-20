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
