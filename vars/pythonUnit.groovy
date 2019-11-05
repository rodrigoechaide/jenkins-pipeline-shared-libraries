def call(Map pipelineParams) {

// Pipeline Definition for Python Units

	pipeline {

	agent none

	// Send Trigger time as parameter

    // triggers {
    //     cron('H 23 * * *')
    // }

		parameters {
			booleanParam(name: 'RELEASE', defaultValue: false, description: 'Are you doing a Release?')
			string(name: 'RELEASE_VERSION', defaultValue: '', description: 'Version to be released (e.g: 1.1.0, 1.1.0rc1)')
			string(name: 'NEXT_DEV_VERSION', defaultValue: '', description: 'Next development version (e.g: 1.2.0.dev1) according to <a href="https://www.python.org/dev/peps/pep-0440/">PEP-440</a>')
		}

		stages {

			stage("Dockerfile Agent Stage") {

				agent {
					dockerfile {
						args '-v /home/administrator:/home/administrator -e HOME=/home/administrator'
					}
				}

				stages {

					stage('Setting Build Name') {
						steps {
							script {
								currentBuild.displayName = "#${env.BUILD_NUMBER} at ${env.GIT_COMMIT.substring(0,6)}"
								//currentBuild.description = "Description"
							}
						}
					}

					stage('Unit-Tests') {
						steps {
							sh 'make -C . -f /inc/release-me-python/python-release-with-params.mk clean dist test'
						}
					}

					stage('Project-Lint') {
						steps {
							sh "make -C . -f /inc/release-me-python/python-release-with-params.mk static-analysis MAIN_DIR=${pipelineParams.srcDir} TESTS_DIR=${pipelineParams.testDir}"
						}
					}

					stage('Sonar-Analisys') {
						steps {
							echo "Executing code analisys in Sonar"
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
							SSH_KEY_CREDENTIALS = credentials('651c7382-f7d9-41a5-93ab-a6e197ee1d77')
							GIT_SSH_COMMAND="ssh -i ${SSH_KEY_CREDENTIALS} -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no"
						}
						steps {
							echo 'Releasing new version of the library'
							sh "make -C . -f /inc/release-me-python/python-release-with-params.mk pre-release upload-to-nexus post-release RELEASE_VERSION=${params.RELEASE_VERSION} NEXT_DEVELOPMENT_VERSION=${params.NEXT_DEV_VERSION}"
						}
					}
				}
			}


			stage("Docker host Agent Stage") {

				agent {
					label 'docker'
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
							echo 'Logging in to DEV Docker Registry'
							script {
								docker.withRegistry("${pipelineParams.dockerRegistrySnapshots}", "73529b15-34f4-4912-9ef6-0829547c9586") {
									echo "Pushing Image to DEV Registry"
									sh "make push-image IMAGE_VERSION=\$(cat IMAGE_VERSION) INTERNAL_REGISTRY_URL=${pipelineParams.dockerRegistrySnapshots.substring(8)}"
									sh "make push-image IMAGE_VERSION=latest INTERNAL_REGISTRY_URL=${pipelineParams.dockerRegistrySnapshots.substring(8)}"
								}
							}
						}
					}

					stage('Push-Image-Release') {
						when {
							expression { params.RELEASE == true }
							}
						environment {
							SSH_KEY_CREDENTIALS = credentials('651c7382-f7d9-41a5-93ab-a6e197ee1d77')
							GIT_SSH_COMMAND="ssh -i ${SSH_KEY_CREDENTIALS} -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no"
						}
						steps {
							echo "Release Version: Doing bump version steps"
							sh "mv IMAGE_VERSION IMAGE_VERSION_OLD"
							sh "echo ${params.RELEASE_VERSION} > IMAGE_VERSION"
							sh "git add IMAGE_VERSION"
							sh "git commit -m \"Bump version: \$(cat IMAGE_VERSION_OLD) → ${params.RELEASE_VERSION}\""
							sh "git tag -a ${params.RELEASE_VERSION} -m \"Created by Jenkins Sabiamar\""
							echo 'Logging in to CMS Docker Registry'
							script {
								docker.withRegistry("${pipelineParams.dockerRegistryReleases}", "73529b15-34f4-4912-9ef6-0829547c9586") {
									echo "Pushing Image to CMS Registry"
									sh "make push-image IMAGE_VERSION=${params.RELEASE_VERSION} INTERNAL_REGISTRY_URL=${pipelineParams.dockerRegistryReleases.substring(8)}"
								}
							}
							echo "Next Dev Version: Doing Bump Version Steps"
							sh "echo ${params.NEXT_DEV_VERSION} > IMAGE_VERSION"
							sh "git add IMAGE_VERSION"
							sh "git commit -m \"Bump version: ${params.RELEASE_VERSION} → ${params.NEXT_DEV_VERSION}\""
							sh "git push origin master"
							sh "git push origin ${params.RELEASE_VERSION}"
							echo 'Logging in to DEV Docker Registry'
							script {
								docker.withRegistry("${pipelineParams.dockerRegistrySnapshots}", "73529b15-34f4-4912-9ef6-0829547c9586") {
									echo "Pushing Image to DEV Registry"
									sh "make push-image IMAGE_VERSION=${params.NEXT_DEV_VERSION} INTERNAL_REGISTRY_URL=${pipelineParams.dockerRegistrySnapshots.substring(8)}"
									sh "make push-image IMAGE_VERSION=latest INTERNAL_REGISTRY_URL=${pipelineParams.dockerRegistrySnapshots.substring(8)}"
								}
							}
						}
					}

					stage('System-Tests') {
						steps {
							echo "Clone System Tests Repo and run docker-compose up"
						}
					}

					stage('Staging') {
						steps {
							echo "Deploy unit to staging area"
						}
					}
				}
			}
		}
	}
}
