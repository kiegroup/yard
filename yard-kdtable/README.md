# Notes

.

## Prepare Minikube kamel

```sh
minikube start --addons registry
kamel install
```

problems with:

```
$ kamel run -d file://target/yard-kdtable-1.0-SNAPSHOT.jar test.yaml --dev
Error: Error trying to upload file://target/yard-kdtable-1.0-SNAPSHOT.jar to the Image Registry.: Get "http://10.111.196.197/v2/": dial tcp 10.111.196.197:80: i/o timeout; Get "https://10.111.196.197/v2/": dial tcp 10.111.196.197:443: i/o timeout
```

```
yard-kdtable $ kamel run -d file:///Users/mmortari/git/yard/yard-kdtable/target/yard-kdtable-1.0-SNAPSHOT.jar test.yaml --dev
Error: Error trying to upload file:///Users/mmortari/git/yard/yard-kdtable/target/yard-kdtable-1.0-SNAPSHOT.jar to the Image Registry.: Get "http://10.111.196.197/v2/": dial tcp 10.111.196.197:80: i/o timeout; Get "https://10.111.196.197/v2/": dial tcp 10.111.196.197:443: i/o timeout
```

```
yard-kdtable $ kamel run -d github:kiegroup/yard test.yaml --dev
Integration "test" created
Progress: integration "test" in phase Initialization
Condition "IntegrationPlatformAvailable" is "True" for Integration test: default/camel-k
Progress: integration "test" in phase Error
Error: integration "test" deployment failed
```