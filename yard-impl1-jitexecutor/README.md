mvn clean package -Dquarkus.container-image.push=true

IFS= read -rd '' output < <(cat demo.yml)
output=$output yq -i '.spec.template.spec.containers[0].env[0].name = "myYaRD" | .spec.template.spec.containers[0].env[0].value = strenv(output)' target/kubernetes/knative.yml 
 
kubectl apply -f target/kubernetes/knative.yml 

IFS= read -rd '' output < <(cat demo.yml)
output=$output docker-compose up
