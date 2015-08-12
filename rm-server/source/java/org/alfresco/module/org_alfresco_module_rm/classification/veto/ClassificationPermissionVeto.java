/**
 * 
 */
package org.alfresco.module.org_alfresco_module_rm.classification.veto;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceBootstrap;
import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.util.TransactionalResourceHelper;
import org.alfresco.repo.security.permissions.veto.PermissionVetoBaseImpl;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Roy Wetherall
 * @since 3.0.a
 */
public class ClassificationPermissionVeto extends PermissionVetoBaseImpl 
{
	private ContentClassificationService contentClassificationService;

	private TransactionalResourceHelper transactionalResourceHelper;
	
	private ClassificationServiceBootstrap classificationServiceBootstrap;
	
	public void setContentClassificationService(ContentClassificationService contentClassificationService) 
	{
		this.contentClassificationService = contentClassificationService;
	}
	
	public void setTransactionalResourceHelper(TransactionalResourceHelper transactionalResourceHelper) 
	{
		this.transactionalResourceHelper = transactionalResourceHelper;
	}
	
	public void setClassificationServiceBootstrap(ClassificationServiceBootstrap classificationServiceBootstrap) 
	{
		this.classificationServiceBootstrap = classificationServiceBootstrap;
	}
	
	/**
	 * @see org.alfresco.repo.security.permissions.veto.PermissionVeto#isVetoed(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
	 */
	@Override
	public boolean isVetoed(NodeRef nodeRef, String perm) 
	{
		boolean result = false;
		
		if (classificationServiceBootstrap.isInitialised())
		{
			Set<Object> processing = transactionalResourceHelper.getSet(ClassificationPermissionVeto.class.getName());
			if (!processing.contains(nodeRef))
			{
				processing.add(nodeRef);
				try
				{
					result = !contentClassificationService.hasClearance(nodeRef);
				}
				finally
				{
					processing.remove(nodeRef);
				}
			}
		}
		
		return result;
	}
}
