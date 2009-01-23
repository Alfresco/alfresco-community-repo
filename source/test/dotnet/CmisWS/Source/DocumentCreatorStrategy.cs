using WcfTestClient.ObjectService;

namespace WcfCmisWSTests {
    ///
    /// author: Dmitry Velichkevich
    ///
    public class DocumentCreatorStrategy: CmisManipulationsStrategy<string> {
        private string objectName;
        private string objectParent;
        private string objectMimeType;
        private enumVersioningState versioningState;
        private byte[] objectContentEntry;

        public DocumentCreatorStrategy(string objectName, string objectParent, string objectMimeType,
                                                  enumVersioningState versioningState, byte[] objectContentEntry) {

            this.objectName = objectName;
            this.objectParent = objectParent;
            this.objectMimeType = objectMimeType;
            this.versioningState = versioningState;
            this.objectContentEntry = objectContentEntry;
        }

        string CmisManipulationsStrategy<string>.getName() {

            return objectName;
        }

        string CmisManipulationsStrategy<string>.performManipulations() {

            return AbstractCmisServicesHelper.createObjectServiceClient().createDocument(
                       AbstractCmisServicesHelper.getAndAssertRepositoryId(), AbstractCmisServicesHelper.DOCUMENT_TYPE,
                                       AbstractCmisServicesHelper.createCmisObjectProperties(objectName), objectParent,
                                       AbstractCmisServicesHelper.createCmisDocumentContent(objectName, objectMimeType,
                                                                                 objectContentEntry), versioningState);
        }
    }
}
