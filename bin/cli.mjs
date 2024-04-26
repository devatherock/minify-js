#!/usr/bin/env node
import fs from 'fs'
import yargs from 'yargs'
import { hideBin } from 'yargs/helpers'
import { minifyFiles } from '../lib/minify-js.mjs'
import { stringToBoolean, valueFromEnvVariables, getLogger } from '../lib/utils.mjs'

function readRegexArray(array) {
  var regexes = []

  if (array) {
    const arrayParts = array.replace('\\n', '\n').split(/[\r\n]+/)
    getLogger().debug('Regexes: ', arrayParts)
  
    for (var index = 0; index < arrayParts.length; index++) {
      regexes.push(RegExp(arrayParts[index]))
    }
  }

  return regexes
}

const inputPathVariables = ['PLUGIN_INPUT_PATH', 'PARAMETER_INPUT_PATH', 'INPUT_DIRECTORY']
const outputPathVariables = ['PLUGIN_OUTPUT_PATH', 'PARAMETER_OUTPUT_PATH', 'INPUT_OUTPUT']
const addSuffixVariables = ['PLUGIN_ADD_SUFFIX', 'PARAMETER_ADD_SUFFIX', 'INPUT_ADD_SUFFIX']
const inclusionsVariables = ['PLUGIN_INCLUSIONS', 'PARAMETER_INCLUSIONS', 'INPUT_INCLUSIONS']
const exclusionsVariables = ['PLUGIN_EXCLUSIONS', 'PARAMETER_EXCLUSIONS', 'INPUT_EXCLUSIONS']

const options = yargs(hideBin(process.argv))
  .option('input-path', {
    alias: 'i',
    type: 'string',
    description: 'File to minify or a folder containing files to minify',
    default: valueFromEnvVariables(inputPathVariables) ? valueFromEnvVariables(inputPathVariables) : process.cwd()
  })
  .option('output-path', {
    alias: 'o',
    type: 'string',
    description: 'Path where the minified files will be saved',
    default: valueFromEnvVariables(outputPathVariables)
  })
  .option('add-suffix', {
    alias: 'a',
    type: 'boolean',
    description: 'Indicates if the output files should have the suffix `.min` added after the name',
    default: valueFromEnvVariables(addSuffixVariables) ? stringToBoolean(valueFromEnvVariables(addSuffixVariables)) : true
  })
  .option('inclusions', {
    type: 'string',
    description: 'Multi-line string, each line of which contains a regex representing files to include/minify',
    default: valueFromEnvVariables(inclusionsVariables)
  })
  .option('exclusions', {
    type: 'string',
    description: 'Multi-line string, each line of which contains a regex representing files to exclude from minification',
    default: valueFromEnvVariables(exclusionsVariables)
  })
  .parse()

const inputPath = options.i
const outputPath = options.o
const addSuffix = options.a
var inclusions = readRegexArray(options.inclusions)
var exclusions = readRegexArray(options.exclusions)

if (fs.existsSync(inputPath)) {
  minifyFiles(inputPath, addSuffix, outputPath, inclusions, exclusions)
} else {
  getLogger().error('Input path ', inputPath, " doesn't exist")
}
