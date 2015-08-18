/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.classification.permission;

import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceBootstrap;
import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.module.org_alfresco_module_rm.util.TransactionalResourceHelper;
import org.alfresco.repo.security.permissions.processor.impl.PermissionPreProcessorBaseImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.util.Triple;

/**
 * Classification permission pre-processor implementation.
 * 
 * @author Roy Wetherall
 * @since 3.0.a
 */
public class ClassificationPermissionPreProcessor extends PermissionPreProcessorBaseImpl 
{
	/** transaction resource keys */
	/*package*/ static final String KEY_PROCESSING = ClassificationPermissionPreProcessor.class.getName() + ".processing";
	/*package*/ static final String KEY_CACHE = ClassificationPermissionPreProcessor.class.getName() + ".cache";
	
	/** content classification service */
	private ContentClassificationService contentClassificationService;

	/** transaction resource helper */
	private TransactionalResourceHelper transactionalResourceHelper;
	
	/** classificaiton service bootstrap */
	private ClassificationServiceBootstrap classificationServiceBootstrap;
	
	/** authentication util */
	private AuthenticationUtil authenticationUtil;
	
	/**
	 * @param contentClassificationService	content classification service
	 */
	public void setContentClassificationService(ContentClassificationService contentClassificationService) 
	{
		this.contentClassificationService = contentClassificationService;
	}
	
	/**
	 * @param transactionalResourceHelper	transaction resource helper
	 */
	public void setTransactionalResourceHelper(TransactionalResourceHelper transactionalResourceHelper) 
	{
		this.transactionalResourceHelper = transactionalResourceHelper;
	}
	
	/**
	 * @param classificationServiceBootstrap	classification service bootstrap
	 */
	public void setClassificationServiceBootstrap(ClassificationServiceBootstrap classificationServiceBootstrap) 
	{
		this.classificationServiceBootstrap = classificationServiceBootstrap;
	}
	
	/**
	 * @param authenticationUtil	authentication util
	 */
	public void setAuthenticationUtil(AuthenticationUtil authenticationUtil) 
	{
		this.authenticationUtil = authenticationUtil;
	}
	
	/**
	 * @see org.alfresco.repo.security.permissions.processor.PermissionPreProcessor#process(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
	 */
	@Override
	public AccessStatus process(NodeRef nodeRef, String perm) 
	{
		AccessStatus result = AccessStatus.UNDETERMINED;
		
		// ensure the classification bootstrap has been initialised
		if (classificationServiceBootstrap.isInitialised())
		{						
			// do not process a node that is already being processed
			Set<Object> processing = transactionalResourceHelper.getSet(KEY_PROCESSING);
			if (!processing.contains(nodeRef))
			{
				processing.add(nodeRef);
				try
				{
					// create key
					final String currentUser = authenticationUtil.getRunAsUser();
					Triple<NodeRef, String, String> key = new Triple<NodeRef, String, String>(nodeRef, perm, currentUser);
					
					// get transaction cache (the purpose of this is prevent duplicate tests within the same transaction)
					Map<Triple<NodeRef, String, String>, AccessStatus> cache = transactionalResourceHelper.getMap(KEY_CACHE);
					if (!cache.containsKey(key))
					{							
						// determine whether the current user has clearance for the node
						if (!contentClassificationService.hasClearance(nodeRef))
						{
							result = AccessStatus.DENIED;
						}
						
						// cache value (in transaction)
						cache.put(key, result);
					}
					else
					{
						result = cache.get(key);
					}
				}
				finally
				{
					processing.remove(nodeRef);
				}
			}				
		}
		
		return result;
	}
}
