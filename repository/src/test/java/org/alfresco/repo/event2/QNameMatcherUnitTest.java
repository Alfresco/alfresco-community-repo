/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2025 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.event2;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.event2.shared.QNameMatcher;
import org.alfresco.repo.transfer.TransferModel;
import org.alfresco.service.namespace.QName;

public class QNameMatcherUnitTest
{
    @Test
    public void shouldMatchOnlyQNamesFromUserModelURI()
    {
        QNameMatcher qNameMatcher = new QNameMatcher(Set.of(QName.createQName(ContentModel.USER_MODEL_URI, "*")));

        assertTrue(qNameMatcher.isMatching(ContentModel.PROP_USER_USERNAME));
        assertTrue(qNameMatcher.isMatching(ContentModel.TYPE_USER));
        assertFalse(qNameMatcher.isMatching(ContentModel.PROP_TITLE));
        assertFalse(qNameMatcher.isMatching(TransferModel.PROP_USERNAME));
        assertFalse(qNameMatcher.isMatching(null));
    }

    @Test
    public void shouldOnlyMatchSpecificQName()
    {
        QNameMatcher qNameMatcher = new QNameMatcher(Set.of(ContentModel.PROP_USER_USERNAME));

        assertTrue(qNameMatcher.isMatching(ContentModel.PROP_USER_USERNAME));
        assertFalse(qNameMatcher.isMatching(ContentModel.PROP_NAME));
        assertFalse(qNameMatcher.isMatching(ContentModel.PROP_USERNAME));
        assertFalse(qNameMatcher.isMatching(TransferModel.PROP_USERNAME));
        assertFalse(qNameMatcher.isMatching(null));
    }
}
