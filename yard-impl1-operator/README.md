// TODO consider removing /index.html from imported kogito jitrunner dep

kn quickstart minikube

// per Knative tutorial
minikube tunnel --profile knative

minikube profile list
minikube profile knative
minikube dashboard

kubectl apply -f k8s/helloyard.yaml
kubectl get ksvc
