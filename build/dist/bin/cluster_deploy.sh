for line in `cat server_list.txt`
do
echo $line
hostName=`echo $line | awk -F"_" '{ print $1 }'`
ipaddr=`echo $line | awk -F"_" '{ print $2 }'`
echo "hostName: "$hostName"|"$ipaddr
scp /home/xiafan/workspace/cassandraxml/build/apache-cassandra-1.0.0-rc1-SNAPSHOT-bin.tar.gz xiafan@$hostName:/home/xiafan
ssh xiafan@$hostName "rm -rf apache-cassandra-1.0.0-rc1-SNAPSHOT && rm -rf var && tar -zxvf apache-cassandra-1.0.0-rc1-SNAPSHOT-bin.tar.gz && exit"
echo "cd apache-cassandra-1.0.0-rc1-SNAPSHOT/conf && sed -e 's/seeds: \"[^\"]*\"/seeds: \"10.11.1.202\"/g' -e 's/listen_address:[^\n]*/listen_address:$ipaddr/g' -e 's/xml_rpc_address: mc[0-9]*/xml_rpc_address: $hostName/g' cassandra.yaml > cassandra.yaml && exit" 
ssh xiafan@$hostName "cd /home/xiafan/apache-cassandra-1.0.0-rc1-SNAPSHOT/conf && mv cassandra.yaml cassandra.yaml.bk && sed -e 's/seeds: \"[^\"]*\"/seeds: \"10.11.1.202\"/g' -e 's/listen_address:[^\n]*/listen_address: $ipaddr/g' -e 's/xml_rpc_address: mc[0-9]*/xml_rpc_address: $hostName/g' cassandra.yaml.bk > cassandra.yaml && rm cassandra.yaml.bk && exit" 

done
