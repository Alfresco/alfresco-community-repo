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

import java.util.List;

import org.alfresco.deployment.DeploymentReceiverTransport;
import org.alfresco.deployment.DeploymentTransportOutputFilter;
/**
 * 
 * DeploymentReceiverTransportAdapter are used to adapt the interface used by the FSR Client to the
 * interface used by the underlying transport implementation.
 * 
 * The DeploymentReceiverTransport objects returned will typically be proxy classes to a remote service.
 * 
 * @see org.alfresco.deployment.impl.client.DeploymentReceiverTransportAdapterRMI   
 * @see org.alfresco.deployment.impl.client.DeploymentReceiverTransportAdapterSpringHTTP 
 * @see org.alfresco.deployment.impl.client.DeploymentReceiverTransportAdapterHessian 
 * 
 * @author mrogers
 *
 */
public interface DeploymentReceiverTransportAdapter 
{
	
	/**
	 * getObject is a factory method to get a DeploymentReceiverTransport object, which will typically 
	 * be a proxy to a remote service.
	 * 
	 * It is up to the adapters themselves to decide whether hostName, port or URL takes precedence.
	 *  
	 * @param adapterName the name of this adapter
	 * @param hostName the name of the host to connect to
	 * @param port the port to connect to
	 * @param version the version of the website
	 * @param the path of the website to be deployed
	 * @return a DeploymentRecieverTransport
	 */
	public DeploymentReceiverTransport getTransport(String hostName, int port, int version, String srcPath);

	/**
	 * Get the content transformers for this transport - if the transport does not support
	 * content transformation then simply return null;
	 * @return the content transformers or null if there are no transformers.
	 */
	public List<DeploymentTransportOutputFilter>getTransformers();
}
