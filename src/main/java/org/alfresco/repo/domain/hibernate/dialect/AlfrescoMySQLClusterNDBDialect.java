/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.domain.hibernate.dialect;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.dialect.MySQLInnoDBDialect;

/**
 * MySQL Cluster NDB specific DAO overrides
 * 
 * WARNING: 
 * - Experimental only (unsupported) !
 * - The NDB storage engine is *not* currently supported or certified !
 * - Can be used for dev/test evaluation (please give us feedback)
 * - Should not be used for live/prod env with real data !
 * - Requires FK support (hence NDB 7.3.x or higher)
 * 
 * @author janv
 *
 */
//note: *not* a dialect of InnoDB but, for now, extends here so that we can override those scripts
public class AlfrescoMySQLClusterNDBDialect extends MySQLInnoDBDialect
{
    protected Log logger = LogFactory.getLog(AlfrescoMySQLClusterNDBDialect.class);
    
    public AlfrescoMySQLClusterNDBDialect()
    {
        super();
        
        logger.error("Using NDB with Alfresco is experimental and unsupported (do not use for live/prod envs) !");
    }
    
    public String getTableTypeString() {
        return " engine=NDB";
    }

}
