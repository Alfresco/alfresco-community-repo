using System;
using NUnit.Framework;
using System.Collections.Generic;
using WcfTestClient.NavigationService;

namespace WcfCmisWSTests {
    ///
    /// author: Dmitry Velichkevich
    ///
    [TestFixture]
    public class NavigationServiceClientTest {
        private const int SINGLE_VALUE = 1;
        private const int PAIR_VALUE = 2;
        private const int OPTIMAL_MINIMAL_CHILDREN_AMOUNT = 3;
        private const int OPTIMAL_HIERARCHY_DEPTH = 4;
        private const int OPTIMAL_FOLDERS_DEPTH_AMOUNT = 5;
        private const int OPTIMAL_MAXIMUM_CHILDREN_AMOUNT = 6;
        private const int OPTIMAL_CHILDREN_AMOUNT = 7;
        private const int MAXIMUM_CHECKEDOUT_DOCS_AMOUNT = 13;

        private const string FULL_PARENTS_HIERARCHY = "-1";

        [Test]
        public void testDocumentParentsReceving() {

            string rootFolderId = AbstractCmisServicesHelper.getAndAssertRootFolder();

            assertSingleParentReceiving(rootFolderId);
            assertMultiFilledParentsReceving(rootFolderId);
        }

        [Test]
        public void testFolderParentsReceiving() {

            assertSingleParentReceiving();
            assertAllParentsReceiving();
        }

        [Test]
        public void testChildrenReceiving() {

            string[] hierarchy = AbstractCmisServicesHelper.createAndAssertFileFolderHierarchy(PAIR_VALUE,
                                                                                    OPTIMAL_CHILDREN_AMOUNT, 0, false);

            bool hasMoreElements;

            AbstractCmisServicesHelper.assertActualIdsFromObjectsWithSpecified(
                                                AbstractCmisServicesHelper.createNavigationServiceClient().getChildren(
                                                   AbstractCmisServicesHelper.getAndAssertRepositoryId(), hierarchy[0],
                                        enumTypesOfFileableObjects.any, AbstractCmisServicesHelper.ANY_PROPERTY_FILTER,
                                                                 false, false, AbstractCmisServicesHelper.TEXTUAL_ZERO,
                                                         AbstractCmisServicesHelper.TEXTUAL_ZERO, out hasMoreElements),
                                  createObjectsCopy(hierarchy, null, SINGLE_VALUE, (hierarchy.Length - SINGLE_VALUE)));

            AbstractCmisServicesHelper.deleteAndAssertHierarchy(hierarchy);
        }

        [Test]
        public void testDescendantsReceiving() {

            string[] hierarchy = AbstractCmisServicesHelper.createAndAssertFileFolderHierarchy(
                     OPTIMAL_HIERARCHY_DEPTH, OPTIMAL_MINIMAL_CHILDREN_AMOUNT, OPTIMAL_MAXIMUM_CHILDREN_AMOUNT, false);

            AbstractCmisServicesHelper.assertActualIdsFromObjectsWithSpecified(
                                             AbstractCmisServicesHelper.createNavigationServiceClient().getDescendants(
                                                   AbstractCmisServicesHelper.getAndAssertRepositoryId(), hierarchy[0],
                                                                enumTypesOfFileableObjects.any, FULL_PARENTS_HIERARCHY,
                                                         AbstractCmisServicesHelper.ANY_PROPERTY_FILTER, false, false),
                                  createObjectsCopy(hierarchy, null, SINGLE_VALUE, (hierarchy.Length - SINGLE_VALUE)));

            AbstractCmisServicesHelper.deleteAndAssertHierarchy(hierarchy);
        }

        [Test]
        public void testCheckedOutDocumentsReceving() {

            string[] hierarchy = createCheckedOutDocumentsHierarchy(SINGLE_VALUE + new Random().Next(
                                                                                      MAXIMUM_CHECKEDOUT_DOCS_AMOUNT));

            bool hasMoreElements;

            AbstractCmisServicesHelper.assertActualIdsFromObjectsWithSpecified(
                                          AbstractCmisServicesHelper.createNavigationServiceClient().getCheckedoutDocs(
                                                   AbstractCmisServicesHelper.getAndAssertRepositoryId(), hierarchy[0],
                                                          AbstractCmisServicesHelper.ANY_PROPERTY_FILTER, false, false,
                                      AbstractCmisServicesHelper.TEXTUAL_ZERO, AbstractCmisServicesHelper.TEXTUAL_ZERO,
                                                               out hasMoreElements), createObjectsCopy(hierarchy, null,
                                                                     SINGLE_VALUE, (hierarchy.Length - SINGLE_VALUE)));

            Assert.IsFalse(hasMoreElements);

            AbstractCmisServicesHelper.deleteAndAssertHierarchy(hierarchy);
        }

