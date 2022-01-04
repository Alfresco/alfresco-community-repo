/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.model.security;

import org.alfresco.repo.security.permissions.AccessDeniedException;

/**
 * Model access denied exception implementation
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class ModelAccessDeniedException extends AccessDeniedException
{
    private static final long serialVersionUID = 6796435040345714366L;

    public ModelAccessDeniedException(String msg)
    {
        super(msg);
    }
    
    public ModelAccessDeniedException(String msg, Throwable cause)
    {
        super(msg, cause);        
    }        
}
