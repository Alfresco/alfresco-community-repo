/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.web.scripts.dictionary;

import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Unit test for Dictionary REST API
 * @author Saravanan Sellathurai
 */

public class DictionaryRestApiTest extends BaseWebScriptTest
{
	private static final String URL_SITES = "/api/classes";
	private static final String URL_PROPERTIES = "/api/properties";
	
	@Override
	protected void setUp() throws Exception 
	{ 
		super.setUp();
	}
	
	@Override
	protected void tearDown() throws Exception
    {
		super.tearDown();
    }
	
	private void validatePropertyDef(JSONObject result) throws Exception
	{
		assertEquals("cm:created", result.get("name"));
		assertEquals("Created Date", result.get("title"));
		assertEquals("Created Date", result.get("description"));
		assertEquals("d:datetime", result.get("dataType"));
		assertEquals(false, result.get("multiValued"));
		assertEquals(true, result.get("mandatory"));
		assertEquals(true, result.get("enforced"));
		assertEquals(true, result.get("protected"));
		assertEquals(true, result.get("indexed"));
		assertEquals(true, result.get("indexedAtomically"));
		assertEquals("/api/property/cm_created", result.get("url"));
		
	}
	
	private void validateChildAssociation(JSONObject result) throws Exception
	{
		assertEquals("wca:formworkflowdefaults", result.get("name"));
		assertEquals("", result.get("title"));
		assertEquals("", result.get("description"));
		assertEquals(true, result.get("isChildAssociation"));
		assertEquals(false, result.get("protected"));
		
		assertEquals("wca:form", result.getJSONObject("source").get("class"));
		assertEquals(false, result.getJSONObject("source").get("mandatory"));
		assertEquals(false, result.getJSONObject("source").get("many"));
		
		assertEquals("wca:workflowdefaults", result.getJSONObject("target").get("class"));
		assertEquals(false, result.getJSONObject("target").get("mandatory"));
		assertEquals(false, result.getJSONObject("target").get("many"));
		
		assertEquals("/api/classes/wca_form/association/wca_formworkflowdefaults", result.get("url"));
	}
	
	private void validateAssociation(JSONObject result) throws Exception
	{
		assertEquals("wca:renderingenginetemplates", result.get("name"));
		assertEquals("Form Data Renderers", result.get("title"));
		assertEquals("", result.get("description"));
		assertEquals(false, result.get("isChildAssociation"));
		assertEquals(false, result.get("protected"));
		
		assertEquals("wca:form", result.getJSONObject("source").get("class"));
		assertEquals("wca:capture", result.getJSONObject("source").get("role"));
		assertEquals(false, result.getJSONObject("source").get("mandatory"));
		assertEquals(false, result.getJSONObject("source").get("many"));
		
		assertEquals("wca:renderingenginetemplate", result.getJSONObject("target").get("class"));
		assertEquals("wca:presentation", result.getJSONObject("target").get("role"));
		assertEquals(false, result.getJSONObject("target").get("mandatory"));
		assertEquals(true, result.getJSONObject("target").get("many"));
		
		assertEquals("/api/classes/wca_form/association/wca_renderingenginetemplates", result.get("url"));
	}
	private void validateAssociationDef(JSONObject result) throws Exception
	{
		assertEquals("cm:avatar", result.get("name"));
		assertEquals("Avatar", result.get("title"));
		assertEquals("The person's avatar image", result.get("description"));
		assertEquals(false, result.get("isChildAssociation"));
		assertEquals(false, result.get("protected"));
		
		assertEquals("cm:person", result.getJSONObject("source").get("class"));
		assertEquals("cm:avatarOf", result.getJSONObject("source").get("role"));
		assertEquals(false, result.getJSONObject("source").get("mandatory"));
		assertEquals(false, result.getJSONObject("source").get("many"));
		
		assertEquals("cm:content", result.getJSONObject("target").get("class"));
		assertEquals("cm:hasAvatar", result.getJSONObject("target").get("role"));
		assertEquals(false, result.getJSONObject("target").get("mandatory"));
		assertEquals(false, result.getJSONObject("target").get("many"));
		
		assertEquals("/api/classes/cm_person/association/cm_avatar", result.get("url"));
	}
	
