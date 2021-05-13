#!/bin/bash

cd /home/mindstone/IdeaProjects/DataScience/DSBDA-HW2/scripts/
#python3 ./generator_logs.py --start-date 01.01.2021 --end-date 25.05.2021


docker rm -f DSBDA_HW2 #; docker rmi custom_centos:latest

docker build --rm -t custom_centos - < Dockerfile
docker run --privileged -d -p 50070:50070 -ti -e container=docker --name=DSBDA_HW2 -v /sys/fs/cgroup:/sys/fs/cgroup  custom_centos /usr/sbin/init

docker cp ../input_data DSBDA_HW2:/project/input_data/
docker cp ./start_project.sh DSBDA_HW2:/project/
docker cp ../target/lab2-1.0-SNAPSHOT-jar-with-dependencies.jar DSBDA_HW2:/project/
docker cp ./flume.conf DSBDA_HW2:/opt/apache-flume-1.9.0-bin/conf/

docker exec -it DSBDA_HW2 /opt/hadoop-2.10.1/bin/hdfs namenode -format
docker exec -it DSBDA_HW2 /opt/hadoop-2.10.1/sbin/start-dfs.sh

echo -e '\n\nWhen you enter the container, \nrun the script /project/start_project.sh\n\n'

docker exec -it DSBDA_HW2 /bin/bash