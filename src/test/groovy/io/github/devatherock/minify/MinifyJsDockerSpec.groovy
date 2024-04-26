package io.github.devatherock.minify

import io.github.devatherock.util.ProcessUtil
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Paths

/**
 * Test class to test the built docker image
 */
class MinifyJsDockerSpec extends Specification {

    @Shared
    def config = [
            'drone' : [
                    'inputPathParam' : 'PLUGIN_INPUT_PATH',
                    'outputPathParam': 'PLUGIN_OUTPUT_PATH',
                    'envPrefix'      : 'PLUGIN_'
            ],
            'vela'  : [
                    'inputPathParam' : 'PARAMETER_INPUT_PATH',
                    'outputPathParam': 'PARAMETER_OUTPUT_PATH',
                    'envPrefix'      : 'PARAMETER_'
            ],
            'github': [
                    'inputPathParam' : 'INPUT_DIRECTORY',
                    'outputPathParam': 'INPUT_OUTPUT',
                    'envPrefix'      : 'INPUT_'
            ]
    ]

    @Shared
    String imageName = "devatherock/minify-js:${System.getenv('DOCKER_TAG') ?: 'latest'}"

    void setupSpec() {
        if (!System.getenv('SKIP_PULL') || !'true'.equalsIgnoreCase(System.getenv('SKIP_PULL'))) {
            ProcessUtil.executeCommand("docker pull ${imageName}")
        }
    }

    void 'test minify - entire workspace provided'() {
        given:
        String outputHtmlFile = "${System.properties['user.dir']}/src/test/resources/static/index.min.html"
        String outputJsFile = "${System.properties['user.dir']}/src/test/resources/scripts/scripts.min.js"
        String outputCssFile = "${System.properties['user.dir']}/src/test/resources/static/main.min.css"

        when:
        def output = ProcessUtil.executeCommand(['docker', 'run', '--rm',
                                                 '-v', "${System.properties['user.dir']}:/work",
                                                 '-w=/work',
                                                 imageName])

        then:
        output[0] == 0
        output[1].contains('Minified /work/src/test/resources/static/index.html > /work/src/test/resources/static/index.min.html')
        output[1].contains('Minified /work/src/test/resources/static/main.css > /work/src/test/resources/static/main.min.css')
        output[1].contains('Minified /work/src/test/resources/scripts/scripts.js > /work/src/test/resources/scripts/scripts.min.js')
        new File(outputHtmlFile).text ==
                '<!doctype html><title>Test title</title><div id=layout><div id=main><div class=header><h1>Test body</h1></div></div></div>\n'
        new File(outputJsFile).text ==
                '$((function(){$("#templateAndModelForm *:input[type!=hidden]:first").focus()}));\n'
        new File(outputCssFile).text ==
                '.content{width:50%;display:block;margin:2% auto}.header{text-align:center;color:#444;border-bottom:1px solid #eee}\n'

        cleanup:
        Files.deleteIfExists(Paths.get(outputHtmlFile))
        Files.deleteIfExists(Paths.get(outputJsFile))
        Files.deleteIfExists(Paths.get(outputCssFile))
    }

    @Unroll
    void 'test minify - input path specified. ci: #ci'() {
        given:
        String outputHtmlFile = "${System.properties['user.dir']}/src/test/resources/static/index.min.html"
        String outputJsFile = "${System.properties['user.dir']}/src/test/resources/scripts/scripts.min.js"
        String outputCssFile = "${System.properties['user.dir']}/src/test/resources/static/main.min.css"

        when:
        def output = ProcessUtil.executeCommand(['docker', 'run', '--rm',
                                                 '-v', "${System.properties['user.dir']}:/work",
                                                 '-w=/work',
                                                 '-e', "${config[ci].inputPathParam}=/work/src/test/resources/static",
                                                 imageName])

        then:
        output[0] == 0
        output[1].contains('Minified /work/src/test/resources/static/index.html > /work/src/test/resources/static/index.min.html')
        output[1].contains('Minified /work/src/test/resources/static/main.css > /work/src/test/resources/static/main.min.css')
        new File(outputHtmlFile).text ==
                '<!doctype html><title>Test title</title><div id=layout><div id=main><div class=header><h1>Test body</h1></div></div></div>\n'
        new File(outputCssFile).text ==
                '.content{width:50%;display:block;margin:2% auto}.header{text-align:center;color:#444;border-bottom:1px solid #eee}\n'

        then: 'javascript output file should not exist as the folder was not included'
        Files.notExists(Paths.get(outputJsFile))
        !output[1].contains('Minified /work/src/test/resources/scripts/scripts.js > /work/src/test/resources/scripts/scripts.min.js')

        cleanup:
        Files.deleteIfExists(Paths.get(outputHtmlFile))
        Files.deleteIfExists(Paths.get(outputJsFile))
        Files.deleteIfExists(Paths.get(outputCssFile))

        where:
        ci << [
                'drone',
                'vela',
                'github'
        ]
    }

