/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.traitextender;

/**
 * Implementors are sets of extension implementations that are registered on
 * specific {@link ExtensionPoint} for given {@link Extender}s.
 *
 * @author Bogdan Horje
 */
public interface ExtensionBundle
{
    /**
     * Sets up an registers extension factories with the give {@link Extender}
     * for all extensions defined by this bundle.
     * 
     * @param extender
     */
    void start(Extender extender);

    /**
     * Unregisters all defined extensions from the given {@link Extender} .
     * 
     * @param extender
     */
    void stop(Extender extender);

    String getId();
}
