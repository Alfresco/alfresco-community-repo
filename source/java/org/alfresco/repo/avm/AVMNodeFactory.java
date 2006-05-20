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

import org.alfresco.repo.avm.hibernate.AVMNodeBean;
import org.alfresco.repo.avm.hibernate.LayeredDirectoryNodeBean;
import org.alfresco.repo.avm.hibernate.LayeredFileNodeBean;
import org.alfresco.repo.avm.hibernate.PlainDirectoryNodeBean;
import org.alfresco.repo.avm.hibernate.PlainFileNodeBean;
import org.hibernate.proxy.HibernateProxy;

/**
 * Responsible for instantiating AVMNode concrete subclasses from 
 * underlying data beans.
 * @author britt
 */
public class AVMNodeFactory
{
    /**
     * Create a node from a data bean.
     * @param bean The AVMNodeBean.
     */
    public static AVMNode CreateFromBean(AVMNodeBean bean)
    {
        if (bean == null)
        {
            return null;
        }
        HibernateProxy proxy = (HibernateProxy)bean;
        bean = (AVMNodeBean)proxy.getHibernateLazyInitializer().getImplementation();
        if (bean instanceof PlainFileNodeBean)
        {
            return new PlainFileNode((PlainFileNodeBean)bean);
        }
        if (bean instanceof PlainDirectoryNodeBean)
        {
            return new PlainDirectoryNode((PlainDirectoryNodeBean)bean);
        }
        if (bean instanceof LayeredDirectoryNodeBean)
        {
            return new LayeredDirectoryNode((LayeredDirectoryNodeBean)bean);
        }
        assert bean instanceof LayeredFileNodeBean : "Unknown AVMNodeBean suptype.";
        return new LayeredFileNode((LayeredFileNodeBean)bean);
    }
}
