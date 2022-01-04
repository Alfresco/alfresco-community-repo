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

package org.alfresco.module.org_alfresco_module_rm.model.security;

import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Model security service interface.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@AlfrescoPublicApi
public interface ModelSecurityService
{
    /**
     * Sets whether model security is enabled globally or not.
     *
     * @param enabled
     */
    void setEnabled(boolean enabled);

    /**
     * Indicates whether model security is enabled or not.
     *
     * @return
     */
    boolean isEnabled();

    /**
     * Disable model security checks for the current thread.
     */
    void disable();

    /**
     * Enable model security checks for the current thread.
     */
    void enable();

    /**
     * Registers a protected model artifact with the service.
     *
     * @param atrifact  protected model artifact
     */
    void register(ProtectedModelArtifact atrifact);

    /**
     * Indicates whether a property is protected or not.
     *
     * @param property  name of property
     * @return boolean  true if property is protected, false otherwise
     */
    boolean isProtectedProperty(QName property);

    /**
     * Get the protected properties
     *
     * @return  {@link Set}&lt;{@link QName} &gt;  all the protected properties
     */
    Set<QName> getProtectedProperties();

    /**
     * Get the details of the protected property, returns null if property
     * is not protected.
     *
     * @param name  name of the protected property
     * @return {@link ProtectedProperty}    protected property details, null otherwise
     */
    ProtectedProperty getProtectedProperty(QName name);

    /**
     * Indicates whether the current user can edit a protected property in the context of
     * a given node.
     * <p>
     * If the property is not protected then returns true.
     *
     * @param nodeRef   node reference
     * @param property  name of the property
     * @return boolean  true if the current user can edit the protected property or the property
     *                  is not protected, false otherwise
     */
    boolean canEditProtectedProperty(NodeRef nodeRef, QName property);

    /**
     * Indicates whether an aspect is protected or not.
     *
     * @param aspect    aspect name
     * @return boolean  true if aspect is protected, false otherwise
     */
    boolean isProtectedAspect(QName aspect);

    /**
     * Get the protected aspects.
     *
     * @return  {@link Set}&lt;{@link QName}&gt;  all the protected aspects
     */
    Set<QName> getProtectedAspects();

    /**
     * Get the details of the protected aspect, returns null if aspect is
     * not protected.
     *
     * @param name  name of the aspect
     * @return {@link ProtectedAspect}  protected aspect details, null otherwise
     */
    ProtectedAspect getProtectedAspect(QName name);

    /**
     * Indicates whether the current user can edit (ie add or remove) a protected
     * aspect in the context of a given node.
     * <p>
     * If the aspect is not protected then returns true.
     *
     * @param nodeRef   node reference
     * @param aspect    name of the of aspect
     * @return boolean  true if the current user can edit the protected aspect or the the
     *                  aspect is not protected, false otherwise
     */
    boolean canEditProtectedAspect(NodeRef nodeRef, QName aspect);
}
