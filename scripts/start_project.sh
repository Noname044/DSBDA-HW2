#!/bin/bash

/opt/hadoop-2.10.1/sbin/start-dfs.sh

if $(/opt/hadoop-2.10.1/bin/hdfs dfs -test -d input); then
	/opt/hadoop-2.10.1/bin/hdfs dfs -rm -r /user/root/input 
fi

flume-ng agent --conf $FLUME_HOME/conf/ \
-f $FLUME_HOME/conf/flume.conf \
-Dflume.root.logger=DEBUG,console \
--no-reload-conf \
-n agent

# проверка, что флюм остановился 
# 
# flag=0
# while [ $flag -en 0]; do
# 	tmp=$HADOOP_HOME/bin/hdfs dfs -ls input/att* | tail -n 1 | awk '{print $8}' | cut -d '/' -f3 | cut -d '.' -f3
# 	if [ -z $tmp ]; then
# 		flag=1
# 	fi
# done
# 

spark-submit --class bdtc.SparkSQLApplication \
            --master local \
            --deploy-mode client \
            --executor-memory 1g \
            --name statisticCount \
            --conf "spark.app.id=SparkSQLApplication" \
            /project/lab2-1.0-SNAPSHOT-jar-with-dependencies.jar \
            hdfs://localhost:9000/user/root/input/attendances/ \
            hdfs://localhost:9000/user/root/input/publications/ \
            hdfs://localhost:9000/user/root/output

echo -e "\nFINISH.\n\n/opt/hadoop-2.10.1/bin/hdfs dfs -ls output:"

/opt/hadoop-2.10.1/bin/hdfs dfs -ls output

echo -e "\nFIRST 20 string:\n"
/opt/hadoop-2.10.1/bin/hdfs dfs -cat output/part* | head -n 20