FROM centos:latest

ENV container docker

EXPOSE 50070 50070

VOLUME /sys/fs/cgroup /sys/fs/cgroup

RUN yum -y install systemd; yum clean all; \
(cd /lib/systemd/system/sysinit.target.wants/; for i in *; do [ $i == systemd-tmpfiles-setup.service ] || rm -f $i; done); \
rm -f /lib/systemd/system/multi-user.target.wants/*;\
rm -f /etc/systemd/system/*.wants/*;\
rm -f /lib/systemd/system/local-fs.target.wants/*; \
rm -f /lib/systemd/system/sockets.target.wants/*udev*; \
rm -f /lib/systemd/system/sockets.target.wants/*initctl*; \
rm -f /lib/systemd/system/basic.target.wants/*;\
rm -f /lib/systemd/system/anaconda.target.wants/*;

VOLUME [ "/sys/fs/cgroup" ]

# Updating and installing instruments
RUN yum update -y --noplugins && yum install -y --noplugins wget java which epel-release && yum install -y --noplugins htop

# Downloading Hadoop
RUN wget https://apache-mirror.rbc.ru/pub/apache/hadoop/common/hadoop-2.10.1/hadoop-2.10.1.tar.gz && tar -zxf hadoop-2.10.1.tar.gz -C /opt/ && rm -rf hadoop-2.10.1.tar.gz

# Installing Yarn
#RUN curl --silent --location https://rpm.nodesource.com/setup_14.x | bash - && yum install -y nodejs && curl --silent --location https://dl.yarnpkg.com/rpm/yarn.repo | tee /etc/yum.repos.d/yarn.repo && rpm --import https://dl.yarnpkg.com/rpm/pubkey.gpg && yum install -y yarn

# Configuring Hadoop
RUN sed -i '/export JAVA_HOME=${JAVA_HOME}/a  export JAVA_HOME=$(readlink -f /usr/bin/java \| sed "s:bin/java::")' /opt/hadoop-2.10.1/etc/hadoop/hadoop-env.sh && sed -i '/export JAVA_HOME=${JAVA_HOME}/D' /opt/hadoop-2.10.1/etc/hadoop/hadoop-env.sh

RUN echo "export PATH=/opt/hadoop-2.10.1/bin:$PATH" | tee -a /etc/profile

RUN sed -i '/<configuration>/a    <property>???\
        <name>fs.defaultFS</name>???\
        <value>hdfs://localhost:9000</value>???\
    </property>' /opt/hadoop-2.10.1/etc/hadoop/core-site.xml && sed -i "s/???/\\n/g" /opt/hadoop-2.10.1/etc/hadoop/core-site.xml

RUN sed -i '/<configuration>/a    <property>???\
        <name>dfs.replication</name>???\
        <value>1</value>???\
    </property>' /opt/hadoop-2.10.1/etc/hadoop/hdfs-site.xml && sed -i "s/???/\\n/g" /opt/hadoop-2.10.1/etc/hadoop/hdfs-site.xml

RUN yum install -y openssh-server openssh-clients && ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa && cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys && chmod 0600 ~/.ssh/authorized_keys

RUN mv /opt/hadoop-2.10.1/etc/hadoop/mapred-site.xml.template /opt/hadoop-2.10.1/etc/hadoop/mapred-site.xml 

RUN sed -i '/<configuration>/a    <property>???\
        <name>mapreduce.framework.name</name>???\
        <value>yarn</value>???\
    </property>' /opt/hadoop-2.10.1/etc/hadoop/mapred-site.xml && sed -i "s/???/\\n/g" /opt/hadoop-2.10.1/etc/hadoop/mapred-site.xml

RUN sed -i '/<configuration>/a    <property>???\
        <name>yarn.nodemanager.aux-services</name>???\
        <value>mapreduce_shuffle</value>???\
    </property>' /opt/hadoop-2.10.1/etc/hadoop/yarn-site.xml && sed -i "s/???/\\n/g" /opt/hadoop-2.10.1/etc/hadoop/yarn-site.xml

ENV HADOOP_LIBEXEC_DIR=/opt/hadoop-2.10.1/libexec

# Installing and configure FLume
RUN wget https://apache-mirror.rbc.ru/pub/apache/flume/1.9.0/apache-flume-1.9.0-bin.tar.gz && tar -zxf apache-flume-1.9.0-bin.tar.gz -C /opt/ && rm -rf apache-flume-1.9.0-bin.tar.gz

ENV FLUME_HOME=/opt/apache-flume-1.9.0-bin 
ENV PATH=$PATH:$FLUME_HOME/bin 
ENV CLASSPATH=$CLASSPATH:$FLUME_HOME/lib/*

RUN mv /opt/apache-flume-1.9.0-bin/conf/flume-conf.properties.template /opt/apache-flume-1.9.0-bin/conf/flume-conf.properties && mv /opt/apache-flume-1.9.0-bin/conf/flume-env.sh.template /opt/apache-flume-1.9.0-bin/conf/flume-env.sh

RUN sed -i "s/# export JAVA_HOME=\/usr\/lib\/jvm\/java-8-oracle/export JAVA_HOME=\/usr/g" /opt/apache-flume-1.9.0-bin/conf/flume-env.sh


# Installing and configure FLume
RUN wget https://archive.apache.org/dist/spark/spark-2.3.1/spark-2.3.1-bin-hadoop2.7.tgz && tar -zxf spark-2.3.1-bin-hadoop2.7.tgz -C /opt/

ENV SPARK_HOME=/opt/spark-2.3.1-bin-hadoop2.7
ENV PATH=$PATH:$SPARK_HOME/bin/
ENV FLUME_CLASSPATH=$FLUME_HOME/lib/
ENV HADOOP_HOME=/opt/hadoop-2.10.1/

RUN yes | cp /opt/hadoop-2.10.1/share/hadoop/common/*.jar $FLUME_HOME/lib/; yes | cp /opt/hadoop-2.10.1/share/hadoop/common/lib/*.jar $FLUME_HOME/lib/

RUN mkdir -p /project

CMD ["/usr/sbin/init"]

#flume-ng agent --conf $FLUME_HOME/conf/ -f $FLUME_HOME/conf/flume.conf -Dflume.root.logger=DEBUG,console -n agent

#spark-submit --class ru.mephi.spark.AirportCompute --master yarn --deploy-mode cluster target/Airport-jar-with-dependencies.jar

# spark-submit --class bdtc.SparkSQLApplication \
#             --master local \
#             --deploy-mode client \
#             --executor-memory 1g \
#             --name statisticCount \
#             --conf "spark.app.id=SparkSQLApplication" \
#             /project/lab2-1.0-SNAPSHOT-jar-with-dependencies.jar \
#             hdfs://localhost:9000/user/root/input/attendances/ \
#             hdfs://localhost:9000/user/root/input/publications/ output