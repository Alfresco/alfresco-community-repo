/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.repo.node;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.transaction.annotation.Transactional;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

/**
 * Tests that exercise the {@code cd:*} model (Cascading Dictionary) from generic public APIs (here: directly via {@link NodeService}, which sits behind the public Node REST API, CMIS, WebDAV, scripts, …).
 * <p>
 * The {@code cd:definition} type, its properties ({@code cd:aspect}, {@code cd:keyProperty}, {@code cd:versionProperty}) and the {@code cd:classifiable} aspect are intended to be modified <b>only</b> by the Cascading Dictionary subsystem in the enterprise repository. No client of a generic Alfresco API should be able to:
 * <ul>
 * <li>create a node of type {@code cd:definition},</li>
 * <li>change an existing node's type to {@code cd:definition},</li>
 * <li>add / update / remove {@code cd:aspect}, {@code cd:keyProperty} or {@code cd:versionProperty} on a node,</li>
 * <li>add the {@code cd:classifiable} aspect,</li>
 * <li>delete or move an existing {@code cd:definition} node.</li>
 * </ul>
 * <p>
 */
@Category(OwnJVMTestsCategory.class)
@Transactional
public class CDDefinitionProtectionTest extends BaseSpringTest
{
    private static final String CD_URI = NamespaceService.CD_MODEL_1_0_URI;

    private static final QName TYPE_CD_DEFINITION = ContentModel.TYPE_CD_DEFINITION;
    private static final QName PROP_CD_ASPECT = ContentModel.PROP_CD_ASPECT;
    private static final QName PROP_CD_KEY_PROPERTY = ContentModel.PROP_CD_KEY;
    private static final QName PROP_CD_VERSION_PROPERTY = ContentModel.PROP_CD_VERSION;
    private static final QName ASPECT_CD_CLASSIFIABLE = ContentModel.ASPECT_CD_CLASSIFIABLE;

    private NodeService nodeService;
    private BehaviourFilter behaviourFilter;
    private AuthenticationComponent authenticationComponent;

    private NodeRef parentFolder;
    private NodeRef otherParentFolder;

