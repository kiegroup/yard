kubectl apply -f broker.yml 
kubectl apply -f trigger.yml -f subscriber.yml 
kubectl apply -f trigger-demo.yml 

kubectl run curl \
    --image=curlimages/curl --rm=true --restart=Never -ti -- \
    -X POST -v \
    -H "content-type: application/json"  \
    -H "ce-specversion: 1.0"  \
    -H "ce-source: my/curl/command"  \
    -H "ce-type: my.demo.event"  \
    -H "ce-id: 0815"  \
    -d '{"Age": 47, "Previous incidents?": true}' \
    http://broker-ingress.knative-eventing.svc.cluster.local/mmortari-dev/my-broker
