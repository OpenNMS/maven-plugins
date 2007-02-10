GWT_VERSION=1.1.10
GWT_HOME=/opt/gwt/current

mvn install:install-file "-Dfile=$GWT_HOME/gwt-user.jar" -DgroupId=com.google.gwt -DartifactId=gwt-user -Dversion=$GWT_VERSION -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
mvn install:install-file "-Dfile=$GWT_HOME/gwt-servlet.jar" -DgroupId=com.google.gwt -DartifactId=gwt-servlet -Dversion=$GWT_VERSION -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
mvn install:install-file "-Dfile=$GWT_HOME/gwt-dev-linux.jar" -DgroupId=com.google.gwt -DartifactId=gwt-dev-linux -Dversion=$GWT_VERSION -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
