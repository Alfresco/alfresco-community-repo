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

package org.alfresco.rm.rest.api.impl;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseYamlUnitTest;
import org.junit.Test;

/**
 * Unit Test class for RM Yaml file validation.
 *
 * @author Sara Aspery
 * @since 2.6
 *
 */
public class RMYamlUnitTest extends BaseYamlUnitTest
{
    private static String RM_COMMUNITY_YAML_FILES_PATH = "../rm-community-rest-api-explorer/src/main/webapp/definitions";

    @Test
    public void validateYamlFile() throws Exception
    {
        validateYamlFiles(getYamlFilesList(RM_COMMUNITY_YAML_FILES_PATH));
    }
}

