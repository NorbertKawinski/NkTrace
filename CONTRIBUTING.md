# Contributing
This file contains notes and guides for contributors of this project.

Thank you for considering making this library better!  
If you\'d like to help, please create a pull request, or open an issue.  

### Compiling
Just run the following command in the commandline
```
mvn clean package
```
You should see the the .jar generated in the ```target/``` directory.

## Deployment
This section might be a bit outdated.  
If in doubt, follow the information regarding publishing at:  
<https://central.sonatype.org>

First of all, install GPG if you haven\'t already.  
I suggest using [Kleopatra](https://www.openpgp.org/software/kleopatra/) as it has a nice GUI for both generating and publishing the GPG keys.

Then add following entries to ```%user_home%/.m2/settings.xml``` file:
```
<settings>
	<servers>
		<server>
			<id>ossrh</id>
			<username>%YOUR_SONATYPE_NEXUS_USERNAME_OR_TOKEN_ID%</username>
			<password>%YOUR_SONATYPE_NEXUS_PASSWORD_OR_TOKEN_VALUE%</password>
		</server>
	</servers>
	
	<profiles>
		<profile>
			<id>ossrh</id>
			<activation>
				<activeByDefault>true</activeByDefault>
			</activation>
			<properties>
				<gpg.executable>gpg</gpg.executable>
				<gpg.passphrase>%YOUR_GPG_KEY_PASSPHRASE%</gpg.passphrase>
			</properties>
		</profile>
	</profiles>
</settings>
```

To create a .jar, simply call:
```
mvn clean package
```
Note: ```package``` goal will also run automatic tests.

To, additionally, push the artifact to Nexus, call:
```
mvn "-Duser.name=YOUR_USER_NAME" clean deploy
```
The ```-Duser.name``` parameter is optional.  
If not set, it\'ll use your system\'s user name. 

