import { minify } from 'minify'
import fs from 'fs'
import os from 'os'
import path from 'path'
import { getLogger, isDirectory, pathWithTrailingSeparator } from '../lib/utils.mjs'

const supportedExtensions = ['.js', '.html', '.css']

export async function minifyFile (inputFile, addSuffix, outputPath) {
  // Calculate output path
  const fileSeparatorIndex = inputFile.lastIndexOf(path.sep)
  if (outputPath) {
    getLogger().debug('Output path is specified')

    // Create output folder if it doesn't exist
    if (!fs.existsSync(outputPath)) {
      fs.mkdirSync(outputPath, { recursive: true })
    }
  } else {
    // Can be -1 if input file is in the working folder
    if (fileSeparatorIndex !== -1) {
      outputPath = inputFile.substring(0, fileSeparatorIndex)
    } else {
      outputPath = ''
    }
  }
  getLogger().debug('Output path: ', outputPath)

  // Extract input file name and extension
  const inputFileExtension = path.extname(inputFile)
  const inputFileName = path.basename(inputFile, inputFileExtension)

  // Calculate complete output file name
  const outputFileName = pathWithTrailingSeparator(outputPath) + inputFileName + (addSuffix ? '.min' : '') + inputFileExtension
  getLogger().debug('Complete output file name: ', outputFileName)

  // Minify file and write to output file
  const minifiedContent = await minify(inputFile)
  getLogger().debug(minifiedContent)
  fs.writeFileSync(outputFileName, `${minifiedContent}${os.EOL}`)

  getLogger().info('Minified ', inputFile, ' > ', outputFileName)
}

export function minifyFiles (inputPath, addSuffix, outputPath, inclusions, exclusions) {
  if (isDirectory(inputPath)) {
    getLogger().debug('Input path ', inputPath, ' is a directory')

    // Loop through all the files in the input path
    fs.readdir(inputPath, function (_, files) {
      files.forEach(function (file, index) {
        minifyFiles(pathWithTrailingSeparator(inputPath) + file, addSuffix, outputPath, inclusions, exclusions)
      })
    })
  } else {
    getLogger().debug('Input path ', inputPath, ' is a file')

    if (supportedExtensions.includes(path.extname(inputPath))) {
      if ((inclusions.length === 0 || inclusions.some((regex) => regex.test(inputPath)))
          && (exclusions.length === 0 || !exclusions.some((regex) => regex.test(inputPath)))) {
        minifyFile(inputPath, addSuffix, outputPath)
      } else {
        getLogger().debug('Skipping file ', inputPath, " that doesn't match any inclusions")
      }
    } else {
      getLogger().debug('Skipping file ', inputPath, ' with unsupported extension')
    }
  }
}
