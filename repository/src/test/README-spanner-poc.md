# ACS-13147 / ACS-13153: Spanner PoC

## Scope and Result

The operations/transactions baseline belongs to [ACS-13153](https://hyland.atlassian.net/browse/ACS-13153); connectivity, bootstrap, and the opt-in Spanner CI path belong to [ACS-13147](https://hyland.atlassian.net/browse/ACS-13147). The ACS-13147 emulator findings and rerun instructions follow the PostgreSQL baseline below.

On 2026-10-15 the selection below passed against ordinary PostgreSQL: **16 tests, 0 failures, 0 errors, 0 skipped; Maven exit code 0**. This establishes expected behavior, not Spanner compatibility or full ACS startup.

| Component | Baseline |
| --- | --- |
| Source | Community `master`, `021c843c69`, plus the related-metadata test in this change |
| ACS version | `26.3.0.77-SNAPSHOT` |
| Runtime | macOS arm64, Homebrew OpenJDK `21.0.11`, Maven `3.9.6` |
| Database | Docker `postgres:17.9`, PostgreSQL `17.9` on aarch64 |
| JDBC | Existing PostgreSQL JDBC dependency `42.7.7`; no driver or dialect changes |
| Endpoint | `jdbc:postgresql://localhost:55433/acs13153` |
| Initialization | ACS Spring test context initialized against the dedicated database |

Use this same source ref when comparing results. Enterprise, Share, packaging, and deployed application startup were not tested. The later emulator attempt used this same Community source copy.

## Selected Tests

All classes belong to the `repository` module's `src/test/java` tree.

| Class | Methods | Expected behavior |
| --- | --- | --- |
| `org.alfresco.repo.content.RoutingContentServiceTest` | `testAutoSettingOfProperties`, `testWriteToNodeWithoutAnyContentProperties` | Write/read content and check content length, encoding, locale, and content metadata. These checks run within the test transaction; content bytes use the filesystem content store, not PostgreSQL. |
| `org.alfresco.repo.node.db.DbNodeServiceImplTest` | `testCreateNodeWithProperties`, `testProperties`, `testCascadeDelete` | Create/read/update metadata, including null and enum values; delete a node graph and verify deleted nodes and associations in a new transaction. Methods are inherited from `BaseNodeServiceTest`. |
| `org.alfresco.repo.node.getchildren.GetChildrenCannedQueryTest` | `testPaging`, `testPropertyStringFiltering`, `testPropertySorting` | Database-backed child queries: page sizes/continuation, case-sensitive and insensitive filters, and sorted results. No Solr dependency. |
| `org.alfresco.repo.transaction.TransactionServiceImplTest` | `testPropagatingTxn`, `testNonPropagatingTxn` | Inner rollback marks a participating outer transaction rollback-only; independent inner rollback allows outer commit. These assert transaction control, not metadata persistence. |
| `org.alfresco.repo.transaction.RetryingTransactionHelperTest` | `testSuccessNoRetry`, `testSuccessWithRetry`, `testNonRetryingFailure`, `testNonRetryingSilentRollback`, `testRelatedMetadataCommitAndRollback` | Successful update; injected retryable failure applies only one increment; failed/silently rolled-back updates do not persist; related parent/child metadata commits together and remains unchanged after forced rollback. |
| `org.alfresco.repo.node.ConcurrentNodeServiceTest` | `testMultithreaded_AspectWrites` | Two threads each add ten distinct aspects to one node; all twenty must be present after completion. |

### Assertion Review

- The one added method, `testRelatedMetadataCommitAndRollback`, creates a child and updates both parent and child in a committed transaction. Separate read-only `requiresNew` transactions check committed values and the parent association, then check that both values remain unchanged after a forced rollback. Cleanup deletes the child.
- `testSuccessWithRetry` injects Spring `ConcurrencyFailureException`; it does not establish that Spanner aborts/SQLSTATEs are translated into retryable ACS exceptions.
- Do not use `RetryingTransactionHelperTest.testConcurrencyRetryingNoFailure` alone as a correctness gate: it joins worker threads but does not assert final values, and workers can absorb retryable failures. The selected aspect-write test instead checks every expected final mutation. It is not a same-property lost-update, isolation-level, or lock-semantics test.
- The larger `testMultiThreaded_PropertyWrites` checks final counters but performs 5,000 updates. Defer that stress test until the small selection works.
- `AllDBTestsTestSuite` includes the selected database node/query classes, but not these transaction, concurrent-node, or content-service classes. Use explicit selectors below; running that suite alone does not cover this baseline. Existing transaction tests remain reachable through their normal application-context suite.
- No production changes, native Spanner driver, custom dialect, persistence rewrite, or exhaustive isolation/permissions/versioning matrix is introduced.

## Rerun

Run from the Community repository root with Java 21 and Maven. Use a disposable database: ACS initialization and these tests create persistent schema/data. The credentials below are local test-only values. This follows the external-PostgreSQL Maven path in `.github/workflows/ci.yml`, narrowed to representative methods.

Start a dedicated database (the container name and port must be free):

```sh
docker run --detach --name acs-13153-postgres \
  --publish 127.0.0.1:55433:5432 \
  --env POSTGRES_USER=alfresco --env POSTGRES_PASSWORD=alfresco \
  --env POSTGRES_DB=acs13153 \
  postgres:17.9 -c max_connections=300
docker exec acs-13153-postgres pg_isready -U alfresco -d acs13153
```

Proceed after PostgreSQL reports that it accepts connections. Select and run the tests:

```sh
tests='DbNodeServiceImplTest#testCreateNodeWithProperties+testProperties+testCascadeDelete,GetChildrenCannedQueryTest#testPaging+testPropertyStringFiltering+testPropertySorting,RetryingTransactionHelperTest#testSuccessNoRetry+testSuccessWithRetry+testNonRetryingFailure+testNonRetryingSilentRollback+testRelatedMetadataCommitAndRollback,TransactionServiceImplTest#testPropagatingTxn+testNonPropagatingTxn,ConcurrentNodeServiceTest#testMultithreaded_AspectWrites,RoutingContentServiceTest#testAutoSettingOfProperties+testWriteToNodeWithoutAnyContentProperties'
mvn -B -ntp test -pl repository -am "-Dtest=$tests" \
  -DfailIfNoTests=false \
  -Ddb.driver=org.postgresql.Driver -Ddb.name=acs13153 \
  -Ddb.url=jdbc:postgresql://localhost:55433/acs13153 \
  -Ddb.username=alfresco -Ddb.password=alfresco
```

Require Maven exit code 0 and all **16 named methods** in Surefire XML with no failures/errors/skips. `failIfNoTests=false` permits upstream reactor modules with no matching tests, so exit code alone is insufficient. The recorded local run also used `-o` because dependencies were cached; omit it when downloads are needed.

### Local Build Caveat

Two builds inside the open VS Code workspace failed during repository test compilation: missing production classes, then `NoSuchFileException` for `NamespacePrefixResolverProvider.class`. The latter class was present again after the build. Concurrent IDE output changes are suspected, not proven. These were not SQL or Spanner failures.

The successful run used a source copy outside the workspace, including the modified test, with generated outputs excluded:

```sh
baseline_dir=$(mktemp -d /tmp/acs-13153.XXXXXX)
tar --exclude='.git' --exclude='target' --exclude='.idea' \
  --exclude='.settings' --exclude='*.log*' -cf - . \
  | tar -xf - -C "$baseline_dir"
cd "$baseline_dir"
```

Then run the Maven selection above. Do not change IDE settings or production code to compensate for this local build issue. The actual successful copy was `/tmp/acs-13153.N0B994`.

Stop the local baseline database when finished:

```sh
docker stop acs-13153-postgres
```

It can be resumed with `docker start acs-13153-postgres`; it has no external volume and retains data in the container until removed.

## Evidence and Handoff

Local evidence is retained under `repository/target/spanner-poc/postgresql-baseline/`: the full Maven log, per-class Surefire XML/text reports, and the ACS test log. These are generated, untracked artifacts, not permanent CI links. Original reports are under `/tmp/acs-13153.N0B994/repository/target/surefire-reports/`. The successful combined run completed at `2026-10-15T13:05:00-05:00` in 1 minute 29 seconds.

For ACS-13147/ACS-13153 CI handoff:

1. Agree the ACS ref and Java/JDBC versions and retain this exact selection for both targets.
2. Add the explicit Maven selector to the existing external-database CI path; archive Surefire XML, ACS/Maven logs, and the exact endpoint configuration without secrets.
3. Once Spanner initialization is sufficient, rerun through the agreed PostgreSQL compatibility endpoint. Do not switch to a native driver or introduce dialect workarounds in this PoC.
4. For each failure record the method, observed state, SQL/SQLSTATE and exception chain, retry behavior, backend/endpoint versions, source ref, and CI artifact link. Separate context/bootstrap failures (ACS-13147) from executed operation failures (ACS-13153). A blocked test is not a pass.
5. Only infer a Spanner-specific difference when this same selection passes against ordinary PostgreSQL at the same source ref. A failed bootstrap means downstream operation tests are blocked, not that their assertions failed.

### Ticket/Confluence Summary

ACS-13153 preparation: selected 16 existing/focused Community repository methods spanning content and metadata operations, database-backed queries, transaction propagation/retry, and concurrent aspect updates. Added one related-metadata commit/rollback test with fresh-transaction state checks. All 16 passed on PostgreSQL 17.9 using Community `021c843c69` (`26.3.0.77-SNAPSHOT`) plus this test change, Java 21.0.11, and JDBC 42.7.7. No production changes. Emulator operations testing is blocked by the ACS-13147 bootstrap failure below; real-Spanner compatibility remains unverified. Attach CI evidence links before publishing final conclusions.

## ACS-13147: Preliminary Emulator Findings

On 2026-10-15, after approval to proceed locally without cloud credentials, the same Community source and PostgreSQL JDBC driver were tested against PGAdapter plus the Spanner emulator. No native Spanner driver, custom dialect, schema workaround, or production code change was used.

| Check | PostgreSQL 17.9 Control | PGAdapter / Spanner Emulator |
| --- | --- | --- |
| JDBC connection and `SELECT 1` | Pass | Pass; JDBC 42.7.7, advertised PostgreSQL version 14.1 |
| `RepositoryStartupTest` | 2 tests, 0 failures/errors/skips; Maven exit 0 | 2 tests, 0 failures, 2 setup errors, 0 skipped; Maven exit 1 |
| Exact bootstrap-marker DDL in separate scratch database | Pass; exit 0 | Rejected; SQLSTATE `P0001`, vendor code 0; exit 1 |
| Schema initialization / test-context startup | Pass | Blocked before startup test bodies execute |
| Selected 16 CRUD/query/transaction methods | Passed in earlier baseline | Not run: initialization prerequisite fails |

`RepositoryStartupTest` exercises Spring test-context initialization and basic repository writes, **not** deployed WAR/Tomcat or full product startup. Its two methods are `testRepoReadWrite` and `testBasicWriteOperations`. Emulator setup errors occur at `RepositoryStartupTest.setUp:55`; they are not failed operation assertions.

### Verified First Blocker

ACS attempts the following statement in auto-commit mode before running its schema script:

```sql
create table alf_bootstrap_lock (charval CHAR(1) NOT NULL)
```

Replaying that exact statement through the same JDBC driver against a separate emulator database returns:

```text
SQLSTATE=P0001
VENDOR_CODE=0
org.postgresql.util.PSQLException: ERROR: Type <bpchar> is not supported.
```

`SchemaBootstrap.setBootstrapStarted()` (`repository/src/main/java/org/alfresco/repo/domain/schema/SchemaBootstrap.java`, around line 807) catches every `Throwable` from this DDL and throws `LockFailedException` without its cause. The loop around line 1731 retries 24 times by default, then reports a long-running upgrade on another cluster node. The scratch probe demonstrates an unsupported fixed-length character type; the cluster-upgrade message does not establish that another node is running. The same statement succeeds on ordinary PostgreSQL in its own scratch database.

This is the first verified bootstrap blocker, not an exhaustive compatibility assessment. The remaining schema DDL, transaction/abort translation, concurrency semantics, enterprise components, and deployed startup have not been validated on Spanner. Do not infer that replacing `CHAR(1)` alone would make ACS compatible. Real managed Spanner must confirm or refute the emulator finding before a production compatibility conclusion.

### Emulator Identity and Local Rerun

- Image: `gcr.io/cloud-spanner-pg-adapter/pgadapter-emulator:v0.56.1`; PGAdapter logs version `0.56.1`.
- Observed image digest: `sha256:f78e5e37e090bdda98cccd7284586063bd37121c7b8d5459d777d88100dd6f35`.
- Platform: `linux/amd64`, emulated on macOS arm64. Host port `127.0.0.1:55434` only.
- Combined image automatically provisions PostgreSQL-dialect databases from connection names under its local `emulator-project` / `test-instance` defaults. These are not cloud resources.
- Configuration: `scripts/ci/docker-compose/docker-compose-spanner.yaml`. Override `SPANNER_PGADAPTER_IMAGE` with the recorded digest to reproduce exact image content; `SPANNER_POSTGRES_PORT` defaults to `55434`.
- Reference: [PGAdapter emulator documentation](https://github.com/GoogleCloudPlatform/pgadapter/blob/postgresql-dialect/docs/emulator.md).

From the Community root, start the emulator, compile the existing test sources, and probe with standard PostgreSQL JDBC:

```sh
docker compose -p acs-13147 -f scripts/ci/docker-compose/docker-compose-spanner.yaml up -d
mvn -B -ntp test-compile -pl repository -am -DskipTests
export POC_JDBC_URL='jdbc:postgresql://localhost:55434/acs13147?sslmode=disable'
export POC_DB_USERNAME=alfresco POC_DB_PASSWORD=alfresco
jshell --feedback concise \
  --class-path "$HOME/.m2/repository/org/postgresql/postgresql/42.7.7/postgresql-42.7.7.jar" \
  scripts/ci/spanner-poc-probe.jsh
mvn -B -ntp test -pl repository -am \
  -Dtest=RepositoryStartupTest -DfailIfNoTests=false \
  -Ddb.driver=org.postgresql.Driver -Ddb.name=acs13147 \
  "-Ddb.url=$POC_JDBC_URL" -Ddb.username=alfresco -Ddb.password=alfresco \
  "-Ddir.root=$PWD/repository/target/alf_data-spanner-poc"
```

Do not continue after a failed compilation or connectivity probe. Use the isolated-copy workaround above if IDE output interference recurs. Local startup runs used the existing copy `/tmp/acs-13153.N0B994`, fresh databases named `acs13147` at ports `55433` (control) and `55434` (emulator), and separate content-store directories. Surefire suffixes `postgresql-startup` and `spanner-startup` preserved both report sets.

To isolate the marker error without changing the startup database, run the probe with `POC_JDBC_URL='jdbc:postgresql://localhost:55434/acs13147-probe?sslmode=disable'` and `POC_SQL_PROBE='create table alf_bootstrap_lock (charval CHAR(1) NOT NULL)'`. This uses JDBC auto-commit, like `SchemaBootstrap`; the expected observed emulator exit code is 1, not success. Use a fresh scratch database for another target because this statement creates a table if supported. Stop the emulator afterward with `docker compose -p acs-13147 -f scripts/ci/docker-compose/docker-compose-spanner.yaml stop`.

### Opt-In CI and Evidence

`.github/workflows/spanner-poc.yml` adds a manual `workflow_dispatch` workflow, separate from mandatory CI. Its independent PostgreSQL and emulator jobs compile the same source, probe standard JDBC, run `RepositoryStartupTest`, and always upload Maven/ACS/database logs, Surefire reports, image identities, and available temporary schema diagnostics. A failed emulator startup additionally runs the marker-DDL probe against a separate scratch database. Failures retain nonzero exit codes; a successful startup must include both named methods with no failures/errors/skips. The PostgreSQL job uses the existing external-database Compose profile; no Share or enterprise job is implied.

The workflow passed Actionlint 1.7.7 including ShellCheck, and both Compose configurations validated locally. **It has not been executed on GitHub Actions**. Once reviewed and published through the normal process, dispatch `ACS Spanner PoC (Emulator)` at the agreed ref; an emulator bootstrap failure is a PoC finding, not a passing compatibility gate. Attach both job artifact links to ACS-13147. The 16-method selection is intentionally deferred on the emulator until bootstrap succeeds.

Local phase-two evidence is retained under `repository/target/spanner-poc/acs-13147/`: JDBC and marker probes, PostgreSQL/emulator Maven startup logs, both startup Surefire XML/text reports, cumulative ACS log, emulator log, and container/image identities. These generated artifacts are not permanent ticket attachments. PostgreSQL startup completed at `2026-10-15T14:47:23-05:00`; emulator startup completed at `2026-10-15T14:53:18-05:00`. Local containers were stopped after evidence capture; `docker start acs-13153-postgres` and the Compose `start` command resume them. Emulator database state is disposable and must not be assumed to survive restart.

### ACS-13147 Ticket Draft

Community `021c843c69` (`26.3.0.77-SNAPSHOT`), Java 21.0.11, JDBC 42.7.7: standard JDBC connection/`SELECT 1` succeeded through PGAdapter 0.56.1 plus the local Spanner emulator. The same two startup tests passed on fresh PostgreSQL 17.9 but both errored during emulator context setup. The first isolated blocker is ACS's `create table alf_bootstrap_lock (charval CHAR(1) NOT NULL)`, rejected as unsupported `bpchar` (SQLSTATE P0001). ACS masks this SQL failure as bootstrap-lock contention, exhausts retries, and reports another node's upgrade. No persistence workarounds were introduced. Downstream CRUD/query/transaction testing on the emulator is blocked, not passed or assertion-failed. An opt-in two-target CI workflow is prepared and locally linted but not run remotely. Real-Spanner confirmation and full deployed ACS startup remain outstanding. Attach evidence links before publishing.

## Recommended Next Steps and Closure

1. Publish the branch through the normal review process, execute the opt-in CI attempt, and attach the PostgreSQL and emulator job/artifact links. Archive the local 16-method PostgreSQL baseline evidence as well; generated `target` files are not included in the source commit.
2. Obtain an approved, disposable managed Spanner PostgreSQL-dialect database and a PGAdapter endpoint. Replay standard JDBC connectivity and the exact marker DDL first, then attempt ACS bootstrap/startup using the same source ref. Do not change the schema or driver to make the probe pass. Keep credentials outside source control and record endpoint/backend versions without secrets. This confirms whether the preliminary emulator blocker applies to the intended service.
3. If bootstrap succeeds on the intended target, run the documented 16-method selection against it and the PostgreSQL control. If bootstrap fails, record the precise blocker and the downstream tests as blocked; further compatibility implementation requires a separate scope decision.
4. Publish the findings to the stories and a durable team-facing report, including source ref, commands, limitations, evidence links, and the explicit decision about blocked operations testing. These feasibility stories permit negative findings; a successful Spanner implementation is not the completion criterion.

**Recommended current disposition:** keep ACS-13147 open for repeatable CI evidence and target confirmation; mark ACS-13153 blocked by ACS-13147, with preparation and PostgreSQL baseline complete. ACS-13147 can close with a documented incompatibility once the agreed target has been attempted and findings shared. ACS-13153 can close after operation evaluation, or after the story owner explicitly accepts a blocker-based conclusion rather than executed operations. If the owner accepts emulator-only evidence as sufficient, record that reduced scope explicitly before closing either story.

Do not require full deployed startup after a confirmed bootstrap blocker, or expand into a Community/Enterprise matrix or dialect rewrite merely to close these stories. A separate diagnostic improvement to preserve the original SQL cause in `SchemaBootstrap` would be useful, but is not a Spanner compatibility fix or part of this PoC.