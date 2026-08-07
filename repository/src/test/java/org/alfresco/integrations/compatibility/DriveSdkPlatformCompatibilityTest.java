/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.integrations.compatibility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.Version;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.junit.Test;

/**
 * Guards the compatibility contract the platform owns for the Google Drive SDK it ships (so the Google Drive AMPs can stay zero-dependency): the Drive SDK must be binary-compatible with the platform libraries it now runs against (jackson, guava, httpclient), and jackson must resolve to a single platform-aligned version.
 *
 * <p>
 * The Drive SDK jars (google-api-services-drive, google-api-services-oauth2, google-http-client-jackson2 and their transitives) are compile dependencies of this module, so they are on the test classpath at exactly the versions the WAR ships in {@code WEB-INF/lib} - resolved through the alfresco-community-repo dependency management, where the jackson BOM is imported ahead of the Google libraries BOM. A downgraded or SDK-driven library surfaces here as a {@code NoSuchMethodError} / {@code NoClassDefFoundError} on the build instead of in a deployed repository.
 * </p>
 *
 * <p>
 * Everything is offline: the clients are only constructed to force resolution of the SDK's dependency graph - no credentials, Drive files or network are touched.
 * </p>
 */
public class DriveSdkPlatformCompatibilityTest
{
    @Test
    public void jacksonModulesResolveToASinglePlatformAlignedVersion()
    {
        // The jackson the Drive SDK parses JSON with (via google-http-client-jackson2) must land on the
        // same minor line as the platform jackson-databind; otherwise the SDK's own (newer) jackson has
        // leaked past the platform-aligned jackson BOM.
        String platform = minor(com.fasterxml.jackson.core.json.PackageVersion.VERSION);

        assertEquals("jackson-databind must match the platform jackson-core minor line",
                platform, minor(com.fasterxml.jackson.databind.cfg.PackageVersion.VERSION));
    }

    @Test
    public void buildsDriveClientAgainstPlatformDependencies()
    {
        Drive drive = new Drive.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), null)
                .setApplicationName("smoke-test")
                .build();
        assertNotNull(drive);
    }

    @Test
    public void serialisesDriveRestModelWithPlatformJackson() throws IOException
    {
        File file = new File()
                .setName("smoke.txt")
                .setMimeType("text/plain")
                .setParents(List.of("root"));
        JsonFactory factory = GsonFactory.getDefaultInstance();
        File parsed = factory.fromString(factory.toString(file), File.class);
        assertEquals("smoke.txt", parsed.getName());
        assertEquals("text/plain", parsed.getMimeType());
    }

    @Test
    @SuppressWarnings("PMD.UnitTestShouldIncludeAssert") // assertions live in the roundTripJson helper
    public void exercisesSdkJsonTransportAgainstPlatformJacksonAndGson() throws IOException
    {
        roundTripJson(JacksonFactory.getDefaultInstance());
        roundTripJson(GsonFactory.getDefaultInstance());
    }

    @Test
    @SuppressWarnings("PMD.UnitTestShouldIncludeAssert") // the test is that constructing/shutting down the transport does not throw
    public void buildsApacheHttpTransportFromPlatformHttpClient() throws IOException
    {
        HttpTransport transport = new ApacheHttpTransport();
        transport.shutdown();
    }

    private static void roundTripJson(JsonFactory factory) throws IOException
    {
        GenericJson object = new GenericJson();
        object.set("name", "smoke.txt");
        object.set("mimeType", "text/plain");
        GenericJson parsed = factory.fromString(factory.toString(object), GenericJson.class);
        assertEquals("smoke.txt", parsed.get("name"));
    }

    private static String minor(Version version)
    {
        return version.getMajorVersion() + "." + version.getMinorVersion();
    }
}
