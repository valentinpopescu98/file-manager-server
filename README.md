# file-manager-server
Demo for Amazon EC2, S3, RDS, Lambda.

Spring Boot Back-End for [file-manager-client](https://github.com/valentinpopescu98/file-manager-client) React Front-End.

The app is the server of the File Manager app. The app is supposed to allow the user to upload files into the S3 bucket and the files metadata to the RDS database. A Lambda Function is supposed to send an e-mail to the registered user when the upload is complete.

How to run:
1. Create RDS PostgreSQL database + S3 bucket + EC2 instance; have a ready a GitHub private key
2. scp -i ~/.ssh/file-manager-key.pem ~/.ssh/id_rsa user@host:~/.ssh/
3. ssh -i ~/.ssh/file-manager-key.pem user@host
4. chmod 600 ~/.ssh/id_rsa
5. git clone git@github.com:valentinpopescu98/file-manager-server.git ~/file-manager-server/
6. Add file in src/main/resources named application-secret.properties with the following fields:
   - spring.security.oauth2.client.registration.google.client-id={client ID from Google Cloud Console}
   - spring.security.oauth2.client.registration.google.client-secret={client secret from Google Cloud Console}
6. (Optional, for dev environment) Add flag for FileManagerServerApplication as '-Dspring.profiles.active=dev' at environment variables
7. ~/file-manager-server/build-and-run.sh

---

- file-manager-key.pem = EC2 private key (should be saved in ~/.ssh/)
- id_rsa = GitHub private key (should be saved in ~/.ssh/)
- user = EC2 user to connect to (for ubuntu it is 'ubuntu')
- host = EC2 instance public IP
