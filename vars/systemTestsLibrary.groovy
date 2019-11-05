def call(Map pipelineParams) {

// Pipeline Definition for Python Libraries

	pipeline {

		agent {
			label 'docker'
		}

		/* parameters {
	    	booleanParam(name: 'RELEASE', defaultValue: false, description: 'Are you doing a Release?')
	    	string(name: 'RELEASE_VERSION', defaultValue: '', description: 'Version to be released (e.g: 1.1.0, 1.1.0rc1)')
	    	string(name: 'NEXT_DEV_VERSION', defaultValue: '', description: 'Next development version (e.g: 1.2.0.dev1) according to <a href="https://www.python.org/dev/peps/pep-0440/">PEP-440</a>')
		} */

		stages {

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
