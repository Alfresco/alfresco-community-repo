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

import org.alfresco.deployment.DeploymentTransportOutputFilter;

/**
 * Abstract Transport Adapter.
 *
 */
public abstract class AbstractDeploymentReceiverTransportAdapter  {
	 
	List<DeploymentTransportOutputFilter> transformers;
	 
	/**
	 * Get the content transformers for this transport - if the transport does not support
	 * content transformation then simply return null;
	 * @return the content transformers or null if there are no transformers.
	 */
	public List<DeploymentTransportOutputFilter>getTransformers() {
		return transformers;
	}
	/**
	 * Set the content transformers for this transport - if the transport does not support
	 * content transformation then simply set null or do not call this method.
	 */	
	public void setTransformers( List<DeploymentTransportOutputFilter> transformers) {
	    this.transformers = transformers;
	}

}
