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
package org.alfresco.module.org_alfresco_module_rm.test;

import org.alfresco.module.org_alfresco_module_rm.action.impl.FileReportActionUnitTest;
import org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition.HoldCapabilityConditionUnitTest;
import org.alfresco.module.org_alfresco_module_rm.forms.RecordsManagementTypeFormFilterUnitTest;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldServiceImplUnitTest;
import org.alfresco.module.org_alfresco_module_rm.job.DispositionLifecycleJobExecuterUnitTest;
import org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator.FrozenEvaluatorUnitTest;
import org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator.TransferEvaluatorUnitTest;
import org.alfresco.module.org_alfresco_module_rm.model.compatibility.DictionaryBootstrapPostProcessorUnitTest;
import org.alfresco.module.org_alfresco_module_rm.patch.v22.RMv22RemoveInPlaceRolesFromAllPatchUnitTest;
import org.alfresco.module.org_alfresco_module_rm.record.RecordMetadataBootstrapUnitTest;
import org.alfresco.module.org_alfresco_module_rm.record.RecordServiceImplUnitTest;
import org.alfresco.module.org_alfresco_module_rm.script.hold.HoldPostUnitTest;
import org.alfresco.module.org_alfresco_module_rm.script.hold.HoldPutUnitTest;
import org.alfresco.module.org_alfresco_module_rm.script.hold.HoldsGetUnitTest;
import org.alfresco.module.org_alfresco_module_rm.util.BeanExtenderUnitTest;
import org.alfresco.repo.action.parameter.DateParameterProcessorUnitTest;
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
    RecordsManagementTypeFormFilterUnitTest.class,
    DispositionLifecycleJobExecuterUnitTest.class,
    DictionaryBootstrapPostProcessorUnitTest.class,
    BeanExtenderUnitTest.class,
    DateParameterProcessorUnitTest.class,
    
    // services
    RecordServiceImplUnitTest.class,
    HoldServiceImplUnitTest.class,
    //FilePlanPermissionServiceImplUnitTest.class,
    
    // evaluators
    TransferEvaluatorUnitTest.class,
    FrozenEvaluatorUnitTest.class,
    
    // web scripts
    HoldsGetUnitTest.class,
    HoldPostUnitTest.class,
    HoldPutUnitTest.class,
    
    // capability conditions
    HoldCapabilityConditionUnitTest.class,
    
    // action implementations
    FileReportActionUnitTest.class,
    
    // patches
    RMv22RemoveInPlaceRolesFromAllPatchUnitTest.class
})
public class AllUnitTestSuite
{
}
