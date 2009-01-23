using System.Text;
using NUnit.Framework;
using WcfTestClient.VersioningService;

namespace WcfCmisWSTests {
    ///
    /// author: Dmitry Velichkevich
    ///
    [TestFixture]
    public class VersioningServiceClientTest {
        private const int EXPECTED_VERSIONS_AMOUNT = 3;
        private const int DIFFERENT_VERSIONS_AMOUNT = EXPECTED_VERSIONS_AMOUNT - 1;

        private const string CHECKIN_COMMENT = "Checked In with NUnit";
        private const string CHECKIN_CONTENT_TEXT = "Check In test result entry";

        private const string COMMON_VERSION_SUFFIX = "/1.";

        private const string EXPECTED_VERSION_NUMBER = COMMON_VERSION_SUFFIX + "1";

        private static byte[] checkinContentEntry = Encoding.GetEncoding(
                                           AbstractCmisServicesHelper.DEFAULT_ENCODING).GetBytes(CHECKIN_CONTENT_TEXT);

        [Test]
        public void testDocumentCheckoutingAndCheckoutCanceling() {

            string documentId = AbstractCmisServicesHelper.createAndAssertDocument(
                                                                         AbstractCmisServicesHelper.generateObjectName(
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_POSTFIX),
                                                                   AbstractCmisServicesHelper.getAndAssertRootFolder(),
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_MIMETYPE,
                                                             AbstractCmisServicesHelper.getTestDocumentContentEntry());

            AbstractCmisServicesHelper.cancelCheckOutAndAssert(
                                                             AbstractCmisServicesHelper.checkOutAndAssert(documentId));

            AbstractCmisServicesHelper.deleteAndAssertDocument(documentId);
        }

        [Test]
        public void testLatestVersionPropertiesReceiving() {

            string documentName = AbstractCmisServicesHelper.generateObjectName(
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_POSTFIX);

            string documentId = AbstractCmisServicesHelper.createAndAssertDocument(documentName,
                                                                   AbstractCmisServicesHelper.getAndAssertRootFolder(),
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_MIMETYPE,
                                                             AbstractCmisServicesHelper.getTestDocumentContentEntry());

            AbstractCmisServicesHelper.getAndAssertLatestVersionProperties(documentId, documentName, null, false);

            AbstractCmisServicesHelper.deleteAndAssertDocument(documentId);

        }

        [Test]
        public void testAllVersionsDeletion() {

            string documentName = AbstractCmisServicesHelper.generateObjectName(
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_POSTFIX);

            string documentId = AbstractCmisServicesHelper.createAndAssertDocument(documentName,
                                                                   AbstractCmisServicesHelper.getAndAssertRootFolder(),
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_MIMETYPE,
                                                             AbstractCmisServicesHelper.getTestDocumentContentEntry());

            assertAllVersionsDeletion(documentName, documentId,
                                                           AbstractCmisServicesHelper.createVersioningServiceClient());

            AbstractCmisServicesHelper.deleteAndAssertDocument(documentId);
        }

        [Test]
        public void testDocumentCheckIning() {

            string documentName = AbstractCmisServicesHelper.generateObjectName(
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_POSTFIX);

            string documentId = AbstractCmisServicesHelper.createAndAssertDocument(documentName,
                                                                   AbstractCmisServicesHelper.getAndAssertRootFolder(),
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_MIMETYPE,
                                                            WcfTestClient.ObjectService.enumVersioningState.checkedout,
                                                             AbstractCmisServicesHelper.getTestDocumentContentEntry());

            documentId = assertCheckIning(documentName, documentId);

            AbstractCmisServicesHelper.deleteAndAssertDocument(documentId);
        }

        [Test]
        public void testAllVersionsReceiving() {

            string documentName = AbstractCmisServicesHelper.generateObjectName(
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_POSTFIX);

            string documentId = AbstractCmisServicesHelper.createAndAssertDocument(documentName,
                                                                   AbstractCmisServicesHelper.getAndAssertRootFolder(),
                                                                     AbstractCmisServicesHelper.TEXT_DOCUMENT_MIMETYPE,
                                                            WcfTestClient.ObjectService.enumVersioningState.checkedout,
                                                             AbstractCmisServicesHelper.getTestDocumentContentEntry());

            documentId = assertCheckIning(documentName, documentId);

            assertVersionsReceiving(documentId, AbstractCmisServicesHelper.createVersioningServiceClient());

            AbstractCmisServicesHelper.deleteAndAssertDocument(documentId);
        }

        private static void assertAllVersionsDeletion(string documentName, string documentId,
                                                                                  VersioningServicePortClient client) {

            client.deleteAllVersions(AbstractCmisServicesHelper.getAndAssertRepositoryId(), documentId);

            AbstractCmisServicesHelper.getAndAssertLatestVersionProperties(documentId, documentName, null, true);
        }

        private static string assertCheckIning(string documentName, string documentId) {

            AbstractCmisServicesHelper.assertCheckedOutDocument(documentId, true);

            AbstractCmisServicesHelper.createVersioningServiceClient().checkIn(
                                          AbstractCmisServicesHelper.getAndAssertRepositoryId(), ref documentId, false,
                                                   null, createVersioningContentStream(documentName), CHECKIN_COMMENT);

            AbstractCmisServicesHelper.getAndAssertLatestVersionProperties(documentId, documentName,
                                                                                       EXPECTED_VERSION_NUMBER, false);

            return documentId;
        }

        private static void assertVersionsReceiving(string documentId, VersioningServicePortClient client) {

            cmisObjectType[] response = client.getAllVersions(AbstractCmisServicesHelper.getAndAssertRepositoryId(),
                                             documentId, AbstractCmisServicesHelper.ANY_PROPERTY_FILTER, false, false);

            Assert.IsNotNull(response);
            Assert.AreEqual(EXPECTED_VERSIONS_AMOUNT, response.Length);

            assertOlderVersions(response);
        }

        private static void assertOlderVersions(cmisObjectType[] response) {

            Assert.IsTrue(((string)AbstractCmisServicesHelper.searchPropertyAndGetValueByName(
                                                     AbstractCmisServicesHelper.VERSIONING_CONVERTER.convertProperties(
                                                              response[0].properties.Items), AbstractCmisServicesHelper
                                                    .OBJECT_IDENTIFIER_PROPERTY)).EndsWith(COMMON_VERSION_SUFFIX + 1));

            for (int currentVersionNumber = (DIFFERENT_VERSIONS_AMOUNT - 1); currentVersionNumber > 0;
                                                                                              currentVersionNumber--) {
                Assert.IsTrue(((string)AbstractCmisServicesHelper.searchPropertyAndGetValueByName(
                                                     AbstractCmisServicesHelper.VERSIONING_CONVERTER.convertProperties(
                                          response[DIFFERENT_VERSIONS_AMOUNT - currentVersionNumber].properties.Items),
                                                      AbstractCmisServicesHelper.OBJECT_IDENTIFIER_PROPERTY)).EndsWith(
                                                                        COMMON_VERSION_SUFFIX + currentVersionNumber));
            }
        }

        private static cmisContentStreamType createVersioningContentStream(string documentName) {

            cmisContentStreamType result = new cmisContentStreamType();
            result.filename = documentName;
            result.length = checkinContentEntry.Length.ToString();
            result.mimeType = AbstractCmisServicesHelper.TEXT_DOCUMENT_MIMETYPE;
            result.stream = checkinContentEntry;

            return result;
        }
    }
}
