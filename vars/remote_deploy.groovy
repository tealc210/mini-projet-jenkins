#!/usr/bin/env groovy

def call(String DEPLOY_ENV="", String DOCKERHUB_USR="", String DOCKERHUB_PWD="", String IMAGE_NAME="", String IMAGE_TAG="", String DB_HOST="", String DB_USER="", String DB_PASS="") {
    steps {
        sshagent(credentials: ['SSHKEY']) {
            sh '''
            [ -d ~/.ssh ] || mkdir ~/.ssh && chmod 0700 ~/.ssh
            ssh-keyscan -t rsa,dsa,ed25519 ${DEPLOY_ENV} >> ~/.ssh/known_hosts
            command1="docker login -u $DOCKERHUB_USR -p $DOCKERHUB_PSW"
            command2="docker pull $DOCKERHUB_USR/$IMAGE_NAME:$IMAGE_TAG"
            command3="docker ps -a | grep $IMAGE_NAME && docker rm -f $IMAGE_NAME || echo 'app does not exist'"
            command4="docker run -d -p 80:8080 -e SPRING_DATASOURCE_USERNAME='${DB_USER}' -e SPRING_DATASOURCE_PASSWORD='${DB_PASS}' -e SPRING_DATASOURCE_URL='jdbc:mysql://${DB_HOST}:3306/db_paymybuddy' --name $IMAGE_NAME $DOCKERHUB_USR/$IMAGE_NAME:$IMAGE_TAG"
            ssh -t ubuntu@${DEPLOY_ENV} \
                -o SendEnv=IMAGE_NAME \
                -o SendEnv=IMAGE_TAG \
                -o SendEnv=DOCKERHUB_USR \
                -o SendEnv=DOCKERHUB_PSW \
                -C "$command1 && $command2 && $command3 && $command4 && sleep 30"
            '''
        }
    }
}