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

package org.alfresco.repo.avm.hibernate;


import org.alfresco.repo.avm.IssuerDAO;
import org.alfresco.service.cmr.avm.AVMException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * DAO for Issuers.  Hibernate version.
 * @author britt
 */
class IssuerDAOHibernate extends HibernateDaoSupport implements
        IssuerDAO
{
    /**
     * Do nothing constructor.
     */
    public IssuerDAOHibernate()
    {
        super();
    }
    
    /**
     * Get the largest issued id for the named issuer.
     * @param The name of the issuer.
     * @return The value or null if the issuer is brand new.
     * @throws AVMException on an invalid name.
     */
    public Long getIssuerValue(String name)
    {
        if (name.equals("content"))
        {
            return (Long)getSession().
                createQuery("select max(fc.id) from FileContentImpl fc").uniqueResult();
        }
        else if (name.equals("layer"))
        {
            return (Long)getSession().
                createQuery("select max(an.layerID) from AVMNodeImpl an").uniqueResult();
        }
        else if (name.equals("node"))
        {
            return (Long)getSession().createQuery("select max(an.id) from AVMNodeImpl an").uniqueResult();            
        }
        throw new AVMException("Unknown issuer type: " + name);
    }
}
