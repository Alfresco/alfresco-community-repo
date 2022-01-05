/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.alfresco.util.GUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 *
 *
 * @author Mark Rogers
 */
@SuppressWarnings("unused")
public class RMCaveatConfigScriptTest extends BaseRMWebScriptTestCase
{
    protected final static String RM_LIST          = "rmc:smListTest";
    protected final static String RM_LIST_URI_ELEM = "rmc_smListTest";

    private static final String URL_RM_CONSTRAINTS = "/api/rma/admin/rmconstraints";

    public void testGetRMConstraints() throws Exception
    {
        {
            Response response = sendRequest(new GetRequest(URL_RM_CONSTRAINTS), Status.STATUS_OK);

            JSONObject top = new JSONObject(response.getContentAsString());
            System.out.println(response.getContentAsString());
            assertNotNull(top.getJSONArray("data"));
        }

        /**
         * Add a list, then get it back via the list rest script
         */
        caveatConfigService.addRMConstraint(RM_LIST, "my title", new String[0]);

        {
            Response response = sendRequest(new GetRequest(URL_RM_CONSTRAINTS), Status.STATUS_OK);
            JSONObject top = new JSONObject(response.getContentAsString());
            System.out.println(response.getContentAsString());
            JSONArray data = top.getJSONArray("data");

            boolean found = false;
            assertTrue("no data returned",  data.length() > 0);
            for(int i = 0; i < data.length(); i++)
            {
                JSONObject obj = data.getJSONObject(i);
                String name = (String)obj.getString("constraintName");
                assertNotNull("constraintName is null", name);
                String url = (String)obj.getString("url");
                assertNotNull("detail url is null", name);
                if(name.equalsIgnoreCase(RM_LIST))
                {
                    found = true;
                }

                /**
                 * vallidate the detail URL returned
                 */
                sendRequest(new GetRequest(url), Status.STATUS_OK);
            }
        }
    }

