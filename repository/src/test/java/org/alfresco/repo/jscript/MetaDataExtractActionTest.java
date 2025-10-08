/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
/*
 * Copyright (C) 2005 Jesper Steen Møller
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

package org.alfresco.repo.jscript;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.Mockito;

import org.alfresco.repo.forms.FormData;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;

public class MetaDataExtractActionTest
{

    @Test
    public void testIsContentChangedReturnsTrue()
    {
        MetaDataExtractAction action = new MetaDataExtractAction();
        ContentService contentService = Mockito.mock(ContentService.class);
        ContentReader reader = Mockito.mock(ContentReader.class);
        FormData formData = Mockito.mock(FormData.class);
        FormData.FieldData fieldData = Mockito.mock(FormData.FieldData.class);

        String nodeRefStr = "workspace://SpacesStore/abc/def";
        Mockito.when(contentService.getReader(Mockito.any(), Mockito.any())).thenReturn(reader);
        Mockito.when(reader.getContentString()).thenReturn("oldContent");
        Mockito.when(formData.getFieldData("prop_cm_content")).thenReturn(fieldData);
        Mockito.when(fieldData.getValue()).thenReturn("newContent");

        action.setContentService(contentService);

        boolean result = action.isContentChanged(nodeRefStr, formData);
        assertTrue(result);
    }

    @Test
    public void testIsContentChangedReturnsFalse()
    {
        MetaDataExtractAction action = new MetaDataExtractAction();
        ContentService contentService = Mockito.mock(ContentService.class);
        ContentReader reader = Mockito.mock(ContentReader.class);
        FormData formData = Mockito.mock(FormData.class);
        FormData.FieldData fieldData = Mockito.mock(FormData.FieldData.class);

        String nodeRefStr = "workspace://SpacesStore/abc/def";
        Mockito.when(contentService.getReader(Mockito.any(), Mockito.any())).thenReturn(reader);
        Mockito.when(reader.getContentString()).thenReturn("sameContent");
        Mockito.when(formData.getFieldData("prop_cm_content")).thenReturn(fieldData);
        Mockito.when(fieldData.getValue()).thenReturn("sameContent");

        action.setContentService(contentService);

        boolean result = action.isContentChanged(nodeRefStr, formData);
        assertFalse(result);
    }

    @Test
    public void testCreate_WhenContentChanged_ReturnsScriptAction()
    {
        MetaDataExtractAction action = new MetaDataExtractAction();

        ServiceRegistry serviceRegistry = Mockito.mock(ServiceRegistry.class);
        ActionService actionService = Mockito.mock(ActionService.class);
        ActionDefinition actionDefinition = Mockito.mock(ActionDefinition.class);
        Action alfrescoAction = Mockito.mock(Action.class);
        ActionCondition actionCondition = Mockito.mock(ActionCondition.class);

        Mockito.when(serviceRegistry.getActionService()).thenReturn(actionService);
        Mockito.when(actionService.getActionDefinition(Mockito.anyString())).thenReturn(actionDefinition);
        Mockito.when(actionService.createAction(Mockito.anyString())).thenReturn(alfrescoAction);
        Mockito.when(actionService.createActionCondition(Mockito.anyString())).thenReturn(actionCondition);

        action.setServiceRegistry(serviceRegistry);

        ScriptAction result = action.create(true);

        assertNotNull("ScriptAction should not be null when content has changed", result);
    }
}
