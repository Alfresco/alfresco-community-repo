using System;
using System.Text;
using NUnit.Framework;
using WcfTestClient.ObjectService;

namespace WcfCmisWSTests {
    ///
    /// author: Dmitry Velichkevich
    ///
    [TestFixture]
    public class ObjectServiceClientTest {
        private const int TEST_HIERARCHY_DEPTH = 4;
        private const int MINIMAL_TEST_OBJECTS_LEVEL_AMOUNT = 2;

        private const string DEFAULT_GUEST_USERNAME = "guest";
        private const string DEFAULT_GUEST_PASSWORD = "guest";

        private const string REPLACED_CONTENT_ENTRY = "Replaced Content Entry";

        private const string NAME_FOR_DOCUMENT_RENAMING = "Renamed Document.txt";
        private const string NAME_FOR_FOLDER_RENAMING = "Renamed Folder";

        private const string CREATOR_PROPERTY_NAME = "CreatedBy";

        [Test]
        public void testDocumentCreationAndDeletion() {

            string documentId = AbstractCmisServicesHelper.createAndAssertDocument(
                                AbstractCmisServicesHelper.generateObjectName(AbstractCmisServicesHelper
                                          .TEXT_DOCUMENT_POSTFIX), AbstractCmisServicesHelper.getAndAssertRootFolder(),
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_MIMETYPE,
                                                             AbstractCmisServicesHelper.getTestDocumentContentEntry());

            AbstractCmisServicesHelper.deleteAndAssertDocument(documentId);
        }

        [Test]
        public void testFolderCreationAndDeletion() {

            string folderId = AbstractCmisServicesHelper.createAndAssertFolder(
                                                                   AbstractCmisServicesHelper.generateObjectName(null),
                                                                  AbstractCmisServicesHelper.getAndAssertRootFolder());

            AbstractCmisServicesHelper.deleteAndAssertFolder(folderId, false);
        }

        [Test]
        public void testPropertiesReceiving() {

            string documentId = AbstractCmisServicesHelper.createAndAssertDocument(
                                              AbstractCmisServicesHelper.generateObjectName(AbstractCmisServicesHelper.
                                           TEXT_DOCUMENT_POSTFIX), AbstractCmisServicesHelper.getAndAssertRootFolder(),
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_MIMETYPE,
                                                             AbstractCmisServicesHelper.getTestDocumentContentEntry());

            cmisPropertiesType properties = AbstractCmisServicesHelper.getObjectProperties(documentId, true);

            Assert.AreEqual(AbstractCmisServicesHelper.UserName,
                  AbstractCmisServicesHelper.searchPropertyAndGetValueByName(properties.Items, CREATOR_PROPERTY_NAME));

            AbstractCmisServicesHelper.deleteAndAssertDocument(documentId);
        }

        [Test]
        public void testPropertiesUpdating() {

            string repositoryId = AbstractCmisServicesHelper.getAndAssertRepositoryId();
            string rootFolderId = AbstractCmisServicesHelper.getAndAssertRootFolder();

            ObjectServicePortClient client = AbstractCmisServicesHelper.createObjectServiceClient();

            assertObjectPropertiesUpdating(repositoryId, new DocumentCreatorStrategy(
                                               AbstractCmisServicesHelper.generateObjectName(AbstractCmisServicesHelper
                              .TEXT_DOCUMENT_POSTFIX), rootFolderId, AbstractCmisServicesHelper.TEXT_DOCUMENT_MIMETYPE,
                                  enumVersioningState.major, AbstractCmisServicesHelper.getTestDocumentContentEntry()),
                                                                                   client, NAME_FOR_DOCUMENT_RENAMING);

            assertObjectPropertiesUpdating(repositoryId, new FolderCreatorStrategy(
                                            AbstractCmisServicesHelper.generateObjectName(null), rootFolderId), client,
                                                                                             NAME_FOR_FOLDER_RENAMING);
        }

        [Test]
        public void testContentStreamReceiving() {

            string documentId = AbstractCmisServicesHelper.createAndAssertDocument(
                                                                         AbstractCmisServicesHelper.generateObjectName(
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_POSTFIX),
                                                                   AbstractCmisServicesHelper.getAndAssertRootFolder(),
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_MIMETYPE,
                                                             AbstractCmisServicesHelper.getTestDocumentContentEntry());

            receiveAndAssertContentStream(documentId, AbstractCmisServicesHelper.createObjectServiceClient(),
                                                             AbstractCmisServicesHelper.getTestDocumentContentEntry());

            AbstractCmisServicesHelper.deleteAndAssertDocument(documentId);
        }

