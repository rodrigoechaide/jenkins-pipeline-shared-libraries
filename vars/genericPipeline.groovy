def call(Map pipelineParams) {

if (pipelineParams.projectType == "Unit") {

// Pipeline Definition for Units

	pipeline {

		agent {

        docker { image pipelineParams.dockerImage }
    	
    	}

		stages {

			stage('Build') {
				
				steps {

				//sh "make build"
				echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
				
				}
			}

			stage('Test') {
				
				steps {
				
				//sh "make test"
				echo "test"
				
				}
			}

			stage('Build-Image') {
				
				when {
				
					branch 'production'
				
				}
				
				steps {
				
				//sh "make build-image"
				echo "build-image"
				
				}
			}

			stage('Push-Image') {
				when {
				
					branch 'production'
				
				}
				
				steps {
				
				//sh "make push-image"
				echo "production"
				
				}
			}

			stage('Deploy') {
				
				steps {
				
				//sh "make deploy"
				
				echo "Deploy"

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

if (pipelineParams.projectType == "Library")	{

// Pipeline Definition for Libraries

	pipeline {

		agent any

		stages {

			stage('Build') {
				
				steps {
				
				//sh "make build"
				echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
				
				}
			}

			stage('Test') {
				
				steps {
				
				//sh "make test"
				
				}
			}

			stage('Upload') {

				steps {

				//sh "make upload"
				
				}
			}

			}

		}
	}

}

