/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.repo.security.authentication.identityservice;

import static java.util.Optional.ofNullable;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Vector;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.TokenException;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

/**
 * Tests the Identity Service based authentication subsystem.
 * 
 * @author Gavin Cornwell
 */
public class IdentityServiceRemoteUserMapperTest extends TestCase
{
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    public void testValidToken()
    {
        final IdentityServiceRemoteUserMapper mapper = givenMapper(Map.of("VaLiD-ToKeN", () -> "johny"));

        HttpServletRequest mockRequest = createMockTokenRequest("VaLiD-ToKeN");

        final String user = mapper.getRemoteUser(mockRequest);
        assertEquals("johny", user);
    }

    public void testWrongTokenWithSilentValidation()
    {
        final IdentityServiceRemoteUserMapper mapper = givenMapper(Map.of("WrOnG-ToKeN", () -> {throw new TokenException("Expected ");}));
        mapper.setValidationFailureSilent(true);

        HttpServletRequest mockRequest = createMockTokenRequest("WrOnG-ToKeN");

        final String user = mapper.getRemoteUser(mockRequest);
        assertNull(user);
    }

    public void testWrongTokenWithoutSilentValidation()
    {
        final IdentityServiceRemoteUserMapper mapper = givenMapper(Map.of("WrOnG-ToKeN", () -> {throw new TokenException("Expected");}));
        mapper.setValidationFailureSilent(false);

        HttpServletRequest mockRequest = createMockTokenRequest("WrOnG-ToKeN");

        assertThatExceptionOfType(AuthenticationException.class)
                .isThrownBy(() -> mapper.getRemoteUser(mockRequest))
                .havingCause().withNoCause().withMessage("Expected");
    }

    private IdentityServiceRemoteUserMapper givenMapper(Map<String, Supplier<String>> tokenToUser)
    {
        final IdentityServiceFacade facade = mock(IdentityServiceFacade.class);
        when(facade.extractUsernameFromToken(anyString()))
                .thenAnswer(i ->
                        ofNullable(tokenToUser.get(i.getArgument(0, String.class)))
                                .map(Supplier::get));

        final PersonService personService = mock(PersonService.class);
        when(personService.getUserIdentifier(anyString())).thenAnswer(i -> i.getArgument(0, String.class));

        final IdentityServiceRemoteUserMapper mapper = new IdentityServiceRemoteUserMapper();
        mapper.setIdentityServiceFacade(facade);
        mapper.setPersonService(personService);
        mapper.setActive(true);
        mapper.setBearerTokenResolver(new DefaultBearerTokenResolver());


        return mapper;
    }

    /**
     * Utility method for creating a mocked Servlet request with a token.
     * 
     * @param token The token to add to the Authorization header
     * @return The mocked request object
     */
    private HttpServletRequest createMockTokenRequest(String token)
    {
        // Mock a request with the token in the Authorization header (if supplied)
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        
        Vector<String> authHeaderValues = new Vector<>(1);
        if (token != null)
        {
            authHeaderValues.add(BEARER_PREFIX + token);
        }

        when(mockRequest.getHeaders(AUTHORIZATION_HEADER))
                .thenReturn(authHeaderValues.elements());
        when(mockRequest.getHeader(AUTHORIZATION_HEADER))
                .thenReturn(authHeaderValues.isEmpty() ? null : authHeaderValues.get(0));
        
        return mockRequest;
    }
}
