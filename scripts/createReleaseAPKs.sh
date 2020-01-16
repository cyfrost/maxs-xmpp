#!/usr/bin/env bash

. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh"

set -e

PUBLISH=false
REMOTE=false
RELEASE_TARGET="parrelease"

while getopts dhprst: OPTION "$@"; do
    case $OPTION in
	d)
	    set -x
	    ;;
	h)
	    cat <<EOF
usage: `basename $0` [-d] [-p] [-r] [-t <tag>]
	-d: debug output
	-p: publish
	-r: get keystore data from remote location
	-s: sequential build instead of parallel
	-t <tag>: prepare release of version <tag>
EOF
	    exit
	    ;;
	p)
	    PUBLISH=true
	    ;;
	r)
	    REMOTE=true
	    ;;
	s)
		RELEASE_TARGET="release"
		;;
	t)
	    RELEASE_TAG="${OPTARG}"
	    ;;
    esac
done

TMPDIR=$(mktemp -d)
trap "rm -rf ${TMPDIR}" EXIT

if ! $REMOTE; then
    KEYSTOREFILE=${KEYSTOREDATA}/release.keystore
    KEYSTOREPASSGPG=${KEYSTOREDATA}/keystore_password.gpg

    if [[ ! -d ${KEYSTOREDATA} ]]; then
	echo "KEYSTOREDATA=${KEYSTOREDATA} does not exist or is not a directory"
	exit 1
    fi

    if [[ ! -f ${KEYSTOREFILE} ]]; then
	echo "KEYSTOREFILE=${KEYSTOREFILE} does not exist or is not a file"
	exit 1
    fi

    if [[ ! -f ${KEYSTOREPASSGPG} ]]; then
	echo "KEYSTOREPASSGPG=${KEYSTOREPASSGPG} does not exist or is not a file"
	exit 1
    fi
    KEYSTOREPASSWORD=$(cat ${KEYSTOREPASSGPG} | gpg -d)
else
    if [[ -n $bamboo_KEYSTOREPASSWORD ]]; then
	KEYSTOREPASSWORD=$bamboo_KEYSTOREPASSWORD
    fi
    if [[ -z $KEYSTOREPASSWORD ]]; then
	echo "error: \$KEYSTOREPASSWORD not set"
	exit 1
    fi
    if [[ -z $KEYSTOREURL ]]; then
	echo "error: \$KEYSTORERUL not set"
	exit 1
    fi
    KEYSTOREFILE=$TMPDIR/release.keystore
    wget -q -O $KEYSTOREFILE $KEYSTOREURL 2>&1 || exit 1
fi

cat <<EOF > ${TMPDIR}/gradle.properties
storeFile=${KEYSTOREFILE}
keyAlias=maxs
keyPassword=${KEYSTOREPASSWORD}
storePassword=${KEYSTOREPASSWORD}
EOF

cd ${BASEDIR}

if [[ -n $RELEASE_TAG ]]; then
    git checkout $RELEASE_TAG
elif $REMOTE; then
    # If we perform a remote build (e.g. on Jenkins), then set the
    # versionCode of all components so that they
    # can get published to the Play Store beta channel
	setMaxsVersions
fi

make ${RELEASE_TARGET} \
	 GRADLE_EXTRA_ARGS="-PkeystorePropertiesFile=\"${TMPDIR}/gradle.properties\"" \

BUILT_DATE=$(date +"%Y-%m-%d_-_%H:%M_%Z")
# It doesn't matter that $RELEASE_TAG may be empty in case we havn't
# defined a release tag. It's still a good idea to copy the apks to
# one location.
if $REMOTE; then
    TARGET_DIR=$BUILT_DATE
else
    TARGET_DIR=${RELEASE_TAG}
fi

[[ -d releases/${TARGET_DIR} ]] || mkdir -p releases/${TARGET_DIR}

for c in $COMPONENTS; do
	cp "${c}/build/outputs/apk/release/"*-release.apk "releases/${TARGET_DIR}"
done

if [[ -n $RELEASE_TAG ]]; then
    git checkout master
fi

if $PUBLISH && [[ -n $RELEASE_TAG ]]; then
    cat <<EOF | sftp $RELEASE_HOST
mkdir ${RELEASE_DIR}/${RELEASE_TAG}
put releases/${RELEASE_TAG}/* ${RELEASE_DIR}/${RELEASE_TAG}
EOF
elif $PUBLISH; then
	BRANCH=$(git rev-parse --abbrev-ref HEAD)
	# If we are building from a non-master branch, then add the branch
	# name to the target directory.
	if [[ "$BRANCH" != "master" ]]; then
		SUBDIR=$BRANCH
		TARGET_DIR="${BRANCH}/"
	else
		SUBDIR=""
		TARGET_DIR=""
	fi
	TARGET_DIR+=$BUILT_DATE
    cat <<EOF | sftp $RELEASE_HOST
mkdir ${RELEASE_DIR}/nightlies/${SUBDIR}
mkdir ${RELEASE_DIR}/nightlies/${TARGET_DIR}
put releases/*.apk ${RELEASE_DIR}/nightlies/${TARGET_DIR}
EOF
fi
