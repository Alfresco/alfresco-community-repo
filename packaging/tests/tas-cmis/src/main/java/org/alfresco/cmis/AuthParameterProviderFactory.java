package org.alfresco.cmis;

import org.alfresco.utility.data.AisToken;
import org.alfresco.utility.data.auth.DataAIS;
import org.alfresco.utility.model.UserModel;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.alfresco.utility.report.log.Step.STEP;

@Service
public class AuthParameterProviderFactory
{
    public static String STEP_PREFIX = "CMIS AuthParameterProvider:";

    @Autowired
    private DataAIS dataAIS;

    @Autowired
    private CmisProperties cmisProperties;

    /**
     *
     * The default provider uses AIS if support for Alfresco Identity Service is enabled.
     * Otherwise a provider which uses Basic authentication is returned.
     *
     * @return Function which takes a {@link UserModel} and returns a map of
     * authentication parameters to be used with {@link CmisWrapper#authenticateUser(UserModel, Function)}
     */
    public Function<UserModel, Map<String, String>> getDefaultProvider()
    {
        if (dataAIS.isEnabled())
        {
            STEP(String.format("%s Retrieved default AIS auth parameter provider.", STEP_PREFIX));
            return new AisAuthParameterProvider();
        }
        else
        {
            STEP(String.format("%s Retrieved default Basic auth parameter provider.", STEP_PREFIX));
            return new BasicAuthParameterProvider();
        }
    }

    public Function<UserModel, Map<String, String>> getAISProvider()
    {
        return new AisAuthParameterProvider();
    }

    public Function<UserModel, Map<String, String>> getBasicProvider()
    {
        return new BasicAuthParameterProvider();
    }

    private class BasicAuthParameterProvider implements Function<UserModel, Map<String, String>>
    {
        @Override
        public Map<String, String> apply(UserModel userModel)
        {
            STEP(String.format("%s Using Basic auth parameter provider.", STEP_PREFIX));
            Map<String, String> parameters = new HashMap<>();
            parameters.put(SessionParameter.USER, userModel.getUsername());
            parameters.put(SessionParameter.PASSWORD, userModel.getPassword());
            return parameters;
        }
    }

    private class AisAuthParameterProvider implements Function<UserModel, Map<String, String>>
    {
        @Override
        public Map<String, String> apply(UserModel userModel)
        {
            Map<String, String> parameters = new HashMap<>();

            STEP(String.format("%s Using AIS auth parameter provider.", STEP_PREFIX));
            AisToken aisToken = getAisAccessToken(userModel);

            parameters.put(SessionParameter.AUTHENTICATION_PROVIDER_CLASS, "org.apache.chemistry.opencmis.client.bindings.spi.OAuthAuthenticationProvider");
            parameters.put(SessionParameter.OAUTH_ACCESS_TOKEN, aisToken.getToken());
            parameters.put(SessionParameter.OAUTH_REFRESH_TOKEN, aisToken.getRefreshToken());
            parameters.put(SessionParameter.OAUTH_EXPIRATION_TIMESTAMP, String.valueOf(System.currentTimeMillis()
                    + (aisToken.getExpiresIn() * 1000))); // getExpiresIn is in seconds
            parameters.put(SessionParameter.OAUTH_TOKEN_ENDPOINT, cmisProperties.aisProperty().getAuthServerUrl()
                    + "/realms/alfresco/protocol/openid-connect/token");
            parameters.put(SessionParameter.OAUTH_CLIENT_ID, cmisProperties.aisProperty().getResource());
            return parameters;
        }

        /**
         * Returns a valid access token for valid user credentials in userModel.
         * An invalid access token is returned for invalid user credentials,
         * which can be used for tests involving non existing or unauthorized users.
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
                // Trying to authenticate with invalid user credentials so return an invalid access token
                if (e.getMessage().contains("invalid_grant"))
                {
                    STEP(String.format("%s Invalid user credentials were provided %s:%s. Using invalid token for reqest.",
                            STEP_PREFIX, userModel.getUsername(), userModel.getPassword()));
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
}
