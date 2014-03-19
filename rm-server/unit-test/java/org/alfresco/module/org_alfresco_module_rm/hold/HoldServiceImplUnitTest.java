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
package org.alfresco.module.org_alfresco_module_rm.hold;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.BaseUnitTest;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


/**
 * Hold service implementation unit test
 * 
 * @author Roy Wetherall
 */
public class HoldServiceImplUnitTest extends BaseUnitTest
{
    /** test values */
    private static final String HOLD_NAME = "holdname";
    private static final String HOLD_REASON = "holdreason";
    private static final String HOLD_DESCRIPTION = "holddescription";
    
    protected NodeRef holdContainer;
    protected NodeRef hold;
    protected NodeRef hold2;
    protected NodeRef notHold;
    
    @Spy @InjectMocks HoldServiceImpl holdService;
    
    @Before
    @Override
    public void before()
    {
        super.before();
        
        holdContainer = generateNodeRef(TYPE_HOLD_CONTAINER);
        hold = generateNodeRef(TYPE_HOLD);
        hold2 = generateNodeRef(TYPE_HOLD);
        notHold = generateNodeRef(TYPE_RECORD_CATEGORY);
        
        when(mockedFilePlanService.getHoldContainer(filePlan)).thenReturn(holdContainer);   
        
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void beforeDeleteNode()
    {
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Exception
            {
                Object[] args = invocation.getArguments();
                ((RunAsWork)args[0]).doWork();
                return null;
            }
        }).when(holdService).runAsSystem(any(RunAsWork.class));
    }
    
    @Test
    public void isHold()
    {
        assertTrue(holdService.isHold(hold));
        assertFalse(holdService.isHold(notHold));
    }
    
    @Test
    public void getHolds()
    {
        // with no holds 
        List<NodeRef> emptyHoldList = holdService.getHolds(filePlan);
        verify(mockedNodeService).getChildAssocs(holdContainer, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);        
        assertNotNull(emptyHoldList);
        assertTrue(emptyHoldList.isEmpty());

        // set up list of two holds
        List<ChildAssociationRef> list = new ArrayList<ChildAssociationRef>(2);        
        list.add(new ChildAssociationRef(generateQName(), holdContainer, generateQName(), hold));
        list.add(new ChildAssociationRef(generateQName(), holdContainer, generateQName(), hold2));        
        when(mockedNodeService.getChildAssocs(holdContainer, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL)).thenReturn(list);
        
        // with 2 holds
        List<NodeRef> holdsList = holdService.getHolds(filePlan);
        verify(mockedNodeService, times(2)).getChildAssocs(holdContainer, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        assertNotNull(holdsList);
        assertEquals(2, holdsList.size());
        
        // check one of the holds
        NodeRef holdFromList = holdsList.get(0);
        assertEquals(TYPE_HOLD, mockedNodeService.getType(holdFromList));
    
    }
    
    @Test
    public void heldBy()
    {
        // TODO
    }

    @Test (expected=AlfrescoRuntimeException.class)
    public void getHold()
    {
        // setup node service interactions
        when(mockedNodeService.getChildByName(eq(holdContainer), eq(ContentModel.ASSOC_CONTAINS), anyString())).thenReturn(null)
                                                                                                               .thenReturn(hold)
                                                                                                               .thenReturn(notHold);
        
        // no hold
        NodeRef noHold = holdService.getHold(filePlan, "notAHold");
        assertNull(noHold);
        
        // found hold
        NodeRef someHold = holdService.getHold(filePlan, "someHold");
        assertNotNull(someHold);
        assertEquals(TYPE_HOLD, mockedNodeService.getType(someHold));
        
        // ensure runtime exception is thrown
        holdService.getHold(filePlan, "notHold");
    }
    
    @Test
    public void getHeld()
    {
        
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void createHold()
    {
        // setup node service interactions
        when(mockedNodeService.createNode(eq(holdContainer), eq(ContentModel.ASSOC_CONTAINS), any(QName.class) , eq(TYPE_HOLD), any(Map.class)))
            .thenReturn(new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, holdContainer, generateQName(), hold));
        
        // create hold
        NodeRef newHold = holdService.createHold(filePlan, HOLD_NAME, HOLD_REASON, HOLD_DESCRIPTION);        
        assertNotNull(newHold);
        assertEquals(TYPE_HOLD, mockedNodeService.getType(newHold));
        assertEquals(hold, newHold);
        
        // check the node service interactions
        ArgumentCaptor<Map> propertyMapCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<QName> assocNameCaptor = ArgumentCaptor.forClass(QName.class);
        verify(mockedNodeService).createNode(eq(holdContainer), eq(ContentModel.ASSOC_CONTAINS), assocNameCaptor.capture() , eq(TYPE_HOLD), propertyMapCaptor.capture());
        
        // check property map
        Map<QName, Serializable> propertyMap = (Map<QName, Serializable>)propertyMapCaptor.getValue();
        assertNotNull(propertyMap);
        assertEquals(3, propertyMap.size());
        assertTrue(propertyMap.containsKey(ContentModel.PROP_NAME));
        assertEquals(HOLD_NAME, propertyMap.get(ContentModel.PROP_NAME));
        assertTrue(propertyMap.containsKey(ContentModel.PROP_DESCRIPTION));
        assertEquals(HOLD_DESCRIPTION, propertyMap.get(ContentModel.PROP_DESCRIPTION));
        assertTrue(propertyMap.containsKey(PROP_HOLD_REASON));
        assertEquals(HOLD_REASON, propertyMap.get(PROP_HOLD_REASON));
        
        // check assoc name
        assertNotNull(assocNameCaptor.getValue());
        assertEquals(NamespaceService.CONTENT_MODEL_1_0_URI, assocNameCaptor.getValue().getNamespaceURI());
        assertEquals(HOLD_NAME, assocNameCaptor.getValue().getLocalName());        
    }
    
    @Test
    public void getHoldReason()
    {
        // setup node service interactions
        when(mockedNodeService.exists(hold))
            .thenReturn(false)
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(true);
        when(mockedNodeService.getProperty(eq(hold), eq(PROP_HOLD_REASON)))
            .thenReturn(null)
            .thenReturn(HOLD_REASON);
        
        // node does not exist
        assertNull(holdService.getHoldReason(hold));
        
        // node isn't a hold
        assertNull(holdService.getHoldReason(notHold));
        
        // hold reason isn't set
        assertNull(holdService.getHoldReason(hold));
        
        // hold reason set
        assertEquals(HOLD_REASON, holdService.getHoldReason(hold));
    }
    
    @Test
    public void setHoldReason()
    {
        // setup node service interactions
        when(mockedNodeService.exists(hold))
            .thenReturn(false)
            .thenReturn(true)
            .thenReturn(true);
        
        // node does not exist
        holdService.setHoldReason(hold, HOLD_REASON);
        verify(mockedNodeService, never()).setProperty(hold, PROP_HOLD_REASON, HOLD_REASON);     
        
        // node isn't a hold
        holdService.setHoldReason(notHold, HOLD_REASON);
        verify(mockedNodeService, never()).setProperty(hold, PROP_HOLD_REASON, HOLD_REASON);  
        
        // set hold reason
        holdService.setHoldReason(hold, HOLD_REASON);
        verify(mockedNodeService).setProperty(hold, PROP_HOLD_REASON, HOLD_REASON);        
    }
    
    @Test
    public void deleteHold()
    {
        // setup node service interactions
        when(mockedNodeService.exists(hold))
            .thenReturn(false)
            .thenReturn(true)
            .thenReturn(true);
        
        // node does not exist
        holdService.deleteHold(hold);
        verify(mockedNodeService, never()).deleteNode(hold);     
        
        // node isn't a hold
        holdService.deleteHold(notHold);
        verify(mockedNodeService, never()).deleteNode(hold);  
        
        // delete hold
        holdService.deleteHold(hold);
        verify(mockedNodeService).deleteNode(hold);   
        
        // TODO check interactions with policy component!!!
    }
    
    @Test
    public void addToHold()
    {
        // TODO
    }
    
    @Test
    public void addToHolds()
    {
        // TODO
    }
    
    @Test
    public void removeFromHold()
    {
        // TODO
    }
    
    @Test
    public void removeFromHolds()
    {
        // TODO
    }
    
    @Test
    public void removeFromAllHolds()
    {
        
    }
}
