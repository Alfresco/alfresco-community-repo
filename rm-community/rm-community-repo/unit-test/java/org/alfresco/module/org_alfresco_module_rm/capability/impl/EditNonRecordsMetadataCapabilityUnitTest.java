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
