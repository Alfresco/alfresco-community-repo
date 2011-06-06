// get the local or default remote connection
var	cmisConnection = null

// get the sample user connection
cmisConnection = cmis.getConnection("cmis-sample-connection")
if(cmisConnection == null)
{
	// if no connection exists, talk to the local server
	cmisConnection = cmis.getConnection()
}

model.cmisSession = cmisConnection.getSession()
model.cmisServers = cmis.getServerDefinitions()