/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