    @Unroll
    void 'test minify - output path specified. ci: #ci'() {
        given:
        String outputHtmlFile = "${System.properties['user.dir']}/src/test/resources/output/index.min.html"
        String outputJsFile = "${System.properties['user.dir']}/src/test/resources/output/scripts.min.js"
        String outputCssFile = "${System.properties['user.dir']}/src/test/resources/output/main.min.css"

        when:
        def output = ProcessUtil.executeCommand(['docker', 'run', '--rm',
                                                 '-v', "${System.properties['user.dir']}:/work",
                                                 '-w=/work',
                                                 '-e', "${config[ci].inputPathParam}=/work/src/test/resources/static",
                                                 '-e', "${config[ci].outputPathParam}=/work/src/test/resources/output",
                                                 imageName])

        then:
        output[0] == 0
        output[1].contains('Minified /work/src/test/resources/static/index.html > /work/src/test/resources/output/index.min.html')
        output[1].contains('Minified /work/src/test/resources/static/main.css > /work/src/test/resources/output/main.min.css')
        new File(outputHtmlFile).text ==
                '<!doctype html><title>Test title</title><div id=layout><div id=main><div class=header><h1>Test body</h1></div></div></div>\n'
        new File(outputCssFile).text ==
                '.content{width:50%;display:block;margin:2% auto}.header{text-align:center;color:#444;border-bottom:1px solid #eee}\n'

        then: 'javascript output file should not exist as the folder was not included'
        Files.notExists(Paths.get(outputJsFile))
        !output[1].contains('Minified /work/src/test/resources/scripts/scripts.js > /work/src/test/resources/output/scripts.min.js')

        cleanup:
        Files.deleteIfExists(Paths.get(outputHtmlFile))
        Files.deleteIfExists(Paths.get(outputJsFile))
        Files.deleteIfExists(Paths.get(outputCssFile))

        where:
        ci << [
                'drone',
                'vela',
                'github'
        ]
    }

    @Unroll
    void 'test minify - no suffix. ci: #ci'() {
        given:
        String htmlFile = "${System.properties['user.dir']}/src/test/resources/static/index.html"
        String jsFile = "${System.properties['user.dir']}/src/test/resources/scripts/scripts.js"
        String cssFile = "${System.properties['user.dir']}/src/test/resources/static/main.css"

        and:
        String inputHtmlFileContent = new File(htmlFile).text
        String inputJsFileContent = new File(jsFile).text
        String inputCssFileContent = new File(cssFile).text

        when:
        def output = ProcessUtil.executeCommand(['docker', 'run', '--rm',
                                                 '-v', "${System.properties['user.dir']}:/work",
                                                 '-w=/work',
                                                 '-e', "${config[ci].inputPathParam}=/work/src/test/resources/static",
                                                 '-e', "${config[ci].envPrefix}ADD_SUFFIX=false",
                                                 imageName])

        then:
        output[0] == 0
        output[1].contains('Minified /work/src/test/resources/static/index.html > /work/src/test/resources/static/index.html')
        output[1].contains('Minified /work/src/test/resources/static/main.css > /work/src/test/resources/static/main.css')
        !output[1].contains('Minified /work/src/test/resources/scripts/scripts.js > /work/src/test/resources/scripts/scripts.js')

        and:
        new File(htmlFile).text ==
                '<!doctype html><title>Test title</title><div id=layout><div id=main><div class=header><h1>Test body</h1></div></div></div>\n'
        new File(htmlFile).text != inputHtmlFileContent
        new File(cssFile).text ==
                '.content{width:50%;display:block;margin:2% auto}.header{text-align:center;color:#444;border-bottom:1px solid #eee}\n'
        new File(cssFile).text != inputCssFileContent

        then: 'javascript file should be unchanged as the folder was not included'
        new File(jsFile).text == inputJsFileContent

        cleanup:
        Files.write(Paths.get(htmlFile), inputHtmlFileContent.bytes)
        Files.write(Paths.get(cssFile), inputCssFileContent.bytes)
        Files.write(Paths.get(jsFile), inputJsFileContent.bytes)

        where:
        ci << [
                'drone',
                'vela',
                'github'
        ]
    }

