/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.web.scripts;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.jscript.ScriptUtils;
import org.alfresco.repo.web.scripts.RepositoryContainer;
import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.extensions.webscripts.WebScript;

/**
 * Override of the JavaScript API ScriptUtils bean "utilsScript" to provide additional
 * Remote API methods using objects not available to base Repository project.
 * <p>
 * See "web-scripts-application-context.xml" for bean definition.
 * 
 * @since 4.2.0
 * @since 5.1 (Moved to Remote API project)
 * @author Kevin Roast
 */
public class WebScriptUtils extends ScriptUtils
{
    protected RepositoryContainer repositoryContainer;
    
    public void setRepositoryContainer(RepositoryContainer repositoryContainer)
    {
        this.repositoryContainer = repositoryContainer;
    }
    
    /**
     * Searches for webscript components with the given family name.
     * 
     * @param family        the family
     * 
     * @return An array of webscripts that match the given family name
     */
    public Object[] findWebScripts(String family)
    {
        List<Object> values = new ArrayList<Object>();
        
        for (WebScript webscript : this.repositoryContainer.getRegistry().getWebScripts())
        {
            if (family != null)
            {
                Set<String> familys = webscript.getDescription().getFamilys();
                if (familys != null && familys.contains(family))
                {
                    values.add(webscript.getDescription());
                }
            }
            else
            {
                values.add(webscript.getDescription());
            }
        }
        
        return values.toArray(new Object[values.size()]);
    }
    
    public String getHostAddress()
    {
        try
        {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e)
        {
            return "Unknown";
        }
    }
    
    public String getHostName()
    {
        try
        {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e)
        {
            return "Unknown";
        }
    }
    
    public RepoUsage getRestrictions()
    {
        return this.services.getRepoAdminService().getRestrictions();
    }
    
    public RepoUsage getUsage()
    {
        return this.services.getRepoAdminService().getUsage();
    }
    
    /**
     * Gets the list of repository stores
     * 
     * @return stores
     */
    public List<StoreRef> getStores()
    {
        return this.services.getNodeService().getStores();
    }
}
