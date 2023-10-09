curl -i -X GET -H 'Authorization: Basic dG9tY2F0OlBhc3N3MHJkIQ==' '10.128.14.53:8080/manager/text/undeploy?path=/si'
curl -i -X PUT -H 'Authorization: Basic dG9tY2F0OlBhc3N3MHJkIQ==' --data-binary @target/logicalis-si-1.0-SNAPSHOT.war '10.128.14.53:8080/manager/text/deploy?path=/si'
