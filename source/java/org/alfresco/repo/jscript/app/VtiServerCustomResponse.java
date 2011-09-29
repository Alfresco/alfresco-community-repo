/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.jscript.app;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.jscript.ScriptUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.migration.commands.StatusCommand;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Return current state of VTI (SharePoint) Server module
 *
 * @author: mikeh
 */
public class VtiServerCustomResponse implements CustomResponse
{
    private static Log logger = LogFactory.getLog(VtiServerCustomResponse.class);

    private static final String VTI_MODULE = "org.alfresco.module.vti";

    private int vtiServerPort = 0;
    private String vtiServerHost;
    private SysAdminParams sysAdminParams;
    private ScriptUtils scriptUtils;

    /*
     * Set ScriptUtils
     *
     * @param scriptUtils
     */
    public void setScriptUtils(ScriptUtils scriptUtils) throws BeansException
    {
        this.scriptUtils = scriptUtils;
    }

    /**
     * Setter for sysAdminParams
     *
     * @param sysAdminParams
     */
    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }

    /**
     * Setter for vtiServer Port
     *
     * @param vtiServerPort
     */
    public void setPort(int vtiServerPort)
    {
        this.vtiServerPort = vtiServerPort;
    }

    /**
     * Setter for vtiServer Host
     *
     * @param vtiServerHost
     */
    public void setHost(String vtiServerHost)
    {
        this.vtiServerHost = vtiServerHost;
    }

    /**
     * Populates the DocLib webscript response with custom metadata
     *
     * @return JSONObject or null
     */
    public Serializable populate()
    {
        try
        {
            // Check module is installed
            if (!this.scriptUtils.moduleInstalled(VTI_MODULE))
            {
                return null;
            }

            Map<String, Serializable> jsonObj = new LinkedHashMap<String, Serializable>(4);
            if (this.vtiServerPort != 0)
            {
                jsonObj.put("port", this.vtiServerPort);
            }
            if (this.vtiServerHost != null)
            {
                jsonObj.put("host", this.sysAdminParams.subsituteHost(this.vtiServerHost));
            }
            return (Serializable)jsonObj;
        }
        catch (Exception e)
        {
            logger.warn("Could not add custom Vti Server response to DocLib webscript");
        }
        return null;
    }
}