	private void validateTypeClass(JSONObject result) throws Exception
	{
		//cm:cmobject is of type =>type
		assertEquals("cm:cmobject", result.get("name"));
		assertEquals(false , result.get("isAspect"));
		assertEquals("Object", result.get("title"));
		assertEquals("", result.get("description"));
		
		assertEquals("sys:base", result.getJSONObject("parent").get("name"));
		assertEquals("base", result.getJSONObject("parent").get("title"));
		assertEquals("/api/classes/sys_base", result.getJSONObject("parent").get("url"));
		
		assertEquals("sys:referenceable", result.getJSONObject("defaultAspects").getJSONObject("sys:referenceable").get("name"));
		assertEquals("Referenceable", result.getJSONObject("defaultAspects").getJSONObject("sys:referenceable").get("title"));
		assertEquals("/api/classes/cm_cmobject/property/sys_referenceable", result.getJSONObject("defaultAspects").getJSONObject("sys:referenceable").get("url"));

		assertEquals("cm:auditable", result.getJSONObject("defaultAspects").getJSONObject("cm:auditable").get("name"));
		assertEquals("Auditable", result.getJSONObject("defaultAspects").getJSONObject("cm:auditable").get("title"));
		assertEquals("/api/classes/cm_cmobject/property/cm_auditable", result.getJSONObject("defaultAspects").getJSONObject("cm:auditable").get("url"));
		
		//assertEquals("cm:name", result.getJSONObject("properties").getJSONObject("cm:name").get("name"));
		//assertEquals("Name", result.getJSONObject("properties").getJSONObject("cm:name").get("title"));
		//assertEquals("/api/classes/cm_cmobject/property/cm_name", result.getJSONObject("properties").getJSONObject("cm:name").get("url"));
		
		//assertEquals(, result.getJSONObject("associations").length());
		//assertEquals(0, result.getJSONObject("childassociations").length());
		
		assertEquals("/api/classes/cm_cmobject", result.get("url"));
		
	}
	
	private void validateAspectClass(JSONObject result) throws Exception
	{
		//cm:thumbnailed is of type =>aspect
		assertEquals("cm:thumbnailed", result.get("name"));
		assertEquals(true , result.get("isAspect"));
		assertEquals("Thumbnailed", result.get("title"));
		assertEquals("", result.get("description"));
		assertEquals(0, result.getJSONObject("defaultAspects").length());
		
		if (result.getJSONObject("properties").has("cm:automaticUpdate") == true)
		{
    		assertEquals("cm:automaticUpdate", result.getJSONObject("properties").getJSONObject("cm:automaticUpdate").get("name"));
    		assertEquals("Automatic Update", result.getJSONObject("properties").getJSONObject("cm:automaticUpdate").get("title"));
    		assertEquals("/api/classes/cm_thumbnailed/property/cm_automaticUpdate", result.getJSONObject("properties").getJSONObject("cm:automaticUpdate").get("url"));
		}
		
		//assertEquals(2, result.getJSONObject("associations").length());
	}

	public void testGetPropertyDef() throws Exception
	{
		Response response = sendRequest(new GetRequest("/api/classes/cm_auditable/property/cm_created"), 200);
		assertEquals(200,response.getStatus());
		JSONObject result = new JSONObject(response.getContentAsString());
		validatePropertyDef(result);
		
		assertEquals(result.length()>0, true);
		response = sendRequest(new GetRequest("/api/classes/cm_hi/property/cm_welcome"), 404);
		assertEquals(404,response.getStatus());
		
		//invalid property name , returns a null JsonObject as such a property doesn't exist under cm_auditable
		response = sendRequest(new GetRequest("/api/classes/cm_auditable/property/cm_welcome"), 200);
		result = new JSONObject(response.getContentAsString());
		assertEquals(0, result.length());
		assertEquals(200,response.getStatus());
	}
	
	public void testGetPropertyDefs() throws Exception
	{
		//validate for a particular property cm_created in the class cm_auditable
		GetRequest req = new GetRequest(URL_SITES + "/cm_auditable/properties");
		Map< String, String > arguments = new HashMap< String, String >();
		arguments.put("nsp", "cm");
		req.setArgs(arguments);
		Response response = sendRequest(req, 200);
		assertEquals(200,response.getStatus());
		
		JSONArray result = new JSONArray(response.getContentAsString());
        assertEquals(200,response.getStatus());
        assertEquals(5, result.length());
		
		//validate with no parameter => returns an array of property definitions
		arguments.clear();
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(200,response.getStatus());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if(result.getJSONObject(i).get("name").equals("cm:created")) 
			{
				validatePropertyDef(result.getJSONObject(i));
			}
		}
		
