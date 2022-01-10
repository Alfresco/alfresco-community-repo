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

package org.alfresco.module.org_alfresco_module_rm.recorded.version.config;

import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseWebScriptUnitTest;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Base test class for the recorded version config tests
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public abstract class BaseRecordedVersionConfigTest extends BaseWebScriptUnitTest implements RecordableVersionModel
{
    /** Recorded version config web script root folder */
    protected static final String RECORDED_VERSION_CONFIG_WEBSCRIPT_ROOT = "alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action/";

    /** Node ref for test document */
    protected NodeRef testdoc;

    /** Setup web script parameters */
    protected Map<String, String> buildParameters()
    {
        testdoc = generateCmContent("testdoc.txt");

        return buildParameters
        (
                "store_type",       testdoc.getStoreRef().getProtocol(),
                "store_id",         testdoc.getStoreRef().getIdentifier(),
                "id",               testdoc.getId()
        );
    }
}
