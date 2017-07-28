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
package org.alfresco.repo.policy;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.namespace.NamespaceService;

/**
 * Marker interface for representing a Policy.
 * 
 * @author David Caruana
 */
@AlfrescoPublicApi
public interface Policy
{
    /**
     * mandatory static field on a <tt>Policy</tt> that can be overridden in
     * derived policies
     */
    static String NAMESPACE = NamespaceService.ALFRESCO_URI;
    
    /**
     * Argument Configuration
     */
    public enum Arg
    {
        KEY,
        START_VALUE,
        END_VALUE
    }
        
}
