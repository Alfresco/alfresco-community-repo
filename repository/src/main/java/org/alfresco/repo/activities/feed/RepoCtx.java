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
package org.alfresco.repo.activities.feed;

import java.io.Serializable;

import org.alfresco.repo.admin.SysAdminParams;

/**
 * Repository context passed from grid task to grid job
 */
public class RepoCtx implements Serializable
{
    private SysAdminParams sysAdminParams;
    private String repoEndPoint;  
    private boolean userNamesAreCaseSensitive = false;
    
	private String ticket;
	
	public static final long serialVersionUID = -3896042917378679686L;
	
    public RepoCtx(SysAdminParams sysAdminParams, String repoEndPoint)
    {
        this.sysAdminParams = sysAdminParams;
	    this.repoEndPoint = repoEndPoint.endsWith("/") ? repoEndPoint.substring(0, repoEndPoint.length()-1) : repoEndPoint;
    }

	public String getRepoEndPoint() {
        String base = sysAdminParams.getAlfrescoProtocol() + "://" + sysAdminParams.getAlfrescoHost() + ":" + sysAdminParams.getAlfrescoPort() + "/" + sysAdminParams.getAlfrescoContext();
        return base + repoEndPoint;
	}

    public String getTicket()
    {
        return ticket;
    }

    public void setTicket(String ticket)
    {
        this.ticket = ticket;
    }

    public boolean isUserNamesAreCaseSensitive()
    {
        return userNamesAreCaseSensitive;
    }

    public void setUserNamesAreCaseSensitive(boolean userNamesAreCaseSensitive)
    {
        this.userNamesAreCaseSensitive = userNamesAreCaseSensitive;
    }
}
