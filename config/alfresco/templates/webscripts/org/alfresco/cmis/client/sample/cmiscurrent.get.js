// get the sample user connection
var connectionId = "cmis-sample-connection"
var cmisConnection = cmis.getConnection(connectionId)
if (cmisConnection == null) {
	// if no connection exists, talk to the local server
	cmisConnection = cmis.getConnection()
}

// get CMIS session
var cmisSession = cmisConnection.getSession();

model.connection = cmisConnection;
model.cmisSession = cmisSession;
model.rootFolderChildren = cmisSession.getRootFolder().getChildren().iterator();
model.baseTypes = cmisSession.getTypeChildren(null, false).iterator();