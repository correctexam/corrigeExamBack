# Credentials of a deployment

`application.properties`, the Docker examples and the Kubernetes examples are public. Every value written in them
is known to anyone: a production deployment must set its own credentials through environment variables.

| Setting | Environment variable | Public value of the repository |
|---|---|---|
| MySQL password (`%prod.quarkus.datasource.password`) | `DB_PASSWORD` | `test` |
| MinIO access key (`quarkus.minio.access-key`) | `QUARKUS_MINIO_ACCESS_KEY` | `admin` |
| MinIO secret key (`quarkus.minio.secret-key`) | `QUARKUS_MINIO_SECRET_KEY` | `minioadmin` |
| RabbitMQ user (`rabbitmq-username`) | `RABBITMQ_USERNAME` | `rabbitmq` |
| RabbitMQ password (`rabbitmq-password`) | `RABBITMQ_PASSWORD` | `rabbitmq` |
| JWT key pair | `JWT_PRIVATE_KEY_LOCATION`, `JWT_PUBLIC_KEY_LOCATION` | see [jwt-keys.md](jwt-keys.md) |
| ALTCHA key of the student portal | `ALTCHA_HMAC_KEY` | none (random key per instance) |

At startup of the `prod` profile, `DefaultCredentialsCheck` logs an error for every setting still equal to its
public value (the MinIO keys only when `correctexam.uses3=true`). The application still starts: change the value on
the deployment, the services (MySQL, MinIO, RabbitMQ) first, then the environment variable.

## Accounts created by Liquibase

The initial data create the accounts `admin`, `user`, `system` and `anonymoususer` with public passwords
(`admin`/`admin`, `user`/`user`…). To list the accounts that still have one of them:

```sql
SELECT login, activated
FROM jhi_user
WHERE password_hash IN (
  '$2a$10$mE.qmcV0mFU5NcKh73TZx.z4ueI/.bDWbj0T1BYyqP481kGGarKLG',
  '$2a$10$j8S5d7Sr7.8VTOYNviDPOeWX8KcYILUVJBsYV83Y5NtECayypx9lO',
  '$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC',
  '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K'
);
```

For each of them, change the password (account page, or the password reset by e-mail), or deactivate the account:

```sql
UPDATE jhi_user SET activated = 0 WHERE login IN ('user', 'system', 'anonymoususer');
```
