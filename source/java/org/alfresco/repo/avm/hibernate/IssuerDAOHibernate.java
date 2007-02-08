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
