# Notes

.

## Camel JBang

```sh
camel run --deps=org.kie.yard:yard-kdtable:1.0-SNAPSHOT flow-test.yaml 
```

takes from application.properties.



## Prepare Minikube kamel

```sh
minikube start --addons registry
kamel install
```

 kubectl logs -f camel-k-operator-844bdcc954-p77xj

## Camel JBang using local kamelet

```sh
camel run --local-kamelet-dir=src/main/resources/kamelets --deps=org.kie.yard:yard-kdtable:1.0-SNAPSHOT  timer-source-binding.yaml 
```

