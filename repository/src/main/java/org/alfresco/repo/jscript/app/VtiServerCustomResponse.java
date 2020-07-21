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
package org.alfresco.repo.jscript.app;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.jscript.ScriptUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Return current state of VTI (SharePoint) Server module
 *
 * @author mikeh
 */
public class VtiServerCustomResponse implements CustomResponse
{
    private static Log logger = LogFactory.getLog(VtiServerCustomResponse.class);

    private static final String VTI_MODULE = "org.alfresco.module.vti";

    private int vtiServerPort = 0;
    private String vtiServerHost;
    private String vtiServerProtocol;
    private String contextPath;
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
     * @param sysAdminParams SysAdminParams
     */
    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }

    /**
     * Setter for vtiServer Port
     *
     * @param vtiServerPort int
     */
    public void setPort(int vtiServerPort)
    {
        this.vtiServerPort = vtiServerPort;
    }

    /**
     * Setter for vtiServer Host
     *
     * @param vtiServerHost String
     */
    public void setHost(String vtiServerHost)
    {
        this.vtiServerHost = vtiServerHost;
    }

    /**
     * Setter for vtiServer Protocol
     *
     * @param vtiServerProtocol String
     */
    public void setProtocol(String vtiServerProtocol)
    {
        this.vtiServerProtocol = vtiServerProtocol;
    }
    
    /**
     * Setter for the vtiServer (external) context path.
     * 
     * @param contextPath String
     */
    public void setContextPath(String contextPath)
    {
        this.contextPath = contextPath;
    }

    /**
     * Populates the CustomResponse with the vti metadata
     *
     * @return JSONObject or null if the vti module is not installed.
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
            if (this.vtiServerProtocol != null)
            {
                jsonObj.put("protocol", this.vtiServerProtocol);
            }
            if (contextPath != null)
            {                
                jsonObj.put("contextPath", contextPath);
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
