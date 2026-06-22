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
package org.alfresco.repo.node.propertyextender;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

@Transactional
public class PropertyExtenderInterceptorIT extends BaseSpringTest
{

    private static final String TEST_NAMESPACE = "http://www.alfresco.org/test/PropertyExtenderInterceptorTest";
    private static final QName KEY_1 = QName.createQName(TEST_NAMESPACE, "key1");
    private static final QName DUPLICATION_1 = QName.createQName(TEST_NAMESPACE, "duplication1");
    private static final QName KEY_2 = QName.createQName(TEST_NAMESPACE, "key2");
    private static final QName DUPLICATION_2 = QName.createQName(TEST_NAMESPACE, "duplication2");

    @Autowired
    private PropertyExtendersHolder propertyExtendersHolder;
    @Autowired
    private NodeService nodeService;

    @Test
    public void testPropertyInterceptorLifecycle()
    {
        propertyExtendersHolder.registerExtender(new TestPropertyDuplicator(KEY_1, DUPLICATION_1));
        propertyExtendersHolder.registerExtender(new TestPropertyDuplicator(KEY_2, DUPLICATION_2));

        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        var rootNodeRef = nodeService.getRootNode(storeRef);

        // create node with property that should be duplicated by the extender
        var nodeRef = nodeService.createNode(rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(TEST_NAMESPACE, GUID.generate()),
                ContentModel.TYPE_CONTENT,
                Map.of(KEY_1, "A")).getChildRef();

        assertThat(nodeService.getProperties(nodeRef))
                .containsAllEntriesOf(Map.of(DUPLICATION_1, "A"))
                .doesNotContainKey(DUPLICATION_2);

        // add next property that should be extended
        nodeService.addProperties(nodeRef, Map.of(KEY_2, "B"));
        assertThat(nodeService.getProperties(nodeRef))
                .containsAllEntriesOf(Map.of(DUPLICATION_1, "A", DUPLICATION_2, "B"));

        // modify properties
        nodeService.addProperties(nodeRef, Map.of(KEY_1, "C", KEY_2, "D"));
        assertThat(nodeService.getProperties(nodeRef))
                .containsAllEntriesOf(Map.of(DUPLICATION_1, "C", DUPLICATION_2, "D"));

        // modify property
        nodeService.setProperty(nodeRef, KEY_1, "E");
        assertThat(nodeService.getProperties(nodeRef))
                .containsAllEntriesOf(Map.of(DUPLICATION_1, "E", DUPLICATION_2, "D"));

        // set properties
        nodeService.setProperties(nodeRef, Map.of(KEY_1, "F", KEY_2, "G"));
        assertThat(nodeService.getProperties(nodeRef))
                .containsAllEntriesOf(Map.of(DUPLICATION_1, "F", DUPLICATION_2, "G"));

        // remove property
        nodeService.removeProperty(nodeRef, KEY_1);
        assertThat(nodeService.getProperties(nodeRef))
                .containsEntry(DUPLICATION_1, null)
                .containsEntry(DUPLICATION_2, "G");
    }

    record TestPropertyDuplicator(QName key, QName duplicateKey) implements PropertyExtender
    {
        @Override
        public CalculationResult calculate(CalculationContext context)
        {
            if (context.propertyChanges().containsKey(key))
            {
                return new CalculationResult(Collections.singletonMap(duplicateKey, context.propertyChanges().get(key)));
            }
            return CalculationResult.NO_OP;
        }
    }
}