    /**
     *
     * @throws Exception
     */
    public void testGetRMConstraint() throws Exception
    {
        /**
         * Delete the list to remove any junk then recreate it.
         */
        if (caveatConfigService.getRMConstraint(RM_LIST) != null)
        {
            caveatConfigService.deleteRMConstraint(RM_LIST);
        }
        caveatConfigService.addRMConstraint(RM_LIST, "my title", new String[0]);


        createUser("fbloggs");
        createUser("jrogers");
        createUser("jdoe");


        List<String> values = new ArrayList<>();
        values.add("NOFORN");
        values.add("FGI");
        caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "fbloggs", values);
        caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "jrogers", values);
        caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "jdoe", values);

        /**
         * Positive test Get the constraint
         */
        {
            String url = URL_RM_CONSTRAINTS + "/" + RM_LIST_URI_ELEM;
            Response response = sendRequest(new GetRequest(url), Status.STATUS_OK);
            JSONObject top = new JSONObject(response.getContentAsString());

            JSONObject data = top.getJSONObject("data");
            System.out.println(response.getContentAsString());

            String constraintName = data.getString("constraintName");
            assertNotNull("constraintName is null", constraintName);
//            JSONArray allowedValues = data.getJSONArray("allowedValues");

//            assertTrue("values not correct", compare(array, allowedValues));

//            JSONArray constraintDetails = data.getJSONArray("constraintDetails");
//
//            assertTrue("details array does not contain 3 elements", constraintDetails.length() == 3);
//            for(int i =0; i < constraintDetails.length(); i++)
//            {
//                JSONObject detail = constraintDetails.getJSONObject(i);
//            }
        }

        /**
         *
         * @throws Exception
         */

        /**
         * Negative test - Attempt to get a constraint that does exist
         */
        {
            String url = URL_RM_CONSTRAINTS + "/" + "rmc_wibble";
            sendRequest(new GetRequest(url), Status.STATUS_NOT_FOUND);
        }

        deleteUser("fbloggs");
        deleteUser("jrogers");
        deleteUser("jdoe");




    }

    /**
     * Create an RM Constraint
     * @throws Exception
     */
    public void testUpdateRMConstraint() throws Exception
    {

        String constraintName = null;
        /*
         * Create a new list
         */
        {
            String title = "test Update RM Constraint title";
            JSONArray array = new JSONArray();
            array.put("LEMON");
            array.put("BANANA");
            array.put("PEACH");

            JSONObject obj = new JSONObject();
            obj.put("allowedValues", array);
            obj.put("constraintTitle", title);
            /**
             * Now do a post to create a new list
             */
            Response response = sendRequest(new PostRequest(URL_RM_CONSTRAINTS, obj.toString(), "application/json"), Status.STATUS_OK);
            JSONObject top = new JSONObject(response.getContentAsString());

            JSONObject data = top.getJSONObject("data");
            constraintName = data.getString("constraintName");
            JSONArray allowedValues = data.getJSONArray("allowedValues");
            assertTrue("values not correct", compare(array, allowedValues));

        }

        /**
          * Now update both values and title - remove BANANA, PEACH, Add APPLE.
          */

        {
            String newTitle = "this is the new title";
            JSONArray array = new JSONArray();
            array.put("LEMON");
            array.put("APPLE");

            JSONObject obj = new JSONObject();
            obj.put("allowedValues", array);
            obj.put("constraintName", constraintName);
            obj.put("constraintTitle", newTitle);

            System.out.println(obj.toString());

            /**
              * Now do a post to update list
              */
            Response response = sendRequest(new PutRequest(URL_RM_CONSTRAINTS + "/" + constraintName, obj.toString(), "application/json"), Status.STATUS_OK);
            // Check the response
            JSONObject top = new JSONObject(response.getContentAsString());
            JSONObject data = top.getJSONObject("data");

            System.out.println(response.getContentAsString());

            String url = data.getString("url");
            String constraintName2 = data.getString("constraintName");
            String constraintTitle = data.getString("constraintTitle");
            JSONArray allowedValues = data.getJSONArray("allowedValues");

            assertTrue(allowedValues.length() == 2);
            assertTrue("values not correct", compare(array, allowedValues));
            assertNotNull(url);
            assertEquals(constraintName2, constraintName);
            assertNotNull(constraintTitle);
            assertEquals("title not as expected", constraintTitle, newTitle);

            // Check that data has been persisted.
            Response resp2 = sendRequest(new GetRequest(url), Status.STATUS_OK);
            JSONObject top2 = new JSONObject(resp2.getContentAsString());
            System.out.println("Problem here");
            System.out.println(resp2.getContentAsString());
            JSONObject data2 = top2.getJSONObject("data");
            String constraintTitle2 = data2.getString("constraintTitle");
            JSONArray allowedValues2 = data2.getJSONArray("allowedValues");
            assertTrue("values not correct", compare(array, allowedValues2));
            assertTrue("allowedValues is not 2", allowedValues2.length() == 2);
            assertEquals(constraintName2, constraintName);
            assertNotNull(constraintTitle2);
            assertEquals("title not as expected", constraintTitle2, newTitle);

        }

        /**
         * Now put without allowed values
         */
        {
            String newTitle = "update with no values";

            JSONObject obj = new JSONObject();

            obj.put("constraintName", RM_LIST);
            obj.put("constraintTitle", newTitle);

            /**
              * Now do a put to update a new list
              */

                Response response = sendRequest(new PutRequest(URL_RM_CONSTRAINTS + "/" + constraintName, obj.toString(), "application/json"), Status.STATUS_OK);
                // Check the response
                JSONObject top = new JSONObject(response.getContentAsString());

                JSONObject data = top.getJSONObject("data");
                System.out.println(response.getContentAsString());

                String url = data.getString("url");
                String constraintName2 = data.getString("constraintName");
                String constraintTitle = data.getString("constraintTitle");
                JSONArray allowedValues = data.getJSONArray("allowedValues");

                assertTrue(allowedValues.length() == 2);

                assertNotNull(url);
                assertEquals(constraintName2, constraintName);
                assertNotNull(constraintTitle);
                assertEquals("title not as expected", constraintTitle, newTitle);
        }

        /**
         * Now post without constraint Title
         */
        {
            JSONArray array = new JSONArray();
            array.put("LEMON");
            array.put("APPLE");

            JSONObject obj = new JSONObject();
            obj.put("allowedValues", array);

            System.out.println(obj.toString());

            /**
              * Now do a Put to update the list - title should remain
              */

                Response response = sendRequest(new PutRequest(URL_RM_CONSTRAINTS + "/" + constraintName, obj.toString(), "application/json"), Status.STATUS_OK);
                // Check the response
                JSONObject top = new JSONObject(response.getContentAsString());

                JSONObject data = top.getJSONObject("data");
                System.out.println(response.getContentAsString());
        }

        /**
         * Add a new value (PEAR) to the list
         */
        {
            JSONArray array = new JSONArray();
            array.put("PEAR");
            array.put("LEMON");
            array.put("APPLE");

            JSONObject obj = new JSONObject();
            obj.put("allowedValues", array);

            System.out.println(obj.toString());

            Response response = sendRequest(new PutRequest(URL_RM_CONSTRAINTS + "/" + constraintName, obj.toString(), "application/json"), Status.STATUS_OK);
            // Check the response
            JSONObject top = new JSONObject(response.getContentAsString());

            JSONObject data = top.getJSONObject("data");
            System.out.println(response.getContentAsString());
        }

        /**
         * Remove a value (PEAR) from the list
         */
        {
            JSONArray array = new JSONArray();
            array.put("APPLE");
            array.put("LEMON");

            JSONObject obj = new JSONObject();
            obj.put("allowedValues", array);

            System.out.println(obj.toString());

            Response response = sendRequest(new PutRequest(URL_RM_CONSTRAINTS + "/" + constraintName, obj.toString(), "application/json"), Status.STATUS_OK);
            // Check the response
            JSONObject top = new JSONObject(response.getContentAsString());

            JSONObject data = top.getJSONObject("data");
            System.out.println(response.getContentAsString());
        }

    }


    /**
     * Create an RM Constraint
     * @throws Exception
     */
    public void testCreateRMConstraint() throws Exception
    {
        /**
         * Delete the list to remove any junk then recreate it.
         */
        //caveatConfigService.deleteRMConstraint(RM_LIST);

        /**
         * create a new list
         */
        {
            JSONArray array = new JSONArray();
            array.put("NOFORN");
            array.put("FGI");

            JSONObject obj = new JSONObject();
            obj.put("allowedValues", array);
            obj.put("constraintName", RM_LIST);
            obj.put("constraintTitle", GUID.generate());

            System.out.println(obj.toString());

            /**
             * Now do a post to create a new list
             */

            Response response = sendRequest(new PostRequest(URL_RM_CONSTRAINTS, obj.toString(), "application/json"), Status.STATUS_OK);
                // Check the response
       }

        /**
         * Now go and get the constraint
         */
        {
            String url = URL_RM_CONSTRAINTS + "/" + RM_LIST_URI_ELEM;
            Response response = sendRequest(new GetRequest(url), Status.STATUS_OK);
            JSONObject top = new JSONObject(response.getContentAsString());

            JSONObject data = top.getJSONObject("data");
            System.out.println(response.getContentAsString());

            String constraintName = data.getString("constraintName");
            assertNotNull("constraintName is null", constraintName);

//            JSONArray constraintDetails = data.getJSONArray("constraintDetails");
//
//            assertTrue("details array does not contain 3 elements", constraintDetails.length() == 3);
//            for(int i =0; i < constraintDetails.length(); i++)
//            {
//                JSONObject detail = constraintDetails.getJSONObject(i);
//            }
        }

        /**
         * Now a constraint with a generated name
         */
        {
            String title = GUID.generate();
            JSONArray array = new JSONArray();
            array.put("Red");
            array.put("Blue");
            array.put("Green");

            JSONObject obj = new JSONObject();
            obj.put("allowedValues", array);
            obj.put("constraintTitle", title);

            System.out.println(obj.toString());

            /**
             * Now do a post to create a new list
             */
            Response response = sendRequest(new PostRequest(URL_RM_CONSTRAINTS, obj.toString(), "application/json"), Status.STATUS_OK);
            JSONObject top = new JSONObject(response.getContentAsString());

            JSONObject data = top.getJSONObject("data");
            System.out.println(response.getContentAsString());

            // Check the response

            String url = data.getString("url");
            String constraintName = data.getString("constraintName");
            String constraintTitle = data.getString("constraintTitle");
            JSONArray allowedValues = data.getJSONArray("allowedValues");

            assertTrue(allowedValues.length() == 3);
            assertNotNull(url);
            assertNotNull(constraintName);
            assertNotNull(constraintTitle);
            assertEquals("title not as expected", constraintTitle, title);
            sendRequest(new GetRequest(url), Status.STATUS_OK);


       }


        /**
         * Now a constraint with an empty list of values.
         */
        {
            JSONArray array = new JSONArray();

            JSONObject obj = new JSONObject();
            obj.put("allowedValues", array);
            obj.put("constraintName", "rmc_whazoo");
            obj.put("constraintTitle", GUID.generate());

            System.out.println(obj.toString());

            /**
             * Now do a post to create a new list
             */

            Response response = sendRequest(new PostRequest(URL_RM_CONSTRAINTS, obj.toString(), "application/json"), Status.STATUS_OK);
            JSONObject top = new JSONObject(response.getContentAsString());

            JSONObject data = top.getJSONObject("data");
            System.out.println(response.getContentAsString());

            // Check the response
       }


//        /**
//         * Negative tests - duplicate list
//         */
//        {
//            JSONArray array = new JSONArray();
//            array.put("NOFORN");
//            array.put("FGI");
//
//            JSONObject obj = new JSONObject();
//            obj.put("allowedValues", array);
//            obj.put("constraintName", RM_LIST);
//            obj.put("constraintTitle", "this is the title");
//
//            System.out.println(obj.toString());
//
//            /**
//             * Now do a post to create a new list
//             */
//            Response response = sendRequest(new PostRequest(URL_RM_CONSTRAINTS, obj.toString(), "application/json"), Status.STATUS_CREATED);
//           JSONObject top = new JSONObject(response.getContentAsString());
//
//            JSONObject data = top.getJSONObject("data");
//            System.out.println(response.getContentAsString());
//
//            // Check the response
//       }


    }


    public void testGetRMConstraintValues() throws Exception
    {
        createUser("fbloggs");
        createUser("jrogers");
        createUser("jdoe");

        /**
         * Delete the list to remove any junk then recreate it.
         */
        {
            if (caveatConfigService.getRMConstraint(RM_LIST) != null)
            {
                caveatConfigService.deleteRMConstraint(RM_LIST);
            }
            caveatConfigService.addRMConstraint(RM_LIST, "my title", new String[0]);

            List<String> values = new ArrayList<>();
            values.add("NOFORN");
            values.add("FGI");
            caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "fbloggs", values);
            caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "jrogers", values);
            caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "jdoe", values);
        }

        /**
         * Positive test Get the constraint
         */
        {
            String url = URL_RM_CONSTRAINTS + "/" + RM_LIST_URI_ELEM + "/values";
            Response response = sendRequest(new GetRequest(url), Status.STATUS_OK);
            JSONObject top = new JSONObject(response.getContentAsString());

            JSONObject data = top.getJSONObject("data");
            System.out.println(response.getContentAsString());

            String constraintName = data.getString("constraintName");
            assertNotNull("constraintName is null", constraintName);
            String constraintTitle = data.getString("constraintTitle");
            assertNotNull("constraintTitle is null", constraintTitle);

            JSONArray values = data.getJSONArray("values");

            assertTrue("details array does not contain 2 elements", values.length() == 2);
            boolean fgiFound = false;
            boolean nofornFound = false;

            for(int i =0; i < values.length(); i++)
            {
                JSONObject value = values.getJSONObject(i);

                if(value.getString("valueName").equalsIgnoreCase("FGI"))
                {
                    fgiFound = true;

                }

                if(value.getString("valueName").equalsIgnoreCase("NOFORN"))
                {
                    nofornFound = true;
                }


            }
            assertTrue("fgi not found", fgiFound);
            assertTrue("noforn not found", nofornFound);
        }

        deleteUser("fbloggs");
        deleteUser("jrogers");
        deleteUser("jdoe");
    }



    /**
     * Update a value in a constraint
     * @throws Exception
     */
    public void testUpdateRMConstraintValue() throws Exception
    {
        if (caveatConfigService.getRMConstraint(RM_LIST) != null)
        {
            caveatConfigService.deleteRMConstraint(RM_LIST);
        }
        caveatConfigService.addRMConstraint(RM_LIST, "my title", new String[0]);

        /**
         * Add some data to an empty list
         */
        {
            JSONArray values = new JSONArray();

            JSONArray authorities = new JSONArray();
            authorities.put("fbloggs");
            authorities.put("jdoe");

            JSONObject valueA = new JSONObject();
            valueA.put("value", "NOFORN");
            valueA.put("authorities", authorities);

            values.put(valueA);

            JSONObject valueB = new JSONObject();
            valueB.put("value", "FGI");
            valueB.put("authorities", authorities);

            values.put(valueB);

            JSONObject obj = new JSONObject();
            obj.put("values", values);


            /**
             * Do the first update - should get back
             * NOFORN - fbloggs, jdoe
             * FGI - fbloggs, jdoe
             */
            Response response = sendRequest(new PostRequest(URL_RM_CONSTRAINTS + "/" + RM_LIST + "/values" , obj.toString(), "application/json"), Status.STATUS_OK);
            JSONObject top = new JSONObject(response.getContentAsString());

            JSONObject data = top.getJSONObject("data");
            System.out.println(response.getContentAsString());
            assertNotNull("data is null", data);

            JSONArray myValues = data.getJSONArray("values");
            assertTrue("two values not found", myValues.length() == 2);
            for(int i = 0; i < myValues.length(); i++)
            {
                JSONObject myObj = myValues.getJSONObject(i);
            }
        }

        /**
         * Add to a new value, NOCON, fbloggs, jrogers
         */
        {
            JSONArray values = new JSONArray();

            JSONArray authorities = new JSONArray();
            authorities.put("fbloggs");
            authorities.put("jrogers");

            JSONObject valueA = new JSONObject();
            valueA.put("value", "NOCON");
            valueA.put("authorities", authorities);

            values.put(valueA);


            JSONObject obj = new JSONObject();
            obj.put("values", values);


            /**
             * Add a new value - should get back
             * NOFORN - fbloggs, jdoe
             * FGI - fbloggs, jdoe
             * NOCON - fbloggs, jrogers
             */
            System.out.println(obj.toString());
            Response response = sendRequest(new PostRequest(URL_RM_CONSTRAINTS + "/" + RM_LIST + "/values" , obj.toString(), "application/json"), Status.STATUS_OK);
            JSONObject top = new JSONObject(response.getContentAsString());

            JSONObject data = top.getJSONObject("data");
            System.out.println(response.getContentAsString());
            assertNotNull("data is null", data);

            JSONArray myValues = data.getJSONArray("values");
            assertTrue("three values not found", myValues.length() == 3);
            for(int i = 0; i < myValues.length(); i++)
            {
                JSONObject myObj = myValues.getJSONObject(i);
            }
        }

        /**
         * Add to an existing value (NOFORN, jrogers)
         * should get back
         * NOFORN - fbloggs, jdoe, jrogers
         * FGI - fbloggs, jdoe
         * NOCON - fbloggs, jrogers
         */
        {
            JSONArray values = new JSONArray();

            JSONArray authorities = new JSONArray();
            authorities.put("fbloggs");
            authorities.put("jrogers");
            authorities.put("jdoe");

            JSONObject valueA = new JSONObject();
            valueA.put("value", "NOFORN");
            valueA.put("authorities", authorities);

            values.put(valueA);


            JSONObject obj = new JSONObject();
            obj.put("values", values);

            Response response = sendRequest(new PostRequest(URL_RM_CONSTRAINTS + "/" + RM_LIST + "/values" , obj.toString(), "application/json"), Status.STATUS_OK);
            JSONObject top = new JSONObject(response.getContentAsString());

            JSONObject data = top.getJSONObject("data");
            System.out.println(response.getContentAsString());
            assertNotNull("data is null", data);

            JSONArray myValues = data.getJSONArray("values");
            assertTrue("three values not found", myValues.length() == 3);
            for(int i = 0; i < myValues.length(); i++)
            {
                JSONObject myObj = myValues.getJSONObject(i);
            }
        }


        /**
         * Remove from existing value (NOCON, fbloggs)
         */
        {
            JSONArray values = new JSONArray();

            JSONArray authorities = new JSONArray();
            authorities.put("jrogers");

            JSONObject valueA = new JSONObject();
            valueA.put("value", "NOCON");
            valueA.put("authorities", authorities);

            values.put(valueA);


            JSONObject obj = new JSONObject();
            obj.put("values", values);


            /**
             * should get back
             * NOFORN - fbloggs, jdoe
             * FGI - fbloggs, jdoe
             * NOCON - jrogers
             */
            Response response = sendRequest(new PostRequest(URL_RM_CONSTRAINTS + "/" + RM_LIST + "/values" , obj.toString(), "application/json"), Status.STATUS_OK);
            JSONObject top = new JSONObject(response.getContentAsString());

            JSONObject data = top.getJSONObject("data");
            System.out.println(response.getContentAsString());
            assertNotNull("data is null", data);

            JSONArray myValues = data.getJSONArray("values");
            assertTrue("three values not found", myValues.length() == 3);
            boolean foundNOCON = false;
            boolean foundNOFORN = false;
            boolean foundFGI = false;

            for(int i = 0; i < myValues.length(); i++)
            {
                JSONObject myObj = myValues.getJSONObject(i);

                if(myObj.getString("valueName").equalsIgnoreCase("NOCON"))
                {
                    foundNOCON = true;
                }
                if(myObj.getString("valueName").equalsIgnoreCase("NOFORN"))
                {
                    foundNOFORN = true;
                }
                if(myObj.getString("valueName").equalsIgnoreCase("FGI"))
                {
                    foundFGI = true;
                }
            }

            assertTrue("not found NOCON", foundNOCON);
            assertTrue("not found NOFORN", foundNOFORN);
            assertTrue("not found FGI", foundFGI);
        }
    }


    /**
     * Delete the entire constraint
     *
     * @throws Exception
     */
    public void testDeleteRMConstraint() throws Exception
    {
        /**
         * Delete the list to remove any junk then recreate it.
         */
        if (caveatConfigService.getRMConstraint(RM_LIST) != null)
        {
            caveatConfigService.deleteRMConstraint(RM_LIST);
        }
        caveatConfigService.addRMConstraint(RM_LIST, "my title", new String[0]);

        /**
         * Now do a delete
         */
        Response response = sendRequest(new DeleteRequest(URL_RM_CONSTRAINTS + "/" + RM_LIST), Status.STATUS_OK);

        /**
         * Now delete the list that should have been deleted
         */
        // TODO NEED TO THINK ABOUT THIS BEHAVIOUR
        //{
        //    sendRequest(new DeleteRequest(URL_RM_CONSTRAINTS + "/" + RM_LIST), Status.STATUS_NOT_FOUND);
        //}

        /**
         * Negative test - delete list that does not exist
         */
        {
            sendRequest(new DeleteRequest(URL_RM_CONSTRAINTS + "/" + "rmc_wibble"), Status.STATUS_NOT_FOUND);
        }
    }

    private boolean compare(JSONArray from, JSONArray to) throws Exception
    {
        List<String> ret = new ArrayList<>();

        if(from.length() != to.length())
        {
            fail("arrays are different lengths" + from.length() +", " + to.length());
            return false;
        }

        for(int i = 0 ; i < to.length(); i++)
        {
            ret.add(to.getString(i));
        }

        for(int i = 0 ; i < from.length(); i++)
        {
            String val = from.getString(i);

            if(ret.contains(val))
            {

            }
            else
            {
               fail("Value not contained in list:" + val);
               return false;
            }
        }

        return true;
    }


    /**
     * Create an RM Constraint value
     * @throws Exception
     */
    public void testGetRMConstraintValue() throws Exception
    {

        String constraintName = null;

        /*
         * Create a new list
         */
        {
            String title = "Get Constraint Value";
            JSONArray array = new JSONArray();
            array.put("POTATO");
            array.put("CARROT");
            array.put("TURNIP");

            JSONObject obj = new JSONObject();
            obj.put("allowedValues", array);
            obj.put("constraintTitle", title);
            /**
             * Now do a post to create a new list
             */
            Response response = sendRequest(new PostRequest(URL_RM_CONSTRAINTS, obj.toString(), "application/json"), Status.STATUS_OK);
            JSONObject top = new JSONObject(response.getContentAsString());

            JSONObject data = top.getJSONObject("data");
            constraintName = data.getString("constraintName");
            JSONArray allowedValues = data.getJSONArray("allowedValues");
            assertTrue("values not correct", compare(array, allowedValues));
        }

        /**
         * Get the CARROT value
         */
        {
            String url = URL_RM_CONSTRAINTS + "/" + constraintName + "/values/" + "CARROT";
            Response response = sendRequest(new GetRequest(url), Status.STATUS_OK);
            JSONObject top = new JSONObject(response.getContentAsString());
        }

        {
            String url = URL_RM_CONSTRAINTS + "/" + constraintName + "/values/" + "ONION";
            sendRequest(new GetRequest(url), Status.STATUS_NOT_FOUND);
        }
    }
}

