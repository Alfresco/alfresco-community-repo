/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.audit;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;

/**
 * An interface to test the use of the auditable annotation.
 * 
 * @author Andy Hind
 */
public interface AnnotationTestInterface
{
    @Auditable()
    public void noArgs();
    
    @Auditable(parameters = {"one", "two"})
    public String getString(String one, String two); 
    
    @Auditable(parameters = {"one"})
    public String getAnotherString(String one); 
}
