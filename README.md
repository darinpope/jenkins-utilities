# jenkins-utilties

./gradlew tasks --all

./gradlew clean flattener:installDist

function doit() {
  git pull
  ./gradlew clean flattener:installDist
  cd flattener/build/install/flattener
  ./bin/flattener $1 $2 $3
}