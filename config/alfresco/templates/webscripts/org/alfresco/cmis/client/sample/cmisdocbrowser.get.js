// get the sample user connection
var connectionId = "cmis-sample-connection"
var cmisConnection = cmis.getConnection(connectionId)
if (cmisConnection == null) {
	// if no connection exists, talk to the local server
	cmisConnection = cmis.getConnection()
}

// get CMIS session
var cmisSession = cmisConnection.getSession();

model.cmisSession = cmisSession;
model.baseTypes = cmisSession.getTypeChildren(null, false).iterator();


if (args.id == null)
{
  model.folder = cmisSession.getRootFolder();
}
else
{
  model.folder = cmisSession.getObject(args.id);
}

model.connection = cmisConnection;

var operationContext = cmisSession.createOperationContext();
operationContext.setRenditionFilterString("cmis:thumbnail");
model.children = model.folder.getChildren(operationContext).iterator();