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

			stage('Build') {
				steps {
					/*
					sh 'pip install --upgrade pip' //--> Already done in docker image
					sh 'pip install --upgrade setuptools==41.2.0' //--> Already done in docker image
					*/
					sh 'make -C . -f inc/release-me-python/python-release-with-params.mk clean dist'
				}
			}

			stage('Unit-Tests') {
				steps {
					sh 'make -C . -f inc/release-me-python/python-release-with-params.mk test'
				}
			}

			stage('Project-Lint') {
				steps {
					/*
					sh 'pip install astroid==2.2.5 pylint==2.3.1 isort==4.2.15 flake8==3.7.8'
					*/
					sh "make -C . -f inc/release-me-python/python-release-with-params.mk static-analysis MAIN_DIR=${pipelineParams.srcDir} TESTS_DIR=${pipelineParams.testDir}"
				}
			}

			stage('Sonar-Analisys') {
				steps {
					echo 'Executing code analisys in Sonar'
				}
			}

			stage('Upload-Snapshot') {
				steps {
					echo 'Releasing snapshot version of the library'			
					sh 'make -C . -f inc/release-me-python/python-release-with-params.mk upload-to-nexus REPO=snapshots'
				}
			}

			stage('Release') {
				when {
					expression { pipelineParams.release == 'True' }
				}
				steps {			
					// sh 'export MAKEFILE=inc/release-me-python/python-release-with-params.mk'
					// pip install --upgrade setuptools==41.2.0;
					// sh 'pip install bumpversion'
					sh 'make -C . -f inc/release-me-python/python-release-with-params.mk pre-release upload-to-nexus post-release RELEASE_VERSION=$RELEASE_VERSION NEXT_DEVELOPMENT_VERSION=$NEXT_DEV_VERSION REPO=releases'
				}
			}
		}
	}
}