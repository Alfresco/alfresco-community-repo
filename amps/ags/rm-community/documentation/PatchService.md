# RM Patch Service

The RM Patch service operates independently of the Core Patch service & behaves differently.

Schema numbering is sequential, it’s a 4 digit number, prefixed with the major/minor version number, e.g. schema from a 2.4 version will be 24xx. This is a different policy to the core numbering (which bumps the schema number by 10 for each release).

Patches run in a single transaction. They may process data in batches, but it’s all wrapped in a single transaction, which is rolled back if the patch fails or is interrupted. AbstractModulePatch#245. When we implement applyInternal within a patch, that whole method runs inside a transaction.

DB Schema numbers update only after every patch runs. This means if a patch fails, earlier patches will re run. (see: ModulePatchExecuterImpl.executeInternal#140). This behaviour is different than core’s behaviour, which updates the schema number after each successful patch.

DB Schema number is stored in the attribute service (key: “module-schema”) against the RM’s module ID. This is not exposed in the UI. Nor in a REST API. The attribute service stores it directly in the DB, so isn’t even accessible via the node browser. 
If a customer wants to determine the schema number for a running system, they’ll need to execute a DB query.

It's possible to configure a patch not to run if being upgraded from a earlier schema version by setting `fixesFromSchema` in the patch config xml.
