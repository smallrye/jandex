# Release Procedure

the `jandex` project relies on a GitHub action to create its release.

# Create a PR to prepare the release

By convention, use a branch named `release-<x.y.z>` where `x.y.z` is the version that is about to be released.

The PR should only contain changes to the `.github/project.yml` file and specifies the version to release, and the next version to set in the POM (with a trailing `-SNAPSHOT`).

This PR must be pushed to the [`smallrye/jandex`](https://github.com/smallrye/jandex) repository. Pushing it to a fork will not create a release.

Once this PR is opened, it will check that it can be used to create a release.

Once merged, a GitHub action will update the project and and perform the release to deploy the artifacts in [Maven Central](https://repo1.maven.org/maven2/io/smallrye/jandex/).

The GitHub action also creates [Release Notes](https://github.com/smallrye/jandex/releases) corresponding to the new tag.