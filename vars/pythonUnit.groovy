def call(Map pipelineParams) {

// Pipeline Definition for Python Units and Libraries

	pipeline {

		agent {
			docker { 
			    image pipelineParams.dockerImage
			    args '-u root:root'
			}
		}

		stages {

			stage('SCM') {
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
					sh 'pip install --upgrade pip' //--> Already done in docker image. Remove this step when pipeline running inside release-me-python docker image
					sh 'pip install --upgrade setuptools==41.2.0' //--> Already done in docker image. Remove this step when pipeline running inside release-me-python docker image
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
					sh 'pip install astroid==2.2.5 pylint==2.3.1 isort==4.2.15 flake8==3.7.8' //--> Already done in docker image. Remove this step when pipeline running inside release-me-python docker image
					sh "make -C . -f inc/release-me-python/python-release-with-params.mk static-analysis MAIN_DIR=pipelineParams.srcDir TESTS_DIR=pipelineParams.testsDir"
				}
			}

			stage('Sonar-Analisys') {
				steps {
					echo 'Executing code analisys in Sonar'
				}
			}

			stage('Upload-Snapshot') {
				when {
					expression { pipelineParams.release == 'False' }
				}
				steps {			
					sh 'make -C . -f inc/release-me-python/python-release-with-params.mk upload-to-nexus REPO=snapshots'
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

			stage('Lint-Dockerfile') {		
				steps {
					echo "Linting Dockerfile"
				}
			}

			stage('Build-Image') {		
				steps {
					echo "Building Docker Image"
				}
			}

			stage('Push-Image') {
				steps {
					echo "Pushing Docker Image to Docker Registry"
				}
			}


			stage('System-Tests') {
				steps {
					echo "Executing Smoke System Tests. Most important system tests"	
				}
			}

			stage('Regression-Tests') {
				//when {
				//	expression { regresionTests == 'True' }
				//}
				steps {
					echo "Executing Full Regresion Tests. Triggers this stage with a cron so it only runs at nights"
				}
			}

			stage('Deploy') {
				steps {
					echo "Deploying Application. Invoke here the deploy repository"
				}
			}
		}
	}
}