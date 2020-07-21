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
package org.alfresco.repo.content;

import org.alfresco.repo.tenant.TenantDeployer;

/**
 * ContentStore capabilities. Allows us to avoid performing {@code instanceof} questions
 * which can become a problem when certain proxies or subsystems are in use.
 * <p>
 * See ACE-2682 (tenant creation failure) for motivation.
 * 
 * @author Matt Ward
 */
public interface ContentStoreCaps
{
    /**
     * Returns the ContentStore cast to a TenantRoutingContentStore if the underlying
     * instance is of that type. Returns null otherwise.
     * <p>
     * Note, the actual return type is a TenantDeployer (supertype of TenantRoutingContentStore)
     * since the data model has no knowledge of that subtype. This interface may
     * need to move to a different project.
     * 
     * @return TenantRoutingContentStore
     */
    TenantDeployer getTenantRoutingContentStore();
    
    /**
     * Returns the ContentStore cast to a TenantDeployer if the underlying
     * instance is of that type. Returns null otherwise.
     * 
     * @return TenantDeployer
     */
    TenantDeployer getTenantDeployer();
}
