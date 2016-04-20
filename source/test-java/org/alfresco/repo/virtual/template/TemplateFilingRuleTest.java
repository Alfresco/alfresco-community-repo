
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
