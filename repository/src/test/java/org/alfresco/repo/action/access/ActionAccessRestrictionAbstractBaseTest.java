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
import org.junit.Before;
import org.junit.Test;
import org.springframework.lang.NonNull;

import java.util.Properties;

import static org.junit.Assert.assertThrows;

public class ActionAccessRestrictionAbstractBaseTest {

    private static final String GLOBAL_PROPERTIES_ACTION_EXPOSE_PREFIX = "org.alfresco.repo.action";
    private static final String GLOBAL_PROPERTIES_ACTION_EXPOSE_SUFFIX = ".exposed";
    private static final String CONTROLLED_CONTEXT = ActionAccessRestriction.V1_ACTION_CONTEXT;
    private static final String NONCONTROLLED_CONTEXT = "random321";

    private static final String MAIL_ACTION = "mail";


    private ActionAccessRestrictionAbstractBase accessRestriction;
    private Properties globalProperties;

    @Before
    public void setup() {
        globalProperties = new Properties();

        accessRestriction = new ActionAccessRestrictionAbstractBase() {
            @Override
            protected void innerVerifyAccessRestriction(Action action) {
                throw new ActionAccessException("Executing verification");
            }
        };
        accessRestriction.setConfigProperties(globalProperties);
    }

    @Test
    public void skipVerificationForNullContext() {
        Action action = getActionWithContext(MAIL_ACTION, null);
        accessRestriction.verifyAccessRestriction(action);
    }

    @Test
    public void skipVerificationForNonControlledContext() {
        Action action = getActionWithContext(MAIL_ACTION, NONCONTROLLED_CONTEXT);
        accessRestriction.verifyAccessRestriction(action);
    }

    @Test
    public void callVerificationForControlledContext() {
        Action action = getActionWithContext(MAIL_ACTION, CONTROLLED_CONTEXT);
        assertThrows(ActionAccessException.class, () -> accessRestriction.verifyAccessRestriction(action));
    }

    @Test
    public void skipVerificationForExposedActionConfig() {
        setGlobalPropertiesActionExposed(MAIL_ACTION, null, true);
        Action action = getActionWithContext(MAIL_ACTION, CONTROLLED_CONTEXT);
        accessRestriction.verifyAccessRestriction(action);
    }

    @Test
    public void skipVerificationForExposedActionContextConfig() {
        setGlobalPropertiesActionExposed(MAIL_ACTION, CONTROLLED_CONTEXT, true);
        Action action = getActionWithContext(MAIL_ACTION, CONTROLLED_CONTEXT);
        accessRestriction.verifyAccessRestriction(action);
    }

    @Test
    public void callVerificationForNonExposedActionConfig() {
        setGlobalPropertiesActionExposed(MAIL_ACTION, null, false);
        Action action = getActionWithContext(MAIL_ACTION, CONTROLLED_CONTEXT);
        assertThrows(ActionAccessException.class, () -> accessRestriction.verifyAccessRestriction(action));
    }

    @Test
    public void callVerificationForNonExposedActionContextConfig() {
        setGlobalPropertiesActionExposed(MAIL_ACTION, CONTROLLED_CONTEXT, false);
        Action action = getActionWithContext(MAIL_ACTION, CONTROLLED_CONTEXT);
        assertThrows(ActionAccessException.class, () -> accessRestriction.verifyAccessRestriction(action));
    }

    private Action getActionWithContext(String actionName, String context) {
        Action action = new ActionImpl(null, "12345", actionName);
        ActionAccessRestriction.setActionContext(action, context);

        return action;
    }

    private void setGlobalPropertiesActionExposed(@NonNull String action, String context, boolean isExposed) {
        StringBuilder property = new StringBuilder(GLOBAL_PROPERTIES_ACTION_EXPOSE_PREFIX);
        property.append("." + action);
        if (context != null) {
            property.append("." + context);
        }
        property.append(GLOBAL_PROPERTIES_ACTION_EXPOSE_SUFFIX);

        globalProperties.setProperty(property.toString(), Boolean.toString(isExposed));
    }
}
