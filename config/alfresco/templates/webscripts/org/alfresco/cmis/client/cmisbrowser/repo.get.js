var connectionId = url.templateArgs["conn"];

if (connectionId) {
	model.conn = cmis.getConnection(connectionId);
} else {
	model.conn = cmis.getConnection();
}

model.repoinfo = model.conn.session.repositoryInfo;
model.rootFolder = model.conn.session.rootFolder;