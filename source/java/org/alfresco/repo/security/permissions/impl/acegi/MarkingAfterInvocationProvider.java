/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.security.permissions.impl.acegi;

import java.util.Collection;

import net.sf.acegisecurity.AccessDeniedException;
import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.ConfigAttribute;
import net.sf.acegisecurity.ConfigAttributeDefinition;
import net.sf.acegisecurity.afterinvocation.AfterInvocationProvider;

import org.alfresco.repo.security.permissions.PermissionCheckedValue;
import org.alfresco.repo.security.permissions.PermissionCheckedValue.PermissionCheckedValueMixin;

/**
 * Invocation provider that can be used to mark entries that have been permission checked.
 * Use an instance of this class at the end of the 'after' invocations.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class MarkingAfterInvocationProvider implements AfterInvocationProvider
{

    @Override
    public Object decide(
            Authentication authentication,
            Object object,
            ConfigAttributeDefinition config,
            Object returnedObject) throws AccessDeniedException
    {
        // If this object has already been marked, then leave it
        if (returnedObject == null)
        {
            return null;
        }
        else if (returnedObject instanceof PermissionCheckedValue)
        {
            return returnedObject;
        }
        else if (object instanceof Collection<?>)
        {
            // Mark it
            return PermissionCheckedValueMixin.create(returnedObject);
        }
        else
        {
            return returnedObject;
        }
    }

    @Override
    public boolean supports(ConfigAttribute attribute)
    {
        return true;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean supports(Class clazz)
    {
        return true;
    }
}
