// get the sample user connection
var connectionId = "cmis-sample-connection"
var cmisConnection = cmis.getConnection(connectionId)
if (cmisConnection == null) {
	// if no connection exists, talk to the local server
	cmisConnection = cmis.getConnection()
}

var cmisSession = cmisConnection.getSession()

// get arguments
var path = args["path"]
var name = args["name"]

if (path != null && name != null)
{
  // get path folder
  var parentFolder = cmisSession.getObjectByPath(path)

  // set up properties
  var properties = cmis.createMap()
  properties["cmis:name"] = name
  properties["cmis:objectTypeId"] = "cmis:folder"

  // create folder
  model.folder = parentFolder.createFolder(properties)
}