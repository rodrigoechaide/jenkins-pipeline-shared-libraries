def call(Map pipelineParams) {

	
// Pipeline Definition for Python Units and Libraries

	pipeline {

		agent {

        docker { image pipelineParams.dockerImage }
    	
    	}

		stages {

		    stage('Aux SCM') {
		      steps {
		        dir('inc') {
		          dir('release-me-python') {
		            git(url: 'git@gitlab.ascentio.com.ar:asc-comp/release-me-python.git', branch: 'master')
		          }
		        }

		        echo 'release-me-python repository succesfully checked out!'
		      }
		    }

			stage('Build') {
				
				steps {

				sh 'pip install --upgrade setuptools==41.2.0' //--> Already done in docker image
				sh 'pip install --upgrade pip' //--> Already done in docker image
				sh 'make -C . -f inc/release-me-python/python-release-with-params.mk clean dist'

				}
			}

			stage('Unit-Tests') {
				
				steps {
				
				sh 'make -C . -f inc/release-me-python/python-release-with-params.mk test'
				
				}
			}

			stage('Lint') {
				
				steps {
				
				sh 'pip install astroid==2.2.5 pylint==2.3.1 isort==4.2.15 flake8==3.7.8'
				sh 'make -C . -f inc/release-me-python/python-release-with-params.mk static-analysis MAIN_DIR=src TESTS_DIR=tests'
				
				}
			}	

			stage('Sonar-Analisys') {
				
				steps {
				
					echo 'Executing code analisys in Sonar'
				
				}
			}

			stage('Release') {

				when {
					expression { pipelineParams.release == 'True' }
				}

				steps {			
				
				//sh 'export MAKEFILE=inc/release-me-python/python-release-with-params.mk'
				//pip install --upgrade setuptools==41.2.0;
				sh 'pip install bumpversion'
				sh 'make -C . -f inc/release-me-python/python-release-with-params.mk pre-release upload-to-nexus post-release RELEASE_VERSION=$RELEASE_VERSION NEXT_DEVELOPMENT_VERSION=$NEXT_DEV_VERSION REPO=releases'
				}

			}	

			stage('Build-Image') {
				
				when {
					expression { pipelineParams.projectType == 'Unit' }
				}
				
				steps {
				
				echo "Building Docker Image"
				
				}
			}

			stage('Push-Image') {
				when {
					expression { pipelineParams.projectType == 'Unit' }
				}
				
				steps {
				
				echo "Pushing Docker Image"
				
				}
			}


			stage('System Tests') {

				when {
					expression { pipelineParams.projectType == 'Unit' }
				}

				steps {

					echo "Executing System Tests"
				
				}
			}


			stage('Regresion Tests') {

				when {
					expression { pipelineParams.projectType == 'Unit' }
				}

				steps {

					echo "Executing Regresion Tests"
				
				}
			}

			stage('Deploy') {

				when {
					expression { pipelineParams.projectType == 'Unit' }
				}
				
				steps {
				
				echo "Deploying Application"

				}
			}

		}
	}

}