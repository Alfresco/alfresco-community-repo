//get the sample user connection
var connectionId = "cmis-sample-connection"
var cmisConnection = cmis.getConnection(connectionId)
if (cmisConnection == null) {
	// if no connection exists, talk to the local server
	cmisConnection = cmis.getConnection()
}

// get CMIS session
var cmisSession = cmisConnection.getSession();
model.cmisSession = cmisSession;

// locate file attributes
for each (field in formdata.fields) {
	if (field.name == "name") {
		name = field.value;
	} else if (field.name == "file" && field.isFile) {
		filename = field.filename;
		content = field.content;
	} else if (field.name == "path") {
		path = field.value;
	}
}

// ensure mandatory file attributes have been located
if (filename == undefined || content == undefined) {
	status.code = 400;
	status.message = "Uploaded file cannot be located in request";
	status.redirect = true;
} else {
	var folder = cmisSession.getObjectByPath(path);

	if (folder != undefined && folder.baseType.id == "cmis:folder") {
		var properties = cmis.createMap()
		properties["cmis:name"] = String(name);
		properties["cmis:objectTypeId"] = "cmis:document";

		var contentStream = cmis.createContentStream(filename, content);

		model.doc = folder.createDocument(properties, contentStream, null);
	} else {
		status.code = 400;
		status.message = "Error: Upload folder does not exist";
		status.redirect = true;
	}
}
