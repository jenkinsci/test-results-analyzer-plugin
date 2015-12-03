echo "SETTING UP DEPENDENCIES..."
mkdir -p ~/.m2/repository/junit/junit/4.12
mkdir -p ~/.m2/repository/org/hamcrest/hamcrest-core/1.3
wget http://central.maven.org/maven2/junit/junit/4.12/junit-4.12.jar -P ~/.m2/repository/junit/junit/4.12
wget http://central.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar -P ~/.m2/repository/org/hamcrest/hamcrest-core/1.3
echo "DOING BUILD..."
mkdir -p target/classes/pkg
mkdir -p target/test-classes/pkg
javac -cp ~/.m2/repository/junit/junit/4.12/junit-4.12.jar src/main/java/pkg/*.java src/test/java/pkg/*.java
mv src/main/java/pkg/*.class target/classes/pkg
mv src/test/java/pkg/*.class target/test-classes/pkg
echo "BUILD COMPLETE, EXECUTING..."
java -cp .:$HOME/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:$HOME/.m2/repository/junit/junit/4.12/junit-4.12.jar:target/classes/:target/test-classes/ org.junit.runner.JUnitCore pkg.SequenceUtilTest