    @Unroll
    void 'test minify - inclusions specified. ci: #ci'() {
        given:
        String outputHtmlFile = "${System.properties['user.dir']}/src/test/resources/output/index.min.html"
        String outputJsFile = "${System.properties['user.dir']}/src/test/resources/output/scripts.min.js"
        String outputCssFile = "${System.properties['user.dir']}/src/test/resources/output/main.min.css"

        when:
        def output = ProcessUtil.executeCommand(['docker', 'run', '--rm',
                                                 '-v', "${System.properties['user.dir']}:/work",
                                                 '-w=/work',
                                                 '-e', "${config[ci].inputPathParam}=/work/src/test/resources",
                                                 '-e', "${config[ci].outputPathParam}=/work/src/test/resources/output",
                                                 '-e', "${config[ci].envPrefix}INCLUSIONS=.*scripts/scripts.js.*\n.*static/index.*",
                                                 imageName])

        then:
        output[0] == 0
        output[1].contains('Minified /work/src/test/resources/static/index.html > /work/src/test/resources/output/index.min.html')
        output[1].contains('Minified /work/src/test/resources/scripts/scripts.js > /work/src/test/resources/output/scripts.min.js')
        new File(outputHtmlFile).text ==
                '<!doctype html><title>Test title</title><div id=layout><div id=main><div class=header><h1>Test body</h1></div></div></div>\n'
        new File(outputJsFile).text ==
                '$((function(){$("#templateAndModelForm *:input[type!=hidden]:first").focus()}));\n'

        then: "css output file should not exist as it wasn't specified in the inclusions"
        Files.notExists(Paths.get(outputCssFile))
        !output[1].contains('Minified /work/src/test/resources/static/main.css > /work/src/test/resources/output/main.min.css')

        cleanup:
        Files.deleteIfExists(Paths.get(outputHtmlFile))
        Files.deleteIfExists(Paths.get(outputJsFile))
        Files.deleteIfExists(Paths.get(outputCssFile))

        where:
        ci << [
                'drone',
                'vela',
                'github'
        ]
    }

    @Unroll
    void 'test minify - exclusions specified. ci: #ci'() {
        given:
        String outputHtmlFile = "${System.properties['user.dir']}/src/test/resources/output/index.min.html"
        String outputJsFile = "${System.properties['user.dir']}/src/test/resources/output/scripts.min.js"
        String outputCssFile = "${System.properties['user.dir']}/src/test/resources/output/main.min.css"

        when:
        def output = ProcessUtil.executeCommand(['docker', 'run', '--rm',
                                                 '-v', "${System.properties['user.dir']}:/work",
                                                 '-w=/work',
                                                 '-e', "${config[ci].inputPathParam}=/work/src/test/resources",
                                                 '-e', "${config[ci].outputPathParam}=/work/src/test/resources/output",
                                                 '-e', "${config[ci].envPrefix}EXCLUSIONS=.*static/.*",
                                                 imageName])

        then:
        output[0] == 0
        output[1].contains('Minified /work/src/test/resources/scripts/scripts.js > /work/src/test/resources/output/scripts.min.js')
        new File(outputJsFile).text ==
                '$((function(){$("#templateAndModelForm *:input[type!=hidden]:first").focus()}));\n'

        then: "html and css output files should not exist as their parent folder was specified in exclusions"
        Files.notExists(Paths.get(outputHtmlFile))
        Files.notExists(Paths.get(outputCssFile))
        !output[1].contains('Minified /work/src/test/resources/static/index.html > /work/src/test/resources/output/index.min.html')
        !output[1].contains('Minified /work/src/test/resources/static/main.css > /work/src/test/resources/output/main.min.css')

        cleanup:
        Files.deleteIfExists(Paths.get(outputHtmlFile))
        Files.deleteIfExists(Paths.get(outputJsFile))
        Files.deleteIfExists(Paths.get(outputCssFile))

        where:
        ci << [
                'drone',
                'vela',
                'github'
        ]
    }
}
