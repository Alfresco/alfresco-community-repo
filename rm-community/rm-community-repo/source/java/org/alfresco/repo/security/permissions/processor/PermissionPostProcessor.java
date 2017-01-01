/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.security.permissions.processor;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;



/**
 * Permission Post Processor.
 * 
 * @author Roy Wetherall
 * @since 2.4.a
 */
public interface PermissionPostProcessor
{
	/**
	 * Process permission.
	 * 
	 * @param  accessStatus			current access status
	 * @param  nodeRef				node reference
	 * @param  perm					permission
	 *
	 * @return {@link AccessStatus}
	 */
	AccessStatus process(AccessStatus accessStatus, NodeRef nodeRef, String perm,
						List configuredReadPermissions, List configuredFilePermissions);
}
