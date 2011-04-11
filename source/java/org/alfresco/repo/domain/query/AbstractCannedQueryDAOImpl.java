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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.domain.query;

import org.alfresco.repo.domain.control.ControlDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DAO implementation providing canned query support.
 * 
 * @author Derek Hulley
 * @since 3.5
 */
public abstract class AbstractCannedQueryDAOImpl implements CannedQueryDAO
{
    protected Log logger = LogFactory.getLog(this.getClass());
    
    protected ControlDAO controlDAO;

    /**
     * @param controlDAO        the DAO that allows controlled rollback, if required
     */
    public void setControlDAO(ControlDAO controlDAO)
    {
        this.controlDAO = controlDAO;
    }
    
    /**
     * Checks that properties have been set
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "controlDAO", controlDAO);
    }
}
