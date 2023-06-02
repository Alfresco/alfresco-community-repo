package org.alfresco.repo.node.db;
/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 20 Alfresco Software Limited
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
public sealed interface Java17Test permits OnlyThis, AndThat {

    void justDoIt();
    static Java17Test getInstance() {
        return Math.random() < 0.5 ? new OnlyThis() : new AndThat();
    }

}

final class OnlyThis implements Java17Test {
    @Override
    public void justDoIt()
    {
        System.out.println("Only this");
    }
}

final class AndThat implements Java17Test {
    @Override
    public void justDoIt()
    {
        System.out.println("And that");
    }
}

//final class NotThisOne implements Java17Test {
//
//}