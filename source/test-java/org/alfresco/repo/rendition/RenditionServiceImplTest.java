/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.rendition;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.repo.action.ActionDefinitionImpl;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.rendition.RenderingEngineDefinition;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 */
public class RenditionServiceImplTest extends TestCase
{
    private final static String ENGINE_NAME = "Engine Name";

    private ServiceRegistry serviceRegistry = new MockedTestServiceRegistry();
    private ActionService actionService = mock(ActionService.class);
    
    private final RenditionDefinitionPersisterImpl renditionDefinitionPersister = mock(RenditionDefinitionPersisterImpl.class);
    private RenditionServiceImpl renditionService;
    
    private final QName ACTION_NAME = QName.createQName(NamespaceService.ALFRESCO_URI, "testName");

    @Override
    protected void setUp() throws Exception
    {
        renditionService = new RenditionServiceImpl();
        renditionService.setServiceRegistry(serviceRegistry);
        renditionService.setActionService(actionService);
        renditionService.setRenditionDefinitionPersister(renditionDefinitionPersister);
    }

    public void testGetRenderingEngineDefinition() throws Exception
    {
        // Check returns null when unknown name specified.
        assertNull(renditionService.getRenderingEngineDefinition(""));

        // Check returns null if action service returns an ActionDefinition
        // which does not implement RenderingActionDefinition.
        ActionDefinition actionDefinition = new ActionDefinitionImpl(ENGINE_NAME);
        when(actionService.getActionDefinition(ENGINE_NAME)).thenReturn(actionDefinition);
        assertNull(renditionService.getRenderingEngineDefinition(ENGINE_NAME));

        // Check returns the definition if the action service returns an
        // ActionDefinition
        // which does implement RenderingActionDefinition.
        ActionDefinition renderingDefinition = new RenderingEngineDefinitionImpl(ENGINE_NAME);
        when(actionService.getActionDefinition(ENGINE_NAME)).thenReturn(renderingDefinition);
        assertSame(renderingDefinition, renditionService.getRenderingEngineDefinition(ENGINE_NAME));
    }

    public void testGetRenderingEngineDefinitions() throws Exception
    {
        LinkedList<ActionDefinition> actionDefs = new LinkedList<ActionDefinition>();
        when(actionService.getActionDefinitions()).thenReturn(actionDefs);

        // Check case where no action definitions returned.
        List<RenderingEngineDefinition> engineDefs = renditionService.getRenderingEngineDefinitions();
        assertTrue("The list of rendering action definitions should be empty!", engineDefs.isEmpty());

        // Check that when the action service returns a rendering engine
        // definition then the rendering service includes this in the list of
        // returned values.
        ActionDefinition renderingDefinition = new RenderingEngineDefinitionImpl(ENGINE_NAME);
        actionDefs.add(renderingDefinition);
        engineDefs = renditionService.getRenderingEngineDefinitions();
        assertEquals(1, engineDefs.size());
        assertSame(renderingDefinition, engineDefs.get(0));
        
        // Check that when the action service returns a non-rendering action
        // definition then the rendering service does not include it.
        ActionDefinition actionDefinition = new ActionDefinitionImpl(ENGINE_NAME);
        actionDefs.add(actionDefinition);
        engineDefs = renditionService.getRenderingEngineDefinitions();
        assertEquals(1, engineDefs.size());
        assertSame(renderingDefinition, engineDefs.get(0));
    }

    public void testCreateRenditionDefinition() throws Exception
    {
        RenditionDefinition renderingAction = renditionService.createRenditionDefinition(ACTION_NAME, ENGINE_NAME);
        assertNotNull(renderingAction);
        assertEquals(ENGINE_NAME, renderingAction.getActionDefinitionName());
        assertEquals(ACTION_NAME, renderingAction.getRenditionName());
        String id = renderingAction.getId();
        assertNotNull(id);
        assertTrue(id.length() > 0);
    }

}