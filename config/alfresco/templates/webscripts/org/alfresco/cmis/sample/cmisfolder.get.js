script:
{
    var id = (url.extension == "") ? "/api/path/workspace/SpacesStore/Company Home" : "/api/" + url.extension;
	var conn = remote.connect("alfresco");
	
	// retrieve folder
	var folderResult = conn.get(stringUtils.urlEncodeComponent(id));
    model.folder = atom.toEntry(folderResult.response);
    
    // retrieve folder children
    var childrenId = model.folder.getLinks("cmis-children").get(0).href;
    var childrenResult = conn.get(childrenId);
    model.children = atom.toFeed(childrenResult.response);
}
