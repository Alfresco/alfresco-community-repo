/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test;

import org.alfresco.module.org_alfresco_module_rm.test.service.CapabilityServiceImplTest;
import org.alfresco.module.org_alfresco_module_rm.test.service.DataSetServiceImplTest;
import org.alfresco.module.org_alfresco_module_rm.test.service.DispositionServiceImplTest;
import org.alfresco.module.org_alfresco_module_rm.test.service.ExtendedActionServiceTest;
import org.alfresco.module.org_alfresco_module_rm.test.service.ExtendedSecurityServiceImplTest;
import org.alfresco.module.org_alfresco_module_rm.test.service.FilePlanPermissionServiceImplTest;
import org.alfresco.module.org_alfresco_module_rm.test.service.FilePlanRoleServiceImplTest;
import org.alfresco.module.org_alfresco_module_rm.test.service.FilePlanServiceImplTest;
import org.alfresco.module.org_alfresco_module_rm.test.service.FreezeServiceImplTest;
import org.alfresco.module.org_alfresco_module_rm.test.service.ModelSecurityServiceImplTest;
import org.alfresco.module.org_alfresco_module_rm.test.service.RecordServiceImplTest;
import org.alfresco.module.org_alfresco_module_rm.test.service.RecordsManagementActionServiceImplTest;
import org.alfresco.module.org_alfresco_module_rm.test.service.RecordsManagementAdminServiceImplTest;
import org.alfresco.module.org_alfresco_module_rm.test.service.RecordsManagementAuditServiceImplTest;
import org.alfresco.module.org_alfresco_module_rm.test.service.RecordsManagementQueryDAOImplTest;
import org.alfresco.module.org_alfresco_module_rm.test.service.RecordsManagementSearchServiceImplTest;
import org.alfresco.module.org_alfresco_module_rm.test.service.ReportServiceImplTest;
import org.alfresco.module.org_alfresco_module_rm.test.service.VitalRecordServiceImplTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


/**
 * RM test suite
 *
 * @author Roy Wetherall
 */
@RunWith(Suite.class)
@SuiteClasses(
{
    ExtendedSecurityServiceImplTest.class,
    ModelSecurityServiceImplTest.class,
    RecordsManagementActionServiceImplTest.class,
    ExtendedActionServiceTest.class,
    DispositionServiceImplTest.class,
    RecordsManagementActionServiceImplTest.class,
    RecordsManagementAdminServiceImplTest.class,
    RecordsManagementAuditServiceImplTest.class,
    //RecordsManagementEventServiceImplTest.class,
    RecordsManagementSearchServiceImplTest.class,
    VitalRecordServiceImplTest.class,
    DataSetServiceImplTest.class,
    FreezeServiceImplTest.class,
    RecordServiceImplTest.class,
    CapabilityServiceImplTest.class,
    FilePlanRoleServiceImplTest.class,
    FilePlanServiceImplTest.class,
    FilePlanPermissionServiceImplTest.class,
    ReportServiceImplTest.class,
    RecordsManagementQueryDAOImplTest.class
})
public class ServicesTestSuite 
{
}
