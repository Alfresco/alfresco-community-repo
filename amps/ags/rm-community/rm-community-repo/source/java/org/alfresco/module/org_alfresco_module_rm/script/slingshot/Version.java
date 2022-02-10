/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.script.slingshot;

import static org.alfresco.util.ParameterCheck.mandatory;
import static org.alfresco.util.ParameterCheck.mandatoryString;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Recordable version class
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
@AlfrescoPublicApi
public class Version
{
    /** The version policy */
    private String policy;

    /** Is the version selected */
    private boolean selected;

    /**
     * Constructor
     *
     * @param policy The version policy
     * @param selected Is the version selected
     */
    public Version(String policy, boolean selected)
    {
        mandatoryString("policy", policy);
        mandatory("selected", selected);

        setPolicy(policy);
        setSelected(selected);
    }

    /**
     * Gets the version policy
     *
     * @return The version policy
     */
    public String getPolicy()
    {
        return this.policy;
    }

    /**
     * Sets the version policy
     *
     * @param policy The version policy
     */
    private void setPolicy(String policy)
    {
        this.policy = policy;
    }

    /**
     * Is the version selected
     *
     * @return <code>true</code> if the version is selected, <code>false</code> otherwise
     */
    public boolean isSelected()
    {
        return this.selected;
    }

    /**
     * Sets the version as selected
     *
     * @param selected <code>true</code> if the version should be selected, <code>false</code> otherwise
     */
    private void setSelected(boolean selected)
    {
        this.selected = selected;
    }
}