    @Before
    public void before()
    {
        nodeService = (NodeService) applicationContext.getBean("NodeService");
        behaviourFilter = (BehaviourFilter) applicationContext.getBean("policyBehaviourFilter");
        authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");

        authenticationComponent.setSystemUserAsCurrentUser();

        StoreRef storeRef = nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE,
                "Test_" + getClass().getSimpleName() + "_" + System.currentTimeMillis());
        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);

        parentFolder = createFolder(rootNodeRef, "parent-" + GUID.generate());
        otherParentFolder = createFolder(rootNodeRef, "other-" + GUID.generate());
    }

    @After
    public void after()
    {
        try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable ignore)
        {
            // ignore
        }
    }

    @Test
    public void createCdDefinitionNodeShouldBeBlocked()
    {
        Map<QName, Serializable> props = buildValidCdDefinitionProperties();

        try
        {
            nodeService.createNode(
                    parentFolder,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(CD_URI, "def-" + GUID.generate()),
                    TYPE_CD_DEFINITION,
                    props);
            fail("Generic NodeService.createNode(cd:definition) must be blocked, but the call succeeded.");
        }
        catch (RuntimeException expected)
        {
            // expected once protection is in place
        }
    }

    @Test
    public void changingNodeTypeToCdDefinitionShouldBeBlocked()
    {
        NodeRef content = createContent(parentFolder, "to-mutate-" + GUID.generate());

        try
        {
            nodeService.setType(content, TYPE_CD_DEFINITION);
            fail("Generic NodeService.setType(..., cd:definition) must be blocked, but the call succeeded.");
        }
        catch (RuntimeException expected)
        {
            // expected once protection is in place
        }
    }

    @Test
    public void addingCdClassifiableAspectShouldBeBlocked()
    {
        NodeRef content = createContent(parentFolder, "to-classify-" + GUID.generate());

        try
        {
            nodeService.addAspect(content, ASPECT_CD_CLASSIFIABLE, null);
            fail("Generic NodeService.addAspect(cd:classifiable) must be blocked, but the call succeeded.");
        }
        catch (RuntimeException expected)
        {
            // expected once protection is in place
        }
    }

    @Test
    public void removingCdClassifiableAspectShouldBeBlocked()
    {
        NodeRef content = createContent(parentFolder, "classified-" + GUID.generate());

        behaviourFilter.disableBehaviour(ASPECT_CD_CLASSIFIABLE);
        try
        {
            nodeService.addAspect(content, ASPECT_CD_CLASSIFIABLE, null);
        }
        finally
        {
            behaviourFilter.enableBehaviour(ASPECT_CD_CLASSIFIABLE);
        }

        try
        {
            nodeService.removeAspect(content, ASPECT_CD_CLASSIFIABLE);
            fail("Generic NodeService.removeAspect(cd:classifiable) must be blocked, but the call succeeded.");
        }
        catch (RuntimeException expected)
        {
            // expected once protection is in place
        }
    }

    @Test
    public void deletingExistingCdDefinitionShouldBeBlocked()
    {
        NodeRef cdDef = legitimatelyCreateCdDefinition(parentFolder, "to-delete-" + GUID.generate());

        try
        {
            nodeService.deleteNode(cdDef);
            fail("Generic NodeService.deleteNode(cd:definition) must be blocked, but the call succeeded.");
        }
        catch (RuntimeException expected)
        {
            // expected once protection is in place
        }
    }

    @Test
    public void movingExistingCdDefinitionShouldBeBlocked()
    {
        NodeRef cdDef = legitimatelyCreateCdDefinition(parentFolder, "to-move-" + GUID.generate());

        try
        {
            nodeService.moveNode(
                    cdDef,
                    otherParentFolder,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(CD_URI, "moved-" + GUID.generate()));
            fail("Generic NodeService.moveNode(cd:definition, ...) must be blocked, but the call succeeded.");
        }
        catch (RuntimeException expected)
        {
            // expected once protection is in place
        }
    }

    @Test
    public void updatingCdDefinitionPropertiesShouldBeBlocked()
    {
        NodeRef cdDef = legitimatelyCreateCdDefinition(parentFolder, "to-update-" + GUID.generate());

        try
        {
            nodeService.setProperty(cdDef, PROP_CD_KEY_PROPERTY, "cm:hijacked");
            fail("Generic NodeService.setProperty(cd:definition, cd:keyProperty, ...) must be blocked, but the call succeeded.");
        }
        catch (RuntimeException expected)
        {
            // expected once protection is in place
        }
    }

    /**
     * Removing a {@code cd:*} property from an existing {@code cd:definition} must be rejected.
     */
    @Test
    public void removingCdDefinitionPropertyShouldBeBlocked()
    {
        NodeRef cdDef = legitimatelyCreateCdDefinition(parentFolder, "to-strip-" + GUID.generate());

        try
        {
            nodeService.removeProperty(cdDef, PROP_CD_VERSION_PROPERTY);
            fail("Generic NodeService.removeProperty(cd:definition, cd:versionProperty) must be blocked, but the call succeeded.");
        }
        catch (RuntimeException expected)
        {
            // expected once protection is in place
        }
    }

    /**
     * {@code setProperties} (full replacement) on a {@code cd:definition} must be rejected, mirroring the {@code setProperty} / {@code addProperties} cases.
     */
    @Test
    public void replacingCdDefinitionPropertiesShouldBeBlocked()
    {
        NodeRef cdDef = legitimatelyCreateCdDefinition(parentFolder, "to-replace-" + GUID.generate());

        Map<QName, Serializable> replacement = new HashMap<>();
        replacement.put(ContentModel.PROP_NAME, "renamed-" + GUID.generate());
        replacement.put(PROP_CD_ASPECT, "hijacked");
        replacement.put(PROP_CD_KEY_PROPERTY, "hijacked");
        replacement.put(PROP_CD_VERSION_PROPERTY, "hijacked");

        try
        {
            nodeService.setProperties(cdDef, replacement);
            fail("Generic NodeService.setProperties(cd:definition, ...) must be blocked, but the call succeeded.");
        }
        catch (RuntimeException expected)
        {
            // expected once protection is in place
        }
    }

    /**
     * Changing a {@code cd:definition}'s type to anything else (e.g. {@code cm:content}) must be rejected — once a node is a cd:definition its type is permanent.
     */
    @Test
    public void changingCdDefinitionTypeAwayShouldBeBlocked()
    {
        NodeRef cdDef = legitimatelyCreateCdDefinition(parentFolder, "to-detype-" + GUID.generate());

        try
        {
            nodeService.setType(cdDef, ContentModel.TYPE_CONTENT);
            fail("Generic NodeService.setType(cdDef, cm:content) must be blocked, but the call succeeded.");
        }
        catch (RuntimeException expected)
        {
            // expected once protection is in place
        }
    }

    private Map<QName, Serializable> buildValidCdDefinitionProperties()
    {
        Map<QName, Serializable> props = new HashMap<>();
        props.put(ContentModel.PROP_NAME, "cd-" + GUID.generate());
        props.put(PROP_CD_ASPECT, "testValue1");
        props.put(PROP_CD_KEY_PROPERTY, "testValue2");
        props.put(PROP_CD_VERSION_PROPERTY, "testValue3");
        return props;
    }

    private NodeRef createFolder(NodeRef parent, String name)
    {
        Map<QName, Serializable> props = new HashMap<>();
        props.put(ContentModel.PROP_NAME, name);
        ChildAssociationRef assoc = nodeService.createNode(
                parent,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(CD_URI, name),
                ContentModel.TYPE_FOLDER,
                props);
        return assoc.getChildRef();
    }

    private NodeRef createContent(NodeRef parent, String name)
    {
        Map<QName, Serializable> props = new HashMap<>();
        props.put(ContentModel.PROP_NAME, name);
        ChildAssociationRef assoc = nodeService.createNode(
                parent,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(CD_URI, name),
                ContentModel.TYPE_CONTENT,
                props);
        return assoc.getChildRef();
    }

    private NodeRef legitimatelyCreateCdDefinition(NodeRef parent, String name)
    {
        behaviourFilter.disableBehaviour(TYPE_CD_DEFINITION);
        try
        {
            ChildAssociationRef assoc = nodeService.createNode(
                    parent,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(CD_URI, name),
                    TYPE_CD_DEFINITION,
                    buildValidCdDefinitionProperties());
            return assoc.getChildRef();
        }
        finally
        {
            behaviourFilter.enableBehaviour(TYPE_CD_DEFINITION);
        }
    }
}
