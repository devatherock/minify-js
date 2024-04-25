# Changelog

## [Unreleased]
### Changed
- Replaced `ubuntu-2004:2022.04.1` image with `ubuntu-2204:2023.04.2`

## [3.0.0] - 2023-11-26
### Added
- [#5](https://github.com/devatherock/minify-js/issues/5): Ability to minify only specific files within the input directory

### Changed
- Rewrote the shell script in Node JS

## [2.0.0] - 2023-06-04
### Changed
- Updated dockerhub readme in CI pipeline
- [#7](https://github.com/devatherock/minify-js/issues/7): Built a multi-arch docker image
- Upgraded gradle to 7 and spock to 2.3, to fix test failures in Java 17

## [1.0.3] - 2021-10-27
### Added
- Functional tests to CI pipeline

## [1.0.2] - 2021-02-08
### Added
- test: Added tests to test the final docker image

### Changed
- fix: When no output path is specified, output to same path as each file. It was previously writing to input base directory 
if specified or to the work directory of the docker image.

## [1.0.1] - 2021-02-07
### Added
- feat: Added support for drone.io/vela