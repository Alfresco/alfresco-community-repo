package org.alfresco.repo.search.impl.querymodel.impl.db;

/**
 * @author Andy
 */
public enum DBQueryBuilderJoinCommandType
{
    NODE
    {

        @Override
        public boolean isMultiValued()
        {
           return false;
        }

    },
    ASPECT
    {

        @Override
        public boolean isMultiValued()
        {
           return true;
        }

    },
    PROPERTY
    {

        @Override
        public boolean isMultiValued()
        {
            return false;
        }
    },
    CONTENT_MIMETYPE
    {

        @Override
        public boolean isMultiValued()
        {
            return false;
        }
    },
    CONTENT_URL
    {

        @Override
        public boolean isMultiValued()
        {
            return false;
        }
    },
    PARENT
    {
        @Override
        public boolean isMultiValued()
        {
            return true;
        }
    },
    MULTI_VALUED_PROPERY
    {
        @Override
        public boolean isMultiValued()
        {
            return true;
        }
    };

    public abstract boolean isMultiValued();
}
