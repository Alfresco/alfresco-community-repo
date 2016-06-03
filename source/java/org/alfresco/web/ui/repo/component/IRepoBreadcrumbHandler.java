package org.alfresco.web.ui.repo.component;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;

/**
 * @author Kevin Roast
 */
public interface IRepoBreadcrumbHandler extends IBreadcrumbHandler
{
   /**
    * Return a NodeRef relevant to this breadcrumb element, if any 
    * 
    * @return a NodeRef if relevant to the breadcrumb element, null otherwise
    */
   public NodeRef getNodeRef();
}
