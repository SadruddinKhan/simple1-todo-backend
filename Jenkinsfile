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

        stage("Welcome stage"){
            steps{
                sh '''
                echo "Hello, Pipeline for "${PROJECT_NAME}" started..."

                '''

                sh '''
                echo "Build Number is "${BUILD_NUMBER}""

                '''
            }

        }
    }

}