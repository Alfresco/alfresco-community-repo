/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.service;

/**
 * An interface to test the use of the auditable annotation.
 * 
 * @author Andy Hind
 */
@PublicService
public interface AnnotationTestInterface
{
    @Auditable()
    public void noArgs();
    
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"one", "two"})
    public String getString(String one, String two); 
    
    @Auditable(key =  Auditable.Key.ARG_0, parameters = {"one"})
    public String getAnotherString(String one); 
}
