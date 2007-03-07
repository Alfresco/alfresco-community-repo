/*
 * Solution to PostgreSQL issue:
 *    function information_schema._pg_keypositions() does not exist
 * Taken from: http://archives.postgresql.org/pgsql-general/2005-12/msg00060.php
 * Author:     Jason Long
 * Tested against PostgreSQL 8.2
 * First seen during upgrade testing of PostgreSQL from Alfresco 1.4.3 to 2.0
 */
SET search_path TO information_schema, public;
CREATE FUNCTION _pg_keypositions() RETURNS SETOF integer
    LANGUAGE sql
    IMMUTABLE
    AS 'select g.s
        from generate_series(1,current_setting(''max_index_keys'')::int,1)
        as g(s)';