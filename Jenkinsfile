pipeline { 
    agent any

    environment{
        PROJECT_NAME="todo-backend"
        DOCKER_IMAGE="springbootmicro/simple-todo-backend"
        DOCKER_TAG="${BUILD_NUMBER}"
        EC2_HOST="13.203.227.65"
        EC2_USER="ubuntu"
        DOCKER_CONTAINER="todo-backend"
        AP_PORT="8082:8080"

    }

    stages{

        stage("Checkout"){
           steps{
              echo "checkout successful"
              echo "Testing from github..."
           }
        }

    stage("Test"){
       steps{
          sh '''
          chmod +x ./mvnw
          ./mvnw test
          '''  
       }
    } 

     stage("Build"){
       steps{
          sh '''
           ./mvnw clean package -DskipTests
          
          '''  
       }
    }

    stage("Docker Build"){
       steps{
          sh '''
           

           docker build \
            --platform linux/arm64 \
           -t ${DOCKER_IMAGE}:${DOCKER_TAG} .

          
          '''  
       }
    }

    stage("Docker Push"){
       steps{

          withCredentials([
              usernamePassword(
                  credentialsId: 'dockerhub-credentials',
                  usernameVariable: 'DOCKERHUB_USERNAME',
                  passwordVariable: 'DOCKERHUB_PASSWORD'
              )
          ])
          {

           sh '''

              echo "$DOCKERHUB_PASSWORD" | docker login \
              -u "$DOCKERHUB_USERNAME" \
              --password-stdin
          
              docker push ${DOCKER_IMAGE}:${DOCKER_TAG}

              docker image prune -af

              docker images 
             
            '''  

          }
          
       }
    }
    stage("EC2 Deploy"){
      steps{

         sshagent([
            'ec2-instance-key'
         ]){
           

           //code goes here

           sh '''
            
           mkdir -p ~/.ssh
              chmod 700 ~/.ssh
              ssh-keyscan -H "$EC2_HOST" >> ~/.ssh/known_hosts

            ssh $EC2_USER@EC2_HOST "
            docker rm -f $DOCKER_CONTAINER || true

            docker pull $DOCKER_IMAGE:$DOCKER_TAG

            docker run -d \
                --name $DOCKER_CONTAINER \
                -p $APP_PORT \
                --restart unless-stopped \
                $DOCKER_IMAGE:$DOCKER_TAG

                docker image prune -f
                "
           '''

         }
      }
    }              
    }

}