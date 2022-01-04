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

package org.alfresco.module.org_alfresco_module_rm.util;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Delegate spring bean for TransactionResourceHelper
 * 
 * @author Roy Wetherall
 * @since 2.3
 * @see org.alfresco.repo.transaction.TransactionalResourceHelper
 */
public class TransactionalResourceHelper
{
    /**
     * @see org.alfresco.repo.transaction.TransactionalResourceHelper#getCount(Object)
     */
    public int getCount(Object resourceKey)
    {
        return org.alfresco.repo.transaction.TransactionalResourceHelper.getCount(resourceKey);
    }
    
    /**
     * @see org.alfresco.repo.transaction.TransactionalResourceHelper#getCount(Object)
     */
    public void resetCount(Object resourceKey)
    {
        org.alfresco.repo.transaction.TransactionalResourceHelper.resetCount(resourceKey);
    }
    
    /**
     * @see org.alfresco.repo.transaction.TransactionalResourceHelper#incrementCount(Object)
     */
    public int incrementCount(Object resourceKey)
    {
        return org.alfresco.repo.transaction.TransactionalResourceHelper.incrementCount(resourceKey);
    }
    
    /**
     * @see org.alfresco.repo.transaction.TransactionalResourceHelper#decrementCount(Object, boolean)
     */
    public int decrementCount(Object resourceKey, boolean allowNegative)
    {
        return org.alfresco.repo.transaction.TransactionalResourceHelper.decrementCount(resourceKey, allowNegative);
    }
    
    /**
     * @see org.alfresco.repo.transaction.TransactionalResourceHelper#getCount(Object)
     */
    public boolean isResourcePresent(Object resourceKey)
    {
        return org.alfresco.repo.transaction.TransactionalResourceHelper.isResourcePresent(resourceKey);
    }
    
    /**
     * @see org.alfresco.repo.transaction.TransactionalResourceHelper#getMap(Object)
     */
    public <K,V> Map<K,V> getMap(Object resourceKey)
    {
        return org.alfresco.repo.transaction.TransactionalResourceHelper.getMap(resourceKey);
    }

    /**
     * @see org.alfresco.repo.transaction.TransactionalResourceHelper#getSet(Object)
     */
    public <V> Set<V> getSet(Object resourceKey)
    {
        return org.alfresco.repo.transaction.TransactionalResourceHelper.getSet(resourceKey);
    }

    /**
     * @see org.alfresco.repo.transaction.TransactionalResourceHelper#getTreeSet(Object)
     */
    public <V> TreeSet<V> getTreeSet(Object resourceKey)
    {
        return org.alfresco.repo.transaction.TransactionalResourceHelper.getTreeSet(resourceKey);
    }

    /**
     * @see org.alfresco.repo.transaction.TransactionalResourceHelper#getList(Object)
     */
    public <V> List<V> getList(Object resourceKey)
    {
        return org.alfresco.repo.transaction.TransactionalResourceHelper.getList(resourceKey);
    }
}
