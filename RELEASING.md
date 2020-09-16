# Releasing

Steps to release:

1. Make sure `dev` workflow is green in actions tab in github repo
1. Run `./release.sh $VERSION$` script by providing version number argument
    **Note:** `PROD=true` environment variable needs to be set before running `release.sh`
