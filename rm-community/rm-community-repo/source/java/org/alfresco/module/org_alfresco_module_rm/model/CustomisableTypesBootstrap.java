 
package org.alfresco.module.org_alfresco_module_rm.model;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Bootstrap bean that indicates that the specified types or aspects are
 * customizable.
 * 
 * @author Roy Wetherall
 * @since 2.0
 */
public class CustomisableTypesBootstrap 
{
	/** Records management admin service */
	private RecordsManagementAdminService recordsManagementAdminService;
	
	/** Namespace service */
	private NamespaceService namespaceService;
	
	/** List of types and aspects to register as customisable */
	private List<String> customisable;
	
	/**
	 * @param recordsManagementAdminService	records management admin service
	 */
	public void setRecordsManagementAdminService(RecordsManagementAdminService recordsManagementAdminService) 
	{
		this.recordsManagementAdminService = recordsManagementAdminService;
	}
	
	/**
	 * @param namespaceService	namespace service
	 */
	public void setNamespaceService(NamespaceService namespaceService) 
	{
		this.namespaceService = namespaceService;
	}
	
	/**
	 * @param customizable	list of types and aspects to register as customisable
	 */
	public void setCustomisable(List<String> customisable)
	{
		this.customisable = customisable;
	}
	
	/**
	 * Bean initialisation method
	 */
	public void init()
	{
		for (String customType : customisable) 
		{
			QName customTypeQName = QName.createQName(customType, namespaceService);
			recordsManagementAdminService.makeCustomisable(customTypeQName);
		}
	}
}
