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

/*
 * Unit Test for Dictionaryervice
 * @author Saravanan Sellathurai
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
	//TODO individual check of all elements
	public void testGetPropertyDef() throws Exception
	{
		Response response = sendRequest(new GetRequest("/api/classes/cm_auditable/property/cm_created"), 200);
		assertEquals(200,response.getStatus());
		JSONObject result = new JSONObject(response.getContentAsString());
		assertEquals("cm:created", result.get("name"));
		assertEquals("Created Date", result.get("title"));
		assertEquals("Created Date", result.get("description"));
		assertEquals("Date and Time", result.get("dataType"));
		assertEquals("false", result.get("multiValued"));
		assertEquals("true", result.get("mandatory"));
		assertEquals("true", result.get("enforced"));
		assertEquals("true", result.get("protected"));
		assertEquals("true", result.get("indexed"));
		assertEquals("true", result.get("indexedAtomically"));
		response = sendRequest(new GetRequest("/api/classes/cm_hi/property/cm_welcome"), 404);
		assertEquals(404,response.getStatus());
		
	}
	
	//TODO individual check of all elements
	public void testGetPropertyDefs() throws Exception
	{
		Response response = sendRequest(new GetRequest("/api/classes/cm_auditable/properties"), 200);
		assertEquals(200,response.getStatus());
		response = sendRequest(new GetRequest("/api/classes/cm_welcome/properties"), 404);
		assertEquals(404,response.getStatus());
	}
	
	//TODO individual check of all elements
	public void testGetClassDetail() throws Exception
	{
		GetRequest req = new GetRequest(URL_SITES + "/cm_auditable");
		Response response = sendRequest(req, 200);
		assertEquals(200,response.getStatus());
		response = sendRequest(new GetRequest("/api/classes/cmsara_hi"), 404);
		assertEquals(404,response.getStatus());
	}
	
	//TODO individual check of all elements
	public void testGetAssociatoinDef() throws Exception
	{
		GetRequest req = new GetRequest(URL_SITES + "/cm_person/association/cm_avatar");
		Response response = sendRequest(req, 200);
		JSONObject result = new JSONObject(response.getContentAsString());
		assertEquals("cm:avatar", result.get("name"));
		assertEquals(false, result.get("isChild"));
		assertEquals(false, result.get("protected"));
		assertEquals(200,response.getStatus());
		response = sendRequest(new GetRequest(URL_SITES +"/cm_personalbe/association/cms_avatarsara"), 404);
		assertEquals(404,response.getStatus());
	}
	
	//TODO individual check of all elements
	public void testGetAssociatoinDefs() throws Exception
	{
		GetRequest req = new GetRequest(URL_SITES + "/cm_person/associations");
		Response response = sendRequest(req, 200);
		assertEquals(200,response.getStatus());
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