        [Test]
        public void testContentStreamDeletion() {

            string documentId = AbstractCmisServicesHelper.createAndAssertDocument(
                                                                         AbstractCmisServicesHelper.generateObjectName(
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_POSTFIX),
                                                                   AbstractCmisServicesHelper.getAndAssertRootFolder(),
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_MIMETYPE,
                                                             AbstractCmisServicesHelper.getTestDocumentContentEntry());

            deleteAndAssertContentStream(documentId, AbstractCmisServicesHelper.getAndAssertRepositoryId(),
                                                               AbstractCmisServicesHelper.createObjectServiceClient());

            AbstractCmisServicesHelper.deleteAndAssertDocument(documentId);
        }

        [Test]
        public void testAllowableActionsReceving() {

            string documentId = AbstractCmisServicesHelper.createAndAssertDocument(
                                                                         AbstractCmisServicesHelper.generateObjectName(
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_POSTFIX),
                                                                   AbstractCmisServicesHelper.getAndAssertRootFolder(),
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_MIMETYPE,
                                                             AbstractCmisServicesHelper.getTestDocumentContentEntry());

            string repositoryId = AbstractCmisServicesHelper.getAndAssertRepositoryId();

            assertAdminActionsReceiving(repositoryId, documentId);

            assertGuestActionsReceiving(repositoryId, documentId);

            AbstractCmisServicesHelper.deleteAndAssertDocument(documentId);
        }

        [Test]
        public void testContentStreamSending() {

            string documentName = AbstractCmisServicesHelper.generateObjectName(
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_POSTFIX);

            string documentId = AbstractCmisServicesHelper.createAndAssertDocument(documentName,
                                                                   AbstractCmisServicesHelper.getAndAssertRootFolder(),
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_MIMETYPE,
                                                             AbstractCmisServicesHelper.getTestDocumentContentEntry());

            ObjectServicePortClient client = AbstractCmisServicesHelper.createObjectServiceClient();

            setAndAssertNewContent(documentName, documentId, client);

            AbstractCmisServicesHelper.deleteAndAssertDocument(documentId);
        }

        [Test]
        public void testObjectMoving() {

            string rootFolderId = AbstractCmisServicesHelper.getAndAssertRootFolder();

            string documentId = AbstractCmisServicesHelper.createAndAssertDocument(
                                                                         AbstractCmisServicesHelper.generateObjectName(
                                                       AbstractCmisServicesHelper.TEXT_DOCUMENT_POSTFIX), rootFolderId,
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_MIMETYPE,
                                                             AbstractCmisServicesHelper.getTestDocumentContentEntry());

            string folderId = AbstractCmisServicesHelper.createAndAssertFolder(
                                                    AbstractCmisServicesHelper.generateObjectName(null), rootFolderId);

            performAndAssertDocumentMoving(documentId, folderId);

            AbstractCmisServicesHelper.deleteAndAssertDocument(documentId);
            AbstractCmisServicesHelper.deleteAndAssertFolder(folderId, false);
        }

        [Test]
        public void testNotEmptyFolderDeletion() {

            string folderId = AbstractCmisServicesHelper.createAndAssertFolder(
                                                                   AbstractCmisServicesHelper.generateObjectName(null),
                                                                  AbstractCmisServicesHelper.getAndAssertRootFolder());

            string documentId = AbstractCmisServicesHelper.createAndAssertDocument(
                                                                         AbstractCmisServicesHelper.generateObjectName(
                                                           AbstractCmisServicesHelper.TEXT_DOCUMENT_POSTFIX), folderId,
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_MIMETYPE,
                                                             AbstractCmisServicesHelper.getTestDocumentContentEntry());

            AbstractCmisServicesHelper.deleteAndAssertFolder(folderId, true);
            AbstractCmisServicesHelper.deleteAndAssertDocument(documentId);
            AbstractCmisServicesHelper.deleteAndAssertFolder(folderId, false);
        }

        [Test]
        public void testTreeCreationAndDeletion() {

            AbstractCmisServicesHelper.deleteAndAssertHierarchy(
                                                  AbstractCmisServicesHelper.createAndAssertFileFolderHierarchy(
                                                    TEST_HIERARCHY_DEPTH, MINIMAL_TEST_OBJECTS_LEVEL_AMOUNT, 0, true));
        }

        private static void assertObjectPropertiesUpdating(string repositoryId,
                                       CmisManipulationsStrategy<string> objectCreator, ObjectServicePortClient client,
                                                                                    string expectedChangedObjectName) {

            string objectId = objectCreator.performManipulations();

            assertPropertiesUpdating(repositoryId, client, objectId, objectCreator.getName(),
                                                                                            expectedChangedObjectName);

            performObjectDeletionAndAssertion(objectCreator, objectId);
        }

        private static void performObjectDeletionAndAssertion(CmisManipulationsStrategy<string> objectCreator,
                                                                                                     string objectId) {

            if (objectCreator is DocumentCreatorStrategy) {
                AbstractCmisServicesHelper.deleteAndAssertDocument(objectId);
            }

            if(objectCreator is FolderCreatorStrategy) {
               AbstractCmisServicesHelper.deleteAndAssertFolder(objectId, false);
            }
        }

