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
package org.alfresco.repo.cmis.rest;

import java.util.Map;

import org.alfresco.cmis.CMISResultSet;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * Base Web Script Implementation that ensures query result sets are closed
 */
public class CMISQueryWebScript extends DeclarativeWebScript
{
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeFinallyImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache, java.util.Map)
     */
    @Override
    protected void executeFinallyImpl(WebScriptRequest req, Status status, Cache cache, Map<String, Object> model)
    {
        Object object = model.get("resultset");
        if (object != null && object instanceof CMISResultSet)
        {
            CMISResultSet resultSet = (CMISResultSet)object;
            resultSet.close();
        }
    }
    

}
