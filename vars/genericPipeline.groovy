def call(String projectType) {

if (projectType == "Unit") {

// Pipeline Definition for Units

	pipeline {

		agent any

		stages {

			stage('Build') {
				
				steps {

				sh "make build"
				echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
				
				}
			}

			stage('Test') {
				
				steps {
				
				sh "make test"
				
				}
			}

			stage('Build-Image') {
				
				when {
				
					branch 'production'
				
				}
				
				steps {
				
				sh "make build-image"
				
				}
			}

			stage('Push-Image') {
				when {
				
					branch 'production'
				
				}
				
				steps {
				
				sh "make push-image"
				
				}
			}

			stage('Deploy') {
				
				steps {
				
				sh "make deploy"
				
				}
			}
			
			stage('Only Development Stage') {
				
				when {
				
					branch 'development'
				
				}

				steps {

					echo "Only Development Stage"
				
				}
			}

		}
	}

	}

if (projectType == "Library")	{

// Pipeline Definition for Libraries

	pipeline {

		agent any

		stages {

			stage('Build') {
				
				steps {
				
				sh "make build"
				echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
				
				}
			}

			stage('Test') {
				
				steps {
				
				sh "make test"
				
				}
			}

			stage('Upload') {

				steps {

				sh "make upload"
				
				}
			}

			}

		}
	}

}

