pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = 'dockerhub-credentials'
        IMAGE_NAME = 'hgray1591/backend-app'  // DockerHub 계정명에 맞게 수정
        REMOTE_USER = 'ubuntu'
        REMOTE_HOST = '43.202.229.52'  // 배포할 서버 IP
        CONTAINER_NAME = 'backend-app'
        PORT = '8080'
        MONGODB_HOST = 'mongodb'  // MongoDB 컨테이너 이름 또는 호스트
        MONGODB_PORT = '27017'
        JWT_SECRET = credentials('jwt-secret')  // Jenkins Credentials에 등록 필요
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/devops3sogang/Backend.git', branch: 'main'  // GitHub 저장소 URL
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    // Docker 빌드
                    docker.build("${IMAGE_NAME}:latest")
                }
            }
        }

        stage('Push to DockerHub') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKERHUB_CREDENTIALS) {
                        docker.image("${IMAGE_NAME}:latest").push()
                    }
                }
            }
        }

        stage('Deploy to Server') {
            steps {
                script {
                    sshagent(credentials: ['admin']) {
                        sh """
                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} " \
                            echo '--- Starting Backend Deployment ---' && \
                            docker stop ${CONTAINER_NAME} || true && \
                            docker rm ${CONTAINER_NAME} || true && \
                            echo '--- Pulling latest backend image ---' && \
                            docker pull ${IMAGE_NAME}:latest && \
                            echo '--- Running new backend container ---' && \
                            docker run -d --name ${CONTAINER_NAME} --restart unless-stopped \
                                -p ${PORT}:8080 \
                                -e SPRING_DATA_MONGODB_URI=mongodb://${MONGODB_HOST}:${MONGODB_PORT}/campus_food \
                                -e JWT_SECRET='${JWT_SECRET}' \
                                --network devops-network \
                                ${IMAGE_NAME}:latest && \
                            echo '--- Pruning old images ---' && \
                            docker image prune -f && \
                            echo '--- Backend deployment successful ---' && \
                            echo '--- Backend is now accessible at http://${REMOTE_HOST}:${PORT} ---' \
                        "
                        """
                    }
                }
            }
        }

        stage('Health Check') {
            steps {
                sh '''
                    echo "Waiting for server to be ready..."
                    sleep 10

                    echo "Checking if backend is accessible..."
                    curl -f http://${REMOTE_HOST}:${PORT}/api/swagger-ui/index.html || exit 1
                    echo "Backend is running successfully!"
                '''
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully! Backend is live at http://${REMOTE_HOST}:${PORT}"
            echo "API Documentation: http://${REMOTE_HOST}:${PORT}/api/swagger-ui/index.html"
        }
        failure {
            echo 'Pipeline failed. Check the logs for details.'
        }
        always {
            echo 'Pipeline execution finished.'
        }
    }
}
