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
package org.alfresco.service.cmr.action;

import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.namespace.QName;

/**
 * Rule action interface.
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface ActionDefinition extends ParameterizedItemDefinition
{
    /**
     * Gets a list of the types that this action item is applicable for
     * 
     * @return set of types never <tt>null</tt>
     */
    public Set<QName> getApplicableTypes();

    /**
     * Get whether the basic action definition supports action tracking or not. This can be overridden for each {@link Action#getTrackStatus() action} but if not, this value is used. Defaults to <tt>false</tt>.
     * 
     * @return <tt>true</tt> to track action execution status or <tt>false</tt> (default) to do no action tracking
     * 
     * @since 3.4.1
     */
    public boolean getTrackStatus();
}
