/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.repo.virtual.template;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

public class TemplateFilingRuleTest extends VirtualizationIntegrationTest
{
    @Test
    public void testFilingSubPath_specialCharacters() throws Exception
    {
        NodeRef vfNodeRef = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                                    "Template 6 With Spaces",
                                                    TEST_TEMPLATE_5_JSON_SYS_PATH);

        NodeRef sfpNodeRef = nodeService.getChildByName(vfNodeRef,
                                                        ContentModel.ASSOC_CONTAINS,
                                                        "SpecialFilingPath5");

        Reference sfpReference = Reference.fromNodeRef(sfpNodeRef);
        ApplyTemplateMethod applyTemplateMethod = new ApplyTemplateMethod(environment);

        VirtualFolderDefinition structure = sfpReference.execute(applyTemplateMethod);

        FilingRule filingRule = structure.getFilingRule();

        assertTrue(filingRule instanceof TemplateFilingRule);

        NodeRef fn = filingRule.filingNodeRefFor(new FilingParameters(sfpReference));
        assertNull(fn);
        createFolder(vfNodeRef,
                     "Space Sub Folder");
        fn = filingRule.filingNodeRefFor(new FilingParameters(sfpReference));
        assertNotNull(fn);
    }

    @Test
    public void testFilingPath_specialCharacters() throws Exception
    {
        NodeRef vfNodeRef = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                                    "Template 6 With Spaces",
                                                    TEST_TEMPLATE_5_JSON_SYS_PATH);

        NodeRef sfpNodeRef = nodeService.getChildByName(vfNodeRef,
                                                        ContentModel.ASSOC_CONTAINS,
                                                        "SpecialFilingPath4");

        Reference sfpReference = Reference.fromNodeRef(sfpNodeRef);
        ApplyTemplateMethod applyTemplateMethod = new ApplyTemplateMethod(environment);

        VirtualFolderDefinition structure = sfpReference.execute(applyTemplateMethod);

        FilingRule filingRule = structure.getFilingRule();
        assertTrue(filingRule instanceof TemplateFilingRule);

        NodeRef fn = filingRule.filingNodeRefFor(new FilingParameters(sfpReference));
        assertNotNull(fn);
    }
}
