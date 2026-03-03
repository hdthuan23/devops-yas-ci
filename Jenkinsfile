pipeline {
    agent any

    tools {
        maven 'Maven 3'
        snyk 'snyk'
    }

    stages {

        // ================================================================
        // STAGE 1: Quét lộ lọt thông tin nhạy cảm (Gitleaks)
        // Luôn chạy trên toàn bộ repo, bất kể service nào thay đổi
        // ================================================================
        stage('Gitleaks Scan') {
            steps {
                echo 'Bắt đầu quét Gitleaks...'
                sh "docker run --rm -v ${WORKSPACE}:/path zricethezav/gitleaks:latest detect --source=/path --report-format=json --report-path=/path/gitleaks-report.json || true"
            }
        }

        // ================================================================
        // STAGE 2: Quét lỗ hổng bảo mật thư viện (Snyk)
        // Luôn chạy — quét toàn bộ monorepo một lần
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
        // STAGE 3: Unit Test - Media Service          [Yêu cầu 5 + 6]
        // - Tách riêng phase Test (mvn clean test), không gộp với Build
        // - [Yêu cầu 6] Logic Monorepo: dùng git diff-tree thay changeset
        //   vì changeset không hoạt động với PR build (empty changelog)
        // ================================================================
        stage('Test - Media Service') {
            when {
                expression {
                    def changedFiles = sh(
                        script: "git diff-tree --no-commit-id -r --name-only HEAD",
                        returnStdout: true
                    ).trim()
                    echo "[Monorepo Check] Files changed: ${changedFiles}"
                    return changedFiles.contains('media/')
                }
            }
            steps {
                echo '[Yêu cầu 5] Chạy Unit Test riêng biệt cho Media Service...'
                dir('media') {
                    // Chỉ chạy test, chưa đóng gói — tách biệt với stage Build
                    sh 'mvn clean test'
                }
            }
            post {
                always {
                    // [Yêu cầu 5] Upload kết quả JUnit XML lên Jenkins UI
                    // Hiển thị biểu đồ test trends trên trang build
                    junit allowEmptyResults: true,
                          testResults: 'media/target/surefire-reports/*.xml'
                }
            }
        }

        // ================================================================
        // STAGE 4: Build Artifact - Media Service     [Yêu cầu 5 + 6]
        // - Tách riêng phase Build (mvn package), chạy SAU khi Test pass
        // - [Yêu cầu 6] Chỉ chạy khi media/ thay đổi (không build vô ích)
        // ================================================================
        stage('Build - Media Service') {
            when {
                expression {
                    def changedFiles = sh(
                        script: "git diff-tree --no-commit-id -r --name-only HEAD",
                        returnStdout: true
                    ).trim()
                    return changedFiles.contains('media/')
                }
            }
            steps {
                echo '[Yêu cầu 5] Build artifact cho Media Service (bỏ qua test vì đã chạy ở stage trước)...'
                dir('media') {
                    // -DskipTests vì test đã được chạy và xác nhận ở stage Test
                    sh 'mvn package -DskipTests'
                }
            }
            post {
                success {
                    // Lưu file JAR như artifact để có thể download từ Jenkins UI
                    archiveArtifacts artifacts: 'media/target/*.jar',
                                     allowEmptyArchive: true
                }
            }
        }

        // ================================================================
        // STAGE 5: SonarQube Analysis - Media Service  [Yêu cầu 6]
        // - [Yêu cầu 6] Chỉ quét service bị thay đổi, không quét toàn bộ
        // ================================================================
        stage('SonarQube Analysis & Quality Gate') {
            when {
                expression {
                    def changedFiles = sh(
                        script: "git diff-tree --no-commit-id -r --name-only HEAD",
                        returnStdout: true
                    ).trim()
                    return changedFiles.contains('media/')
                }
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
            echo 'Tất cả stage PASSED.'
        }
        failure {
            echo 'Pipeline FAILED — kiểm tra log ở stage bị đỏ.'
        }
    }
}
