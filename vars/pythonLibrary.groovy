def call(Map pipelineParams) {

// Pipeline Definition for Python Units and Libraries

	pipeline {

		agent {
			docker { 
			    image pipelineParams.dockerImage
			    args '-u root:root -v /home/administrator/.ssh:/root/.ssh'
			}
		}

		parameters {
	    	booleanParam(name: 'RELEASE', defaultValue: false, description: 'Are you doing a Release?')
	    	string(name: 'RELEASE_VERSION', defaultValue: '', description: 'Version to be released (e.g: 1.1.0, 1.1.0rc1)')
	    	string(name: 'NEXT_DEV_VERSION', defaultValue: '', description: 'Next development version (e.g: 1.2.0.dev1) according to <a href="https://www.python.org/dev/peps/pep-0440/">PEP-440</a>')
		}

		stages {

			stage('Build') {
				steps {
					sh 'make -C . -f /inc/release-me-python/python-release-with-params.mk clean dist'
				}
			}

			stage('Unit-Tests') {
				steps {
					sh 'make -C . -f /inc/release-me-python/python-release-with-params.mk test'
				}
			}

			stage('Project-Lint') {
				steps {
					sh "make -C . -f /inc/release-me-python/python-release-with-params.mk static-analysis MAIN_DIR=${pipelineParams.srcDir} TESTS_DIR=${pipelineParams.testDir}"
				}
			}

			stage('Sonar-Analisys') {
				steps {
					echo 'Executing code analisys in Sonar'
				}
			}

			stage('Upload-Snapshot') {
				when {
					expression { params.RELEASE == false }
					}
				environment {
					ARTIFACT_REGISTRY_URL = "${pipelineParams.artifactRegistrySnapshots}"
					ARTIFACT_REGISTRY_CREDENTIALS = credentials('73529b15-34f4-4912-9ef6-0829547c9586')
				}
				steps {
					echo 'Releasing snapshot version of the library'			
					sh 'make -C . -f /inc/release-me-python/python-release-with-params.mk upload-to-nexus'
				}
			}

			stage('Release') {
				when {
					expression { params.RELEASE == true }
					}
				environment {
					ARTIFACT_REGISTRY_URL = "${pipelineParams.artifactRegistryReleases}"
					ARTIFACT_REGISTRY_CREDENTIALS = credentials('73529b15-34f4-4912-9ef6-0829547c9586')
				}
				steps {
					sh "git config --local --add core.sshCommand 'ssh -i ~/.ssh/id_rsa'"
					sh "make -C . -f /inc/release-me-python/python-release-with-params.mk pre-release upload-to-nexus post-release RELEASE_VERSION=${params.RELEASE_VERSION} NEXT_DEVELOPMENT_VERSION=${params.NEXT_DEV_VERSION}"
				}
			}
		}

    	//post {
		//	always { 
		//		cleanWs()
		//	}
		//}
	}
}