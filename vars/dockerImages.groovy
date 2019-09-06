def call(Map pipelineParams) {

	pipeline {

		agent any

		stages {
			stage('Lint-Image') {
				steps {
				sh "make lint-image"
				}
			}
			stage('Build-Image') {
				steps {
				sh "make build-image"
				}
			}
			stage('Push-Image') {
				steps {
				sh "make push-image"
				}
			}
		}
	}
}