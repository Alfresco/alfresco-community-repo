# AWS IAM Authentication For Search Enterprise

## Description

This note captures the recommended implementation shape for adding AWS IAM authentication support between Alfresco Content Services and Alfresco Search Enterprise when the search backend is an AWS-managed OpenSearch deployment.

The current implementation supports basic authentication only:

- In community-repo, the repository search client is configured by `elasticsearch.user` and `elasticsearch.password` and built in `ElasticsearchHttpClientFactory`.
- In alfresco-elasticsearch-connector, the indexing and reindexing clients are configured by `spring.elasticsearch.username` and `spring.elasticsearch.password` and built through Spring auto-configuration.

AWS IAM support should be introduced as an explicit authentication mode, not as an extension of the existing username and password fields.

## Scope

The implementation touches two codepaths:

- `alfresco-community-repo/repository`: repository-side OpenSearch client used by Search query and admin flows. As of ACS-11434, this codepath was moved from `alfresco-enterprise-repo` to `alfresco-community-repo`. Enterprise-repo retains only `EnterpriseElasticsearchHttpClientFactory`, a thin subclass that adds mTLS support on top of the community base class.
- `alfresco-elasticsearch-connector/alfresco-elasticsearch-indexing/alfresco-elasticsearch-live-indexing-shared`: connector-side OpenSearch client used by live indexing and shared indexing utilities.

## Recommended Property Model

### Community Repo Properties

Add the following properties to the Search subsystem:

```properties
elasticsearch.auth.mode=basic
elasticsearch.aws.region=
elasticsearch.aws.service=es
```

Semantics:

- `elasticsearch.auth.mode`: supported values `basic` and `aws-iam`; default `basic`.
- `elasticsearch.aws.region`: AWS region used for SigV4 signing, for example `us-east-1`.
- `elasticsearch.aws.service`: AWS signing service name; use `es` for Amazon OpenSearch Service and `aoss` for OpenSearch Serverless.

Keep the existing properties for backward compatibility:

```properties
elasticsearch.user=
elasticsearch.password=
```

Rules:

- `elasticsearch.user` and `elasticsearch.password` are used only when `elasticsearch.auth.mode=basic`.
- `elasticsearch.aws.region` is required when `elasticsearch.auth.mode=aws-iam`.
- `elasticsearch.aws.service` defaults to `es` and should not be required for standard managed OpenSearch domains.
- Do not add new properties for AWS access key, secret key, or session token. The implementation should rely on the AWS SDK default credentials provider chain.

### Connector Properties

Add the following properties to the connector Spring configuration:

```properties
spring.elasticsearch.auth.mode=basic
spring.elasticsearch.aws.region=
spring.elasticsearch.aws.service=es
```

Keep the existing connector properties for backward compatibility:

```properties
spring.elasticsearch.username=
spring.elasticsearch.password=
```

Rules:

- `spring.elasticsearch.username` and `spring.elasticsearch.password` remain valid only for `basic` mode.
- New AWS IAM properties should be added only under `spring.elasticsearch.*`.
- Do not extend deprecated `spring.elasticsearch.rest.*` properties with new AWS IAM settings.

## Affected Modules And Files

### Community Repo

Primary implementation files:

- `repository/src/main/java/org/alfresco/repo/search/impl/elasticsearch/client/ElasticsearchHttpClientFactory.java`
- `repository/src/main/java/org/alfresco/repo/search/impl/elasticsearch/contentmodelsync/ElasticsearchInitialiser.java`
- `repository/src/main/resources/alfresco/subsystems/Search/elasticsearch/elasticsearch-community-context.xml`
- `repository/src/main/resources/alfresco/subsystems/Search/elasticsearch/elasticsearch.properties`
- `repository/pom.xml`

Primary tests added or extended:

- `repository/src/test/java/org/alfresco/repo/search/impl/elasticsearch/client/ElasticsearchHttpClientFactoryTest.java`
- `repository/src/test/java/org/alfresco/repo/search/impl/elasticsearch/client/ElasticsearchHttpClientFactoryAwsIamTest.java` (new, IAM-specific coverage)
- `repository/src/test/java/org/alfresco/repo/search/impl/elasticsearch/contentmodelsync/ElasticsearchInitialiserTest.java`
- `repository/src/test/java/org/alfresco/AllUnitTestsSuite.java` (registers the new IAM test)

