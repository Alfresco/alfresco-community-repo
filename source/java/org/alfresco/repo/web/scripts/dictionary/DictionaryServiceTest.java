/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.dictionary;

import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.web.scripts.TestWebScriptServer.GetRequest;
import org.alfresco.web.scripts.TestWebScriptServer.Response;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * 
 * Unit Test for Dictionaryervice REST API
 * @author Saravanan Sellathurai
 * 
 */

public class DictionaryServiceTest extends BaseWebScriptTest
{
	private static final String URL_SITES = "/api/classes";
	
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
	
	private boolean validatePropertyDef(JSONObject result) throws Exception
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
		//assertEquals check is yet to be made on constraints
		assertEquals("/api/classes/cm_auditable/property/cm_created", result.get("url"));
		return true;
	}
	
	private boolean validateAssociationDef(JSONObject result) throws Exception
	{
		assertEquals("cm:avatar", result.get("name"));
		assertEquals("", result.get("title"));
		assertEquals("", result.get("description"));
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
		
		return true;
	}
	
	private boolean validateTypeClass(JSONObject result) throws Exception
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
		
		assertEquals("cm:name", result.getJSONObject("properties").getJSONObject("cm:name").get("name"));
		assertEquals("Name", result.getJSONObject("properties").getJSONObject("cm:name").get("title"));
		assertEquals("/api/classes/cm_cmobject/property/cm_name", result.getJSONObject("properties").getJSONObject("cm:name").get("url"));
		
		assertEquals(0, result.getJSONObject("associations").length());
		assertEquals(0, result.getJSONObject("childassociations").length());
		
		assertEquals("/api/classes/cm_cmobject", result.get("url"));
		
		return true;
	}
	
	private boolean validateAspectClass(JSONObject result) throws Exception
	{
		//cm:thumbnailed is of type =>aspect
		assertEquals("cm:thumbnailed", result.get("name"));
		assertEquals(true , result.get("isAspect"));
		assertEquals("Thumbnailed", result.get("title"));
		assertEquals("", result.get("description"));
		assertEquals(0, result.getJSONObject("parent").length());
		assertEquals(0, result.getJSONObject("defaultAspects").length());
		
		assertEquals("cm:automaticUpdate", result.getJSONObject("properties").getJSONObject("cm:automaticUpdate").get("name"));
		assertEquals("Automatic Update", result.getJSONObject("properties").getJSONObject("cm:automaticUpdate").get("title"));
		assertEquals("/api/classes/cm_thumbnailed/property/cm_automaticUpdate", result.getJSONObject("properties").getJSONObject("cm:automaticUpdate").get("url"));
		
		assertEquals(0, result.getJSONObject("associations").length());
		
		assertEquals("cm:thumbnails", result.getJSONObject("childassociations").getJSONObject("cm:thumbnails").get("name"));
		assertEquals("/api/classes/cm_thumbnailed/childassociation/cm_thumbnails", result.getJSONObject("childassociations").getJSONObject("cm:thumbnails").get("url"));
		
		return true;
	}

	
	public void testGetPropertyDef() throws Exception
	{
		Response response = sendRequest(new GetRequest("/api/classes/cm_auditable/property/cm_created"), 200);
		assertEquals(200,response.getStatus());
		JSONObject result = new JSONObject(response.getContentAsString());
		assertEquals(true, validatePropertyDef(result));
		// TODO Constraint data has to be added... yet to do
		assertEquals(13, result.length());
		response = sendRequest(new GetRequest("/api/classes/cm_hi/property/cm_welcome"), 404);
		assertEquals(404,response.getStatus());
	}
	
	public void testGetPropertyDefs() throws Exception
	{
		Response response = sendRequest(new GetRequest("/api/classes/cm_auditable/properties"), 200);
		JSONArray result = new JSONArray(response.getContentAsString());
		for(int i=0; i<result.length(); i++)
		{
			if(result.getJSONObject(i).get("name").equals("cm:created")) 
				assertEquals(true, validatePropertyDef(result.getJSONObject(i)));
		}
		assertEquals(200,response.getStatus());
		assertEquals(5, result.length());
		response = sendRequest(new GetRequest("/api/classes/cm_welcome/properties"), 404);
		assertEquals(404,response.getStatus());
	}
	
	public void testGetClassDetail() throws Exception
	{
		GetRequest req = new GetRequest(URL_SITES + "/cm_thumbnailed");
		Response response = sendRequest(req, 200);
		JSONObject result = new JSONObject(response.getContentAsString());
		assertEquals(200,response.getStatus());
		assertEquals(true, validateAspectClass(result));
		
		req = new GetRequest(URL_SITES + "/cm_cmobject");
		response = sendRequest(req, 200);
		result = new JSONObject(response.getContentAsString());
		assertEquals(200,response.getStatus());
		assertEquals(true, validateTypeClass(result));
		
		response = sendRequest(new GetRequest("/api/classes/cm_hi"), 404);
		assertEquals(404,response.getStatus());
	}
	
	public void testGetClassDetails() throws Exception
	{
		/**
		 *  There are seven scenarios with getting class details , all are optional fields
		 *  Classfilter   namespaceprefix   name   Returns  
		 *  1   yes				yes			 yes	single class
		 *  2   yes				yes			 no     Array of classes [returns array of classes of the particular namespaceprefix]
		 *  3   yes				no			 no     Array of classes [depends on classfilter, either type or aspect or all classes in the repo]
		 * 	4   no				no			 no		Array of classes [returns all classes of both type and aspects in the entire repository]
		 * 	5   no				yes			 yes	single class [returns a single class of a valid namespaceprefix:name combination]
		 * 	6   no				yes			 no		Array of classes [returns an array of all aspects and types under particular namespaceprefix]
		 * 	7   no				no			 yes    Array of all classes [since name alone doesn't makes any meaning]
		 * 
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
		boolean flag = false;
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:thumbnailed")) flag = validateAspectClass(result.getJSONObject(i));
		}
		assertEquals(true , flag);
		assertEquals(200,response.getStatus());
		
		//check for a type under cm with name cmobject [case-type:1]
		arguments.clear();
		arguments.put("cf", "type");
		arguments.put("nsp", "cm");
		arguments.put("n", "cmobject");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		flag = false;
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject")) flag = validateTypeClass(result.getJSONObject(i));
		}
		assertEquals(true , flag);
		assertEquals(200,response.getStatus());
		
		//check for a type under cm with name cmobject [case-type:1]
		arguments.clear();
		arguments.put("cf", "all");
		arguments.put("nsp", "cm");
		arguments.put("n", "cmobject");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		flag = false;
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject")) flag = validateTypeClass(result.getJSONObject(i));
		}
		assertEquals(true , flag);
		assertEquals(200,response.getStatus());
		
		//check for a type under cm without options=>name, namespaceprefix [case-type:2]
		arguments.clear();
		arguments.put("cf", "type");
		arguments.put("nsp", "cm");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		flag = false;
		// the above result has all the types under cm, so now check for the presence type cm:cmobject in the array of classes of all types
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject")) flag = validateTypeClass(result.getJSONObject(i));
		}
		assertEquals(true , flag);
		assertEquals(200,response.getStatus());
		
		//check for a aspect under cm without options=>name [case-type:2]
		arguments.clear();
		arguments.put("cf", "aspect");
		arguments.put("nsp", "cm");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		flag = false;
		// the above result has all the aspects under cm, so now check for the presence aspect cm:thumnailed in the array of classes of all aspects
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:thumbnailed")) flag = validateAspectClass(result.getJSONObject(i));
		}
		assertEquals(true , flag);
		assertEquals(200,response.getStatus());
		
		
		//check for all aspects under cm without options=>name [case-type:2]
		arguments.clear();
		arguments.put("cf", "all");
		arguments.put("nsp", "cm");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		flag = false;
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:thumbnailed")) flag = validateAspectClass(result.getJSONObject(i));
		}
		assertEquals(true , flag);
		assertEquals(200,response.getStatus());
		
		//check for all type under cm without options=>name, namespaceprefix [case-type:3]
		arguments.clear();
		arguments.put("cf", "type");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		flag = false;
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject")) flag = validateTypeClass(result.getJSONObject(i));
		}
		assertEquals(true , flag);
		assertEquals(200,response.getStatus());
		
		//check for all aspect under cm without options=>name, namespaceprefix [case-type:3]
		arguments.clear();
		arguments.put("cf", "aspect");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		flag = false;
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:thumbnailed")) flag = validateAspectClass(result.getJSONObject(i));
		}
		assertEquals(true , flag);
		assertEquals(200,response.getStatus());
		
		//check for all aspect and type in the repository when nothing is given [case-type:4]
		arguments.clear();
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		flag = false;
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:thumbnailed")) flag = validateAspectClass(result.getJSONObject(i));
		}
		assertEquals(true , flag);
		assertEquals(200,response.getStatus());
		
		//check for all aspect and type in the repository when nothing is given [case-type:4]
		arguments.clear();
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		flag = false;
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject")) flag = validateTypeClass(result.getJSONObject(i));
		}
		assertEquals(true , flag);
		assertEquals(200,response.getStatus());
		
		//check for a classname [namespaceprefix:name => cm:cmobject] without classfilter option [case-type:5]
		arguments.clear();
		arguments.put("nsp", "cm");
		arguments.put("n", "cmobject");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		flag = false;
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject")) flag = validateTypeClass(result.getJSONObject(i));
		}
		assertEquals(true , flag);
		assertEquals(200,response.getStatus());
		
		//check for a classname [namespaceprefix:name => cm:thumbnailed] without classfilter option [case-type:5]
		arguments.clear();
		arguments.put("nsp", "cm");
		arguments.put("n", "cmobject");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		flag = false;
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:thumbnailed")) flag = validateAspectClass(result.getJSONObject(i));
		}
		assertEquals(true , flag);
		assertEquals(200,response.getStatus());
		
		//check for a namespaceprefix [namespaceprefix => cm] without classfilter and name option [case-type:6]
		arguments.clear();
		arguments.put("nsp", "cm");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		flag = false;
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:thumbnailed")) flag = validateAspectClass(result.getJSONObject(i));
		}
		assertEquals(true , flag);
		assertEquals(200,response.getStatus());
		
		//check for a namespaceprefix [namespaceprefix => cm] without classfilter and name option [case-type:6]
		arguments.clear();
		arguments.put("nsp", "cm");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		flag = false;
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject")) flag = validateTypeClass(result.getJSONObject(i));
		}
		assertEquals(true , flag);
		assertEquals(200,response.getStatus());
		
		//check for a namespaceprefix [namespaceprefix => cm] without classfilter and name option [case-type:6]
		arguments.clear();
		arguments.put("nsp", "cm");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		flag = false;
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject")) flag = validateTypeClass(result.getJSONObject(i));
		}
		assertEquals(true , flag);
		assertEquals(200,response.getStatus());
		
		//check for a name alone without classfilter and namespaceprefix option [case-type:7]
		arguments.clear();
		arguments.put("n", "cmobject");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		flag = false;
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:cmobject")) flag = validateTypeClass(result.getJSONObject(i));
		}
		assertEquals(true , flag);
		assertEquals(200,response.getStatus());
		
		//check for a name alone without classfilter and namespaceprefix option [case-type:7]
		arguments.clear();
		arguments.put("n", "thumbnailed");
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		result = new JSONArray(response.getContentAsString());
		flag = false;
		for(int i=0; i<result.length(); i++)
		{
			if (result.getJSONObject(i).get("name").equals("cm:thumbnailed")) flag = validateAspectClass(result.getJSONObject(i));
		}
		assertEquals(true , flag);
		assertEquals(200,response.getStatus());
		
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
		
		//check for all data under cm without option=>name
		arguments.clear();
		arguments.put("cf", "all");
		arguments.put("nsp", "cmbb");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
		//check for all dictionary data  without option=>name and option=>namespaceprefix
		arguments.clear();
		arguments.put("cf", "a£&llsara");
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
		
		//check with an invalid namespaceprefix
		arguments.clear();
		arguments.put("nsp", "cmsd");
		req.setArgs(arguments);
		response = sendRequest(req, 404);
		assertEquals(404,response.getStatus());
		
		//check for all types dictionary data  without option=>name and option=>namespaceprefix and option=>classfilter
		arguments.clear();
		req.setArgs(arguments);
		response = sendRequest(req, 200);
		assertEquals(200,response.getStatus());
	}
	
	

	public void testGetAssociatoinDef() throws Exception
	{
		GetRequest req = new GetRequest(URL_SITES + "/cm_person/association/cm_avatar");
		Response response = sendRequest(req, 200);
		JSONObject result = new JSONObject(response.getContentAsString());
		assertEquals(true, validateAssociationDef(result));
		response = sendRequest(new GetRequest(URL_SITES +"/cm_personalbe/association/cms_avatarsara"), 404);
		assertEquals(404,response.getStatus());
	}
	
	public void testGetAssociatoinDefs() throws Exception
	{
		GetRequest req = new GetRequest(URL_SITES + "/cm_person/associations");
		Response response = sendRequest(req, 200);
		JSONArray result = new JSONArray(response.getContentAsString());
		boolean flag = true;
		for(int i=0; i<result.length(); i++)
		{
			if(result.getJSONObject(i).get("name").equals("cm:avatar")) 
				flag = validateAssociationDef(result.getJSONObject(i));
		}
		assertEquals(true, flag);
		assertEquals(1,result.length());
		response = sendRequest(new GetRequest(URL_SITES +"/cmsa_personalbe/associations"), 404);
		assertEquals(404,response.getStatus());
	}
	
	//TODO individual check of all elements
	public void testGetChildAssociatoinDef() throws Exception
	{
		GetRequest req = new GetRequest(URL_SITES + "/cm_thumbnailed/childassociation/cm_thumbnails");
		Response response = sendRequest(req, 200);
		assertEquals(200,response.getStatus());
		response = sendRequest(new GetRequest(URL_SITES +"/cm_thumbnailed:sara/childassociation/cm:thumbnails"), 404);
		assertEquals(404,response.getStatus());
		
	}
	
	//TODO individual check of all elements
	public void testGetChildAssociatoinDefs() throws Exception
	{
		GetRequest req = new GetRequest(URL_SITES + "/cm_thumbnailed/childassociations");
		Response response = sendRequest(req, 200);
		assertEquals(200,response.getStatus());
		response = sendRequest(new GetRequest(URL_SITES +"/cm_thumbnailed:sara/childassociations"), 404);
		assertEquals(404,response.getStatus());
	}

}
