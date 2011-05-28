// get the local or default remote connection
var	cmisConnection = cmisclient.getConnection();

// get CMIS session
var cmisSession = cmisConnection.getSession();

model.cmisSession = cmisSession;
model.rootFolderChildren = cmisSession.getRootFolder().getChildren().iterator();
model.baseTypes = cmisSession.getTypeChildren(null, false).iterator();