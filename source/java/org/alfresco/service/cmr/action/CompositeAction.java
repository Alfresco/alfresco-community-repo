
package org.alfresco.service.cmr.action;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Composite action
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface CompositeAction extends Action, ActionList<Action>
{

}
