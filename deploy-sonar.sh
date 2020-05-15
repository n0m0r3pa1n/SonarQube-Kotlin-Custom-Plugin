eval 'mvn clean package'
eval 'rm -r ~/Downloads/sonarqube-8.3.0.34182/extensions/plugins/testperformance-plugin-8.1.0.jar'
eval 'cp ~/Workspace/examples/sonar-custom-plugin-example/target/testperformance-plugin-8.1.0.jar ~/Downloads/sonarqube-8.3.0.34182/extensions/plugins/'
eval '~/Downloads/sonarqube-8.3.0.34182/bin/linux-x86-64/sonar.sh restart'
sleep 60
eval 'cd ~/Workspace/android-project'
eval './gradlew sonarqube --scan --debug -Dsonar.host.url=http://localhost:9000   -Dsonar.login=9c0c8d6d4f74950d640cf348d8a12eb2953d6cc2 --info'
eval 'cd ~/Workspace/examples/sonar-custom-plugin-example/'
