# Run minio on k8s

```bash
microk8s kubectl apply -f minio-dev.yaml
```


# connect your Browser to the MinIO Server

```bash
microk8s kubectl port-forward -n correctexam service/minio 9000 9090
```

Access the MinIO Console by opening a browser on the local machine and navigating to http://127.0.0.1:9090.

Log in to the Console with the credentials minioadmin | minioadmin. These are the default root user credentials.

Creat a new user with specific acess right

You can use the MinIO Console for general administration tasks like Identity and Access Management, Metrics and Log Monitoring, or Server Configuration. Each MinIO server includes its own embedded MinIO Console.


# Connect with s3fs to check

Before you run s3fs, you will need to save your MinIO credentials in a file. In the command below, replace access_key and secret_key with your actual MinIO credentials.

```bash
echo "access_key:secret_key" > ~/.s3cred
chmod 600 ~/.s3cred 
```

Now create a directory to mount the bucket. I will use /s3 for this cookbook to keep it simple.


```bash
mkdir ~/s3
```

Run s3fs to mount the bucket from the MinIO server using the MinIO credentials from the previous command.


```bash
s3fs test1 ~/s3 -o passwd_file=~/.s3cred,use_path_request_style,url=http://localhost:9000
```
# delete pod

```bash
microk8s kubectl delete pod -n correctexam minio 
microk8s kubectl delete service -n correctexam minio 
```
