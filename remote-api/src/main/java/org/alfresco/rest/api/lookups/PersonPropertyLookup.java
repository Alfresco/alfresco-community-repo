/*-
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.lookups;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.rest.api.impl.NodesImpl;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.service.ServiceRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Looks up information about a person.
 * Pass in a userId and the display name is returned.
 *
 * @author Gethin James
 */
public class PersonPropertyLookup implements PropertyLookup<String>
{
    private Set<String> supported = new HashSet<>();
    private ServiceRegistry serviceRegistry;

    @Override
    public String lookup(String propertyValue)
    {
        Map<String, UserInfo> mapUserInfo = TransactionalResourceHelper.getMap("PERSON_PROPERTY_LOOKUP_USER_INFO_CACHE");
        UserInfo user = Node.lookupUserInfo(propertyValue, mapUserInfo, serviceRegistry.getPersonService());
        if (user != null) return user.getDisplayName();
        return null;
    }

    @Override
    public Set<String> supports()
    {
        return supported;
    }

    public void setSupported(List<String> supported)
    {
        NodesImpl.PROPS_USERLOOKUP.forEach(entry -> this.supported.add(entry.toString()));
        this.supported.addAll(supported);
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }
}
