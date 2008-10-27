
/**
 * Post (create) wcm web project
 * @return the wcm web project ref
 */ 

function main() {

	var name = json.get("name");
	var title = json.get("title");
	var dnsName = json.get("dnsName");
	var description = json.get("description");
	
	if (name == null || name.length == 0)
	{
		status.setCode(status.STATUS_BAD_REQUEST, "property 'Name' missing when creating web site");
		return;
	}
	if (title == null || title.length == 0)
	{
		status.setCode(status.STATUS_BAD_REQUEST, "Property 'Title' missing when creating web site");
		return;
	}
	if (dnsName == null || dnsName.length == 0)
	{
		status.setCode(status.STATUS_BAD_REQUEST, "property 'dnsName' missing when creating web site");
		return;
	}
	
	if(description == null)
	{
		description = "web project" + name;
	}
	
	var webproject = webprojects.createWebProject(dnsName, name, title, description);	
	
	// Set Return value
	model.webproject = webproject;	
}

main()
