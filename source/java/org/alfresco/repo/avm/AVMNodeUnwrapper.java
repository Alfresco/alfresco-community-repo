/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.alfresco.repo.avm;

import org.hibernate.proxy.HibernateProxy;

/**
 * Utility for unwrapping (getting the actual instance of) an AVMNode from what
 * may be a HibernateProxy.  Bitter Hibernate note: Hibernate proxies for polymorphic
 * types are fundamentally broken.
 * @author britt
 */
public class AVMNodeUnwrapper
{
    public static AVMNode Unwrap(AVMNode node)
    {
        if (node instanceof HibernateProxy)
        {
            return (AVMNode)((HibernateProxy)node).getHibernateLazyInitializer().getImplementation();
        }
        return node;
    }
}
