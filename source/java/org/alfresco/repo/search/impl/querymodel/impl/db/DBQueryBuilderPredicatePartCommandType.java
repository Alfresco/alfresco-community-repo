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
