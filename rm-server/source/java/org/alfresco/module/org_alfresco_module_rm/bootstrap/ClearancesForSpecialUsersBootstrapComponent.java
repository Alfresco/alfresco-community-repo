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

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceBootstrap;
import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
import org.alfresco.module.org_alfresco_module_rm.patch.v30.RMv30ClearancesForSpecialUsers;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;

/**
 * Provide the highest clearance to the admin and system users. This needs to be run once (either bootstrapped into a
 * fresh system, or as part of an upgrade in {@link RMv30ClearancesForSpecialUsers}) per installation.
 *
 * @author tpage
 */
public class ClearancesForSpecialUsersBootstrapComponent implements ClassifiedContentModel
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
     * Give the admin and system users the maximum clearance.
     */
    public void createClearancesForSpecialUsers()
    {
        // Ensure the classification levels are loaded before this patch runs. (Nb. This will result in the
        // classification service bootstrap method being called twice on the start-up that includes this call).
        classificationServiceBootstrap.onBootstrap(null);

        Serializable mostSecureLevel = classificationServiceBootstrap.getClassificationLevelManager()
                    .getMostSecureLevel().getId();
        String systemUserName = authenticationUtil.getSystemUserName();
        NodeRef system = personService.getPerson(systemUserName);
        nodeService.setProperty(system, PROP_CLEARANCE_LEVEL, mostSecureLevel);
        String adminUserName = authenticationUtil.getAdminUserName();
        NodeRef admin = personService.getPerson(adminUserName);
        nodeService.setProperty(admin, PROP_CLEARANCE_LEVEL, mostSecureLevel);
    }
}
