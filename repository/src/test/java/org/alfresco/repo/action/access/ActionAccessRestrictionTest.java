/*
 * #%L
 * Alfresco Repository
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
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.action.access;

import org.alfresco.repo.action.ActionImpl;
import org.alfresco.service.cmr.action.Action;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ActionAccessRestrictionTest {

    @Test
    public void testSettingContext() {
        Action mailAction = new ActionImpl(null, "12345", "mail");

        ActionAccessRestriction.setActionContext(mailAction, ActionAccessRestriction.RULE_ACTION_CONTEXT);
        assertEquals(ActionAccessRestriction.RULE_ACTION_CONTEXT, ActionAccessRestriction.getActionContext(mailAction));
    }
}
