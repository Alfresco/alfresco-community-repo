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

package org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator;

import static org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock.generateQName;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

/**
 * Transfer evaluator unit test.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public class TransferEvaluatorUnitTest extends BaseUnitTest
{
    private NodeRef transfer;
    
    @Spy @InjectMocks TransferEvaluator transferEvaluator;
    
    @Override
    public void before() throws Exception
    {
        super.before();
        
        // setup node references
        transfer = generateNodeRef(TYPE_TRANSFER);
    }
    
    private List<ChildAssociationRef> getParentAssocs(NodeRef provided)
    {
        List<ChildAssociationRef> result = new ArrayList<>(1);
        result.add(new ChildAssociationRef(ASSOC_TRANSFERRED, transfer, generateQName(), provided, false, 1));
        return result;
    }
    
    @Test
    public void isNotTransferringRecord()
    {
        // setup interactions
        doReturn(Collections.emptyList()).when(mockedNodeService).getParentAssocs(record, RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        
        // evaluate
        assertFalse(transferEvaluator.evaluate(record));
        
        // verify interactions
        verify(mockedNodeService, times(1)).getParentAssocs(record, RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        verify(mockedNodeService, never()).getProperty(transfer, RecordsManagementModel.PROP_TRANSFER_ACCESSION_INDICATOR);
        verify(mockedRecordFolderService, times(1)).getRecordFolders(record);
        
    }
    
    @Test
    public void isTransferringWhenExpectingAccending()
    {
        // setup interactions
        doReturn(Boolean.FALSE).when(mockedNodeService).getProperty(transfer, RecordsManagementModel.PROP_TRANSFER_ACCESSION_INDICATOR);
        doReturn(getParentAssocs(record)).when(mockedNodeService).getParentAssocs(record, RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        transferEvaluator.setTransferAccessionIndicator(true);
        
        // evaluate
        assertFalse(transferEvaluator.evaluate(record));
        
        // verify interactions
        verify(mockedNodeService, times(1)).getParentAssocs(record, RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        verify(mockedNodeService, times(1)).getProperty(transfer, RecordsManagementModel.PROP_TRANSFER_ACCESSION_INDICATOR);
        verify(mockedRecordFolderService, never()).getRecordFolders(record);
    }
    
    @Test
    public void transferringRecord()
    {
        // setup interactions
        doReturn(Boolean.FALSE).when(mockedNodeService).getProperty(transfer, RecordsManagementModel.PROP_TRANSFER_ACCESSION_INDICATOR);
        doReturn(getParentAssocs(record)).when(mockedNodeService).getParentAssocs(record, RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        
        // evaluate
        assertTrue(transferEvaluator.evaluate(record));
        
        // verify interactions
        verify(mockedNodeService, times(1)).getParentAssocs(record, RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        verify(mockedNodeService, times(1)).getProperty(transfer, RecordsManagementModel.PROP_TRANSFER_ACCESSION_INDICATOR);
        verify(mockedRecordFolderService, never()).getRecordFolders(record);
        
    }
    
    @Test
    public void transferringRecordFolder()
    {
        // setup interactions
        doReturn(Boolean.FALSE).when(mockedNodeService).getProperty(transfer, RecordsManagementModel.PROP_TRANSFER_ACCESSION_INDICATOR);
        doReturn(getParentAssocs(recordFolder)).when(mockedNodeService).getParentAssocs(recordFolder, RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        
        // evaluate
        assertTrue(transferEvaluator.evaluate(recordFolder));
        
        // verify interactions
        verify(mockedNodeService, times(1)).getParentAssocs(recordFolder, RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        verify(mockedNodeService, times(1)).getProperty(transfer, RecordsManagementModel.PROP_TRANSFER_ACCESSION_INDICATOR);
        verify(mockedRecordFolderService, never()).getRecordFolders(record);
    }
    
    @Test
    public void transferringRecordWithinRecordFolder()
    {
        // setup interactions
        doReturn(Boolean.FALSE).when(mockedNodeService).getProperty(transfer, RecordsManagementModel.PROP_TRANSFER_ACCESSION_INDICATOR);
        doReturn(Collections.emptyList()).when(mockedNodeService).getParentAssocs(record, RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        doReturn(getParentAssocs(recordFolder)).when(mockedNodeService).getParentAssocs(recordFolder, RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        
        // evaluate
        assertTrue(transferEvaluator.evaluate(record));
        
        // verify interactions
        verify(mockedNodeService, times(1)).getParentAssocs(record, RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        verify(mockedNodeService, times(1)).getProperty(transfer, RecordsManagementModel.PROP_TRANSFER_ACCESSION_INDICATOR);
        verify(mockedRecordFolderService, times(1)).getRecordFolders(record);
        
    }
    
    @Test
    public void accendingRecord()
    {
        // setup interactions
        doReturn(Boolean.TRUE).when(mockedNodeService).getProperty(transfer, RecordsManagementModel.PROP_TRANSFER_ACCESSION_INDICATOR);
        doReturn(getParentAssocs(record)).when(mockedNodeService).getParentAssocs(record, RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        transferEvaluator.setTransferAccessionIndicator(true);
        
        // evaluate
        assertTrue(transferEvaluator.evaluate(record));
        
        // verify interactions
        verify(mockedNodeService, times(1)).getParentAssocs(record, RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        verify(mockedNodeService, times(1)).getProperty(transfer, RecordsManagementModel.PROP_TRANSFER_ACCESSION_INDICATOR);
        verify(mockedRecordFolderService, never()).getRecordFolders(record);
        
    }

}
