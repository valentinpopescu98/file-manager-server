# file-manager-server
Demo for EC2, S3, RDS, Lambda.

Spring Boot Back-End for [file-manager-client](https://github.com/valentinpopescu98/file-manager-client) React Front-End.

The app is the server of the File Manager app. The app is supposed to allow the user to upload files into the S3 bucket and the files metadata to the RDS database. A Lambda Function is supposed to send an e-mail to the registered user when the upload is complete.

How to run:
1. scp -r -i ~/.ssh/file-manager-key.pem /path/to/file-manager-server user@host:~/
2. ssh -i ~/.ssh/file-manager-key.pem user@host
3. chmod u+x ~/file-manager-server/build-and-run.sh
4. ~/file-manager-server/build-and-run.sh
