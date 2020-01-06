def call(Map pipelineParams) {

	pipeline {

		agent {
			dockerfile {
				args '-v /home/administrator:/home/administrator -e HOME=/home/administrator'
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

			stage('Build') {
				steps {
					sh 'export DISPLAY=:99'
					sh 'npm install'
					sh 'ng build --prod'
				}
			}

			stage('Unit-Tests') {
				steps {
					sh 'export DISPLAY=:99'
					sh 'ng test --browsers=ChromeHeadlessCI --watch=false --code-coverage'
				}
			}

			stage('Project-Lint') {
				steps {
					echo "Project Linting"
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
					/*sh 'export DISPLAY=:99'
					sh 'npm publish --registry $ARTIFACT_REGISTRY_URL' // http://nexus.ascentio.com.ar/nexus/repository/npm-snapshots*/
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
					/*sh 'export DISPLAY=:99'
					sh 'npm install'
					sh 'npm test'
					sh 'grunt release:$RELEASE_VERSION'
					sh 'npm publish --registry $ARTIFACT_REGISTRY_URL' // http://nexus.ascentio.com.ar/nexus/repository/npm-releases/
					sh 'grunt next-development-version:$NEXT_DEV_VERSION'*/
				}
			}
		}
	}
}