/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.content.transform;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertTrue;

/**
 * Tests the retry mechanism in the RemoteTransformerClient.
 *
 * @since 6.0
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public class RemoteTransformerClientTest
{
    public static final int STARTUP_RETRY_PERIOD_SECONDS = 2;

    @Mock private ContentReader mockReader;
    @Mock private ContentWriter mockWriter;
    @Mock private Log mockLogger;
    @Mock private CloseableHttpResponse mockHttpResponse;
    @Mock private HttpEntity mockRequestEntity;
    @Mock private HttpEntity mockResponseEntity;
    @Mock private Header mockResponseContentType;
    @Mock private Header mockResponseContentEncoding;
    @Mock private StatusLine mockStatusLine;
    @Mock private HttpEntity mockReqEntity;

    @Spy private RemoteTransformerClient remoteTransformerClient = new RemoteTransformerClient("TRANSFORMER", "http://localhost:1234/test");

    private String sourceMimetype = "application/msword";
    private String sourceExtension = "doc";
    private String targetExtension = "pdf";
    private long timeoutMs = 120000;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        remoteTransformerClient.setStartupRetryPeriodSeconds(STARTUP_RETRY_PERIOD_SECONDS);

        doReturn(mockHttpResponse).when(remoteTransformerClient).execute(any(), any(HttpGet.class));
        doReturn(mockHttpResponse).when(remoteTransformerClient).execute(any(), any(HttpPost.class));
        doReturn(mockRequestEntity).when(remoteTransformerClient).getRequestEntity(any(), any(),
                any(), any(), anyLong(), any(), any());//,

        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockHttpResponse.getEntity()).thenReturn(mockResponseEntity);
        when(mockStatusLine.getStatusCode()).thenReturn(200);

//        when(mockResponseEntity.getContentLength()).thenReturn(1024L);
//        when(mockResponseEntity.getContentType()).thenReturn(mockResponseContentType);
//        when(mockResponseEntity.getContentEncoding()).thenReturn(mockResponseContentEncoding);
//        long responseContentLength = resEntity.getContentLength();
//        Header responseContentType = resEntity.getContentType();
//        Header responseContentEncoding = resEntity.getContentEncoding();

//        when(mockInputStream.)

    }

    private void assertRequestTransformError(String expectedMessage)
    {
        try
        {
            requestTransform();
            fail("There should have been an exception");
        }
        catch (Exception e)
        {
            assertEquals(expectedMessage, getMessage(e));
        }
    }

    private void requestTransform() throws IllegalAccessException
    {
        remoteTransformerClient.request(mockReader, mockWriter, sourceMimetype, sourceExtension, targetExtension,
                timeoutMs, mockLogger);
    }

    // Strip the number for AlfrescoRuntimeExceptions
    // 03030000 Remote TRANSFORMER check command returned a 0 status AN ERROR MESSAGE http://localhost:1234/test/version
    private String getMessage(Exception e)
    {
        String msg = e.getMessage();
        return getMessage(msg);
    }

    private String getMessage(String msg)
    {
        int i = msg.indexOf(' ');
        if (i > 0)
        {
            msg = msg.substring(i+1);
        }
        return msg;
    }

    @Test
    public void successCheckTest() throws Exception
    {
        remoteTransformerClient.check(mockLogger);
        // TODO get the version
    }

    @Test
    public void successRequestTest() throws Exception
    {
        requestTransform();
    }

    @Test
    public void non200CheckTest() throws Exception
    {
        when(mockStatusLine.getStatusCode()).thenReturn(1234);
        doReturn("\"message\":\"AN ERROR MESSAGE\",\"path\":").when(remoteTransformerClient).getContent(any());

        Pair<Boolean, String> available = remoteTransformerClient.check(mockLogger);
        assertFalse("Any failure should result in false", available.getFirst());
        assertEquals("TRANSFORMER check returned a 1234 status AN ERROR MESSAGE http://localhost:1234/test/version",
                getMessage(available.getSecond()));
    }

    @Test
    public void non200RequestTest() throws Exception
    {
        when(mockStatusLine.getStatusCode()).thenReturn(1234);
        doReturn("\"message\":\"AN ERROR MESSAGE\",\"path\":").when(remoteTransformerClient).getContent(any());

        assertRequestTransformError("TRANSFORMER returned a 1234 status AN ERROR MESSAGE http://localhost:1234/test/transform");
    }

    @Test
    // Test the initial alfresco startup when the transformer is not there yet.
    public void noConnectionCheckTest() throws Exception
    {
        // Mock a connection failure
        doThrow(IOException.class).when(remoteTransformerClient).execute(any(), any(HttpGet.class));

        Pair<Boolean, String> available = remoteTransformerClient.check(mockLogger);
        assertFalse("Any failure should result in false", available.getFirst());
        assertEquals("TRANSFORMER check failed to connect or to read the response",
                getMessage(available.getSecond()));

        assertTransformerBecomesAvailableAgainAfterFailure();
    }

    @Test
    // Test the restart of the transformer at some point after it has been running for a while.
    public void noConnectionRequestTest() throws Exception
    {
        requestTransform();

        // Mock a connection failure, check the error and reset the mock
        doThrow(IOException.class).when(remoteTransformerClient).execute(any(), any(HttpPost.class));
        assertRequestTransformError("TRANSFORMER failed to connect or to read the response");
        assertFalse(remoteTransformerClient.isAvailable());
        doReturn(mockHttpResponse).when(remoteTransformerClient).execute(any(), any(HttpPost.class));

        doThrow(IOException.class).when(remoteTransformerClient).execute(any(), any(HttpGet.class));
        assertTransformerBecomesAvailableAgainAfterFailure();

        requestTransform();
    }

    @Test
    public void assertOnceAvailableAlwaysAvailable() throws Exception
    {
        // Mock a connection failure
        doThrow(IOException.class).when(remoteTransformerClient).execute(any(), any(HttpGet.class));
        Pair<Boolean, String> available = remoteTransformerClient.check(mockLogger);
        assertFalse("Any failure should result in false", available.getFirst());

        // Mock a normal response from the /version request. It will not be made until the end of the wait period
        doReturn(mockHttpResponse).when(remoteTransformerClient).execute(any(), any(HttpGet.class));
        Thread.sleep(STARTUP_RETRY_PERIOD_SECONDS*1000);
        available = remoteTransformerClient.check(mockLogger);
        assertTrue("No failure so should result in true", available.getFirst());

        // Mock another connection failure. This time the code should not check but simply return success.
        doThrow(IOException.class).when(remoteTransformerClient).execute(any(), any(HttpGet.class));
        available = remoteTransformerClient.check(mockLogger);
        assertTrue("Should return true as it has before", available.getFirst());
        }

    protected void assertTransformerBecomesAvailableAgainAfterFailure() throws InterruptedException, IOException
    {
        assertFalse(remoteTransformerClient.isAvailable());

        // ------------- If the transformer takes a long time to start, the request will fail again even after the wait period

        Thread.sleep(STARTUP_RETRY_PERIOD_SECONDS*1000);
        Pair<Boolean, String> available = remoteTransformerClient.check(mockLogger);
        assertFalse("Any failure should result in false", available.getFirst());
        assertFalse(remoteTransformerClient.isAvailable());

        // Mock a normal response from the /version request. It will not be made until the end of the wait period
        doReturn(mockHttpResponse).when(remoteTransformerClient).execute(any(), any(HttpGet.class));

        // ------------- If we check during the wait period there should be no request

        available = remoteTransformerClient.check(mockLogger);
        assertTrue("During the wait period null should be returned", available.getFirst() == null);
        assertTrue("During the wait period null should be returned", available.getSecond() == null);
        assertFalse(remoteTransformerClient.isAvailable());

        // Sleep for a bit,  but not long enough.
        Thread.sleep(1000);
        available = remoteTransformerClient.check(mockLogger);
        assertTrue("During the wait period null should be returned", available.getFirst() == null);
        assertFalse(remoteTransformerClient.isAvailable());

        // Wait until the end of the period.
        Thread.sleep((STARTUP_RETRY_PERIOD_SECONDS-1)*1000);

        // ------------- After the wait period

        available = remoteTransformerClient.check(mockLogger);
        assertTrue("Any failure should result in false", available.getFirst());
        assertTrue("The transformer should have come back", remoteTransformerClient.isAvailable());
    }
}
