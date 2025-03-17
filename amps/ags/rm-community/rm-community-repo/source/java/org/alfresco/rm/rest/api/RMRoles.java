package org.alfresco.rm.rest.api;

import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.model.RoleModel;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * RM Roles API
 */
public interface RMRoles
{

    String PARAM_INCLUDE_SYSTEM_ROLES="includeSystemRoles";
    String PARAM_PERSON_ID="personId";


    /**
     * Gets a list of roles.
     *
     * @param filePlan the file plan node reference
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     *        including:
     *        - filter, sort & paging params (where, orderBy, skipCount, maxItems)
     *        - include param (personId, includeSystemRoles)
     * @return a paged list of {@code org.alfresco.rm.rest.api.model.RoleModel} objects
     */
    CollectionWithPagingInfo<RoleModel> getRoles(NodeRef filePlan, Parameters parameters);
}
