// create default connection if it doesn't exist yet
var defaultConnection = cmis.getConnection();
	
// get all connections and servers
model.connections = cmis.getUserConnections();
model.servers = cmis.getServerDefinitions();
