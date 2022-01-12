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

package org.alfresco.module.org_alfresco_module_rm.patch.v24;

import static org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock.generateNodeRef;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * RM V2.4 File Plan container rule inheritance patch unit test.
 * 
 * @author Roy Wetherall
 * @since 2.4
 */
public class RMv24FilePlanContainerRuleInheritancePatchUnitTest
{
    private @Mock NodeService mockedNodeService;
    private @Mock FilePlanService mockedFilePlanService;
  
    private @InjectMocks RMv24FilePlanContainerRuleInheritancePatch patch;
    
    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
    }
    
    /**
     * Given there are not file plans,
     * When the patch is executed,
     * Then nothing happens
     */
    @SuppressWarnings("unchecked")
    @Test
    public void noFilePlans()
    {
        // given
        when(mockedFilePlanService.getFilePlans())
            .thenReturn(Collections.EMPTY_SET);
        
        // when
        patch.applyInternal();
        
        // then
        verifyZeroInteractions(mockedNodeService);
    }
    
    /**
     * Given there is a file plan,
     * When the patch is executed,
     * Then the file plan containers are updated
     */
    @Test
    public void atLeastOneFilePlan()
    {
        NodeRef filePlan = generateNodeRef(mockedNodeService, RecordsManagementModel.TYPE_FILE_PLAN);
        NodeRef holdsContainer = generateNodeRef(mockedNodeService);
        NodeRef transferContainer = generateNodeRef(mockedNodeService);
        NodeRef unfiledRecordsContainer = generateNodeRef(mockedNodeService);
        
        // given        
        when(mockedFilePlanService.getFilePlans())
            .thenReturn(Collections.singleton(filePlan));
        when(mockedFilePlanService.getHoldContainer(filePlan))
            .thenReturn(holdsContainer);
        when(mockedFilePlanService.getTransferContainer(filePlan))
            .thenReturn(transferContainer);
        when(mockedFilePlanService.getUnfiledContainer(filePlan))
            .thenReturn(unfiledRecordsContainer);
        
        // when
        patch.applyInternal();
        
        // then
        verify(mockedNodeService).addAspect(holdsContainer, RuleModel.ASPECT_IGNORE_INHERITED_RULES, null);
        verify(mockedNodeService).addAspect(transferContainer, RuleModel.ASPECT_IGNORE_INHERITED_RULES, null);
        verify(mockedNodeService).addAspect(unfiledRecordsContainer, RuleModel.ASPECT_IGNORE_INHERITED_RULES, null);        
    }
}
