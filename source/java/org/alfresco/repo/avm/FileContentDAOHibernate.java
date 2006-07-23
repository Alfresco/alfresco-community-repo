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

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author britt
 *
 */
class FileContentDAOHibernate extends HibernateDaoSupport implements
        FileContentDAO
{
    /**
     * Do nothing constructor.
     */
    public FileContentDAOHibernate()
    {
        super();
    }
    
    /**
     * Save one.
     * @param content The one to save.
     */
    public void save(FileContent content)
    {
        getSession().save(content);
    }
    
    /**
     * Delete one.
     * @param content To be deleted.
     */
    public void delete(FileContent content)
    {
        getSession().delete(content);
    }
}
