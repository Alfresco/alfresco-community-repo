/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.dictionary;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.StaticMessageLookup;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

/**
 * DictionaryNamespaceComponent and DictionaryComponent mostly delegate to other methods. This test is
 * really about exercising all of the public endpoints.
 *
 * @author Gethin James
 */
public class DictionaryComponentTest extends AbstractModelTest
{
    DictionaryNamespaceComponent ndc;

    protected void setUp() throws Exception
    {
        super.setUp();
        NamespaceDAO nao = mock(NamespaceDAO.class);
        when(nao.getPrefixes(anyString())).thenReturn(Arrays.asList("pref1"));
        when(nao.getPrefixes()).thenReturn(Arrays.asList("pref1", "pref2"));
        when(nao.getURIs()).thenReturn(Arrays.asList("uri1", "uri2"));
        when(nao.getNamespaceURI(anyString())).thenReturn("uri1");
        ndc = new DictionaryNamespaceComponent();
        ndc.setNamespaceDAO(nao);
    }

    public void testGetter() throws Exception
    {
        assertNotNull(ndc.getURIs());
        assertNotNull(ndc.getPrefixes());
        assertNotNull(ndc.getPrefixes("something"));
        assertNotNull(ndc.getNamespaceURI("something uri"));
    }

    public void testUnsupportedOperations() throws Exception
    {
        try
        {
            ndc.registerNamespace(null, null);
            fail("Should not be here");
        }
        catch (UnsupportedOperationException expected)
        {
            //
        }

        try
        {
            ndc.unregisterNamespace(null);
            fail("Should not be here");
        }
        catch (UnsupportedOperationException expected)
        {
            //
        }

    }

    public void testDCGetters() throws Exception
    {
        DictionaryComponent dc = new DictionaryComponent();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(AbstractModelTest.MODEL6_UPDATE1_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);

        dc.setDictionaryDAO(dictionaryDAO);

        assertNotNull(dc.getAllModels());
        assertNotNull(dc.getAllDataTypes());
        assertNotNull(dc.getAllTypes());
        assertNotNull(dc.getAllAspects());
        assertNotNull(dc.getAllAssociations());

        assertNotNull(dc.getModel(modelName));
        assertNotNull(dc.getDataTypes(modelName));
        assertNotNull(dc.getTypes(modelName));
        assertNotNull(dc.getAspects(modelName));
        assertNotNull(dc.getAssociations(modelName));
        assertNotNull(dc.getConstraints(modelName));
        assertNotNull(dc.getProperties(modelName));
        assertNotNull(dc.getProperties(modelName, ContentModel.PROP_NAME));
        assertNotNull(dc.getAllProperties(ContentModel.PROP_NAME));

        assertNotNull(dc.getSubTypes(ContentModel.PROP_NAME, false));
        assertNotNull(dc.getSubAspects(ContentModel.ASPECT_HIDDEN, false));
        assertNotNull(dc.getConstraints(modelName, false));

        QName[] types = (QName[]) dc.getTypes(modelName).toArray(new QName[2]);
        QName text = QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "text");
        QName testAspect = QName.createQName(modelName.getNamespaceURI(), "aspect1");
        assertTrue(types.length>0);
        assertNotNull(dc.getAnonymousType(types[0], Arrays.asList(testAspect)));
        assertNotNull(dc.getAnonymousType(types[0]));
        assertNotNull(dc.getPropertyDefs(testAspect));

        DataTypeDefinition dataTypeDefinition = dc.getDataType(text);
        assertEquals(dataTypeDefinition, dc.getDataType(Class.forName(dataTypeDefinition.getJavaClassName())));
    }

    public void testMessageLookup() throws Exception
    {
        DictionaryComponent dc = new DictionaryComponent();
        dc.setDictionaryDAO(dictionaryDAO);
        dc.setMessageLookup(new StaticMessageLookup());

        assertNull(dc.getMessage("fred"));
        assertNull(dc.getMessage("fred", Locale.getDefault()));
        assertNull(dc.getMessage("fred","cat"));
        assertNull(dc.getMessage("fred", Locale.getDefault(), "dog"));

    }

    public void testLifeCycle() throws Exception
    {
        DictionaryComponent dc = new DictionaryComponent();
        dc.setDictionaryDAO(dictionaryDAO);
        dc.init();
        dc.onEnableTenant();
        dc.onDisableTenant();
        dc.destroy();
        dc.init();
    }
}