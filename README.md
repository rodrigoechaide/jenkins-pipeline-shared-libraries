# jenkins-pipeline-shared-libraries

Shared Libraries Repository for Jenkins Pipelines

## Framework Testing for pipelines and shared libraries

* https://github.com/jenkinsci/JenkinsPipelineUnit

## Libraries Available

### pythonLibrary

Library to use to build python Projects (Units and Libraries). In the following snippet is shown an example of how to call it and all custom parameters availables:

```
@Library('jenkins-shared-libraries@master') _

pythonLibrary(artifactRegistrySnapshots: 'http://nexus.example.com.ar/nexus/repository/pypi-snapshots/',
	      artifactRegistryReleases: 'http://nexus.example.com.ar/nexus/repository/pypi-releases/',
	      buildDockerfile: 'Dockerfile',
	      cache: false,
	      srcDir: 'productionplanner',
	      testDir: 'tests')
```

### javaScriptLibrary

Library to use to build JavaScript Projects (Units and Libraries). In the following snippet is shown an example of how to call it and all custom parameters availables:

```
@Library('jenkins-shared-libraries@master') _

javaScriptLibrary(artifactRegistrySnapshots: 'http://nexus.example.com.ar/nexus/repository/npm-snapshots/',
	          artifactRegistryReleases: 'http://nexus.example.com.ar/nexus/repository/npm-releases/',
		  buildDockerfile: 'Dockerfile',
		  cache: false)
```

### angularLibray

Library to use to build Angular Projects (Units and Libraries). In the following snippet is shown an example of how to call it and all custom parameters availables:

```
@Library('jenkins-shared-libraries@master') _

angularLibrary(artifactRegistrySnapshots: 'http://nexus.example.com.ar/nexus/repository/npm-snapshots/',
               artifactRegistryReleases: 'http://nexus.example.com.ar/nexus/repository/npm-releases/',
               buildDockerfile: 'Dockerfile',
	       cache: false)
```

### dockerImages

Library to use to build, lint and push docker images. In the following snippet is shown an example of how to call it and all custom parameters availables:

```
@Library('jenkins-shared-libraries@master') _

dockerImages(dockerRegistrySnapshots: 'https://nexus.example.com.ar:7443',
	     dockerRegistryReleases: 'https://registry-cms.example.com.ar')
```

## To Do:

* Implement Unit Testing with https://github.com/jenkinsci/JenkinsPipelineUnit
