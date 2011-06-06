// get the sample user connection
var connectionId = "cmis-sample-connection"
var cmisConnection = cmis.getConnection(connectionId)
if (cmisConnection == null) {
	// if no connection exists, talk to the local server
	cmisConnection = cmis.getConnection()
}

model.connection = cmisConnection;
var cmisSession = cmisConnection.getSession()

// get arguments
var path = args["path"]

if (path != null) {
	// if the path is set, get the document
	var doc = cmisSession.getObjectByPath(path)
	if (!cmis.isDocument(doc)) {
		status.code = 404;
		status.message = "Object is not a document!";
		status.redirect = true;
	} else {
		model.doc = doc
	}
}