--
-- Title:      Updates 'locale_id' column in 'alf_node' for nodes with cm:mlDocument aspect and of type cm:mlContainer
-- Database:   MySQL
-- Since:      V4.2.4 Schema 6068
-- Author:     Pavel Yurkevich
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE tmp_locale_upgrade
(
   node_id BIGINT NOT NULL,
   string_value text,
   KEY idx_tmp_loc_n_id (node_id),
   KEY idx_tmp_loc_str_v (string_value(42))
) ENGINE=InnoDB;

--ASSIGN:locale_qname_id=id!-1
SELECT alf_qname.id AS id FROM alf_qname 
JOIN alf_namespace ON (alf_namespace.id = alf_qname.ns_id)
WHERE 
   alf_namespace.uri = 'http://www.alfresco.org/model/system/1.0'
AND
   alf_qname.local_name = 'locale';

INSERT INTO tmp_locale_upgrade (node_id, string_value)
(
   SELECT alf_node_properties.node_id, string_value FROM alf_node_properties
   JOIN alf_node_aspects ON (alf_node_aspects.node_id = alf_node_properties.node_id)
   JOIN alf_qname ON (alf_qname.id = alf_node_aspects.qname_id)
   JOIN alf_namespace ON (alf_namespace.id = alf_qname.ns_id)
   WHERE 
      alf_namespace.uri = 'http://www.alfresco.org/model/content/1.0'
   AND
      (
         alf_qname.local_name = 'mlDocument'
      )
   AND
      alf_node_properties.qname_id = ${locale_qname_id}
);

INSERT INTO tmp_locale_upgrade (node_id, string_value)
(
   SELECT alf_node_properties.node_id, string_value FROM alf_node_properties   
   JOIN alf_node ON (alf_node.id = alf_node_properties.node_id)
   JOIN alf_qname ON (alf_qname.id = alf_node.type_qname_id)
   JOIN alf_namespace ON (alf_namespace.id = alf_qname.ns_id)
   WHERE 
      alf_namespace.uri = 'http://www.alfresco.org/model/content/1.0'
   AND
         alf_qname.local_name = 'mlContainer'
   AND
      alf_node_properties.qname_id = ${locale_qname_id}
);

INSERT INTO alf_locale (version, locale_str)
(
   SELECT DISTINCT 0, string_value FROM tmp_locale_upgrade
   LEFT JOIN alf_locale ON (alf_locale.locale_str = tmp_locale_upgrade.string_value)
   WHERE alf_locale.locale_str IS NULL
);

UPDATE alf_node 
JOIN tmp_locale_upgrade ON (tmp_locale_upgrade.node_id = alf_node.id)
JOIN alf_locale ON (tmp_locale_upgrade.string_value = alf_locale.locale_str)
SET locale_id = alf_locale.id;

DROP TABLE tmp_locale_upgrade;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.2-migrate-locale-multilingual';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.2-migrate-locale-multilingual', 'Manually executed script upgrade V4.2: Updates locale_id column in alf_node for ML nodes',
    0, 6067, -1, 6068, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );