
/**
 * Get list of wcm web projects
 * @return the wcm web projects
 */ 

function main() 
{
	var service = webprojects;	
	
	var userFilter = args["userName"];
	
	var result;
	
	if(userFilter != null)
	{
		result = service.listWebProjects(userFilter);
	} 
	else
	{
		result = service.listWebProjects();
	}
	
	model.webprojects = result;	
}

main()
