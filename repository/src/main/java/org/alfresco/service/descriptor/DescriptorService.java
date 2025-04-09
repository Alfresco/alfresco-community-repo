/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.service.descriptor;

import java.io.InputStream;

import org.alfresco.service.NotAuditable;
import org.alfresco.service.license.LicenseDescriptor;

/**
 * Service for retrieving meta-data about Alfresco stack.
 * 
 * @author David Caruana
 *
 */
// This is not a public service in the normal sense
public interface DescriptorService
{
    /**
     * Get descriptor for the alfresco software installed on the server.
     * <p>
     * The information contained by this descriptor is read from a property file.
     * 
     * The following properties are available in the descriptor
     * <ul>
     * <li>Major</li>
     * <li>Minor</li>
     * <li>Revision</li>
     * <li>Label</li>
     * <li>Build</li>
     * <li>Edition</li>
     * <li>Schema</li>
     * </ul>
     * 
     * The following properties are not applicable to the server descriptor.
     * <ul>
     * <li>id</li>
     * <li>licenceKey</li>
     * <li>name - unknown</li>
     * </ul>
     * 
     * @return server descriptor
     */
    @NotAuditable
    public Descriptor getServerDescriptor();

    /**
     * Get current descriptor for the repository.
     * <p>
     * The information in this descriptor is read from a node in the system store. After the patch process runs successfully, the version Major/Minor/Revision should be equal to the server descriptor.
     * <p>
     * The "repository id" that uniquely identifies each alfresco repository is available in the "id" property.
     * <p>
     * The following properties are available in the descriptor
     * <ul>
     * <li>Major</li>
     * <li>Minor</li>
     * <li>Revision</li>
     * <li>Label</li>
     * <li>Build</li>
     * <li>Schema</li>
     * <li>name</li>
     * <li>id</li>
     * </ul>
     * 
     * The following properties may be present
     * <ul>
     * <li>LicenceKey</li>
     * <li>Edition</li>
     * </ul>
     * 
     * @return repository descriptor
     */
    @NotAuditable
    public Descriptor getCurrentRepositoryDescriptor();

    /**
     * Get descriptor for the repository as it was when first installed.
     * <p>
     * The information in this descriptor is read from a node in the system store.
     * 
     * @return repository descriptor
     */
    @NotAuditable
    public Descriptor getInstalledRepositoryDescriptor();

    /**
     * Gets the License Descriptor
     * 
     * @return the license descriptor
     */
    @NotAuditable
    public LicenseDescriptor getLicenseDescriptor();

    /**
     * Attempts to load the license.
     * 
     * @return Returns a message telling the user what happened
     */
    public String loadLicense();

    /**
     * Attempts to load the license from the input stream.
     * 
     * @return Returns a message telling the user what happened
     */
    public String loadLicense(InputStream licenseStream);

    /**
     * Is this service bootstrapped?
     * 
     * @return true, the service is bootstrapped and available
     */
    boolean isBootstrapped();

}
