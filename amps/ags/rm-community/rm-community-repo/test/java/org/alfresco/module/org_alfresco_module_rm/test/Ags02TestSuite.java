/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.module.org_alfresco_module_rm.test;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite that runs a subset of AGS tests (from {@link AllTestSuite}) so that the AGS build jobs are shorter and
 * more in keeping with the repo jobs, making the build shorter over all.
 *
 * @author Alan Davis
 * @since 11
 */
@RunWith(Categories.class)
@Suite.SuiteClasses({
        org.alfresco.module.org_alfresco_module_rm.test.legacy.capabilities.CompositeCapabilityTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.capabilities.DeclarativeCapabilityTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.security.MethodSecurityTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.jscript.JSONConversionComponentTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.action.RecordableVersionConfigActionTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.action.RejectActionTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.action.FileReportActionTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.action.HideRecordActionTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.action.CreateRecordActionTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.action.DeclareVersionAsRecordActionTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.action.FileToActionTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.action.MoveRecordActionTest.class,
//        org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript.EmailMapScriptTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript.CapabilitiesRestApiTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript.SubstitutionSuggestionsRestApiTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript.RMConstraintScriptTest.class,
//        org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript.RmRestApiTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript.EventRestApiTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript.EmailMapKeysRestApiTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript.RmAuthoritiesRestApiTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript.DispositionRestApiTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript.RmPropertiesRestApiTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript.ActionDefinitionsRestApiTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript.DataSetRestApiTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript.RoleRestApiTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript.AuditRestApiTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript.RMCaveatConfigScriptTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript.RmClassesRestApiTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.RecordsManagementQueryDAOImplTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.ExtendedActionServiceTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.ReportServiceImplTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.FilePlanServiceImplTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.DataSetServiceImplTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.FilePlanPermissionServiceImplTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.ModelSecurityServiceImplTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.RecordsManagementAuditServiceImplTest.class,
//        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.RecordsManagementEventServiceImplTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.RecordsManagementAdminServiceImplTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.ServiceBaseImplTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.CustomEMailMappingServiceImplTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.FilePlanRoleServiceImplTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.FreezeServiceImplTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.RecordsManagementSearchServiceImplTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.VitalRecordServiceImplTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.RecordsManagementActionServiceImplTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.CapabilityServiceImplTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.RecordsManagementServiceImplTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.RecordServiceImplTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.ExtendedSecurityServiceImplTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.legacy.service.DispositionServiceImplTest.class
})
public class Ags02TestSuite {
}
