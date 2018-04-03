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
package org.alfresco.repo.action.executer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.dictionary.RepositoryLocation;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.transaction.annotation.Transactional;

/**
 * Specialise type action execution test
 * 
 * @author Roy Wetherall
 */
@Category(BaseSpringTestsCategory.class)
@Transactional
public class SpecialiseTypeActionExecuterTest extends BaseAlfrescoSpringTest
{    
    /**
     * The test node reference
     */
    private NodeRef nodeRef;
    
    /**
     * The specialise action executer
     */
    private SpecialiseTypeActionExecuter executer;
    
    /**
     * Id used to identify the test action created
     */
    private final static String ID = GUID.generate();
    
    private NodeRef nodeRefDicType;
    
    private ServiceRegistry serviceRegistry;

    
    /**
     * Called at the begining of all tests
     */
    @Before
    public void before() throws Exception
    {
        super.before();

        this.serviceRegistry = (ServiceRegistry) applicationContext.getBean("ServiceRegistry");

        // Create the node used for tests
        this.nodeRef = this.nodeService
                .createNode(this.rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}testnode"), ContentModel.TYPE_CONTENT)
                .getChildRef();

        // Create the node used for tests in
        // (/app:company_home/app:dictionary/app:models)
        this.nodeRefDicType = this.nodeService
                .createNode(findModelParent(), ContentModel.ASSOC_CHILDREN, QName.createQName("{test}testnode"), ContentModel.TYPE_CONTENT)
                .getChildRef();

        // Get the executer instance
        this.executer = (SpecialiseTypeActionExecuter) this.applicationContext.getBean(SpecialiseTypeActionExecuter.NAME);
    }

    /**
     * Test execution
     */
    @Test
    public void testExecution()
    {
        // Check the type of the node
        assertEquals(ContentModel.TYPE_CONTENT, this.nodeService.getType(this.nodeRef));
        
        // Execute the action
        ActionImpl action = new ActionImpl(null, ID, SpecialiseTypeActionExecuter.NAME, null);
        action.setParameterValue(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, ContentModel.TYPE_FOLDER);
        this.executer.execute(action, this.nodeRef);
        
        // Check that the node's type has not been changed since it would not be a specialisation
        assertEquals(ContentModel.TYPE_CONTENT, this.nodeService.getType(this.nodeRef));
        
        try
        {
            // Execute the action again and will fail is not in the correct
            // location (/app:company_home/app:dictionary/app:models)
            action.setParameterValue(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, ContentModel.TYPE_DICTIONARY_MODEL);
            this.executer.execute(action, this.nodeRef);
            fail("the executer should throw InvalidTypeException");
        }
        catch (InvalidTypeException ex)
        {

        }
        
    }
    
    @Test
    public void testCreateDicTypeExceptionLocation()
    {
        final QName modelName = QName.createQName("{http://www.alfresco.org/test/testmodel" + "D" + "/1.0}testModel" + "D");
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        contentProps.put(ContentModel.PROP_NAME, modelName);

        final NodeRef model1 = nodeService
                .createNode(findModelParent(), ContentModel.ASSOC_CONTAINS, modelName, ContentModel.TYPE_DICTIONARY_MODEL, contentProps)
                .getChildRef();

        // Check that the node's type has now been changed
        assertEquals(ContentModel.TYPE_DICTIONARY_MODEL, this.nodeService.getType(model1));

        try
        {
            nodeService.createNode(this.rootNodeRef, ContentModel.ASSOC_CONTAINS, modelName, ContentModel.TYPE_DICTIONARY_MODEL, contentProps)
                    .getChildRef();

            fail("the creation should throw InvalidTypeException");
        }
        catch (InvalidTypeException ex)
        {

        }

    }
    
    /**
     * Test execution
     */
    @Test
    public void testChangeDicTypeExecution()
    {
        // Check the type of the node
        assertEquals(ContentModel.TYPE_CONTENT, this.nodeService.getType(this.nodeRefDicType));
        this.nodeService.getParentAssocs(this.nodeRefDicType);

        // Execute the action
        ActionImpl action = new ActionImpl(null, ID, SpecialiseTypeActionExecuter.NAME, null);
        action.setParameterValue(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, ContentModel.TYPE_FOLDER);
        this.executer.execute(action, this.nodeRefDicType);

        // Check that the node's type has not been changed since it would not be
        // a specialisation
        assertEquals(ContentModel.TYPE_CONTENT, this.nodeService.getType(this.nodeRefDicType));

        // Execute the action agian
        action.setParameterValue(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, ContentModel.TYPE_DICTIONARY_MODEL);
        this.executer.execute(action, this.nodeRefDicType);
        
        // Check that the node's type has now been changed
        assertEquals(ContentModel.TYPE_DICTIONARY_MODEL, this.nodeService.getType(this.nodeRefDicType));
    }

    private NodeRef findModelParent()
    {
        RepositoryLocation modelLocation = (RepositoryLocation) applicationContext.getBean("customModelsRepositoryLocation");
        NodeRef rootNode = nodeService.getRootNode(modelLocation.getStoreRef());
        List<NodeRef> modelParents = serviceRegistry.getSearchService().selectNodes(rootNode, modelLocation.getPath(), null,
                serviceRegistry.getNamespaceService(), false);
        if (modelParents.size() == 0)
        {
            throw new IllegalStateException("Unable to find model location: " + modelLocation.getPath());
        }
        if (modelParents.size() > 1)
        {
            throw new IllegalStateException("More than one model location? [" + modelLocation.getPath() + "]");
        }

        return modelParents.get(0);
    }

}
