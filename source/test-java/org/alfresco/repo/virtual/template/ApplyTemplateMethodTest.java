/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.template;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.repo.virtual.ref.NewVirtualReferenceMethod;
import org.alfresco.repo.virtual.ref.Protocols;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.junit.Test;

public class ApplyTemplateMethodTest extends VirtualizationIntegrationTest
{
    @Test
    public void testExecute_vanillaISO9075ActualPath() throws Exception
    {
        ChildAssociationRef iso9075FolderAssoc = createFolder(testRootFolder.getNodeRef(),
                                                              "Acutal ISO9075 Node");
        NodeRef iso9075Folder = iso9075FolderAssoc.getChildRef();

        NewVirtualReferenceMethod newVirtualReferenceMethod = new NewVirtualReferenceMethod(TEST_TEMPLATE_5_JSON_SYS_PATH,
                                                                                            "/",
                                                                                            iso9075Folder,
                                                                                            VANILLA_PROCESSOR_JS_CLASSPATH);

        ApplyTemplateMethod applyTemplateMethod = new ApplyTemplateMethod(environment);

        Reference ref = Protocols.VANILLA.protocol.dispatch(newVirtualReferenceMethod,
                                                            null);
        VirtualFolderDefinition structure = ref.execute(applyTemplateMethod);

        VirtualFolderDefinition sfp5 = structure.findChildByName("SpecialFilingPath5");
        VirtualQuery query = sfp5.getQuery();

        assertEquals("(PATH:'/app:company_home/cm:TestFolder/cm:Acutal_x0020_ISO9075_x0020_Node/cm:Space_x0020_Sub_x0020_Folder/*')  and =cm:description:'SpecialFilingPath_5'",
                     query.getQueryString());

        ChildAssociationRef iso9075SubFolderAssoc = createFolder(iso9075Folder,
                                                                 "Space Sub Folder");

        ChildAssociationRef someContentAssoc = createContent(iso9075SubFolderAssoc.getChildRef(),
                                                             "someContent");

        nodeService.setProperty(someContentAssoc.getChildRef(),
                                ContentModel.PROP_DESCRIPTION,
                                "SpecialFilingPath_5");

        // check query for validity
        SearchParameters searchParameters = new SearchParameters();
        
        searchParameters.setQuery(query.getQueryString());
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setLanguage(query.getLanguage());
        ResultSet qresult = searchService.query(searchParameters);
        assertEquals(1,
                     qresult.getNumberFound());

    }

    @Test
    public void testExecute_virtualSysClasspath() throws Exception
    {
        ApplyTemplateMethod applyTemplateMethod = new ApplyTemplateMethod(environment);

        NewVirtualReferenceMethod newVirtualReferenceMethod = new NewVirtualReferenceMethod(TEST_TEMPLATE_1_JS_SYS_PATH,
                                                                                            "/",
                                                                                            rootNodeRef,
                                                                                            null);
        Reference ref = Protocols.VIRTUAL.protocol.dispatch(newVirtualReferenceMethod,
                                                            null);
        VirtualFolderDefinition structure = ref.execute(applyTemplateMethod);

        String templateName = structure.getName();
        assertEquals("template1_name",
                     templateName);

        List<VirtualFolderDefinition> children = structure.getChildren();
        assertEquals(3,
                     children.size());

        VirtualFolderDefinition child1 = structure.findChildByName("My Documents");
        assertTrue(child1 != null);

        VirtualFolderDefinition child2 = structure.findChildByName("Recent Documents");
        assertTrue(child2 != null);

        VirtualFolderDefinition child3 = structure.findChildByName("Other Documents");
        assertTrue(child3 != null);

    }

    @Test
    public void testExecute_vanillaRepositoryJSON() throws Exception
    {
        ChildAssociationRef templateAssoc = createContent(testRootFolder.getNodeRef(),
                                                          "template1.json",
                                                          ApplyTemplateMethodTest.class
                                                                      .getResourceAsStream(TEST_TEMPLATE_1_JSON_NAME),
                                                          MimetypeMap.MIMETYPE_JSON,
                                                          StandardCharsets.UTF_8.name());
        ApplyTemplateMethod applyTemplateMethod = new ApplyTemplateMethod(environment);

        NewVirtualReferenceMethod newVirtualReferenceMethod = new NewVirtualReferenceMethod(templateAssoc.getChildRef(),
                                                                                            "/",
                                                                                            virtualFolder1NodeRef,
                                                                                            VANILLA_PROCESSOR_JS_CLASSPATH);
        Reference ref = Protocols.VANILLA.protocol.dispatch(newVirtualReferenceMethod,
                                                            null);
        VirtualFolderDefinition structure = ref.execute(applyTemplateMethod);

        String templateName = structure.getName();
        assertEquals("Test",
                     templateName);

        List<VirtualFolderDefinition> children = structure.getChildren();
        assertEquals(2,
                     children.size());

        VirtualFolderDefinition child1 = structure.findChildByName("Node1");
        assertTrue(child1 != null);

        VirtualFolderDefinition child2 = structure.findChildByName("Node2");
        assertTrue(child2 != null);

    }

    @Test
    public void testExecuteRepositoryJS() throws Exception
    {
        // TODO:
    }
}
