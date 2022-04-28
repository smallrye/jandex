#!/bin/bash

VERSIONS="HEAD"
BENCHMARKS=".*"

help() {
  echo "Usage: run-benchmarks.sh [--versions <versions>] [--benchmars <benchmarks>] [--help]"
  echo
  echo "  --versions <versions>: comma-separated list of Jandex versions (Git commits) to benchmark"
  echo "                         defaults to 'HEAD'"
  echo
  echo "  --benchmarks <benchmarks>: comma-separated list of regexps that select the benchmarks to run"
  echo "                             defaults to '.*'"
  echo
  echo "  --help: print this text"
}

while [[ $# -gt 0 ]] ; do
  case $1 in
    --help)
      help
      exit
      ;;
    --versions)
      VERSIONS="${2//,/ }"
      shift
      shift
      ;;
    --benchmarks)
      BENCHMARKS="$2"
      shift
      shift
      ;;
    *)
      help
      exit 1
      ;;
  esac
done

echo "Running benchmarks $BENCHMARKS against Jandex versions:"
for VERSION in $VERSIONS ; do
  echo "- $VERSION"
done

ROOT_DIR=$(git rev-parse --show-toplevel)
for VERSION in $VERSIONS ; do
  TEMP=$(mktemp --tmpdir -d jandex.XXXXXXXXXX)
  git -C $ROOT_DIR archive $VERSION | tar -x -C $TEMP
  pushd $TEMP
  GROUP_ID=$(mvn org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -q -DforceStdout -Dexpression=project.groupId)
  mvn -q versions:set -DnewVersion=1.0.0-dev-SNAPSHOT
  mvn -q versions:commit
  mvn -q clean install -DskipTests -Dinvoker.skip -Dformat.skip -Dimpsort.skip
  popd
  rm -r $TEMP

  mvn jmh:benchmark -Djmh.benchmarks=$BENCHMARKS -Djmh.rf=json -Djmh.rff=target/results-$VERSION.json -DgroupId.jandex=$GROUP_ID -Dversion.jandex=1.0.0-dev-SNAPSHOT
done

# this requires current Jandex workspace to be built and installed to local Maven repo (`mvn clean install -DskipTests`)
RESULTS=$(find target -type f -name "results-*.json" -print0 | tr '\0' ',' | sed -e 's/,$//')
mvn compile exec:java -Dexec.mainClass=org.jboss.jandex.chart.ChartGenerator -Dexec.arguments="$RESULTS"