		// test /api/properties
		req = new GetRequest(URL_PROPERTIES);
		response = sendRequest(req, 200);
		assertEquals(200, response.getStatus());		
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
        for (int i = 0; i < result.length(); i++)
        {
            if(result.getJSONObject(i).get("name").equals("cm:created")) 
            {
                validatePropertyDef(result.getJSONObject(i));
            }
            
            String title = "";
            if (result.getJSONObject(i).has("title") == true)
            {
                title = result.getJSONObject(i).getString("title");
            }
            System.out.println(title + " - " + result.getJSONObject(i).getString("name"));
      }
        
//        System.out.println("/n/n");
        
        // test /api/properties?name=cm:name&name=cm:title&name=cm:description
        req = new GetRequest(URL_PROPERTIES + "?name=cm:name&name=cm:title&name=cm:description");
        response = sendRequest(req, 200);
        assertEquals(200, response.getStatus());        
        result = new JSONArray(response.getContentAsString());
        assertEquals(3, result.length());
//        for (int i = 0; i < result.length(); i++)
//        {
//            String title = "";
//            if (result.getJSONObject(i).has("title") == true)
//            {
//                title = result.getJSONObject(i).getString("title");
//            }
//            System.out.println(title + " - " + result.getJSONObject(i).getString("name"));
//        }
		
		
	}
	
	public void testGetClassDetail() throws Exception
	{
		GetRequest req = new GetRequest(URL_SITES + "/cm_thumbnailed");
		Response response = sendRequest(req, 200);
		JSONObject result = new JSONObject(response.getContentAsString());
		assertEquals(result.length()>0, true);
		assertEquals(200,response.getStatus());
		validateAspectClass(result);
		
		req = new GetRequest(URL_SITES + "/cm_cmobject");
		response = sendRequest(req, 200);
		result = new JSONObject(response.getContentAsString());
		assertEquals(result.length()>0, true);
		assertEquals(200,response.getStatus());
		validateTypeClass(result);
		
		response = sendRequest(new GetRequest("/api/classes/cm_hi"), 404);
		assertEquals(404,response.getStatus());
	}
	

	public void testGetClassDetails() throws Exception
	{
		/**
		 *  There are eight scenarios with getting class details , all are optional fields
		 *  Classfilter   namespaceprefix   name   Returns  
		 *  1   yes				yes			 yes	single class
		 *  2   yes				yes			 no     Array of classes [returns array of classes of the particular namespaceprefix]
		 *  3   yes				no			 no     Array of classes [depends on classfilter, either type or aspect or all classes in the repo]
		 * 	4   no				no			 no		Array of classes [returns all classes of both type and aspects in the entire repository]
		 * 	5   no				yes			 yes	single class [returns a single class of a valid namespaceprefix:name combination]
		 * 	6   no				yes			 no		Array of classes [returns an array of all aspects and types under particular namespaceprefix]
		 * 	7   no				no			 yes    404 error [since name alone doesn't makes any meaning]
		 *  8   yes 		    no 			 yes	404 error [since name alone doesn't makes any meaning]
		 * 	Test cases are provided for all the above scenarios	
		 */
		
		
		//check for a aspect under cm with name thumbnailes [case-type:1]
		GetRequest req = new GetRequest(URL_SITES);
		Map< String, String > arguments = new HashMap< String, String >();
		arguments.put("cf", "aspect");
		arguments.put("nsp", "cm");
		arguments.put("n", "thumbnailed");
		req.setArgs(arguments);
		Response response = sendRequest(req, 200);
		JSONArray result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:thumbnailed"))
			{
				validateAspectClass(result.getJSONObject(i));
			}
		}
		//check array length
		assertEquals(200,response.getStatus());
		
		//check for a type under cm with name cmobject [case-type:1]
		arguments.clear();
		arguments.put("cf", "type");
		arguments.put("nsp", "cm");
		arguments.put("n", "cmobject");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject")) 
			{
				validateTypeClass(result.getJSONObject(i));
			}
		}
		assertEquals(200,response.getStatus());
		
		//check for a type under cm with name cmobject [case-type:1]
		arguments.clear();
		arguments.put("cf", "all");
		arguments.put("nsp", "cm");
		arguments.put("n", "cmobject");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject")) 
			{
				validateTypeClass(result.getJSONObject(i));
			}
		}
		assertEquals(200,response.getStatus());
		
		//check for a type under cm without options=>name, namespaceprefix [case-type:2]
		arguments.clear();
		arguments.put("cf", "type");
		arguments.put("nsp", "cm");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		// the above result has all the types under cm, so now check for the presence type cm:cmobject in the array of classes of all types
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject"))
			{
				validateTypeClass(result.getJSONObject(i));
			}
		}
		assertEquals(200,response.getStatus());
		
		//check for a aspect under cm without options=>name [case-type:2]
		arguments.clear();
		arguments.put("cf", "aspect");
		arguments.put("nsp", "cm");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		// the above result has all the aspects under cm, so now check for the presence aspect cm:thumnailed in the array of classes of all aspects
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:thumbnailed")) 
			{
				validateAspectClass(result.getJSONObject(i));
			}
		}
		
		//check for all aspects under cm without options=>name [case-type:2]
		arguments.clear();
		arguments.put("cf", "all");
		arguments.put("nsp", "cm");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:thumbnailed"))
			{
				validateAspectClass(result.getJSONObject(i));
			}	
		}
		assertEquals(200,response.getStatus());
		
		//check for all type under cm without options=>name, namespaceprefix [case-type:3]
		arguments.clear();
		arguments.put("cf", "type");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject"))
			{
			    System.out.println(result.getJSONObject(i).toString());
				validateTypeClass(result.getJSONObject(i));
			}
		}
		assertEquals(200,response.getStatus());
		
		//check for all aspect under cm without options=>name, namespaceprefix [case-type:3]
		arguments.clear();
		arguments.put("cf", "aspect");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:thumbnailed")) 
			{
				validateAspectClass(result.getJSONObject(i));
			}
		}
		assertEquals(200,response.getStatus());
		
		//check for all aspect and type in the repository when nothing is given [case-type:4]
		arguments.clear();
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:thumbnailed"))
			{
				validateAspectClass(result.getJSONObject(i));
			}
		}
		assertEquals(200,response.getStatus());
		
		//check for all aspect and type in the repository when nothing is given [case-type:4]
		arguments.clear();
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject"))
			{
				validateTypeClass(result.getJSONObject(i));
			}
		}
		assertEquals(200,response.getStatus());
		
		//check for a classname [namespaceprefix:name => cm:cmobject] without classfilter option [case-type:5]
		arguments.clear();
		arguments.put("nsp", "cm");
		arguments.put("n", "cmobject");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject"))
			{
				validateTypeClass(result.getJSONObject(i));
			}
		}
		assertEquals(200,response.getStatus());
		
		//check for a classname [namespaceprefix:name => cm:thumbnailed] without classfilter option [case-type:5]
		arguments.clear();
		arguments.put("nsp", "cm");
		arguments.put("n", "thumbnailed");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:thumbnailed"))
			{
				validateAspectClass(result.getJSONObject(i));
			}
		}
		assertEquals(200,response.getStatus());
		
		//check for a namespaceprefix [namespaceprefix => cm] without classfilter and name option [case-type:6]
		arguments.clear();
		arguments.put("nsp", "cm");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:thumbnailed"))
			{
				validateAspectClass(result.getJSONObject(i));
			}
		}
		assertEquals(200,response.getStatus());
		
		//check for a namespaceprefix [namespaceprefix => cm] without classfilter and name option [case-type:6]
		arguments.clear();
		arguments.put("nsp", "cm");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject")) 
			{
				validateTypeClass(result.getJSONObject(i));
			}
		}
		assertEquals(200,response.getStatus());
		
		//check for a namespaceprefix [namespaceprefix => cm] without classfilter and name option [case-type:6]
		arguments.clear();
		arguments.put("nsp", "cm");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject")) 
			{
				validateTypeClass(result.getJSONObject(i));
			}
		}
		assertEquals(200,response.getStatus());
		
		//check for a name alone without classfilter and namespaceprefix option [case-type:7] => returns 404 error
		arguments.clear();
		arguments.put("n", "cmobject");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
		
		//check for a type under cm with name cmobject and no namespaceprefix [case-type:8] => returns 404 error
		arguments.clear();
		arguments.put("cf", "type");
		arguments.put("n", "cmobject");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
		// Test with wrong data
		//check for all aspects under cm without option=>name
		arguments.clear();
		arguments.put("cf", "aspects");
		arguments.put("nsp", "cm");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
		//check for all types under cm without option=>name
		arguments.clear();
		arguments.put("cf", "types");
		arguments.put("nsp", "cmd");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
		//check for all dictionary data  without option=>name and option=>namespaceprefix
		arguments.clear();
		arguments.put("cf", "a�&llsara");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
		//check for all aspect dictionary data  without option=>name and option=>namespaceprefix
		arguments.clear();
		arguments.put("cf", "aspectb");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
		//check for all types dictionary data  without option=>name and option=>namespaceprefix
		arguments.clear();
		arguments.put("cf", "typesa");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
		//check for all types dictionary data  without option=>name and option=>namespaceprefix and option=>classfilter
		arguments.clear();
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		assertEquals(200,response.getStatus());
	}

	public void testSubClassDetails() throws Exception
	{
		GetRequest req = new GetRequest(URL_SITES + "/sys_base/subclasses");
		Map< String, String > arguments = new HashMap< String, String >();
		arguments.put("r", "true");
		req.setArgs(arguments);
		Response response = sendRequest(req, 200);
		assertEquals(200,response.getStatus());
		response = sendRequest(req, 200);
		JSONArray result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject"))
			{
				validateTypeClass(result.getJSONObject(i));
			}
		}
		assertEquals(200,response.getStatus());
		
		arguments.clear();
		arguments.put("r", "false");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		assertEquals(200,response.getStatus());
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject"))
			{
				validateTypeClass(result.getJSONObject(i));
			}
		}
		assertEquals(200,response.getStatus());
		
		assertEquals(200,response.getStatus());
		arguments.clear();
		arguments.put("r", "false");
		arguments.put("nsp", "cm");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		assertEquals(200,response.getStatus());
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject"))
			{
				validateTypeClass(result.getJSONObject(i));
			}
		}
		assertEquals(200,response.getStatus());
		
		arguments.clear();
		arguments.put("r", "true");
		arguments.put("nsp", "cm");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		assertEquals(200,response.getStatus());
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject"))
			{
				validateTypeClass(result.getJSONObject(i));
			}
		}
		assertEquals(200,response.getStatus());
		
		// data with only name along
		arguments.clear();
		arguments.put("r", "true");
		arguments.put("n", "cmobject");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
		// invalid name and namespaceprefix
		arguments.clear();
		arguments.put("r", "true");
		arguments.put("n", "dublincore"); //name and namespaceprefix are valid one , but its not present in sys_base as a sub-class
		arguments.put("nsp", "cm");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
		//invalid name and a valid namespaceprefix
		arguments.clear();
		arguments.put("r", "true");
		arguments.put("n", "dublincoresara"); //name and namespaceprefix are invalid one
		arguments.put("nsp", "cm");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
		
	}
	
	public void testGetAssociationDef() throws Exception
	{
		GetRequest req = new GetRequest(URL_SITES + "/cm_person/association/cm_avatar");
		Response response = sendRequest(req, 200);
		JSONObject result = new JSONObject(response.getContentAsString());
		assertEquals(200,response.getStatus());
		validateAssociationDef(result);
		
		req = new GetRequest(URL_SITES + "/wca_form/association/wca_formworkflowdefaults");
		response = sendRequest(req, 200);
		result = new JSONObject(response.getContentAsString());
		validateChildAssociation(result);
		assertEquals(200,response.getStatus());
		
		req = new GetRequest(URL_SITES + "/wca_form/association/wca_renderingenginetemplates");
		response = sendRequest(req, 200);
		result = new JSONObject(response.getContentAsString());
		validateAssociation(result);
		assertEquals(200,response.getStatus());
		
		//wrong data
		response = sendRequest(new GetRequest(URL_SITES +"/cm_personalbe/association/cms_avatarsara"), 404);
		assertEquals(404,response.getStatus());
		
		//ask for an invalid association under wca_form , which returns a null array 
		response = sendRequest(new GetRequest(URL_SITES +"/wca_form/association/cmsavatarsara"), 200);
		result = new JSONObject(response.getContentAsString()); // change to return 404
		assertEquals(0,result.length());
		assertEquals(200,response.getStatus());
	}
	
	public void testGetAssociationDefs() throws Exception
	{
		//validate with associationfilter=>all  and classname=>wca_form
		GetRequest req = new GetRequest(URL_SITES + "/wca_form/associations");
		Map< String, String > arguments = new HashMap< String, String >();
		arguments.put("af", "all");
		req.setArgs(arguments);
		Response response = sendRequest(req, 200);
		JSONArray result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if(result.getJSONObject(i).get("name").equals("wca:formworkflowdefaults")) 
				validateChildAssociation(result.getJSONObject(i));
		}
		for(int i=0; i<result.length(); i++)
		{
			if(result.getJSONObject(i).get("name").equals("wca:renderingenginetemplates")) 
				validateAssociation(result.getJSONObject(i));
		}
		assertEquals(200,response.getStatus());
		
		//validate with associationfilter=>child and classname=>wca_form
		arguments.clear();
		arguments.put("af", "child");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if(result.getJSONObject(i).get("name").equals("wca:formworkflowdefaults")) 
				validateChildAssociation(result.getJSONObject(i));
		}
		assertEquals(200,response.getStatus());
		
		//validate with associationfilter=>general(that means an association and not child) and classname=>wca_form
		arguments.clear();
		arguments.put("af", "general");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if(result.getJSONObject(i).get("name").equals("wca:renderingenginetemplates")) 
				validateAssociation(result.getJSONObject(i));
		}
		assertEquals(200,response.getStatus());
		
		//look for association wca_renderingenginetemplates in the class wca_form => returns a single valid class
		arguments.clear();
		arguments.put("af", "general");
		arguments.put("nsp", "wca");
		arguments.put("n", "renderingenginetemplates");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if(result.getJSONObject(i).get("name").equals("wca:renderingenginetemplates")) 
				validateAssociation(result.getJSONObject(i));
		}
		assertEquals(200,response.getStatus());
		
		//look for childassociation wca_formworkflowdefaults in the class wca_form =>returns a single valid class
		arguments.clear();
		arguments.put("af", "child");
		arguments.put("nsp", "wca");
		arguments.put("n", "formworkflowdefaults");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if(result.getJSONObject(i).get("name").equals("wca:formworkflowdefaults")) 
				validateChildAssociation(result.getJSONObject(i));
		}
		assertEquals(200,response.getStatus());
		
		//look for details on wca_formworkflowdefaults in the class wca_form , with no classfilter
		arguments.clear();
		arguments.put("nsp", "wca");
		arguments.put("n", "formworkflowdefaults");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(result.length()>0, true);
		for(int i=0; i<result.length(); i++)
		{
			if(result.getJSONObject(i).get("name").equals("wca:formworkflowdefaults")) 
				validateChildAssociation(result.getJSONObject(i));
		}
		assertEquals(200,response.getStatus());
		
		// wca_formworkflowdefaults has a child_assoc relation with wca_form , but ask for general association, this then returns a null array 
		arguments.clear();
		arguments.put("af", "general");
		arguments.put("nsp", "wca");
		arguments.put("n", "formworkflowdefaults");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(0,result.length());
		assertEquals(200,response.getStatus());
		
		// wca_renderingenginetemplates has a general association relation with wca_form , but ask for child association, this then returns a null array 
		arguments.clear();
		arguments.put("af", "child");
		arguments.put("nsp", "wca");
		arguments.put("n", "renderingenginetemplates");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		assertEquals(0,result.length());
		assertEquals(200,response.getStatus());
		
		//look for childassociation  in the class wca_form , with no name parameter =>both name and namespaceprefix are needed
		arguments.clear();
		arguments.put("af", "child");
		arguments.put("nsp", "wca");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
		arguments.clear();
		arguments.put("af", "child");
		arguments.put("n", "renderingenginetemplates");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
		arguments.clear();
		arguments.put("af", "general");
		arguments.put("n", "formworkflowdefaults");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
		//look for associations (excluding child assocs)  in the class wca_form , with no name parameter 
		arguments.clear();
		arguments.put("af", "general");
		arguments.put("nsp", "wca");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
		//wrong data
		response = sendRequest(new GetRequest(URL_SITES +"/cmsa_personalbe/associations"), 404);
		assertEquals(404,response.getStatus());
		
		//ask for a child-association which is actually not a valid child of classname - wca_form 
		arguments.clear();
		arguments.put("af", "child");
		arguments.put("nsp", "wca");
		arguments.put("n", "renderingenginetemplates");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		assertEquals(200,response.getStatus());
		
		arguments.clear();
		arguments.put("af", "general");
		arguments.put("nsp", "wca"); // invalid namespaceprefix => should be of class-type wca
		arguments.put("n", "renderingenginetemplates");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		assertEquals(200,response.getStatus());
		
		//data without name parameter
		arguments.clear();
		arguments.put("nsp", "cm");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
		//data with invalid association in wca_form
		arguments.clear();
		arguments.put("nsp", "wca");
		arguments.put("n", "dublincore");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
		//data with invalid association in wca_form
		arguments.clear();
		arguments.put("nsp", "cm");
		arguments.put("n", "hiwelcome");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
		//data with invalid class in wca_form
		arguments.clear();
		arguments.put("nsp", "wca");
		arguments.put("n", "dublincore");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
	}
	
}
