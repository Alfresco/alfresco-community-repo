/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.bootstrap;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.alfresco.module.org_alfresco_module_rm.patch.ModulePatchExecuter;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Bootstrap importer module component unit test
 *
 * @author Roy Wetherall
 * @since 2.3
 */
public class BootstrapImporterModuleComponentUnitTest extends BaseUnitTest
{
    /** RM config node */
    private static final NodeRef configNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "rm_config_folder");

    /** mocks */
    @Mock(name="importer")                                  private ImporterBootstrap                         mockedImporter;
    @Mock(name="modulePatchExecuter")                       private ModulePatchExecuter                       mockedModulePatchExecuter;
    @Mock(name="recordContributorsGroupBootstrapComponent") private RecordContributorsGroupBootstrapComponent mockedRecordContributorsGroupBootstrapComponent;

    /** importer */
    @InjectMocks
    private BootstrapImporterModuleComponent importer;

    /**
     * Given that the system has already been bootstraped
     * When I try and boostrap the system
     * Then the system is not bootstraped again
     */
    @Test
    public void alreadyBootstraped() throws Throwable
    {
        // config node exists
        doReturn(true).when(mockedNodeService).exists(configNodeRef);

        // boostrap
        importer.executeInternal();

        // not bootstraped
        verify(mockedImporter, never()).bootstrap();
        verify(mockedModulePatchExecuter, never()).initSchemaVersion();
        verify(mockedRecordContributorsGroupBootstrapComponent, never()).createRecordContributorsGroup();
    }

    /**
     * Given that the system has not been bootstraped
     * When I try and bootstrap the system
     * Then the system is bootstraped
     */
    @Test
    public void boostrap() throws Throwable
    {
        // config node does not exist
        doReturn(false).when(mockedNodeService).exists(configNodeRef);

        // boostrap
        importer.executeInternal();

        // not bootstraped
        verify(mockedImporter, times(1)).bootstrap();
        verify(mockedModulePatchExecuter, times(1)).initSchemaVersion();
        verify(mockedRecordContributorsGroupBootstrapComponent, times(1)).createRecordContributorsGroup();
    }
}
