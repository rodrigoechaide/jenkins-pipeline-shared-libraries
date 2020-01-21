def call(Map pipelineParams) {



def cache = pipelineParams.get('cache', '')

if(!cache) {
    cache = '--no-cache'
}
else {
    cache = ''
}

// Pipeline Definition for Python Libraries

	pipeline {

		agent {
			dockerfile {
				additionalBuildArgs "${cache}" // pipelineParams.get('cache', '')
				filename pipelineParams.get('buildDockerfile', 'Dockerfile') // "${pipelineParams.buildDockerfile}"
			}
		}

		parameters {
	    	booleanParam(name: 'RELEASE', defaultValue: false, description: 'Are you doing a Release?')
	    	string(name: 'RELEASE_VERSION', defaultValue: '', description: 'Version to be released (e.g: 1.1.0, 1.1.0rc1)')
	    	string(name: 'NEXT_DEV_VERSION', defaultValue: '', description: 'Next development version (e.g: 1.2.0.dev1) according to <a href="https://www.python.org/dev/peps/pep-0440/">PEP-440</a>')
		}

		stages {

			stage('Setting Build Name') {
				steps {
					script {
						currentBuild.displayName = "#${env.BUILD_NUMBER} at ${env.GIT_COMMIT.substring(0,6)}"
						//currentBuild.description = "Test Description"
					}
				}
			}

			stage('Checking out Auxiliar Project') {
				steps {
					dir('inc') {
						dir('release-me-python') {
							echo "Checking out release-me-python project"
							git(url: 'git@gitlab.ascentio.com.ar:asc-comp/release-me-python/release-me-python.git', 
								branch: 'master',
								credentialsId: '651c7382-f7d9-41a5-93ab-a6e197ee1d77')
						}
					}
					echo 'release-me-python project succesfully checked out!'
				}
			}

			stage('Unit-Tests') {
				steps {
					sh 'make -C . -f inc/release-me-python/python-release-with-params.mk clean dist test'
				}
			}

			stage('Project-Lint') {
				steps {
					sh "make -C . -f inc/release-me-python/python-release-with-params.mk static-analysis MAIN_DIR=${pipelineParams.srcDir} TESTS_DIR=${pipelineParams.testDir}"
				}
			}

			stage('Sonar-Analisys') {
				steps {
					echo 'Executing code analisys in Sonar'
				}
			}

			stage('Regression-Tests') {
				when {
					triggeredBy "TimerTrigger"
				}
				steps {
					echo "Regression Tests triggered by Cron Job"
				}
			}

			stage('Upload-Snapshot') {
				when {
					environment name: 'gitlabActionType', value: 'PUSH'
					expression { params.RELEASE == false }
					}
				environment {
					ARTIFACT_REGISTRY_URL = "${pipelineParams.artifactRegistrySnapshots}"
					ARTIFACT_REGISTRY_CREDENTIALS = credentials('73529b15-34f4-4912-9ef6-0829547c9586')
				}
				steps {
					echo 'Releasing snapshot version of the library'			
					sh 'make -C . -f inc/release-me-python/python-release-with-params.mk upload-to-nexus'
				}
			}

			stage('Release') {
				when {
					expression { params.RELEASE == true }
					}
				environment {
					ARTIFACT_REGISTRY_URL = "${pipelineParams.artifactRegistryReleases}"
					ARTIFACT_REGISTRY_CREDENTIALS = credentials('73529b15-34f4-4912-9ef6-0829547c9586')
					SSH_KEY_CREDENTIALS = credentials('651c7382-f7d9-41a5-93ab-a6e197ee1d77')
					GIT_SSH_COMMAND="ssh -i ${SSH_KEY_CREDENTIALS} -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no"
				}
				steps {
					echo 'Releasing new version of the library'
					sh "make -C . -f inc/release-me-python/python-release-with-params.mk pre-release upload-to-nexus post-release RELEASE_VERSION=${params.RELEASE_VERSION} NEXT_DEVELOPMENT_VERSION=${params.NEXT_DEV_VERSION}"
				}
			}
		}	
	}
}
