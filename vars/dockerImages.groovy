def call(Map pipelineParams) {

	pipeline {

		agent {
			label 'docker'
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

			stage('Push-Image-Snapshot') {
				when {
					environment name: 'gitlabActionType', value: 'PUSH'
					expression { params.RELEASE == false }
					}
				steps {
					sh "make push-image INTERNAL_REGISTRY_URL=${pipelineParams.dockerRegistrySnapshots}"
				}
			}

			stage('Push-Image-Release') {
				when {
					expression { params.RELEASE == true }
					}
				environment {
					//ARTIFACT_REGISTRY_URL = "${pipelineParams.artifactRegistryReleases}"
					//ARTIFACT_REGISTRY_CREDENTIALS = credentials('73529b15-34f4-4912-9ef6-0829547c9586')
					//SSH_KEY_CREDENTIALS = credentials('651c7382-f7d9-41a5-93ab-a6e197ee1d77')
					//GIT_SSH_COMMAND="ssh -i ${SSH_KEY_CREDENTIALS} -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no"
				}
				steps {
					sh "make push-image IMAGE_VERSION=${params.RELEASE_VERSION} INTERNAL_REGISTRY_URL=${pipelineParams.dockerRegistryReleases}"
				}
			}
		}
	}
}