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
package org.alfresco.web.ui.wcm.component;

import org.alfresco.web.bean.wcm.DeploymentServerConfig;

/**
 * Comparator to compare the values of a property on a DeploymentServrtConfig.
 */

public class DeploymentServerConfigComparator implements java.util.Comparator<DeploymentServerConfig>{

	public DeploymentServerConfigComparator(String propertyName) {
		this.propertyName = propertyName;
	}
	private String propertyName;
	
	public int compare(DeploymentServerConfig o1, DeploymentServerConfig o2) {
		String prop1 = (String)o1.getProperties().get(propertyName);
		String prop2 = (String)o2.getProperties().get(propertyName);
		
		if(prop1 != null) {
			int result = prop1.compareTo(prop2 != null ? prop2 : "");
			return result;
		} 
		if(prop2 != null){
			// prop1 is null, prop2 is something
			return -1;
		}
		// both prop1 and prop2 are null
		return 0;
	}
}
