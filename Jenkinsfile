pipeline {
    // Chạy trên bất kỳ agent nào có sẵn
    agent any

    // Khai báo các công cụ đã cấu hình trong Jenkins (phải khớp tên chính xác)
    tools {
        maven 'Maven 3'
        snyk 'snyk'
    }

    stages {
        // Giai đoạn 1: Quét lộ lọt thông tin nhạy cảm (Mật khẩu, Token, Key...)
        stage('Gitleaks Scan') {
            steps {
                echo 'Bắt đầu quét Gitleaks...'
                // Chạy Gitleaks qua Docker (Thêm || true để pipeline không dừng ngay nếu phát hiện lỗi ở bài Lab này)
                sh "docker run --rm -v ${WORKSPACE}:/path zricethezav/gitleaks:latest detect --source=/path --report-format=json --report-path=/path/gitleaks-report.json || true"
            }
        }

        // Giai đoạn 2: Quét lỗ hổng bảo mật thư viện mã nguồn mở
        stage('Snyk Scan') {
            steps {
                echo 'Bắt đầu quét Snyk...'
                // Lấy snyk-token từ Credentials của Jenkins
                withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                    // Xác thực Snyk
                    sh 'snyk auth ${SNYK_TOKEN}'
                    // Quét toàn bộ dự án
                    sh 'snyk test --all-projects || true'
                }
            }
        }

        // Giai đoạn 3: Phân tích chất lượng code và Đợi kết quả Quality Gate
        stage('SonarQube Analysis & Quality Gate') {
            steps {
                echo 'Bắt đầu quét SonarCloud và đợi kết quả...'
                // Lấy sonar-token từ Credentials của Jenkins
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    // Chạy lệnh Maven kèm tham số đợi kết quả (-Dsonar.qualitygate.wait=true)
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

    // Hành động sau khi Pipeline chạy xong
    post {
        always {
            echo 'Quy trình DevSecOps Pipeline đã hoàn tất!'
        }
    }
}
