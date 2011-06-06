script: {
	try {
		model.conn = cmis.getConnection(args["conn"]);
		model.parent = model.conn.session.getObject(args["parent"]);

		var properties = cmis.createMap();
		properties["cmis:name"] = args["name"];
		properties["cmis:objectTypeId"] = args["objectType"];

		if (args["type"] == "document") {
			var contentStream = null;

			for each (field in formdata.fields) {
				if (field.isFile) {
					contentStream = cmis.createContentStream(field.filename, field.content);

					if (properties["cmis:name"] == null || properties["cmis:name"].length == 0) {
						properties["cmis:name"] = String(field.filename);
					}
				}
			}
			model.object = model.parent.createDocument(properties, contentStream, null);
		} else {
			model.object = model.parent.createFolder(properties);
		}
	} catch (e) {
		model.error = (e.javaException == null ? e.rhinoException.message : e.javaException.message);
	}
}
