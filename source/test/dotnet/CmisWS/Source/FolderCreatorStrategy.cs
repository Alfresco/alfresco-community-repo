namespace WcfCmisWSTests {
    ///
    /// author: Dmitry Velichkevich
    ///
    public class FolderCreatorStrategy: CmisManipulationsStrategy<string> {
        private string objectName;
        private string objectParent;

        string CmisManipulationsStrategy<string>.getName() {

            return objectName;
        }

        public FolderCreatorStrategy(string objectName, string objectParent) {

            this.objectName = objectName;
            this.objectParent = objectParent;
        }

        string CmisManipulationsStrategy<string>.performManipulations() {

            return AbstractCmisServicesHelper.createObjectServiceClient().createFolder(
                         AbstractCmisServicesHelper.getAndAssertRepositoryId(), AbstractCmisServicesHelper.FOLDER_TYPE,
                                      AbstractCmisServicesHelper.createCmisObjectProperties(objectName), objectParent);
        }
    }
}
