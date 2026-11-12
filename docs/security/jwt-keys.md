# JWT signing keys

CorrectExam signs its login tokens (JWT) with an RSA private key and checks them with the matching public key.
**Anyone holding the private key can sign a token for any user, administrators included.**

## The rule

- `src/main/resources/jwt/dev-only/` holds a key pair for development, tests and the offline standalone version.
  It is in the public repository: it protects nothing.
- Every deployment reachable from a network uses **its own** key pair, kept outside the image (secret, mounted file).
- At startup, `JwtKeyCheck` refuses:
  - the development pair, unless `correctexam.jwt.allow-dev-key=true` (set for the `dev`, `test`, `teste2e` and
    `alone` profiles only);
  - a public key that does not match the private key.

> Before November 2026 the repository contained `src/main/resources/jwt/privateKey.pem`, used by default by every
> build. A deployment built with it must be considered compromised: generate a new pair, deploy it, and check the
> accounts with the `ROLE_ADMIN` authority.

## Generating a key pair

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out privateKey.pem   # PKCS#8, as expected
openssl rsa -in privateKey.pem -pubout -out publicKey.pem
chmod 600 privateKey.pem
```

A PKCS#1 key (`-----BEGIN RSA PRIVATE KEY-----`) is rejected with a message explaining how to convert it:
`openssl pkcs8 -topk8 -nocrypt -in key.pem -out privateKey.pem`.

## Configuring the application

| Variable | Property | Example |
|---|---|---|
| `JWT_PRIVATE_KEY_LOCATION` | `jhipster.security.authentication.jwt.private-key.location` | `file:/run/secrets/jwt/privateKey.pem` |
| `JWT_PUBLIC_KEY_LOCATION` | `mp.jwt.verify.publickey.location` | `file:/run/secrets/jwt/publicKey.pem` |

Use the `file:` prefix for a file; without prefix, the location is read from the classpath.

### Docker

```bash
docker run \
  -v /etc/correctexam/jwt:/run/secrets/jwt:ro \
  -e JWT_PRIVATE_KEY_LOCATION=file:/run/secrets/jwt/privateKey.pem \
  -e JWT_PUBLIC_KEY_LOCATION=file:/run/secrets/jwt/publicKey.pem \
  correctexam-back
```

### Kubernetes

```bash
kubectl -n correctexam create secret generic correctexam-jwt \
  --from-file=privateKey.pem --from-file=publicKey.pem
```

then mount the secret and set the two variables, as in `src/main/docker/k8s/back-deployment.yaml`.

## Rotating the keys

Replace both files and restart. Every token in circulation becomes invalid: users log in again. With several
instances, deploy the same pair on all of them.

## Standalone version

The `alone` profile keeps the development pair because it is an offline tool for a laptop. It therefore only listens
on `127.0.0.1`. Do not expose it on a network (`QUARKUS_HTTP_HOST`) without first setting a key pair of your own and
`correctexam.jwt.allow-dev-key=false`.
