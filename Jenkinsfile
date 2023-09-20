pipeline {
    agent {
        label 'gsb'
    }

    options {
        disableConcurrentBuilds() //Maximal ein Build pro Branch zur gleichen Zeit
        buildDiscarder(logRotator(numToKeepStr: '30'))
    }

    stages {
        stage("Assemble") {
            steps {
                sh "./gradlew assemble"
                dir('test-projects') {
                    sh "./gradlew assemble"
                }
            }
        }
        stage("Publish") {
            steps {
                sh "./gradlew publish"
            }
        }
        stage("Check") {
            steps {
                sh "./gradlew check --continue"
                dir('test-projects') {
                    sh "./gradlew build -s --continue"
                }
            }
            post {
                always {
                    junit "**/build/test-results/test/*.xml"
                    jacoco classPattern: '**/build/classes/*/main', execPattern: '**/build/jacoco/*.exec'
                    recordIssues(tools: [
                        java(pattern: '**/build/reports/javac/compileJava.err'),
                        javaDoc(pattern: '**/build/reports/javadoc/*'),
                        cpd(pattern: '**/build/reports/cpd/*.xml')
                    ])
                }
            }
        }
    }
    post {
        failure {
            emailext attachLog: true, body: '${DEFAULT_CONTENT}', recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'DevelopersRecipientProvider']], subject: '${DEFAULT_SUBJECT}'
        }
        cleanup {
            cleanWs deleteDirs: true, disableDeferredWipeout: true
        }
    }
}