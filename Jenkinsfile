pipeline {
  agent any

  tools {
    jdk 'jdk8'
    maven 'maven3'
    nodejs 'node20'
  }

  stages {
    stage('Backend Test') {
      steps {
        sh 'mvn -B test'
      }
    }

    stage('React Build') {
      steps {
        dir('frontend/react-portal') {
          sh 'npm ci'
          sh 'npm run build'
        }
      }
    }

    stage('Docker Build') {
      steps {
        sh 'docker compose build treasury-api react-portal'
      }
    }
  }
}
