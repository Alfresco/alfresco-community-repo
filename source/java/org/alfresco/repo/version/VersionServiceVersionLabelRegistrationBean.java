/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.version;

import org.alfresco.repo.version.VersionServicePolicies.CalculateVersionLabelPolicy;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;

/**
 * Utility class to register a version label policy version service.
 * 
 * Used to configure the version service via spring.
 * 
 */
public class VersionServiceVersionLabelRegistrationBean 
{
	private String typeQName;
	
	private CalculateVersionLabelPolicy policy;
	
	private VersionService versionService;
	
	private NamespacePrefixResolver prefixResolver;
	
	/**
	 * Register the deployment target with the deployment target registry
	 */
	public void register()
	{
		PropertyCheck.mandatory(this, "typeQName", typeQName);
		PropertyCheck.mandatory(this, "versionService", getVersionService());
		PropertyCheck.mandatory(this, "policy", policy);
	    PropertyCheck.mandatory(this, "prefixResolver", prefixResolver);
		
		/**
		 * Go ahead and register the version label policy with the 
		 * versionService
		 */
		QName qName = QName.createQName(typeQName, prefixResolver);
		getVersionService().registerVersionLabelPolicy(qName, policy);
	    
	}
	
	public void setTypeQName(String typeQName) 
	{
		this.typeQName = typeQName;
	}
	
	public String getTypeQName() 
	{
		return typeQName;
	}
	
	public void setPolicy(CalculateVersionLabelPolicy policy)
	{
	    this.policy = policy;
	}
	
	public CalculateVersionLabelPolicy getPolicy()
	{
	    return policy;
	}

    public void setNamespacePrefixResolver(NamespacePrefixResolver prefixResolver)
    {
        this.prefixResolver = prefixResolver;
    }
    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return prefixResolver;
    }

    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }

    public VersionService getVersionService()
    {
        return versionService;
    }
	
}
