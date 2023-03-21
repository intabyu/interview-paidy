# Paidy forex exercise

## Implementation details

To be able to support at least 10,000 successful requests per day with 1 API token,
we query the One-Frame service between 2 and 5 minutes for all useful currency pairs and cache the results.

## Business constraint

- (A) | The service returns an exchange rate when provided with 2 supported currencies
- (B) | The rate should not be older than 5 minutes
- (C) | The service should support at least 10,000 successful requests per day with 1 API token
- (D) | The One-Frame service supports a maximum of 1000 requests per day for any given authentication token.
- (E) | The One-Frame API [...] One or more pairs per request are allowed.

Thus:

- (X) | 60 * 60 * 24 = 86400 seconds in a day 
- (Z) | (D) & (X) => 1 query at most every 86,4 seconds to the One-Frame API
- (B) & (Z) => `cache-duration-seconds should be set in production between 2 and 4 minutes` (to have some leeway)
- (C) & (X) => 1 query on average to the proxy, every 8 seconds => `no optimization needed unless we have spikes`

## Getting started

Usual commands are available in the `Makefile`.

```shell
# pull the one-frame docker image
make setup
```

```shell
# inside terminal 0, start the one-frame service on local port 8081
make run_one_frame

# inside terminal 1, start the proxy
make run
```

```shell
# ensure one-frame services runs correctly
make test_curl_one_frame

# ensure the proxy runs correctly
make test_curl_proxy
```

### Updating settings

Default settings can be found at:
- `src/main/resources/application.conf`
- `src/main/resources/logback.xml`

for production environment we should override most settings.

## Possible Improvements

- [ ] update code to generate all pairs and remove the query parameter from the config url unless we want to control this
- [ ] test the domain logic: `src/main/scala/forex/services/rates/interpreters/oneframe` by mocking `src/main/scala/forex/infra/oneframe/SyncClient`
- [ ] timestamp from one-frame response is expected to be parsed successfully, or it may not: `src/main/scala/forex/domain/Timestamp.scala`
- [ ] improve the error handling, cf appendix A
- [ ] have an integration test for the infra: `src/main/scala/forex/infra/oneframe`
- [ ] document `src/main/scala/forex/services/rates/interpreters/oneframe` and `src/main/scala/forex/infra/oneframe`
- [ ] one-frame client is synchronous and blocking, it should ideally be running in the background with a cron unless SLA does not require this
- [ ] if High Availability is needed, we will need to pay attention to the frequency at which we call one-frame service and we will need to distribute the cache, e.g. using Redis and having a way to "elect" a single service to update the cache at each update

## Appendix

### Appendix A - Error on missing currency pair

We should not log a stacktrace for a known error. 
Also, we lose information, e.g. `Could not find: Pair(USD,CAD)` is present for the `DEBUG` log and then we have `null` for the error handled later on.

We should, ideally, return a documented HTTP error code with an appropriate & explicit message, e.g. `Pair USD, CAD is not available`

```shell
# run the proxy **without** a given pair, e.g. (USD, CAD)

# running the following query
curl -X GET 'http://localhost:8080/rates?from=USD&to=CAD'
# should return an error 500 and produce the following logs
```

> [...]
> 15:26:16.147 [ioapp-compute-4] DEBUG forex - result: Left(OneFrameLookupFailed(Could not find: Pair(USD,CAD)))
> 15:26:16.155 [ioapp-compute-4] ERROR org.http4s.server.service-errors - Error servicing request: GET /rates from 127.0.0.1
> forex.programs.rates.errors$Error$RateLookupFailed: null
> [...]
