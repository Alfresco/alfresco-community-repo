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

package org.alfresco.repo.forms.processor.workflow;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.*;
import static org.alfresco.repo.workflow.WorkflowModel.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.forms.processor.FilterRegistry;
import org.alfresco.repo.forms.processor.node.DefaultFieldProcessor;
import org.alfresco.repo.forms.processor.node.MockClassAttributeDefinition;
import org.alfresco.repo.forms.processor.node.MockFieldProcessorRegistry;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

import junit.framework.TestCase;

public abstract class FormProcessorTest extends TestCase
{
    protected static final String TASK_DEF_NAME = "TaskDef";
    protected static final QName DESC_NAME = PROP_DESCRIPTION;
    protected static final QName STATUS_NAME = PROP_STATUS;
    protected static final QName PROP_WITH_ = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "some_prop");
    protected static final QName ACTORS_NAME = ASSOC_POOLED_ACTORS;
    protected static final QName ASSIGNEE_NAME = ASSOC_ASSIGNEE;
    protected static final QName ASSOC_WITH_ = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "some_assoc");
    protected static final NodeRef FAKE_NODE = new NodeRef(NamespaceService.BPM_MODEL_1_0_URI + "/FakeNode");
    protected static final NodeRef FAKE_NODE2 = new NodeRef(NamespaceService.BPM_MODEL_1_0_URI + "/FakeNode2");
    protected static final NodeRef FAKE_NODE3 = new NodeRef(NamespaceService.BPM_MODEL_1_0_URI + "/FakeNode3");
    protected static final NodeRef PCKG_NODE = new NodeRef(NamespaceService.BPM_MODEL_1_0_URI + "/FakePackage");

    protected WorkflowService workflowService;
    protected NodeService nodeService;
    protected NamespaceService namespaceService;
    protected AbstractWorkflowFormProcessor processor;
    protected Item item;

    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected DefaultFieldProcessor makeDefaultFieldProcessor(DictionaryService dictionaryService) throws Exception
    {
        DefaultFieldProcessor defaultProcessor = new DefaultFieldProcessor();
        defaultProcessor.setDictionaryService(dictionaryService);
        defaultProcessor.setNamespaceService(namespaceService);
        defaultProcessor.afterPropertiesSet();
        return defaultProcessor;
    }

    public void testPersistPropertyComment(String taskId) throws Exception
    {
        // MNT-11809 Activiti losses data with large Aspect or Type property values
        // wiki Hamlet text
        StringBuilder wikiHamlet = new StringBuilder();
        wikiHamlet.append("Hamlet is the son of the King of Denmark. When Hamlet`s father dies, his uncle Claudius becomes king and marries Hamlet`s mother (Gertrude).");
        wikiHamlet.append("Hamlet`s father appears as a ghost and tells Hamlet that he was murdered by Claudius. Hamlet is not sure that the ghost is really his father.");
        wikiHamlet.append("He gets some travelling actors to perform a play which shows the murder of a king in the same way Hamlet`s father said he was killed.");
        wikiHamlet.append("When Claudius reacts badly to seeing this, Hamlet believes he is guilty.");
        wikiHamlet.append("Hamlet tells his mother that he knows about the murder. While there he kills Polonius, who is the king`s advisor, because he thinks he is Claudius.");
        wikiHamlet.append("Rosencrantz and Guildenstern were Hamlet`s childhood friends.");
        wikiHamlet
                .append("Claudius sends Rosencrantz and Guildenstern with Hamlet to England to have Hamlet killed, but their ship is attacked by pirates who take Hamlet prisoner but then return him to Denmark.");
        wikiHamlet
                .append("Rosencrantz and Guildenstern are taken to England where they die. Ophelia is Polonius` daughter . After her father, Polonius, is killed by Hamlet she goes mad.");
        wikiHamlet.append("Then she falls into a river and drowns. Hamlet returns just as her funeral is happening. Laertes, her brother, decides to kill Hamlet in revenge.");
        wikiHamlet
                .append("He challenges Hamlet to a sword fight, and puts poison on his own sword. Claudius makes some poisoned wine for Hamlet to drink in case that does not work.");
        wikiHamlet.append("At first Hamlet wins the sword fight, and in the meantime his mother drinks the poisoned wine without knowing, and dies.");
        wikiHamlet
                .append("On the other hand Laertes falsely pierces Hamlet with a poisoned blade, but then Hamlet stabs Laertes with the same sword. Laertes tells Hamlet about the plot and then dies.");
        wikiHamlet.append("Hamlet kills Claudius with the poisoned sword. Horatio, Hamlet`s friend, tells everyone about the murder of the old king.");
        wikiHamlet.append("Hamlet tells everyone that the Norwegian prince, Fortinbras, should be king, and then dies from the poison.");
        wikiHamlet.append("When Fortinbras arrives, Horatio recounts the tale and Fortinbras orders Hamlet`s body borne off in honour.");
        wikiHamlet.append("Hamlet is one of the hardest parts for an actor to perform. It is one of the largest roles written by Shakespeare.");
        wikiHamlet.append("Many people disagree about what Hamlet is really thinking. For many actors, playing Hamlet is one of the most important parts of their career.");

        // Get max length of jbpm_comment property from repository.properties
        String maxLength = "4000";

        String dir = System.getProperty("user.dir");
        File propFile = new File(dir + File.separator + "config" + File.separator + "alfresco" + File.separator + "repository.properties");

        if (propFile.exists())
        {
            InputStream propStream = null;
            try
            {
                propStream = new FileInputStream(propFile);
                Properties properties = new Properties();
                properties.load(propStream);

                maxLength = properties.getProperty("system.workflow.jbpm.comment.property.max.length");
            }
            finally
            {
                if (propStream != null)
                {
                    propStream.close();
                }
            }
        }

        // Convert value of property to int
        int maxLengthPropery;
        try
        {
            maxLengthPropery = Integer.valueOf(maxLength);
        }
        catch (NumberFormatException e)
        {
            maxLengthPropery = 4000;
        }

        FormData data = new FormData();
        boolean repeatTest = true;
        String test = wikiHamlet.toString();
        Item item = new Item("task", taskId);

        while (repeatTest)
        {
            data.addFieldData("prop_bpm_comment", test);

            int value = test.getBytes().length;

            try
            {
                processor.persist(item, data);

                assertTrue("Value is " + value, value < maxLengthPropery);
            }
            catch (Exception e)
            {
                assertTrue("Caught Exception, value is " + value, value > maxLengthPropery);
            }

            if (value < maxLengthPropery)
            {
                test += wikiHamlet.toString();
            }
            else
            {
                repeatTest = false;
            }
        }
    }

    protected AbstractWorkflowFormProcessor makeTaskFormProcessor(AbstractWorkflowFormProcessor processor1, DictionaryService dictionaryService,
            MockFieldProcessorRegistry fieldProcessorRegistry, DefaultFieldProcessor defaultProcessor)
    {
        processor1.setWorkflowService(workflowService);
        processor1.setNodeService(nodeService);
        processor1.setNamespaceService(namespaceService);
        processor1.setDictionaryService(dictionaryService);
        processor1.setFieldProcessorRegistry(fieldProcessorRegistry);
        processor1.setBehaviourFilter(mock(BehaviourFilter.class));
        // MNT-11809 Activiti losses data with large Aspect or Type property values
        FilterRegistry filterRegistry = new FilterRegistry();
        filterRegistry.addFilter(new WorkflowFormFilter());
        processor1.setFilterRegistry(filterRegistry);
        return processor1;
    }

    protected void mockPackageItems(NodeRef... children)
    {
        ArrayList<ChildAssociationRef> results = new ArrayList<ChildAssociationRef>(children.length);
        for (NodeRef nodeRef : children)
        {
            ChildAssociationRef child = new ChildAssociationRef(ASSOC_PACKAGE_CONTAINS, PCKG_NODE, null, nodeRef);
            results.add(child);
        }
        when(nodeService.getChildAssocs(eq(PCKG_NODE), (QNamePattern) any(), (QNamePattern) any())).thenReturn(results);

    }

    protected void checkRemovedPackageItem(NodeRef child, boolean wasCalled)
    {
        int times = wasCalled ? 1 : 0;
        verify(nodeService, times(times)).removeChild(PCKG_NODE, child);
    }

    protected void checkAddPackageItem(NodeRef child, boolean wasCalled)
    {
        int times = wasCalled ? 1 : 0;
        verify(nodeService, times(times)).addChild(eq(PCKG_NODE), eq(child), eq(ASSOC_PACKAGE_CONTAINS), (QName) any());
    }

    protected void checkSingleProperty(Form form, String fieldName, Serializable fieldData)
    {
        String expDataKey = makeDataKeyName(fieldName);
        checkSingleField(form, fieldName, fieldData, expDataKey);
    }

    protected void checkSingleAssociation(Form form, String fieldName, Serializable fieldData)
    {
        String expDataKey = makeAssociationDataKey(fieldName);
        checkSingleField(form, fieldName, fieldData, expDataKey);
    }

    protected void checkSingleField(Form form, String fieldName, Serializable fieldData, String expDataKey)
    {
        List<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        assertEquals(1, fieldDefs.size());
        FieldDefinition fieldDef = fieldDefs.get(0);
        assertEquals(fieldName, fieldDef.getName());
        String dataKey = fieldDef.getDataKeyName();
        assertEquals(expDataKey, dataKey);
        FieldData data = form.getFormData().getFieldData(dataKey);
        if (fieldData != null  && data != null)
        {
            assertEquals(fieldData, data.getValue());
        }
        else
        {
            assertNull(data);
        }
    }

    protected String makeDataKeyName(String fieldName)
    {
        return PROP_DATA_PREFIX + fieldName.replace(":", "_");
    }

    protected String makeDataKeyName(String fieldName, boolean added)
    {
        String assocDataKey = makeAssociationDataKey(fieldName);
        String suffix = added ? ASSOC_DATA_ADDED_SUFFIX : ASSOC_DATA_REMOVED_SUFFIX;
        return assocDataKey + suffix;
    }

    protected String makeAssociationDataKey(String fieldName)
    {
        return ASSOC_DATA_PREFIX + fieldName.replace(":", "_");
    }

    protected Map<QName, AssociationDefinition> makeTaskAssociationDefs()
    {
        Map<QName, AssociationDefinition> associations = new HashMap<QName, AssociationDefinition>();
        QName actorName = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "Actor");

        // Add Assigneee association
        MockClassAttributeDefinition assigneeDef = MockClassAttributeDefinition.mockAssociationDefinition(ASSIGNEE_NAME, actorName);
        associations.put(ASSIGNEE_NAME, assigneeDef);

        // Add Assigneee association
        MockClassAttributeDefinition actorsDef = MockClassAttributeDefinition.mockAssociationDefinition(ACTORS_NAME, actorName);
        associations.put(ACTORS_NAME, actorsDef);

        // Add association with _
        MockClassAttributeDefinition with_ = MockClassAttributeDefinition.mockAssociationDefinition(ASSOC_WITH_, actorName);
        associations.put(ASSOC_WITH_, with_);

        return associations;
    }

}
