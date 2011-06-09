var serverName = args["server"];
var connectionId = "cmis-browser-" + serverName;

// get server definition
var serverDefinition = cmis.getServerDefinition(serverName);
if (serverDefinition == null) {
	status.code = 400;
	status.message = "Unknown server!";
	status.redirect = true;
}

// clean up old connection
var connection = cmis.getConnection(connectionId);
if (connection != null) {
	connection.close();
	connection = null;
}

model.serverDefinition = serverDefinition;

// connect
try {
	if (args["username"] != null && args["username"].length > 0) {
		if (args["repositoryid"] != null && args["repositoryid"].length > 0) {
			serverDefinition = cmis.createServerDefinition(serverDefinition, args["username"], args["password"], args["repositoryid"])
		} else {
			serverDefinition = cmis.createServerDefinition(serverDefinition, args["username"], args["password"])
		}
	}

	connection = cmis.createUserConnection(serverDefinition, connectionId);

	model.conn = connection;
	model.repoinfo = model.conn.session.repositoryInfo;
	model.rootFolder = model.conn.session.rootFolder;
} catch (e) {
	if(connection != null) {
		connection.close();
	}
	
	model.error = (e.javaException == null ? e.rhinoException.message : e.javaException.message);
}