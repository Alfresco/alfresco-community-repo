/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.example.webservice.classification;

import javax.xml.rpc.ServiceException;

import junit.framework.AssertionFailedError;

import org.alfresco.example.webservice.BaseWebServiceSystemTest;
import org.alfresco.example.webservice.types.Category;
import org.alfresco.example.webservice.types.ClassDefinition;
import org.alfresco.example.webservice.types.Classification;
import org.alfresco.example.webservice.types.Predicate;
import org.alfresco.example.webservice.types.Reference;
import org.alfresco.util.GUID;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.configuration.FileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClassificationServiceSystemTest extends BaseWebServiceSystemTest
{
    private static Log logger = LogFactory
            .getLog(ClassificationServiceSystemTest.class);

    private ClassificationServiceSoapBindingStub classificationService;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        try
        {
            EngineConfiguration config = new FileProvider(getResourcesDir(),
                    "client-deploy.wsdd");
            this.classificationService = (ClassificationServiceSoapBindingStub) new ClassificationServiceLocator(
                    config).getClassificationService();
        } catch (ServiceException jre)
        {
            if (jre.getLinkedCause() != null)
            {
                jre.getLinkedCause().printStackTrace();
            }

            throw new AssertionFailedError("JAX-RPC ServiceException caught: "
                    + jre);
        }

        assertNotNull("contentService is null", this.classificationService);

        // Time out after a minute
        this.classificationService.setTimeout(60000);
    }

    /**
     * Tests the getClassifications service method
     * 
     * @throws Exception
     */
    public void testGetClassifications() throws Exception
    {
        Classification[] classifications = this.classificationService
                .getClassifications(getStore());

        assertNotNull(classifications);
        assertTrue((classifications.length != 0));
        Classification classification = classifications[0];
        assertNotNull(classification.getTitle());
        assertNotNull(classification.getRootCategory());
        assertNotNull(classification.getRootCategory().getId());
        assertNotNull(classification.getRootCategory().getTitle());
        
        if (logger.isDebugEnabled() == true)
        {
            for (Classification item : classifications)
            {
                logger.debug(
                        "Classification '" +
                        item.getTitle() +
                        "' with root category '" +
                        item.getRootCategory().getTitle() +
                        "'");
            }
        }
    }

    /**
     * Tests the getChildCategories service method
     * 
     * @throws Exception
     */
    public void testGetChildCategories() throws Exception
    {
        Classification[] classifications = this.classificationService.getClassifications(getStore());
        Reference parentCategory = classifications[0].getRootCategory().getId();
        
        Category[] categories = this.classificationService.getChildCategories(parentCategory);
        assertNotNull(categories);
        assertTrue((categories.length != 0));
        Category item = categories[0];
        assertNotNull(item.getId());
        assertNotNull(item.getTitle());
        
        if (logger.isDebugEnabled() == true)
        {
            for (Category category : categories)
            {
                logger.debug(
                        "Sub-category '" +
                        category.getTitle() +
                        "'");
            }
        }
    }

    /**
     * Tests the getCategories and setCategories service methods
     * 
     * @throws Exception
     */
    public void testGetAndSetCategories() throws Exception
    {
        Classification[] classifications = this.classificationService.getClassifications(getStore());
        String classification = classifications[0].getClassification();
        Reference category = classifications[0].getRootCategory().getId();
        
        Reference reference = createContentAtRoot(GUID.generate(), "Any old content.");
        Predicate predicate = convertToPredicate(reference);
        
        // First try and get the categories for a uncategoried node
        CategoriesResult[] result1 = this.classificationService.getCategories(predicate);
        assertNotNull(result1);
        assertEquals(1, result1.length);
        assertNull(result1[0].getCategories());
        
        AppliedCategory appliedCategory = new AppliedCategory();
        appliedCategory.setCategories(new Reference[]{category});
        appliedCategory.setClassification(classification);
        
        AppliedCategory[] appliedCategories = new AppliedCategory[]{appliedCategory};
        
        // Now classify the node
        CategoriesResult[] result2 = this.classificationService.setCategories(predicate, appliedCategories);
        assertNotNull(result2);
        assertEquals(1, result2.length);
        
        // Now get the value back
        CategoriesResult[] result3 = this.classificationService.getCategories(predicate);
        assertNotNull(result3);
        assertEquals(1, result3.length);
        CategoriesResult categoryResult = result3[0];
        assertEquals(reference.getUuid(), categoryResult.getNode().getUuid());
        AppliedCategory[] appCats = categoryResult.getCategories();
        assertNotNull(appCats);
        assertEquals(1, appCats.length);
        AppliedCategory appCat = appCats[0];
        assertEquals(classification, appCat.getClassification());
        Reference[] refs = appCat.getCategories();
        assertNotNull(refs);
        assertEquals(1, refs.length);
        Reference ref = refs[0];
        assertEquals(category.getUuid(), ref.getUuid());
        
        // TODO test multiple classifiations 
        // TODO test clearing the classifications
        // TODO test updating the classifications
    }


    /**
     * Tests the describeClassification service method
     * 
     * @throws Exception
     */
    public void testDescribeClassification() throws Exception
    {
        Classification[] classifications = this.classificationService.getClassifications(getStore());
        String classification = classifications[0].getClassification();
        
        ClassDefinition classDefinition = this.classificationService.describeClassification(classification);
        
        assertNotNull(classDefinition);
    }
}
