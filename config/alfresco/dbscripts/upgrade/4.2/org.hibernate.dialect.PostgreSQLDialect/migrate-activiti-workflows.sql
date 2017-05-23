--
-- Title:      Migrate old workflow details into act_hi_varinst
-- Database:   PostgreSQL
-- Since:      V4.2 Schema 6080
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- Migrate old workflow details into act_hi_varinst

--ASSIGN:START_INDEX=VALUE_
SELECT VALUE_ FROM ACT_GE_PROPERTY WHERE NAME_ = 'next.dbid';

CREATE SEQUENCE VARINST_ID_SEQ START ${START_INDEX};

--count the current items in act_hi_varinst, before migration
--ASSIGN:INITIAL_ROW_COUNT=ROW_COUNT
select count(*) as ROW_COUNT from ACT_HI_VARINST;

-- increment the value to be used by the indexes with 1;
select nextval('VARINST_ID_SEQ');

-- insert from act_hi_detail into act_hi_varinst, the id will be generated starting from the next.dbid
-- only the most recent version of a variable must by migrated
-- the most recent version of a variable is considered to be the one with the highest revision and timestamp

CREATE INDEX ACT_IDX_HI_DETAIL_TMP ON ACT_HI_DETAIL (PROC_INST_ID_, NAME_, REV_, time_);

CREATE TABLE ACT_HI_DETAIL_TMP (
    PROC_INST_ID_ varchar(64),
    NAME_ varchar(255) NOT NULL,
    REV_ int4,
    TIME_ timestamp NOT NULL
);

CREATE INDEX ACT_HI_DETAIL_TMP_IDX ON ACT_HI_DETAIL_TMP(PROC_INST_ID_, NAME_, REV_, TIME_);

INSERT INTO ACT_HI_DETAIL_TMP
    SELECT
        PROC_INST_ID_,
        NAME_,
        MAX(REV_),
        MAX(time_)
    FROM ACT_HI_DETAIL
    GROUP BY PROC_INST_ID_, NAME_;

CREATE TABLE ACT_HI_VARINST_TMP (LIKE ACT_HI_VARINST INCLUDING DEFAULTS INCLUDING INDEXES);

INSERT INTO ACT_HI_VARINST_TMP(
    ID_,
    PROC_INST_ID_,
    EXECUTION_ID_,
    TASK_ID_,
    NAME_,
    VAR_TYPE_,
    REV_,
    BYTEARRAY_ID_,
    DOUBLE_,
    LONG_,
    TEXT_,
    TEXT2_
    )
SELECT
    nextval('VARINST_ID_SEQ'),
    AHD.PROC_INST_ID_,
    AHD.EXECUTION_ID_,
    AHD.TASK_ID_,
    AHD.NAME_,
    AHD.VAR_TYPE_,
    AHD.REV_,
    AHD.BYTEARRAY_ID_,
    AHD.DOUBLE_,
    AHD.LONG_,
    AHD.TEXT_,
    AHD.TEXT2_
FROM ACT_HI_DETAIL AHD
INNER JOIN ACT_HI_DETAIL_TMP AS DETAIL
    ON AHD.PROC_INST_ID_ = DETAIL.PROC_INST_ID_
         AND AHD.NAME_ = DETAIL.NAME_
         AND AHD.REV_ = DETAIL.REV_
         AND AHD.TIME_ = DETAIL.TIME_
LEFT OUTER JOIN ACT_HI_VARINST as VAR
    ON AHD.PROC_INST_ID_ = VAR.PROC_INST_ID_
    WHERE VAR.PROC_INST_ID_ IS NULL;


INSERT INTO ACT_HI_VARINST(
    ID_,
    PROC_INST_ID_,
    EXECUTION_ID_,
    TASK_ID_,
    NAME_,
    VAR_TYPE_,
    REV_,
    BYTEARRAY_ID_,
    DOUBLE_,
    LONG_,
    TEXT_,
    TEXT2_
)
    SELECT
        ID_,
        PROC_INST_ID_,
        EXECUTION_ID_,
        TASK_ID_,
        NAME_,
        VAR_TYPE_,
        REV_,
        BYTEARRAY_ID_,
        DOUBLE_,
        LONG_,
        TEXT_,
        TEXT2_
    FROM
        ACT_HI_VARINST_TMP;

--update act_ge_property
--ASSIGN:TOTAL_ROW_COUNT=ROW_COUNT
select count(*) as ROW_COUNT from ACT_HI_VARINST;
--increase the next.dbid value so that following ids will be created starting with the new value
update ACT_GE_PROPERTY set VALUE_ = VALUE_::integer + ${TOTAL_ROW_COUNT} - ${INITIAL_ROW_COUNT}  where NAME_ = 'next.dbid';

--revision is currently increased each time a block id is reserved, so we're simulating this behaviour
update ACT_GE_PROPERTY set REV_ = VALUE_::integer / 100 + 1 where NAME_ = 'next.dbid';

DROP SEQUENCE IF EXISTS VARINST_ID_SEQ;
DROP TABLE ACT_HI_DETAIL_TMP;
DROP TABLE ACT_HI_VARINST_TMP;
DROP INDEX ACT_IDX_HI_DETAIL_TMP;
--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-v4.2-migrate-activiti-workflows';
INSERT INTO alf_applied_patch
    (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
    VALUES
    (
        'patch.db-v4.2-migrate-activiti-workflows', 'Manually executed script upgrade V4.2: migrate-activiti-workflows',
        0, 6080, -1, 6081, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
    );