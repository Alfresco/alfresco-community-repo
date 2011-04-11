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

import javax.servlet.http.HttpServletResponse;

import org.alfresco.scripts.ScriptException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.ScriptContent;
import org.springframework.extensions.webscripts.WebScriptException;


/** 
 * Extended WebScript for converting an exception to an appropriate CMIS
 * status response code
 *  
 * @author davidc
 */
public class CMISWebScript extends DeclarativeWebScript
{

    @Override
    protected void executeScript(ScriptContent location, Map<String, Object> model)
    {
        try
        {
            super.executeScript(location, model);
        }
        catch(ScriptException e)
        {
            Throwable root = e.getCause();
            if (root != null && root instanceof FileExistsException)
            {
                throw new WebScriptException(HttpServletResponse.SC_CONFLICT, e.getMessage(), e);
            }
            throw e;
        }
    }

}
