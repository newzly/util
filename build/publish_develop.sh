#!/usr/bin/env bash
echo "Pull request: ${TRAVIS_PULL_REQUEST}; Branch: ${TRAVIS_BRANCH}"

if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "develop" ];
then
    if [ "${TRAVIS_SCALA_VERSION}" == "2.11.8" ] && [ "${TRAVIS_JDK_VERSION}" == "oraclejdk8" ];
    then

        echo "Setting git user email to ci@outworkers.com"
        git config user.email "ci@outworkers.com"

        echo "Setting git user name to Travis CI"
        git config user.name "Travis CI"

        echo "The current JDK version is ${TRAVIS_JDK_VERSION}"
        echo "The current Scala version is ${TRAVIS_SCALA_VERSION}"

        echo "Creating credentials file"
        if [ -e "$HOME/.bintray/.credentials" ]; then
            echo "Bintray credentials file already exists"
        else
            mkdir -p "$HOME/.bintray/"
            touch "$HOME/.bintray/.credentials"
            echo "realm = Bintray API Realm" >> "$HOME/.bintray/.credentials"
            echo "host = api.bintray.com" >> "$HOME/.bintray/.credentials"
            echo "user = $bintray_user" >> "$HOME/.bintray/.credentials"
            echo "password = $bintray_password" >> "$HOME/.bintray/.credentials"
        fi

        if [ -e "$HOME/.bintray/.credentials" ]; then
            echo "Bintray credentials file successfully created"
        else
            echo "Bintray credentials still not found"
        fi

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

        if [ -e "$HOME/.bintray/.credentials" ]; then
            echo "Bintray credentials file successfully created"
        else
            echo "Bintray credentials still not found"
        fi


        if [ "$TRAVIS_BRANCH" == "develop" ];
        then
            echo "Publishing new version to Maven Central"
            echo "Creating GPG deploy key"
            openssl aes-256-cbc -K $encrypted_759d2b7e5bb0_key -iv $encrypted_759d2b7e5bb0_iv -in build/deploy.asc.enc -out build/deploy.asc -d

            echo "importing GPG key to local GBP repo"
            gpg --fast-import build/deploy.asc

            echo "Setting MAVEN_PUBLISH mode to true"
            export MAVEN_PUBLISH="true"
            export pgp_passphrase=${maven_password}
            sbt release with-defaults
        else
            echo "Not deploying to Maven Central, branch is not develop, current branch is ${TRAVIS_BRANCH}"
        fi

    else
        echo "Only publishing version for Scala 2.11.8 and Oracle JDK 8 to prevent multiple artifacts"
    fi
else
    echo "This is either a pull request or the branch is not develop, deployment not necessary"
fi
