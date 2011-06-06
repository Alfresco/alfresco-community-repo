var connectionId = "cmis-sample-connection"

// get the current user connection, returns null if it is the default connection
var cmisConnection = cmis.getConnection(connectionId)

var error = null
var type = args["type"]

if (type == null) {
	error = "Connection type not set!"
} else if (type == "local") {
	// local Alfresco server
	
	// close current connection and get the local connection
	if (cmisConnection != null) {
		cmisConnection.close()
	}
	cmisConnection = cmis.getConnection()

} else if (type == "config") {
	// OpenCMIS configuration

	var config = args["config"]
	if (config == null) {
		error = "No configuration!"
	} else {
		var parameters = cmis.createMap()
        var lines = config.split("\r\n")
		
		// parse the configuration
		for (idx in lines) {
			var line = lines[idx]
			var x = line.indexOf("=")
			if (x > 0) {
				parameters[line.substring(0, x)] = line.substring(x + 1)
			}
		}

		// set up a server definition
		var serverDefinition = cmis.createServerDefinition("customserver", parameters)

		// close current connection and open a new one
		if (cmisConnection != null) {
			cmisConnection.close()
		}
		cmisConnection = cmis.createUserConnection(serverDefinition, connectionId)
	}

} else if (type == "server") {
	// preconfigured server

	var server = args["server"]
	if (server == null) {
		error = "No server provided!"
	} else {
		// find the server definition
		var serverDefinition = cmis.getServerDefinition(server)
		if (serverDefinition == null) {
			error = "Server definition not found!"
		} else {
			// if username and password are set, create a new server definition
			if (args["username"] != null && args["username"].length > 0) {
				if (args["repositoryid"] != null && args["repositoryid"].length > 0) {
					serverDefinition = cmis.createServerDefinition(serverDefinition, args["username"], args["password"], args["repositoryid"])
				} else {
					serverDefinition = cmis.createServerDefinition(serverDefinition, args["username"], args["password"])
				}
			}
			
			// close current connection and open a new one
			if (cmisConnection != null) {
				cmisConnection.close()
			}
			cmisConnection = cmis.createUserConnection(serverDefinition, connectionId)
		}
	}

} else {
	error = "Unknown type!"
}

// handle errors
if (error != null) {
	model.error = error

	if (cmisConnection == null) {
		cmisConnection = cmis.getConnection()
	}
}

model.cmisSession = cmisConnection.getSession()
model.rootFolderChildren = model.cmisSession.getRootFolder().getChildren().iterator();
model.baseTypes = model.cmisSession.getTypeChildren(null, false).iterator();
