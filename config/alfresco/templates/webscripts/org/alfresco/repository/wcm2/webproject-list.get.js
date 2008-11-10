var dmType = "{http://www.alfresco.org/model/wcmappmodel/1.0}webfolder";	
var query = "TYPE:\"" + dmType + "\"";
var results = search.luceneSearch(query);
	
model.results = results;
