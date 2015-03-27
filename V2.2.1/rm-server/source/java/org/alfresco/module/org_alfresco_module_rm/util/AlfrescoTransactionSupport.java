/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.util;

/**
 * Alfresco Transaction Support delegation bean.
 * 
 * @author Roy Wetherall
 * @since 2.3
 * @see org.alfresco.repo.transaction.AlfrescoTransactionSupport
 */
public class AlfrescoTransactionSupport
{
    /**
     * @see org.alfresco.repo.transaction.AlfrescoTransactionSupport#bindResource(Object, Object)
     */
    public void bindResource(Object key, Object resource)
    {
        org.alfresco.repo.transaction.AlfrescoTransactionSupport.bindResource(key, resource);
    }
    
    /**
     * @see org.alfresco.repo.transaction.AlfrescoTransactionSupport#unbindResource(Object)
     */
    public void unbindResource(Object key)
    {
        org.alfresco.repo.transaction.AlfrescoTransactionSupport.unbindResource(key);
    }
}
