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

# Run minio on k8s

```bash
cd src/main/docker/k8s
mkdir /data # local folder to put miniodata, if you change it, change it int minio-dev.yaml
microk8s kubectl apply -f namespace.yaml
microk8s kubectl apply -f minio-dev.yaml
```


# connect your Browser to the MinIO Server

```bash
microk8s kubectl port-forward -n doodle service/minio 9000 9090
```

Access the MinIO Console by opening a browser on the local machine and navigating to http://127.0.0.1:9090.

Log in to the Console with the credentials minioadmin | minioadmin. These are the default root user credentials.





## build your image locally

```bash
git clone -b develop https://github.com/doodle/corrigeExamBack
cd corrigeExamBack/src/main/docker
docker-compose -f app.yml build --no-cache  back front
docker image tag barais/doodle-back:latest localhost:32000/barais/doodle-back:latest
docker image tag barais/doodle-front:latest localhost:32000/barais/doodle-front:latest
docker image push localhost:32000/barais/doodle-back:latest
docker image push localhost:32000/barais/doodle-front:latest
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
microk8s kubectl port-forward -n doodle service/myadmin 8082:80
```
and you can access to phpmyadmin at http://127.0.0.1:8082

### remove everything

```bash
microk8s kubectl delete deploy -n doodle  doodle-mysql  back front maildev myadmin
microk8s kubectl delete service -n doodle doodle-mysql  back front maildev myadmin
microk8s kubectl delete ingress -n doodle doodle
microk8s kubectl delete configmaps -n doodle  mysqlinit-cfgmap
microk8s kubectl delete pod -n doodle minio 
microk8s kubectl delete service -n doodle minio 
microk8s kubectl delete namespace  doodle
```