Likely additional integration coverage:

- Search subsystem integration tests that exercise repository search requests against OpenSearch.

### Enterprise Repo

No functional changes required for IAM support. `EnterpriseElasticsearchHttpClientFactory` remains the mTLS-aware subclass and inherits the new auth-mode branching from the community base class. If the IAM client construction path needs an enterprise-only customization later, it should be added by overriding the relevant protected hook on the subclass.

### Connector

Primary implementation files:

- `alfresco-elasticsearch-indexing/alfresco-elasticsearch-live-indexing-shared/src/main/java/org/alfresco/indexing/shared/config/searchengine/SearchEngineProperties.java`
- `alfresco-elasticsearch-indexing/alfresco-elasticsearch-live-indexing-shared/src/main/java/org/alfresco/indexing/shared/config/searchengine/SearchEngineRestClientConfigurations.java`
- `alfresco-elasticsearch-indexing/alfresco-elasticsearch-live-indexing-shared/src/main/java/org/alfresco/indexing/shared/config/searchengine/SearchEngineRestClientAutoConfiguration.java`
- `alfresco-elasticsearch-indexing/alfresco-elasticsearch-live-indexing-shared/pom.xml`

Primary tests to extend:

- `alfresco-elasticsearch-indexing/alfresco-elasticsearch-live-indexing-shared/src/test/java/org/alfresco/indexing/shared/config/searchengine/SearchEngineRestClientSnifferConfigurationTest.java`
- New tests for auth mode selection and IAM-specific bean wiring.

## Implementation Strategy

### 1. Introduce Explicit Auth Mode

Add an auth mode property in both codepaths and make `basic` the default.

This keeps current behavior unchanged for all existing deployments and avoids ambiguous combinations of username/password plus IAM signing.

### 2. Keep Basic Auth Path Intact

Do not refactor the existing basic auth behavior beyond moving it behind a mode check.

That means:

- Community repo keeps using credentials provider setup when `auth.mode=basic`.
- Connector keeps using the current `RestClientBuilder` and credentials provider behavior when `auth.mode=basic`.

### 3. Add A Separate AWS IAM Client Path

For `aws-iam`, create a dedicated OpenSearch client construction path based on OpenSearch's AWS transport instead of trying to combine SigV4 signing with the current basic-auth-focused client code.

Expected additions:

- OpenSearch AWS transport dependency.
- AWS SDK v2 region and credentials provider dependencies.

Behavior:

- Build the client with AWS SigV4 signing.
- Resolve credentials through the AWS SDK default provider chain.
- Sign requests with the configured region and service name.

### 4. Disable Sniffing For AWS IAM Mode In The Connector

The current connector configuration creates a `Sniffer` off the low-level `RestClient`.

For AWS-managed OpenSearch this should be disabled for the IAM path because:

- AWS endpoints are typically managed service endpoints rather than directly sniffable cluster nodes.
- The AWS IAM path is expected to use a different transport construction flow.

### 5. Validate Configuration Early

Add fast-fail validation for invalid combinations:

- `aws-iam` without `aws.region` should fail startup.
- `basic` should continue to accept empty credentials when the target cluster allows anonymous or network-based access.
- Any attempt to use both explicit basic credentials and IAM-specific required settings should prefer the selected `auth.mode` and ignore unrelated fields.

## Suggested Community Repo Checklist

1. Extend `elasticsearch.properties` with `elasticsearch.auth.mode`, `elasticsearch.aws.region`, and `elasticsearch.aws.service`.
2. Wire the new properties into `elasticsearch-community-context.xml` and expose setters on `ElasticsearchHttpClientFactory`.
3. Refactor `ElasticsearchHttpClientFactory` so client creation branches on `elasticsearch.auth.mode`.
4. Preserve the existing Apache HttpClient 5 basic-auth path for `basic` mode.
5. Add a separate AWS IAM transport builder for `aws-iam` mode.
6. Add unit tests for default mode, explicit basic mode, IAM mode validation, and client creation behavior.
7. Verify the enterprise-repo `EnterpriseElasticsearchHttpClientFactory` subclass still compiles and that its mTLS override does not regress with the new auth-mode branching.

