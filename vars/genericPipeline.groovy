def call(Map pipelineParams) {

	
//if (pipelineParams.projectType == "Unit") {

// Pipeline Definition for Units

	pipeline {

		agent any
/*		agent {

        docker { image pipelineParams.dockerImage }
    	
    	}*/

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
					expression { pipelineParams.projectType == 'Library' }
				}
				
				steps {
				
				//sh "make push-image"
				echo "The Project Type is: ${pipelineParams.projectType}"
				
				}
			}

			stage('Push-Image') {
				when {
					expression { pipelineParams.projectType == 'Unit' }
				}
				
				steps {
				
				//sh "make push-image"
				echo "The Project Type is: ${pipelineParams.projectType}"
				
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

