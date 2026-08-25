pipeline { 
    agent any

    environment{
        PROJECT_NAME="todo-backend"
        DOCKER_IMAGE="SadruddinKhan/simple1-todo-backend"
        DOCKER_TAG="${BUILD_NUMBER}"

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
           
           docker image prune -af

           docker build \
            --platform linux/arm64 \
           -t ${DOCKER_IMAGE}:${DOCKER_TAG} .

           docker images
          
          '''  
       }
    }       
    }

}