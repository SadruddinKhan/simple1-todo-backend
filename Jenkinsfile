pipeline { 
    agent any

    environment{
        PROJECT_NAME="todo-backend"
        DOCKER_IMAGE="SadruddinKhan/simple1-todo-backend"
        DOCKER_TAG="${BUILD_NUMBER}"

    }

    stages{

        stage("Welcome stage"){
            step{
                echo 'Hello, Pipeline for "${PROJECT_NAME}" started...'
                echo 'Build Number is "${BUILD_NUMBER}"'
            }

        }
    }

}