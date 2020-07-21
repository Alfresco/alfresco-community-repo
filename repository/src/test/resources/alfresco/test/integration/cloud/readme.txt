Please do not add any contents to this package.
-----------------------------------------------


The three packages alfresco/test/integration/{community,enterprise,cloud} all exist within the "Community" project despite our
only really needing the community package. The packages exist in order to let us define spring beans which should be
available for test code in the various versions of the product.

Unfortunately, the current method of importing these spring context files
(see alfresco/test/integration/global-integration-test-context.xml) *requires* us to have the {enterprise,cloud} packages.

Please do not add any content to this directory. Please consider putting your content in a folder (at the same path) in the
relevant project instead i.e. enterprise-repo or cloud.
