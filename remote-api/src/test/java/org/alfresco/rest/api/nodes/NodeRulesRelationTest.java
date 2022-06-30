/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api.nodes;

import junit.framework.TestCase;
import org.alfresco.rest.api.Rules;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class NodeRulesRelationTest extends TestCase
{

    private static final String NODE_ID = "dummy-node-id";

    @Mock
    private Rules rules;

    @InjectMocks
    private NodeRulesRelation nodeRulesRelation;

    @Override
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testReadAll()
    {
        final Paging paging = Paging.DEFAULT;
        final Params.RecognizedParams params = new Params.RecognizedParams(null, paging, null, null, null, null, null, null, false);
        final Parameters parameters = Params.valueOf(NODE_ID, params, null, null);

        // when
        nodeRulesRelation.readAll(NODE_ID, parameters);

        then(rules).should().getRules(eq(NODE_ID), eq(paging));
        then(rules).shouldHaveNoMoreInteractions();
    }
}