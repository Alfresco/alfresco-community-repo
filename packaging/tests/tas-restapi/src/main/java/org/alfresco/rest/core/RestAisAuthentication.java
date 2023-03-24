/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.core;

import static org.alfresco.utility.report.log.Step.STEP;

import org.alfresco.utility.data.AisToken;
import org.alfresco.utility.data.auth.DataAIS;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RestAisAuthentication
{
    public static String STEP_PREFIX = "RestAisAuthProvider:";
    public static String USER_DISABLED_MSG = "Account disabled";

    @Autowired
    private DataAIS dataAIS;

    /**
     *
     * Get the AIS access token for the specified user model.
     *
     * @param userModel
     * @return
     */
    public String getAisAuthenticationToken(UserModel userModel)
    {
        STEP(String.format("%s Retrieving AIS authentication.", STEP_PREFIX));
        AisToken aisToken = getAisAccessToken(userModel);

        return aisToken.getToken();
    }

    /**
     * Check if the Alfresco Identity Service is enabled 
     * @return True if Alfresco Identity Service is enabled (the identity service URL is not null or empty)
     */
    public Boolean isAisAuthenticationEnabled()
    {
        return dataAIS.isEnabled() ? true : false;
    }

    /**
     * Returns a valid access token for valid user credentials in userModel. An
     * invalid access token is returned for invalid user credentials, which can
     * be used for tests involving non existing or unauthorized users.
     * 
     * @param userModel
     * @return
     */
    private AisToken getAisAccessToken(UserModel userModel)
    {
        String badToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJUazFPZ2JqVlo1UEw2bmtsNWFvTUlacTZ4cW9PZzc5WGtzdnJTTUcxLUFZIn0.eyJqdGkiOiI3NTVkMGZiOS03NzI5LTQ1NzYtYWM4Ny1hZWZjZWNiZDE0ZGEiLCJleHAiOjE1NTM2MjQ1NDgsIm5iZiI6MCwiaWF0IjoxNTUzNjI0MjQ4LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0L2F1dGgvcmVhbG1zL2FsZnJlc2NvIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6Ijk4NDE0Njg4LTUwMDUtNDVmOS05YTVjLTlkMDRlODMyYTNkMiIsInR5cCI6IkJlYXJlciIsImF6cCI6ImFsZnJlc2NvIiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiNjJlN2U5YzktZmFlNS00N2RhLTk5MDItMTZjYTJhZWUwMWMwIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwOi8vbG9jYWxob3N0KiIsImh0dHBzOi8vbG9jYWxob3N0KiJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoidXNlci12eGlrcXd3cG5jYmpzeHgifQ.PeLGCNCzj-P2m0knwUU9Vfx4dzLLQER9IdV7GyLel9LRN-3J9nh7GBDRQsyDJ0pqhObQyMg4V3wSsrsXRQ6gKhmUyDemmD-w1YMC2a2HKX6GlxsTEF_f1K_R15lIQOawNVErlWjZWORJGCvCYZOJ99SOmeOC6PGY79zLL94MMnf6dXcegePPMOKG-59eNjBkOylTipYebvM40nbbKrS5vzNHQlvUh4ALFeBoMSKGnLSjQd06Dj4SWojG0p1BrxurqDjW0zz6pQlEAm4vcWApRZ6qBLZcMH8adYix07zCDb87GOn1pmfEBWpwd3BEgC_LLu06guaCPHC9tpeIaDTHLg";
        String badRefreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJmM2YyMjhjYS1jMzg5LTQ5MGUtOGU1Zi02YWI1MmJhZDVjZGEifQ.eyJqdGkiOiIyNmExZWNhYy00Zjk0LTQwYzctYjJjNS04NTlhZmQ3NjBiYWMiLCJleHAiOjE1NTM2MjYwNDgsIm5iZiI6MCwiaWF0IjoxNTUzNjI0MjQ4LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0L2F1dGgvcmVhbG1zL2FsZnJlc2NvIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdC9hdXRoL3JlYWxtcy9hbGZyZXNjbyIsInN1YiI6Ijk4NDE0Njg4LTUwMDUtNDVmOS05YTVjLTlkMDRlODMyYTNkMiIsInR5cCI6IlJlZnJlc2giLCJhenAiOiJhbGZyZXNjbyIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjYyZTdlOWM5LWZhZTUtNDdkYS05OTAyLTE2Y2EyYWVlMDFjMCIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIn0.lRBJQc7tj0rk7JBC0zpM0dDdZgDKjm9wcxP8nzLnXe4";

        AisToken aisToken;
        try
        {
            // Attempt to get an access token for userModel from AIS
            aisToken = dataAIS.perform().getAccessToken(userModel);
        }
        catch (AssertionError e)
        {
            // Trying to authenticate with invalid user credentials or disabled
            // user so return an invalid access token
            if (e.getMessage().contains("invalid_grant"))
            {
                STEP(String.format("%s User disabled or invalid user credentials were provided %s:%s. Using invalid token for request.", STEP_PREFIX,
                        userModel.getUsername(), userModel.getPassword()));
                aisToken = new AisToken(badToken, badRefreshToken, System.currentTimeMillis(), 300000);
            }
            else
            {
                throw e;
            }
        }
        return aisToken;
    }

}
