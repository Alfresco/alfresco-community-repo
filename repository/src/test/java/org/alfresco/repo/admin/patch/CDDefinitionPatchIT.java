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
package org.alfresco.repo.admin.patch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;

public class CDDefinitionPatchIT extends BaseSpringTest
{
    private static final String EXPECTED_CD_HOME_NAME = "Cascading Dictionary Definitions";
    private static final String EXPECTED_CD_HOME_TITLE = "Customized Cascading Dictionary Definitions";
    private static final String EXPECTED_CD_HOME_DESC = "Folder to store all cascading dictionaries that will be used by the system during classification";

    @Autowired
    private NodeService nodeService;
    @Autowired
    private SearchService searchService;
    @Autowired
    private Repository repository;
    @Autowired
    private NamespaceService namespaceService;
    @Autowired
    private PatchExecuter patchExecuter;

    @Value("${spaces.company_home.childname}")
    private String companyHomeChildName;
    @Value("${spaces.dictionary.childname}")
    private String dictionaryChildName;
    @Value("${spaces.cd_definitions.childname}")
    private String cdDefinitionsChildName;

    @Test
    public void testCDPatchConfiguration()
    {
        // given
        var cdHomeXPath = "/" + companyHomeChildName + "/" + dictionaryChildName + "/" + cdDefinitionsChildName;

        // when application stated, then
        assertCDHomeIsCorrect(cdHomeXPath);

        // when patches are executed again (simulating system restart)
        patchExecuter.applyOutstandingPatches();

        // then
        assertCDHomeIsCorrect(cdHomeXPath);
    }

    private void assertCDHomeIsCorrect(String cdHomeXPath)
    {
        var refs = searchService.selectNodes(repository.getRootHome(), cdHomeXPath, null, namespaceService, false);
        assertThat(refs).hasSize(1);
        var cdHomeNodeRef = refs.getFirst();
        var props = nodeService.getProperties(cdHomeNodeRef);

        // then
        assertThat(props)
                .containsEntry(QName.createQName("cm:name", namespaceService), EXPECTED_CD_HOME_NAME)
                .containsEntry(QName.createQName("cm:title", namespaceService), EXPECTED_CD_HOME_TITLE)
                .containsEntry(QName.createQName("cm:description", namespaceService), EXPECTED_CD_HOME_DESC);
    }
}
