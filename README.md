[![CircleCI](https://circleci.com/gh/devatherock/minify-js.svg?style=svg)](https://circleci.com/gh/devatherock/minify-js)
[![Version](https://img.shields.io/docker/v/devatherock/minify-js?sort=semver)](https://hub.docker.com/r/devatherock/minify-js/)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/a8694aab3fe44e6da2696ad628daf618)](https://www.codacy.com/gh/devatherock/minify-js/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=devatherock/minify-js&amp;utm_campaign=Badge_Grade)
[![Docker Pulls](https://img.shields.io/docker/pulls/devatherock/minify-js.svg)](https://hub.docker.com/r/devatherock/minify-js/)
[![Docker Image Size](https://img.shields.io/docker/image-size/devatherock/minify-js.svg?sort=date)](https://hub.docker.com/r/devatherock/minify-js/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/JossyDevers/minify-js/blob/master/LICENSE)
# Minify-JS Github Action/CI Plugin
Github action/CI Plugin to minify html, javascript and css files, using [minify](https://www.npmjs.com/package/minify).

## Usage
### Github Action
The following parameters can be set to configure the action.

*   **directory** - File to minify or a folder containing files to minify. By default, all files in current folder and
  its sub-folders will be minified

*   **output** - Path where the minified files will be saved. By default, the minified files will be saved in the
  original file path

*   **add_suffix** - Indicates if the output files should have the suffix `.min` added after the name. Default is true

*   **inclusions** -  Multi-line string, each line of which contains a regex representing files/paths within the input directory to include/minify. By default, all files in the input directory will be minified

*   **exclusions** -  Multi-line string, each line of which contains a regex representing files to exclude from minification. By default, no file in the input directory will be excluded

```yaml
jobs:
  build:
    runs-on: ubuntu-latest      # Docker-based github actions have to run on a linux environment
    steps:
      - name: HTML/CSS/JS Minifier
        uses: docker://devatherock/minify-js:3.0.0
        with:
          directory: 'src'      # Optional
          output: 'minify/src'  # Optional
          add_suffix: false     # Optional
          inclusions: |-        # Optional
            .*assets.*
            .*static/index.html
          exclusions: |-        # Optional
            .*assets/generated/.*
```

### Docker

```shell
docker run --rm \
  -v "/path/to/files":/work \
  -w=/work \
  -e PARAMETER_INPUT_PATH=/work/src \
  -e PARAMETER_OUTPUT_PATH=/work/minify/src \
  -e PARAMETER_ADD_SUFFIX=false \
  -e PARAMETER_INCLUSIONS=".*assets.*\n.*static/index.html" \
  -e PARAMETER_EXCLUSIONS=".*assets/generated/.*" \
  devatherock/minify-js:3.0.0
```

### vela
The following parameters can be set to configure the plugin.

*   **input_path** - File to minify or a folder containing files to minify. By default, all files in current folder and
  its sub-folders will be minified

*   **output_path** - Path where the minified files will be saved. By default, the minified files will be saved in the
  original file path

*   **add_suffix** - Indicates if the output files should have the suffix `.min` added after the name. Default is true

*   **inclusions** -  Multi-line string, each line of which contains a regex representing files to include/minify. By default, all files in the input directory will be minified

*   **exclusions** -  Multi-line string, each line of which contains a regex representing files to exclude from minification. By default, no file in the input directory will be excluded

```yaml
steps:
  - name: minify_js
    ruleset:
      branch: master
      event: push
    image: devatherock/minify-js:3.0.0
    parameters:
      input_path: src
      output_path: minify/src
      add_suffix: false
      inclusions: |-
        .*assets.*
        .*static/index.html
      exclusions: |-
        .*assets/generated/.*        
```

### CircleCI

```yaml
version: 2.1
jobs:
  minify_js:
    docker:
      - image: devatherock/minify-js:3.0.0
    working_directory: ~/my-repo
    environment:
      PARAMETER_INPUT_PATH: src
      PARAMETER_OUTPUT_PATH: minify/src
      PARAMETER_ADD_SUFFIX: false
      PARAMETER_INCLUSIONS: '.*assets.*\n.*static/index.html'
      PARAMETER_EXCLUSIONS: '.*assets/generated/.*'
    steps:
      - checkout
      - run: node /app/bin/cli.mjs
```

## Tests
To test the latest plugin image, run the below command

```shell
./gradlew test
```
