pipeline {
    agent any

    tools {
        maven 'Maven 3'
        snyk 'snyk'
    }

    stages {

        // ================================================================
        // STAGE 1: Phát hiện thay đổi - tính toán 1 lần, dùng cho tất cả stage
        // - Hỗ trợ cả PR build (so sánh với branch target) lẫn branch build
        // - Kết quả lưu vào env.MEDIA_CHANGED để các stage sau dùng lại
        // ================================================================
        stage('Detect Changes') {
            steps {
                script {
                    // Nếu là PR build → CHANGE_TARGET = "main", so sánh toàn bộ PR
                    // Nếu là branch build → so sánh với commit trước (HEAD~1)
                    def baseRef = env.CHANGE_TARGET
                        ? "origin/${env.CHANGE_TARGET}"
                        : "HEAD~1"

                    def changedFiles = sh(
                        script: """
                            git fetch origin ${env.CHANGE_TARGET ?: 'main'} 2>/dev/null || true
                            git diff --name-only ${baseRef}...HEAD 2>/dev/null \
                                || git diff-tree --no-commit-id -r --name-only HEAD
                        """,
                        returnStdout: true
                    ).trim()

                    echo "=== [Monorepo] Files changed in this build ==="
                    echo changedFiles ?: "(no files detected)"
                    echo "=============================================="

                    // Lưu kết quả vào biến môi trường để dùng ở các stage sau
                    env.MEDIA_CHANGED = changedFiles.contains('media/') ? 'true' : 'false'

                    echo "MEDIA_CHANGED = ${env.MEDIA_CHANGED}"
                }
            }
        }

        // ================================================================
        // STAGE 2: Quét lộ lọt thông tin nhạy cảm (Gitleaks)
        // - Dùng binary trực tiếp thay Docker → không cần Docker CLI
        // - Luôn chạy trên toàn bộ repo
        // ================================================================
        stage('Gitleaks Scan') {
            steps {
                echo 'Bắt đầu quét Gitleaks...'
                sh """
                    # Tải binary gitleaks nếu chưa có (không cần Docker)
                    if ! command -v gitleaks &>/dev/null; then
                        echo 'Tải Gitleaks binary...'
                        curl -sSfL https://github.com/gitleaks/gitleaks/releases/download/v8.21.2/gitleaks_8.21.2_linux_x64.tar.gz \
                            -o /tmp/gitleaks.tar.gz 2>/dev/null \
                            && tar -xzf /tmp/gitleaks.tar.gz -C /tmp gitleaks 2>/dev/null \
                            && chmod +x /tmp/gitleaks || true
                        GITLEAKS_CMD=/tmp/gitleaks
                    else
                        GITLEAKS_CMD=gitleaks
                    fi

                    # Chạy quét
                    \$GITLEAKS_CMD detect --source=. \
                        --report-format=json \
                        --report-path=gitleaks-report.json \
                        --exit-code=0 || true
                    echo 'Gitleaks hoàn tất.'
                """
            }
            post {
                always {
                    // Lưu báo cáo Gitleaks như artifact
                    archiveArtifacts artifacts: 'gitleaks-report.json',
                                     allowEmptyArchive: true
                }
            }
        }

        // ================================================================
        // STAGE 3: Quét lỗ hổng bảo mật thư viện (Snyk)
        // - || true để pipeline không dừng nếu Snyk chưa được cài
        // - Luôn chạy toàn bộ monorepo
        // ================================================================
        stage('Snyk Scan') {
            steps {
                echo 'Bắt đầu quét Snyk...'
                withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                    sh 'snyk auth ${SNYK_TOKEN} || true'
                    sh 'snyk test --all-projects || true'
                }
            }
        }

        // ================================================================
        // STAGE 4: Unit Test - Media Service          [Yêu cầu 5 + 6]
        // - Tách riêng phase Test (mvn clean test), không gộp với Build
        // - [Yêu cầu 6] Dùng env.MEDIA_CHANGED tính từ stage Detect Changes
        // ================================================================
        stage('Test - Media Service') {
            when {
                environment name: 'MEDIA_CHANGED', value: 'true'
            }
            steps {
                echo '[Yêu cầu 5] Chạy Unit Test riêng biệt cho Media Service...'
                dir('media') {
                    sh 'mvn clean test'
                }
            }
            post {
                always {
                    // [Yêu cầu 5] Upload kết quả JUnit lên Jenkins UI
                    junit allowEmptyResults: true,
                          testResults: 'media/target/surefire-reports/*.xml'
                }
            }
        }

        // ================================================================
        // STAGE 5: Build Artifact - Media Service     [Yêu cầu 5 + 6]
        // - Tách riêng phase Build, chạy SAU khi Test pass
        // ================================================================
        stage('Build - Media Service') {
            when {
                environment name: 'MEDIA_CHANGED', value: 'true'
            }
            steps {
                echo '[Yêu cầu 5] Build artifact (test đã pass ở stage trước)...'
                dir('media') {
                    sh 'mvn package -DskipTests'
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'media/target/*.jar',
                                     allowEmptyArchive: true
                }
            }
        }

        // ================================================================
        // STAGE 6: SonarQube Analysis - Media Service [Yêu cầu 6]
        // - Chỉ quét service bị thay đổi, không quét toàn bộ monorepo
        // ================================================================
        stage('SonarQube Analysis & Quality Gate') {
            when {
                environment name: 'MEDIA_CHANGED', value: 'true'
            }
            steps {
                echo 'Phân tích SonarCloud cho Media Service...'
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    dir('media') {
                        sh """
                            mvn verify sonar:sonar \
                            -Dsonar.projectKey=yas-ci-key \
                            -Dsonar.organization=devops-yas-ci \
                            -Dsonar.host.url=https://sonarcloud.io \
                            -Dsonar.token=${SONAR_TOKEN} \
                            -Dsonar.qualitygate.wait=true
                        """
                    }
                }
            }
        }

    }

    post {
        always {
            echo 'DevSecOps Pipeline hoàn tất!'
        }
        success {
            echo "Tất cả stage PASSED. MEDIA_CHANGED=${env.MEDIA_CHANGED}"
        }
        failure {
            echo 'Pipeline FAILED — kiểm tra log ở stage bị đỏ.'
        }
    }
}
