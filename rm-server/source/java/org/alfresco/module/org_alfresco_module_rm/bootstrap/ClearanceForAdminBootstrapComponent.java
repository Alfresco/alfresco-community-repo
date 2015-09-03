/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.bootstrap;

import java.io.Serializable;

import org.alfresco.module.org_alfresco_module_rm.patch.v24.RMv24ClearanceForAdmin;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceBootstrap;
import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;

/**
 * Provide the highest clearance to the admin user. This needs to be run once (either bootstrapped into a
 * fresh system, or as part of an upgrade in {@link RMv24ClearanceForAdmin}) per installation.
 *
 * @author tpage
 */
public class ClearanceForAdminBootstrapComponent implements ClassifiedContentModel
{
    private AuthenticationUtil authenticationUtil;
    private NodeService nodeService;
    private PersonService personService;
    private ClassificationServiceBootstrap classificationServiceBootstrap;

    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil) { this.authenticationUtil = authenticationUtil; }
    public void setNodeService(NodeService nodeService) { this.nodeService = nodeService; }
    public void setPersonService(PersonService personService) { this.personService = personService; }
    public void setClassificationServiceBootstrap(ClassificationServiceBootstrap classificationServiceBootstrap) { this.classificationServiceBootstrap = classificationServiceBootstrap; }

    /**
     * Give the admin user the maximum clearance.
     */
    public void createClearanceForAdmin()
    {
        // Ensure the classification levels are loaded before this patch runs. (Nb. This will result in the
        // classification service bootstrap method being called twice on the start-up that includes this call).
        classificationServiceBootstrap.onBootstrap(null);

        Serializable mostSecureLevel = classificationServiceBootstrap.getClassificationLevelManager()
                    .getMostSecureLevel().getId();
        String adminUserName = authenticationUtil.getAdminUserName();
        NodeRef admin = personService.getPerson(adminUserName, false);
        nodeService.setProperty(admin, PROP_CLEARANCE_LEVEL, mostSecureLevel);
    }
}
