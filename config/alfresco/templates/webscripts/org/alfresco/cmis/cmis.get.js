var testRunner = Packages.org.alfresco.repo.cmis.rest.test.CMISTestRunner();
model.tests = testRunner.getTestNames("*");
model.cmisVersion = cmis.version;
model.querySupport = cmis.querySupport.label;
model.joinSupport = cmis.joinSupport.label;
model.pwcSearchable = cmis.pwcSearchable;
model.allVersionsSearchable = cmis.allVersionsSearchable;