        private static void assertPropertiesUpdating(string repositoryId, ObjectServicePortClient client,
                                                    string objectId, string expectedName, string expectedChangedName) {

            Assert.AreEqual(expectedName, AbstractCmisServicesHelper.searchPropertyAndGetValueByName(
                                           AbstractCmisServicesHelper.getObjectProperties(objectId, true).Items,
                                                                     AbstractCmisServicesHelper.NAME_PROPERTY));

            client.updateProperties(repositoryId, ref objectId, null,
                                    AbstractCmisServicesHelper.createCmisObjectProperties(expectedChangedName));

            Assert.AreEqual(expectedChangedName, AbstractCmisServicesHelper.searchPropertyAndGetValueByName(
                                                  AbstractCmisServicesHelper.getObjectProperties(objectId, true).Items,
                                                                            AbstractCmisServicesHelper.NAME_PROPERTY));
        }

        private static void receiveAndAssertContentStream(string documentId, ObjectServicePortClient client,
                                                                                              byte[] expectedContent) {

            cmisContentStreamType result = client.getContentStream(
                                                    AbstractCmisServicesHelper.getAndAssertRepositoryId(), documentId);

            Assert.AreEqual(expectedContent.Length, result.stream.Length);
            Assert.AreEqual(Encoding.GetEncoding(AbstractCmisServicesHelper.DEFAULT_ENCODING).
                                           GetString(expectedContent), Encoding.GetEncoding(AbstractCmisServicesHelper.
                                                                           DEFAULT_ENCODING).GetString(result.stream));
        }

        private void deleteAndAssertContentStream(string documentId, string repositoryId,
                                                                                      ObjectServicePortClient client) {

            receiveAndAssertContentStream(documentId, client,
                                                             AbstractCmisServicesHelper.getTestDocumentContentEntry());

            client.deleteContentStream(repositoryId, documentId);

            try {
                client.getContentStream(repositoryId, documentId);

                Assert.Fail("Content stream was not deleted");
            } catch(Exception) {
            }
        }

        private static void assertAdminActionsReceiving(string repositoryId, string documentId) {

            ObjectServicePortClient client = AbstractCmisServicesHelper.createObjectServiceClient();

            cmisAllowableActionsType response = client.getAllowableActions(repositoryId, documentId);

            Assert.IsNotNull(response);
            Assert.IsTrue(response.canDelete);
            Assert.IsTrue(response.canDeleteContent);
            Assert.IsTrue(response.canCheckout);
            Assert.IsTrue(response.canUpdateProperties);
            Assert.IsFalse(response.canGetAllVersions);
            Assert.IsFalse(response.canCheckin);
            Assert.IsFalse(response.canCancelCheckout);
        }

        private static void assertGuestActionsReceiving(string repositoryId, string documentId) {

            ObjectServicePortClient client = AbstractCmisServicesHelper.createObjectServiceClient(
                                                                       DEFAULT_GUEST_USERNAME, DEFAULT_GUEST_PASSWORD);

            cmisAllowableActionsType response = client.getAllowableActions(repositoryId, documentId);

            Assert.IsNotNull(response);
            Assert.IsFalse(response.canDelete);
            Assert.IsFalse(response.canDeleteContent);
            Assert.IsFalse(response.canCheckout);
            Assert.IsFalse(response.canUpdateProperties);
            Assert.IsFalse(response.canGetAllVersions);
            Assert.IsFalse(response.canCheckin);
            Assert.IsFalse(response.canCancelCheckout);
        }

        private static void setAndAssertNewContent(string documentName, string documentId,
                                                                                      ObjectServicePortClient client) {

            try {
                client.setContentStream(AbstractCmisServicesHelper.getAndAssertRepositoryId(), ref documentId,
                                        true, AbstractCmisServicesHelper.createCmisDocumentContent(documentName,
                                                              AbstractCmisServicesHelper.TEXT_DOCUMENT_MIMETYPE,
                                               Encoding.GetEncoding(AbstractCmisServicesHelper.DEFAULT_ENCODING)
                                                                                   .GetBytes(REPLACED_CONTENT_ENTRY)));

                receiveAndAssertContentStream(documentId, client, Encoding.GetEncoding(
                                        AbstractCmisServicesHelper.DEFAULT_ENCODING).GetBytes(REPLACED_CONTENT_ENTRY));
            } catch(Exception e) {
                Assert.Fail(e.Message);
            }
        }

        private static void performAndAssertDocumentMoving(string documentId, string folderId) {

            ObjectServicePortClient client = AbstractCmisServicesHelper.createObjectServiceClient();

            client.moveObject(AbstractCmisServicesHelper.getAndAssertRepositoryId(), documentId, folderId, null);

            AbstractCmisServicesHelper.assertDocumentParents(documentId, new string[] {folderId});
        }
    }
}
