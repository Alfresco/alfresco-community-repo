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
			int result = prop1.compareTo(prop2);
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
