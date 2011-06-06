script: {
	try {
		model.conn = cmis.getConnection(args["conn"]);
		model.object = model.conn.session.getObject(args["id"]);
		model.parent = model.object.parents.get(0);
		
		if (args["confirm"] == "1") {
			if (model.object.baseType.id == "cmis:folder") {
				model.object.deleteTree(true, null, true);
			} else {
				model.object["delete"](true);
			}
		} else {
			model.needsConfirmation = true;
		}
	} catch (e) {
		model.error = (e.javaException == null ? e.rhinoException.message : e.javaException.message);
	}
}
