# demo-aws
Demo for EC2, S3, RDS, Lambda.

The app is supposed to allow the user to upload files into the S3 bucket and the files metadata to the RDS database. A Lambda Function is supposed to send an e-mail to the registered user when the upload is complete.

How to run:
1. scp -r -i ~/.ssh/ec2-ssh-key.pem /path/to/demo-aws user@host:~/
2. ssh -i ~/.ssh/ec2-ssh-key.pem user@host
3. chmod u+x ~/demo-aws/build-and-run.sh
4. ~/demo-aws/build-and-run.sh