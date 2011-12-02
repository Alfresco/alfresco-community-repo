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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
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

    private ContentService contentService;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        contentService = serviceRegistry.getContentService();
    }

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

    //ALF-6275
    public void testEditionServiceWithContent()
    {
        // create a mlContainer with some content
        NodeRef mlContainerNodeRef = createMLContainerWithContent("0.1");
        // get the french translation
        NodeRef frenchContentNodeRef = multilingualContentService.getTranslationForLocale(mlContainerNodeRef,
                Locale.FRENCH);
        checkContentVersionValuesForEditions(mlContainerNodeRef);
        // create a new edition starting from the french translation
        NodeRef newStartingPoint = editionService.createEdition(frenchContentNodeRef, null);
        frenchContentNodeRef = multilingualContentService.getTranslationForLocale(mlContainerNodeRef, Locale.FRENCH);
        modifyContent(frenchContentNodeRef, FRENCH_CONTENT + "-" + Locale.FRENCH + "-" + "0.2" + "-" + "0.2");
        checkContentVersionValuesForEditions(mlContainerNodeRef);
        modifyContent(frenchContentNodeRef, FRENCH_CONTENT + "-" + Locale.FRENCH + "-" + "0.2" + "-" + "0.3");
        checkContentVersionValuesForEditions(mlContainerNodeRef);
        modifyContent(frenchContentNodeRef, FRENCH_CONTENT + "-" + Locale.FRENCH + "-" + "0.2" + "-" + "0.4");
        // checkContentVersionValuesForEditions(mlContainerNodeRef);
        frenchContentNodeRef = multilingualContentService.getTranslationForLocale(mlContainerNodeRef, Locale.FRENCH);

        newStartingPoint = editionService.createEdition(frenchContentNodeRef, null);
        frenchContentNodeRef = multilingualContentService.getTranslationForLocale(mlContainerNodeRef, Locale.FRENCH);
        modifyContent(frenchContentNodeRef, FRENCH_CONTENT + "-" + Locale.FRENCH + "-" + "0.3" + "-" + "0.2");
        checkContentVersionValuesForEditions(mlContainerNodeRef);
        modifyContent(frenchContentNodeRef, FRENCH_CONTENT + "-" + Locale.FRENCH + "-" + "0.3" + "-" + "0.3");
        checkContentVersionValuesForEditions(mlContainerNodeRef);
        NodeRef chineseContentNodeRef = createContent(CHINESE_CONTENT + "-" + Locale.CHINESE + "-" + "0.3" + "-0.1");
        multilingualContentService.addTranslation(chineseContentNodeRef, mlContainerNodeRef, Locale.CHINESE);
        checkContentVersionValuesForEditions(mlContainerNodeRef);
        NodeRef japaneseContentNodeRef = createContent(JAPANESE_CONTENT + "-" + Locale.JAPANESE + "-" + "0.3" + "-0.1");
        multilingualContentService.addTranslation(japaneseContentNodeRef, mlContainerNodeRef, Locale.JAPANESE);
        checkContentVersionValuesForEditions(mlContainerNodeRef);

        japaneseContentNodeRef = multilingualContentService
                .getTranslationForLocale(mlContainerNodeRef, Locale.JAPANESE);
        japaneseContentNodeRef = editionService.createEdition(japaneseContentNodeRef, null);
        checkContentVersionValuesForEditions(mlContainerNodeRef);
        japaneseContentNodeRef = multilingualContentService
                .getTranslationForLocale(mlContainerNodeRef, Locale.JAPANESE);
        modifyContent(japaneseContentNodeRef, JAPANESE_CONTENT + "-" + Locale.JAPANESE + "-" + "0.4" + "-0.2");
        chineseContentNodeRef = createContent(CHINESE_CONTENT + "-" + Locale.CHINESE + "-" + "0.4" + "-0.1");

        multilingualContentService.addTranslation(chineseContentNodeRef, mlContainerNodeRef, Locale.CHINESE);
        checkContentVersionValuesForEditions(mlContainerNodeRef);
        frenchContentNodeRef = createContent(FRENCH_CONTENT + "-" + Locale.FRENCH + "-" + "0.4" + "-0.1");
        multilingualContentService.addTranslation(frenchContentNodeRef, mlContainerNodeRef, Locale.FRENCH);
        checkContentVersionValuesForEditions(mlContainerNodeRef);

        frenchContentNodeRef = multilingualContentService.getTranslationForLocale(mlContainerNodeRef, Locale.FRENCH);
        newStartingPoint = editionService.createEdition(frenchContentNodeRef, null);
        frenchContentNodeRef = multilingualContentService.getTranslationForLocale(mlContainerNodeRef, Locale.FRENCH);
        modifyContent(frenchContentNodeRef, FRENCH_CONTENT + "-" + Locale.FRENCH + "-" + "0.5" + "-" + "0.2");
        checkContentVersionValuesForEditions(mlContainerNodeRef);


        japaneseContentNodeRef = multilingualContentService.getTranslationForLocale(mlContainerNodeRef, Locale.JAPANESE);
        HashMap<String, Serializable> versionProperties = new HashMap<String, Serializable>();
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
        japaneseContentNodeRef = editionService.createEdition(japaneseContentNodeRef, versionProperties);

         Collection<Version> editions = editionService.getEditions(mlContainerNodeRef).getAllVersions();
         Version secondEdition = editions.iterator().next();
         // Ensure that the version label is 2.0
         assertTrue("The edition label would be 2.0 and not " + secondEdition.getVersionLabel(), secondEdition
          .getVersionLabel().equals("1.0"));
         frenchContentNodeRef = createContent(FRENCH_CONTENT + "-" + Locale.FRENCH + "-" + "1.0" + "-0.1");
         modifyContent(frenchContentNodeRef, FRENCH_CONTENT + "-" + Locale.FRENCH + "-" + "1.0" + "-" + "0.2");
         checkContentVersionValuesForEditions(mlContainerNodeRef);

         frenchContentNodeRef = multilingualContentService.getTranslationForLocale(mlContainerNodeRef, Locale.FRENCH);

         versionProperties = new HashMap<String, Serializable>();
         versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
         frenchContentNodeRef = editionService.createEdition(frenchContentNodeRef, versionProperties);

          editions = editionService.getEditions(mlContainerNodeRef).getAllVersions();
          secondEdition = editions.iterator().next();
          // Ensure that the version label is 2.0
          assertTrue("The edition label would be 3.0 and not " + secondEdition.getVersionLabel(), secondEdition
           .getVersionLabel().equals("2.0"));
          modifyContent(frenchContentNodeRef, FRENCH_CONTENT + "-" + Locale.FRENCH + "-" + "2.0" + "-" + "0.2");
          checkContentVersionValuesForEditions(mlContainerNodeRef);


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
    
    private void checkContentVersionValuesForEditions(NodeRef mlContainerNodeRef)
    {
        // the convention applied for this test is that the content MUST end up with
        // _LOCALE_EDITION-VERION_INDIVIDUAL-VERSION
        // get the edition list of edition
        VersionHistory editionHistory = editionService.getEditions(mlContainerNodeRef);

        // Ensure that the first edition of the mlContainer is created
        assertNotNull("The edition history can't be null", editionHistory);
        // Ensure that it contains only one version
        assertTrue("The edition history must contain only one edition", editionHistory.getAllVersions().size() >= 1);

        // iterate on editions
        for (Version editionVersion : editionHistory.getAllVersions())
        {
            // getting the edition label
            String editionLabel = editionVersion.getVersionLabel();
            // check if it is the head version
            if (editionHistory.getHeadVersion() == editionVersion)
            {
                // this is the living edition
                System.out.println("Head version edition:" + editionLabel);
                // dump content of head edition
                Map<Locale, NodeRef> translations = multilingualContentService.getTranslations(mlContainerNodeRef);
                Locale pivotLocale = (Locale) nodeService.getProperty(mlContainerNodeRef, ContentModel.PROP_LOCALE);
                Set<Locale> keySet = translations.keySet();
                for (Locale locale : keySet)
                {
                    NodeRef translatedNode = translations.get(locale);
                    // get content as string and compare
                    ContentReader reader = contentService.getReader(translatedNode, ContentModel.PROP_CONTENT);
                    String liveContent = reader.getContentString();
                    Triplet parsedTriplet = new Triplet(liveContent);
                    System.out.println("Content:" + liveContent);

                    // get all the version of current translation
                    VersionHistory versionHistory = versionService.getVersionHistory(translatedNode);
                    for (Version version : versionHistory.getAllVersions())
                    {
                        NodeRef frozenNodeRef = version.getFrozenStateNodeRef();
                        String versionLabel = version.getVersionLabel();
                        Locale versionLocale = (Locale) nodeService
                                .getProperty(frozenNodeRef, ContentModel.PROP_LOCALE);
                        // get content as string and compare
                        reader = contentService.getReader(frozenNodeRef, ContentModel.PROP_CONTENT);
                        String versionnedContent = reader.getContentString();
                        System.out.println("Individual version " + versionLabel + ":" + versionnedContent);
                        if (versionService.getCurrentVersion(translatedNode).getFrozenStateNodeRef().equals(
                                version.getFrozenStateNodeRef()))
                        {
                            // this is the head version of the translation therefore content should be equal
                            assertTrue(
                                    "The content in head version should be equal to the content of the translation:",
                                    versionnedContent.equals(liveContent));
                        }
                        // checking if content respects conventions XXX*locale*edition_version_*document_version
                        // the exception should be the version used to start the new edition with
                        // exception exist for root version because root version can be the first created
                        // and if is the pivot language then its content is a copy of the previous edition.
                        // This breaks the conventions and other checks must be done
                        if ((versionHistory.getRootVersion().getFrozenStateNodeRef().equals(version
                                .getFrozenStateNodeRef()))
                                && (pivotLocale.equals(versionLocale)))
                        {
                            System.out.println("Some special on live version has to be done:" + versionnedContent);
                            // get previous edition
                            Version previousEditionVersion = editionHistory.getPredecessor(editionVersion);

                            if (previousEditionVersion != null)
                            {
                                String previousEditionLabel = previousEditionVersion.getVersionLabel();
                                System.out.println("Current edition Label:" + editionLabel + " Previous edition label:"
                                        + previousEditionLabel);
                                List<VersionHistory> versionTranslations = editionService
                                        .getVersionedTranslations(previousEditionVersion);
                                // for all languages iterate to find the corresponding language
                                for (VersionHistory versionTranslation : versionTranslations)
                                {
                                    // most recent first
                                    Version newestVersion = versionTranslation.getHeadVersion();
                                    NodeRef newestVersionFrozenNodeRef = newestVersion.getFrozenStateNodeRef();
                                    String newestVersionVersionLabel = newestVersion.getVersionLabel();
                                    ContentReader readerContentWeStartedWith = contentService.getReader(
                                            newestVersionFrozenNodeRef, ContentModel.PROP_CONTENT);
                                    Locale oldestVersionLocale = (Locale) nodeService.getProperty(
                                            newestVersionFrozenNodeRef, ContentModel.PROP_LOCALE);
                                    String contentWeStartedWith = readerContentWeStartedWith.getContentString();
                                    System.out.println("CONTENT:" + contentWeStartedWith);
                                    if (versionLocale.equals(oldestVersionLocale))
                                    {
                                        // content should match
                                        assertTrue(
                                                "The content in head version should be equal to the content we started with:",
                                                contentWeStartedWith.equals(versionnedContent));
                                    }
                                }
                            }

                        }
                        else
                        {
                            // it is not a root version therefore it should respect the conventions
                            Triplet testTriplet = new Triplet(versionnedContent);
                            assertTrue(testTriplet.locale.equals(versionLocale.toString()));
                            assertTrue(testTriplet.edition.equals(editionLabel));
                            assertTrue(testTriplet.version.equals(versionLabel));
                        }

                    }
                }
            }
            else
            {
                // get pivot language of the current versionned edition
                // This is not the current/head edition
                Version nextEditionVersion = editionHistory.getSuccessors(editionVersion).iterator().next();
                
                //get Next verion label
                String nextEditionLabel = nextEditionVersion.getVersionLabel();
                System.out.println("Edition:" + editionLabel + " Next edition label:" + nextEditionLabel);
                // get the translations of the version
                List<VersionHistory> versionTranslations = editionService.getVersionedTranslations(editionVersion);
                // iterate on versionTranslations (all languages)

                //strange that we have to go to the next edition to find the current pivot language.
                //maybe there is a reason for that but not logical logical
                dumpFrozenMetaData(editionVersion);
                Locale editionPivotlanguage = (Locale) (editionService.getVersionedMetadatas(editionVersion)
                        .get(ContentModel.PROP_LOCALE));
                System.out.println("Edition:" + editionLabel + " Previous pivot language:" + editionPivotlanguage + " Current pivot language:" + editionPivotlanguage);
                for (VersionHistory versionTranslation : versionTranslations)
                {
                    Collection<Version> versions = versionTranslation.getAllVersions();
                    // for a language, iterate on all versions
                    for (Version version : versions)
                    {
                        NodeRef frozenNodeRef = version.getFrozenStateNodeRef();
                        String versionLabel = version.getVersionLabel();
                        // get content language
                        Locale currentVersionLocale = (Locale) nodeService.getProperty(frozenNodeRef,
                                ContentModel.PROP_LOCALE);
                        System.out.println("Current version locale:" + currentVersionLocale
                                + " Previous edition locale:" + editionPivotlanguage);
                        // get content as string and compare
                        ContentReader reader = contentService.getReader(frozenNodeRef, ContentModel.PROP_CONTENT);
                        String content = reader.getContentString();
                        System.out.println("Content:" + content);
                        // checking content respects conventions XXX*locale*edition_version_*document_version
                        // the exception should be the version used to start the new edition with
                        Version initialVersion = versionTranslation.getRootVersion();
                        if (initialVersion.equals(version) && currentVersionLocale.equals(editionPivotlanguage))
                        {
                            System.out.println("Some special test has to be done:" + content);
                            Version previousEditionVersion = editionHistory.getPredecessor(editionVersion);

                            if (previousEditionVersion != null)
                            {
                                String previousEditionLabel = previousEditionVersion.getVersionLabel();
                                System.out.println("Current edition Label:" + editionLabel + " Previous edition label:"
                                        + previousEditionLabel);
                                List<VersionHistory> versionTranslations2 = editionService
                                        .getVersionedTranslations(previousEditionVersion);
                                // for all languages iterate to find the corresponding language
                                for (VersionHistory versionTranslation2 : versionTranslations2)
                                {
                                    // most recent first
                                    Version newestVersion = versionTranslation2.getHeadVersion();
                                    NodeRef newestVersionFrozenNodeRef = newestVersion.getFrozenStateNodeRef();
                                    String newestVersionVersionLabel = newestVersion.getVersionLabel();
                                    ContentReader readerContentWeStartedWith = contentService.getReader(
                                            newestVersionFrozenNodeRef, ContentModel.PROP_CONTENT);
                                    Locale oldestVersionLocale = (Locale) nodeService.getProperty(
                                            newestVersionFrozenNodeRef, ContentModel.PROP_LOCALE);
                                    String contentWeStartedWith = readerContentWeStartedWith.getContentString();
                                    System.out.println("CONTENT:" + contentWeStartedWith);
                                    if (currentVersionLocale.equals(oldestVersionLocale))
                                    {
                                        // content should match
                                        assertTrue(
                                                "The content in head version should be equal to the content we started with:",
                                                contentWeStartedWith.equals(content));
                                    }
                                }
                            }
                            else
                            {
                                // normal invariant here because it is not the initial version
                                Triplet testTriplet = new Triplet(content);
                                assertTrue(testTriplet.locale.equals(currentVersionLocale.toString()));
                                assertTrue(testTriplet.edition.equals(editionLabel));
                                assertTrue(testTriplet.version.equals(versionLabel));
                            }
                        }
                    }
                }
            }

        }
    }
    private 
    NodeRef createMLContainerWithContent(String editionSuffix)
    {
        NodeRef chineseContentNodeRef = createContent(CHINESE_CONTENT + "-" + Locale.CHINESE + "-" + editionSuffix
                + "-0.1");
        NodeRef frenchContentNodeRef = createContent(FRENCH_CONTENT + "-" + Locale.FRENCH + "-" + editionSuffix
                + "-0.1");
        NodeRef japaneseContentNodeRef = createContent(JAPANESE_CONTENT + "-" + Locale.JAPANESE + "-" + editionSuffix
                + "-0.1");

        multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        multilingualContentService.addTranslation(frenchContentNodeRef, chineseContentNodeRef, Locale.FRENCH);
        multilingualContentService.addTranslation(japaneseContentNodeRef, chineseContentNodeRef, Locale.JAPANESE);

        return multilingualContentService.getTranslationContainer(chineseContentNodeRef);
    }
    
    private void modifyContent(NodeRef nodeRef, String value)
    {
        ContentWriter contentWriter = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.putContent(value);
    }
    
    private void dumpFrozenMetaData(Version editionVersion)
    {
        System.out.println("---------------------------------------------------");
        //Get current version label
        System.out.println("Version Label: " + editionVersion.getVersionLabel());
        System.out.println("---------------------------------------------------");
        //Map<QName,Serializable> mapOfFrozenProps = editionService.getVersionedMetadatas(editionVersion);
        Map<String,Serializable> mapOfFrozenProps = editionVersion.getVersionProperties();
        if(mapOfFrozenProps == null )
        {
            System.out.println("Nul... ");
            return;
        }

        for(String  q: mapOfFrozenProps.keySet())
        {
            String val = mapOfFrozenProps.get(q)==null?"null":mapOfFrozenProps.get(q).toString();
            System.out.println("QName:" + q + ":" +  val);
        }
    }

    
    /**
     * Parse the content to extract the local,edition lablel,version label
     *
     * @author Philippe Dubois
     */
    private class Triplet
    {
        public String locale;
        public String edition;
        public String version;

        public Triplet(String content)
        {
            String[] tokens = content.split("-");
            locale = tokens[1];
            edition = tokens[2];
            version = tokens[3];
        }

    }
    
    protected NodeRef createContent(String name)
    {
        NodeRef contentNodeRef = fileFolderService.create(folderNodeRef, name+".txt", ContentModel.TYPE_CONTENT).getNodeRef();
        // add some content
        ContentWriter contentWriter = fileFolderService.getWriter(contentNodeRef);
        contentWriter.putContent(name);
        // done
        return contentNodeRef;
    }

}
