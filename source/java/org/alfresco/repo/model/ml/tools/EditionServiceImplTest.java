/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.model.ml.tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionType;

/**
 * Edition Service test cases
 *
 * @since 2.1
 * @author Yannick Pignot
 */
public class EditionServiceImplTest extends AbstractMultilingualTestCases
{
    private static String  FRENCH_CONTENT   = "FRENCH_CONTENT";
    private static String  CHINESE_CONTENT  = "CHINESE_CONTENT";
    private static String  JAPANESE_CONTENT = "JAPANESE_CONTENT";

    public void testAutoEdition() throws Exception
    {
        // create a mlContainer with some content
        checkFirstVersion(this.createMLContainerWithContent());
    }

    public void testEditionLabels()
    {
        // create a mlContainer with some content
        NodeRef mlContainerNodeRef = createMLContainerWithContent();
        Map<String, Serializable> versionProperties = null;
        List<Version> editions = null;
        NodeRef pivot = multilingualContentService.getPivotTranslation(mlContainerNodeRef);

        checkFirstVersion(mlContainerNodeRef);

        /*
         * at the creation (1.0)
         */

        Version rootEdition = editionService.getEditions(mlContainerNodeRef).getAllVersions().iterator().next();
        // Ensure that the version label is 1.0
        assertTrue("The edition label would be 0.1 and not " + rootEdition.getVersionLabel(), rootEdition.getVersionLabel().equals("0.1"));

        /*
         * default (1.1)
         */

        pivot = editionService.createEdition(pivot, versionProperties);
        editions = new ArrayList<Version>(editionService.getEditions(mlContainerNodeRef).getAllVersions());
        Version firstEdition = editions.get(0);
        // Ensure that the version label is 1.1
        assertTrue("The edition label would be 0.2 and not " + firstEdition.getVersionLabel(), firstEdition.getVersionLabel().equals("0.2"));

        /*
         * major (2.0)
         */

        versionProperties = new HashMap<String, Serializable>();
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
        pivot = editionService.createEdition(pivot, versionProperties);
        editions = new ArrayList<Version>(editionService.getEditions(mlContainerNodeRef).getAllVersions());
        Version secondEdition = editions.get(0);
        // Ensure that the version label is 1.0
        assertTrue("The edition label would be 1.0 and not " + secondEdition.getVersionLabel(), secondEdition.getVersionLabel().equals("1.0"));

        /*
         * minor (2.1)
         */

        versionProperties = new HashMap<String, Serializable>();
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
        pivot = editionService.createEdition(pivot, versionProperties);
        editions = new ArrayList<Version>(editionService.getEditions(mlContainerNodeRef).getAllVersions());
        Version thirdEdition = editions.get(0);
        // Ensure that the version label is 2.1
        assertTrue("The edition label would be 1.1 and not " + thirdEdition.getVersionLabel(), thirdEdition.getVersionLabel().equals("1.1"));
    }

    public void testCreateEdition() throws Exception
    {
        // create a mlContainer with some content
        NodeRef mlContainerNodeRef = createMLContainerWithContent();
        // get the french translation
        NodeRef frenchContentNodeRef = multilingualContentService.getTranslationForLocale(mlContainerNodeRef, Locale.FRENCH);

        checkFirstVersion(mlContainerNodeRef);

        // create a new edition form the french translation
        NodeRef newStartingPoint = editionService.createEdition(frenchContentNodeRef, null);
        // get the edition history
        VersionHistory editionHistory = editionService.getEditions(mlContainerNodeRef);

        // Ensure that the edition history contains two versions
        assertTrue("The edition history must contain two versions", editionHistory.getAllVersions().size() == 2);

        // Ensure that the locale of the container is changer
        assertTrue("The locale of the conatiner should be changed", nodeService.getProperty(mlContainerNodeRef, ContentModel.PROP_LOCALE).equals(Locale.FRENCH));

        // get the two editions
        Version rootEdition = editionHistory.getVersion("0.1");
        Version actualEdition = editionHistory.getVersion("0.2");

        // get the translations of the root versions
        List<VersionHistory> rootVersionTranslations = editionService.getVersionedTranslations(rootEdition);

        // Ensure that the editions are not null
        assertNotNull("The root edition can't be null", rootEdition);
        assertNotNull("The actual edition can't be null", actualEdition);
        assertNotNull("The translations list of the root edition can't be null", rootVersionTranslations);

        // Ensure that the new starting document noderef is different that the initial one
        assertFalse("The created starting document must be different that the starting document of the edition", frenchContentNodeRef.equals(newStartingPoint));
        // Ensure that the new starting document is the pivot of the current translation
        assertTrue("The new pivot must be equal to the created starting document", newStartingPoint.equals(multilingualContentService.getPivotTranslation(mlContainerNodeRef)));

        int numberOfTranslations;

        // Ensure that the current translations size is 1
        numberOfTranslations = multilingualContentService.getTranslations(mlContainerNodeRef).size();
        assertEquals("The number of translations must be 1 and not " + numberOfTranslations, 1, numberOfTranslations);
        // Ensure that the number of translations of the current edition is 0
        numberOfTranslations = editionService.getVersionedTranslations(actualEdition).size();
        assertEquals("The number of translations must be 0 and not " + numberOfTranslations, 0, numberOfTranslations);
        // Ensure that the number of translations of the root verions is 3
        numberOfTranslations = rootVersionTranslations.size();
        assertEquals("The number of translations must be 3 and not " + numberOfTranslations, 3, numberOfTranslations);
    }

    public void testReadVersionedContent() throws Exception
    {

    }

    public void testReadVersionedProperties() throws Exception
    {

    }

    private void checkFirstVersion(NodeRef mlContainerNodeRef)
    {
        // get the edition list of edition
        VersionHistory editionHistory = editionService.getEditions(mlContainerNodeRef);

        // Ensure that the first edition of the mlContainer is created
        assertNotNull("The edition history can't be null", editionHistory);
        // Ensure that it contains only one version
        assertTrue("The edition history must contain only one edition", editionHistory.getAllVersions().size() == 1);

        // get the edition
        Version currentEdition = editionHistory.getAllVersions().iterator().next();

        // Ensure that this version is the edition of the mlContainer
        assertTrue("The versioned mlContainer noderef of the editon must be the noderef of the created mlContainer", currentEdition.getVersionedNodeRef().equals(mlContainerNodeRef));

        // get the list of translations
        List<VersionHistory> translations = editionService.getVersionedTranslations(currentEdition);

        // Ensure that the get versioned translations is empty
        assertNotNull("The translations list of the current edition can't be null", translations);
        // Ensure that the list is empty
        assertTrue("The translations list of the current edition would be empty", translations.size() == 0);
    }

    private NodeRef createMLContainerWithContent()
    {
        NodeRef chineseContentNodeRef  = createContent(CHINESE_CONTENT + "_1.0");
        NodeRef frenchContentNodeRef   = createContent(FRENCH_CONTENT + "_1.0");
        NodeRef japaneseContentNodeRef = createContent(JAPANESE_CONTENT + "_1.0");

        multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        multilingualContentService.addTranslation(frenchContentNodeRef, chineseContentNodeRef, Locale.FRENCH);
        multilingualContentService.addTranslation(japaneseContentNodeRef, chineseContentNodeRef, Locale.JAPANESE);

        return multilingualContentService.getTranslationContainer(chineseContentNodeRef);
    }
}
