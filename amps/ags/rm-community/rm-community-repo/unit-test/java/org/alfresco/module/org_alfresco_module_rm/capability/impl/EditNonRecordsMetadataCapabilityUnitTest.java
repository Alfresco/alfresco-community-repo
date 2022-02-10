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

package org.alfresco.module.org_alfresco_module_rm.capability.impl;

import static org.mockito.Mockito.when;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.record.RecordServiceImpl;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

/**
 * Edit non records metadata capability unit test
 * 
 * @author Roy Wetherall
 * @since 2.3
 */
public class EditNonRecordsMetadataCapabilityUnitTest extends BaseUnitTest
{
    /** mocked set */
    @Mock private Set<Object> mockedSet;
    
    /** test capability */
    @InjectMocks private EditNonRecordMetadataCapability capability;
    
    /**
     * Given that the evaluated node is held in the transaction cache as new
     * When evaluated
     * Then access is granted
     */
    @Test
    public void newRecord()
    {
        NodeRef nodeRef = generateNodeRef();
        when(mockedTransactionalResourceHelper.getSet(RecordServiceImpl.KEY_NEW_RECORDS))
            .thenReturn(mockedSet);        
        when(mockedSet.contains(nodeRef))
            .thenReturn(true);
        
        Assert.assertEquals(AccessDecisionVoter.ACCESS_GRANTED, capability.evaluate(nodeRef));
    }   
}
