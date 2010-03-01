/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import org.hibernate.proxy.HibernateProxy;

/**
 * Utility for unwrapping (getting the actual instance of) an AVMNode from what
 * may be a HibernateProxy.  Bitter Hibernate note: Hibernate proxies for polymorphic
 * types are fundamentally broken.
 * @author britt
 * 
 * @deprecated
 */
public class AVMNodeUnwrapper
{
    /**
     * @deprecated
     */
    public static AVMNode Unwrap(AVMNode node)
    {
        if (node instanceof HibernateProxy)
        {
            return (AVMNode)((HibernateProxy)node).getHibernateLazyInitializer().getImplementation();
        }
        return node;
    }
}
