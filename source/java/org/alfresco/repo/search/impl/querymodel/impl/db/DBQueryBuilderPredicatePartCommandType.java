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
package org.alfresco.repo.search.impl.querymodel.impl.db;

/**
 * @author Andy
 */
public enum DBQueryBuilderPredicatePartCommandType
{
    OPEN,
    CLOSE,
    AND,
    OR,
    NOT,
    EQUALS
    {
        @Override
        public DBQueryBuilderPredicatePartCommandType propertyNotFound()
        {
            return NP_FAILS;
        }
    },
    EXISTS
    {
        @Override
        public DBQueryBuilderPredicatePartCommandType propertyNotFound()
        {
            return NP_FAILS;
        }
    },
    NOTEXISTS
    {
        @Override
        public DBQueryBuilderPredicatePartCommandType propertyNotFound()
        {
            return NP_MATCHES;
        }
    },
    GT
    {
        @Override
        public DBQueryBuilderPredicatePartCommandType propertyNotFound()
        {
            return NP_FAILS;
        }

        @Override
        public DBQueryBuilderPredicatePartCommandType propertyAndValueReversed()
        {
           return LT;
        }
    },
    GTE
    {
        @Override
        public DBQueryBuilderPredicatePartCommandType propertyNotFound()
        {
            return NP_FAILS;
        }

        @Override
        public DBQueryBuilderPredicatePartCommandType propertyAndValueReversed()
        {
            return LTE;
        }
    },
    LT
    {
        @Override
        public DBQueryBuilderPredicatePartCommandType propertyNotFound()
        {
            return NP_FAILS;
        }

        @Override
        public DBQueryBuilderPredicatePartCommandType propertyAndValueReversed()
        {
            return GT;
        }
    },
    LTE
    {
        @Override
        public DBQueryBuilderPredicatePartCommandType propertyNotFound()
        {
            return NP_FAILS;
        }

        @Override
        public DBQueryBuilderPredicatePartCommandType propertyAndValueReversed()
        {
            return GTE;
        }
    },
    IN
    {
        @Override
        public DBQueryBuilderPredicatePartCommandType propertyNotFound()
        {
            return NP_FAILS;
        }
    },
    NOTIN
    {
        @Override
        public DBQueryBuilderPredicatePartCommandType propertyNotFound()
        {
            return NP_MATCHES;
        }
    },
    LIKE
    {
        @Override
        public DBQueryBuilderPredicatePartCommandType propertyNotFound()
        {
            return NP_FAILS;
        }
    },
    NOTLIKE
    {
        @Override
        public DBQueryBuilderPredicatePartCommandType propertyNotFound()
        {
            return NP_MATCHES;
        }
    },
    NOTEQUALS
    {
        @Override
        public DBQueryBuilderPredicatePartCommandType propertyNotFound()
        {
            return NP_MATCHES;
        }
    },
    TYPE,
    ASPECT,
    NP_MATCHES
    {
        @Override
        public DBQueryBuilderPredicatePartCommandType propertyNotFound()
        {
            return NP_FAILS;
        }
    },
    NP_FAILS
    {
        @Override
        public DBQueryBuilderPredicatePartCommandType propertyNotFound()
        {
            return NP_MATCHES;
        }
    },
    ORDER
    {
        @Override
        public DBQueryBuilderPredicatePartCommandType propertyNotFound()
        {
            return NO_ORDER;
        }
    },
    NO_ORDER;
    

    /**
     * @return DBQueryBuilderPredicatePartCommandType
     */
    public  DBQueryBuilderPredicatePartCommandType propertyNotFound()
    {
        return this;
    }

    public DBQueryBuilderPredicatePartCommandType propertyAndValueReversed()
    {
        return this;
    }
}
