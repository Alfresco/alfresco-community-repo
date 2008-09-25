/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.deploy;

import org.alfresco.deployment.DeploymentReceiverTransport;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

/**
 * Transport adapter to connect to the FSR using RMI protocol.
 * 
 * @author mrogers
 *
 */
public class DeploymentReceiverTransportAdapterRMI extends AbstractDeploymentReceiverTransportAdapter implements DeploymentReceiverTransportAdapter {

	public DeploymentReceiverTransport getTransport(String hostName,
			int port, int version, String srcPath) 
	{
		
       	// Code to use RMI transport 
        RmiProxyFactoryBean factory = new RmiProxyFactoryBean();
        factory.setRefreshStubOnConnectFailure(true);
        factory.setServiceInterface(DeploymentReceiverTransport.class);
        factory.setServiceUrl("rmi://" + hostName + ":" + port + "/deployment");
        factory.afterPropertiesSet();
        DeploymentReceiverTransport transport = (DeploymentReceiverTransport)factory.getObject();
        
		return transport;
	}

}
