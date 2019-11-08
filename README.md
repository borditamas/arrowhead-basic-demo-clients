1) Download (or fork) the arrowhead client-skeleton repository
2) Copy the consumer, provider folder and the pom, gitignore file to your project folder
3) Delete the maven modules from the pom file
4) Customize the following field in the parent pom
	- GroupID
	- ArtifactID
	- Version
	- Name
	- Description
	- Url (or delete)
	- Licenses (or delete)
	
5-6) Customize the followings field in the consumer and provider pom
	- Parent
	- ArtifactID
	- Name
	
7) Add the consumer and provider artifact ids to the parent pom as a module
8) Rename the consumer and provider folders inline with the artifact ids
9) Verify your settings with a mvn clean install command
10) Change the target paths in gitignore
11) Import the project as a maven project into your IDE
12) Customize the application.properties and create the certificates accordingly

PROVIDER:

1) Create your own packages
2) Add the basic package into the main class
4) Create you controller (your service)
3) Customize the ApplicationInitListener (register/unregister the service)

CONSUMER:

1) Create your own packages
2) Add the basic package into the main class
3) Implement your logic
