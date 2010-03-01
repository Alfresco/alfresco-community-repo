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
package org.alfresco.repo.deploy;

import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.deploy.DeploymentEvent;

class DeploymentWork 
{
    private DeploymentEvent event;
    private AVMNodeDescriptor src;
    private String ticket;
    private int version;
    	
    public DeploymentWork(DeploymentEvent event, String ticket) 
    {
    	this.event = event;
    	this.ticket = ticket;	
    }
    	
    public DeploymentWork(DeploymentEvent event, String ticket,  AVMNodeDescriptor src, int version) 
    {
    	this.event = event;
    	this.ticket = ticket;
    	this.setSrc(src);
    	this.version = version;
    }
    	
    public DeploymentEvent getEvent() 
    {
    	return event;
    }
    	
    public String getTicket()
    {
    	return this.ticket;
    }

	public void setSrc(AVMNodeDescriptor src) {
		this.src = src;
	}

	public AVMNodeDescriptor getSrc() {
		return src;
	}
	
	public int getVersion()
	{
		return version;
	}
    
}
