#!/usr/bin/env bash

echo "Pull request: ${TRAVIS_PULL_REQUEST}; Branch: ${TRAVIS_BRANCH}"
github_url="https://${github_token}@${GH_REF}"

#!/usr/bin/env bash
function fix_git {
    echo "Fixing git setup for $TRAVIS_BRANCH"
    git checkout ${TRAVIS_BRANCH}
    git branch -u origin/${TRAVIS_BRANCH}
    git config branch.${TRAVIS_BRANCH}.remote origin
    git config branch.${TRAVIS_BRANCH}.merge refs/heads/${TRAVIS_BRANCH}
}

function setup_git_credentials {
    local target="build/git_credentials.asc"
    touch ${target}

    echo "protocol=https" >> ${target}
    echo "host=$GH_REF" >> ${target}
    echo "username=alexflav23" >> ${target}
    echo "password=$github_token" >> ${target}
    git config --global credential.helper cache
    git credential-store --file ${target}

    git remote set-url origin ${github_url}
}

function prepare_maven_release {
  echo "Publishing new version to Maven Central"
  echo "Creating GPG deploy key"

  openssl aes-256-cbc -K $encrypted_d543e1d8b539_key -iv $encrypted_d543e1d8b539_iv -in build/deploy_key.asc.enc -out build/deploy_key.asc -d

  echo "importing GPG key to local GBP repo"
  gpg --fast-import build/deploy_key.asc

  echo "Setting MAVEN_PUBLISH mode to true"
  export MAVEN_PUBLISH="true"
  export pgp_passphrase=${maven_password}
}

function publish_to_bintray {
    export MAVEN_PUBLISH="false"
    echo "Publishing new version to bintray"
    sbt "+publish"
}

function setup_credentials {
    echo "The current JDK version is ${TRAVIS_JDK_VERSION}"
    echo "The current Scala version is ${TRAVIS_SCALA_VERSION}"

    if [ -e "$HOME/.ivy2/.credentials" ]; then
        echo "Maven credentials file already exists"
    else
        mkdir -p "$HOME/.ivy2/"
        touch "$HOME/.ivy2/.credentials"
        echo "realm = Sonatype Nexus Repository Manager" >> "$HOME/.ivy2/.credentials"
        echo "host = oss.sonatype.org" >> "$HOME/.ivy2/.credentials"
        echo "user = $maven_user" >> "$HOME/.ivy2/.credentials"
        echo "password = $maven_password" >> "$HOME/.ivy2/.credentials"
    fi

    if [ -e "$HOME/.ivy2/.credentials" ]; then
        echo "Maven credentials file successfully created"
    else
        echo "Maven credentials still not found"
    fi
}

if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "develop" ];
then
    if [ "${PUBLISH_ARTIFACT}" == "true" ]
    then

        echo "Setting git user email to ci@outworkers.com"
        git config user.email "ci@outworkers.com"

        echo "Setting git user name to Travis CI"
        git config user.name "Travis CI"

        setup_credentials
        fix_git
        setup_git_credentials
        prepare_maven_release

        sbt "release with-defaults"

    else
        echo "Publish arfifact: ${PUBLISH_ARTIFACT}; Only publishing version for Scala $TARGET_SCALA_VERSION and Oracle JDK 8 to prevent multiple artifacts"
    fi
else
    echo "Publish arfifact: ${PUBLISH_ARTIFACT}; Travis PR: ${TRAVIS_PULL_REQUEST}; Scala Version: ${TRAVIS_SCALA_VERSION}; Target version: ${TARGET_SCALA_VERSION}"
    echo "This is either a pull request or the branch is not develop, deployment not necessary."
fi
