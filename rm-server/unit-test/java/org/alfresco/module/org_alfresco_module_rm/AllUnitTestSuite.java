/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm;

import org.alfresco.module.org_alfresco_module_rm.forms.RecordsManagementTypeFormFilterUnitTest;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldServiceImplUnitTest;
import org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator.TransferEvaluatorUnitTest;
import org.alfresco.module.org_alfresco_module_rm.record.RecordMetadataBootstrapUnitTest;
import org.alfresco.module.org_alfresco_module_rm.record.RecordServiceImplUnitTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * All unit test suite.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@RunWith(Suite.class)
@SuiteClasses(
{
    RecordMetadataBootstrapUnitTest.class,
    RecordServiceImplUnitTest.class,
    RecordsManagementTypeFormFilterUnitTest.class,
    HoldServiceImplUnitTest.class,
    TransferEvaluatorUnitTest.class
})
public class AllUnitTestSuite
{
}