## Suggested Connector Checklist

1. Extend `SearchEngineProperties` with `auth.mode`, `aws.region`, and `aws.service`.
2. Update `SearchEngineRestClientConfigurations` so `basic` keeps the current `RestClient` wiring.
3. Add a dedicated IAM bean path that constructs `OpenSearchClient` with AWS SigV4 transport.
4. Ensure `Sniffer` creation is skipped or disabled for IAM mode.
5. Add tests for property binding, mode selection, and IAM validation.
6. Keep deprecated `spring.elasticsearch.rest.*` compatibility as-is without adding new IAM settings there.

## Dependency Notes

Expected dependency changes:

- Community repo: add AWS SDK v2 dependencies needed for SigV4 transport support.
- Connector shared indexing module: add the same AWS SDK and OpenSearch AWS transport support.

The preferred implementation uses the AWS SDK default credentials chain, so it works with:

- environment variables
- instance profiles
- ECS task roles
- EKS IRSA
- shared credentials/config files

## Risks And Open Questions

### Transport Compatibility

The OpenSearch AWS transport is the cleanest option for IAM signing, but it should be verified against the actual request mix used by Alfresco before broad rollout.

The focused repository and connector validation slices now pass, but broader runtime coverage against AWS-managed OpenSearch endpoints is still recommended before rollout.

### Sniffer Behavior

The existing connector low-level `RestClient` includes optional sniffing. That behavior is not a good fit for managed AWS OpenSearch endpoints and should remain tied to the non-IAM path only.

### Packaging And Runtime Documentation

After the code is in place, user-facing docs should be added in the relevant packaging or product documentation repo to describe:

- required IAM permissions
- expected AWS credential resolution
- example configuration for managed OpenSearch and OpenSearch Serverless

## Implementation Status

- Repo-side IAM support is productized in `alfresco-community-repo` (the codepath was moved out of `alfresco-enterprise-repo` by ACS-11434), including validation that `elasticsearch.baseUrl` must be `/`, and explicit transport and AWS HTTP client cleanup.
- Connector-side IAM support is productized in `alfresco-elasticsearch-connector`, including multi-endpoint transport selection, `spring.elasticsearch.path-prefix` support, TLS key store and trust store wiring via the existing `client.ssl.*` properties, and explicit AWS HTTP client cleanup.
- Focused validation passes on both sides: `ElasticsearchHttpClientFactoryTest` plus the new `ElasticsearchHttpClientFactoryAwsIamTest` in community-repo, and `SearchEngineRestClientSnifferConfigurationTest` in alfresco-elasticsearch-connector.
- A broader shared-indexing-module test run completed with 158 tests, and the AWS IAM searchengine slice remained green.
- The only module-level failure observed in that broader run was `AcceptedContentMediaTypesCacheWithMTLSTest`, which fails during test initialization because `searchEngine.keystore` is not found on the test classpath. That failure is outside the AWS IAM Search Enterprise implementation changes.

## Minimal Example Configurations

### Community Repo

```properties
elasticsearch.host=search-example.us-east-1.es.amazonaws.com
elasticsearch.port=443
elasticsearch.baseUrl=/
elasticsearch.secureComms=https
elasticsearch.auth.mode=aws-iam
elasticsearch.aws.region=us-east-1
elasticsearch.aws.service=es
```

### Connector

```properties
spring.elasticsearch.uris=https://search-example.us-east-1.es.amazonaws.com:443
spring.elasticsearch.auth.mode=aws-iam
spring.elasticsearch.aws.region=us-east-1
spring.elasticsearch.aws.service=es
```

Current implementation notes:

- Both repo and connector IAM paths rely on the AWS SDK default credentials provider chain.
- The connector IAM path now honors all configured `spring.elasticsearch.uris` entries through a round-robin transport wrapper.
- The connector IAM path now applies `spring.elasticsearch.path-prefix`.
- The connector IAM path reuses the existing `client.ssl.key-store*` and `client.ssl.trust-store*` properties for TLS material.
- The connector IAM path explicitly rejects `client.ssl.hostname-verification-disabled=true` over HTTPS because the AWS SDK URLConnection transport used here does not support disabling hostname verification safely.