        private static void assertSingleParentReceiving(string rootFolderId) {

            string documentId = AbstractCmisServicesHelper.createAndAssertDocument(
                                                                         AbstractCmisServicesHelper.generateObjectName(
                                                       AbstractCmisServicesHelper.TEXT_DOCUMENT_POSTFIX), rootFolderId,
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_MIMETYPE,
                                                             AbstractCmisServicesHelper.getTestDocumentContentEntry());

            AbstractCmisServicesHelper.assertDocumentParents(documentId, new string[] {rootFolderId});

            AbstractCmisServicesHelper.deleteAndAssertDocument(documentId);
        }

        private static void assertMultiFilledParentsReceving(string rootFolderId) {

            KeyValuePair<string, KeyValuePair<string, string[]>> multifilledDocument =
                                                    AbstractCmisServicesHelper.createMultiFilledDocument(rootFolderId);

            AbstractCmisServicesHelper.assertDocumentParents(multifilledDocument.Value.Key,
                                                                                      multifilledDocument.Value.Value);

            AbstractCmisServicesHelper.deleteAndAssertDocument(multifilledDocument.Value.Key);
            AbstractCmisServicesHelper.deleteAndAssertFolder(multifilledDocument.Key, false);
        }

        private static void assertSingleParentReceiving() {

            string[] hierarchy = AbstractCmisServicesHelper.createAndAssertFileFolderHierarchy(PAIR_VALUE,
                                                                                     SINGLE_VALUE, SINGLE_VALUE, true);
            AbstractCmisServicesHelper.assertFolderParents(hierarchy[hierarchy.Length - SINGLE_VALUE],
                                      createObjectsCopy(hierarchy, null, 0, (hierarchy.Length - SINGLE_VALUE)), false);
            AbstractCmisServicesHelper.deleteAndAssertHierarchy(hierarchy);
        }

        private static void assertAllParentsReceiving() {

            string[] hierarchy = AbstractCmisServicesHelper.createAndAssertFileFolderHierarchy(
                                                       OPTIMAL_FOLDERS_DEPTH_AMOUNT, SINGLE_VALUE, SINGLE_VALUE, true);
            AbstractCmisServicesHelper.assertFolderParents(hierarchy[hierarchy.Length - SINGLE_VALUE],
                            createObjectsCopy(hierarchy, AbstractCmisServicesHelper.getAndAssertRootFolder(), 0,
                                                                             (hierarchy.Length - SINGLE_VALUE)), true);
            AbstractCmisServicesHelper.deleteAndAssertHierarchy(hierarchy);
        }

        private static string[] createCheckedOutDocumentsHierarchy(int documentsAmount) {

            string[] hierarchy = new string[documentsAmount + SINGLE_VALUE];

            hierarchy[0] = AbstractCmisServicesHelper.createAndAssertFolder(
                                                                   AbstractCmisServicesHelper.generateObjectName(null),
                                                                  AbstractCmisServicesHelper.getAndAssertRootFolder());

            for(int i = 0; i < documentsAmount; i++) {
                hierarchy[i + SINGLE_VALUE] = AbstractCmisServicesHelper.createAndAssertDocument(
                                                                         AbstractCmisServicesHelper.generateObjectName(
                                                       AbstractCmisServicesHelper.TEXT_DOCUMENT_POSTFIX), hierarchy[0],
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_MIMETYPE,
                                                            WcfTestClient.ObjectService.enumVersioningState.checkedout,
                                                             AbstractCmisServicesHelper.getTestDocumentContentEntry());
            }

            return hierarchy;
        }

        private static string[] createObjectsCopy(string[] source, string firstElemetn, int sourceBegin,
                                                                                                  int elementsAmount) {

            int absentElment = (firstElemetn != null) ? (SINGLE_VALUE):(0);

            string[] result = new string[elementsAmount + absentElment];

            result[0] = firstElemetn;

            Array.Copy(source, sourceBegin, result, absentElment, elementsAmount);

            return result;
        }
    }
}
