/*
 * Copyright 2014-2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.domain.contentdata;

import java.io.Serializable;

/**
 * 
 * @author sglover
 *
 */
public class SymmetricKeyCount implements Serializable
{
	private static final long serialVersionUID = -7823962733045613866L;

	private String masterKeyAlias;
	private int count;

	public SymmetricKeyCount()
	{
	}

	public String getMasterKeyAlias()
	{
		return masterKeyAlias;
	}

	public void setMasterKeyAlias(String masterKeyAlias)
	{
		this.masterKeyAlias = masterKeyAlias;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	@Override
	public String toString()
	{
		return "SymmetricKeyCount [masterKeyAlias=" + masterKeyAlias
				+ ", count=" + count + "]";
	}
}
