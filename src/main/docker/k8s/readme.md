## install microk8s

## install addon

```bash
microk8s enable dns storage
microk8s enable dashboard ingress
microk8s kubectl create token default
microk8s kubectl port-forward -n kube-system service/kubernetes-dashboard 10443:443
```



### setup docker to use the local registry

```bash
microk8s enable registry
```

Pushing to this insecure registry may fail in some versions of Docker unless the daemon is explicitly configured to trust this registry. To address this we need to edit /etc/docker/daemon.json and add:

```json
{
  "insecure-registries" : ["localhost:32000"]
}
```

The new configuration should be loaded with a Docker daemon restart:

```bash
sudo systemctl restart docker
```

## build your image locally

```bash
git clone -b develop https://github.com/correctexam/corrigeExamBack
cd corrigeExamBack/src/main/docker
docker-compose -f app.yml build --no-cache  back front
docker image tag barais/correctexam-back:latest localhost:32000/barais/correctexam-back:latest
docker image tag barais/correctexam-front:latest localhost:32000/barais/correctexam-front:latest
docker image push localhost:32000/barais/correctexam-back:latest
docker image push localhost:32000/barais/correctexam-front:latest
```


## Deploy you app 

```bash
# from where you clone your project
cd src/main/docker/k8s
microk8s kubectl apply -f .
```

You can then access the Dashboard at https://127.0.0.1:10443.

and the app at http://localhost


If you want to access the php myadmin service

```bash
microk8s kubectl port-forward service/myadmin 8082:80
```
and you can access to phpmyadmin at http://127.0.0.1:8082
