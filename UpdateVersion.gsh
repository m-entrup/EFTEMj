#!/usr/bin/env groovy
/**
 * Created by michael on 13.10.16.
 */
import groovy.io.FileType
import java.nio.file.Files
import java.nio.file.Paths
import groovy.xml.XmlUtil
import java.nio.file.StandardCopyOption

// specify parameters
def cli = new CliBuilder(usage: 'groovy main [option] file')
cli.l(longOpt: 'list', 'list versions')
cli.u(longOpt: 'update', args: 1, argName: 'version', 'update versions')
cli.h(longOpt: 'help', 'display usage')

def getFiles(path) {
    def list = []
    if (!path) {
        path = "."
    }
    def dir = new File(path)
    dir.eachFileRecurse(FileType.FILES) { file ->
        if (file.getName() == "pom.xml") {
            list << file
        }
    }
    return list
}

// parse and process parameters
def options = cli.parse(args)
if (options.h) cli.usage()
else if (options.l) {
    list = getFiles(options.arguments()[0])
    list.each {
        println "Reading $it:"
        def xmlSource = it
        def pom = new XmlSlurper(false, false).parse(xmlSource)
        if (pom.version != "") {
            def version = pom.version
            def id = pom.artifactId
            println "The version of $id is $version."
        } else {
            def version = pom.parent.version
            def id = pom.artifactId
            def parent = pom.parent.artifactId
            println "$id inherits from version $version of $parent."
        }
    }
}
if (options.u) {
    list = getFiles(options.arguments()[0])
    newVersion = options.u
    // TODO: Format der Version überprüfen.
    list.each {
        println "Updating $it:"
        def xmlSource = it
        def xmlBackup = new File(xmlSource.getParent() + File.separator + "~" + xmlSource.getName())
        def pom = new XmlSlurper(false, false).parse(xmlSource)
        if (pom.version != "") {
            def version = pom.version.toString()
            def id = pom.artifactId
            pom.version = newVersion
            println "Updating the version of $id from $version to $newVersion."
        } else {
            def id = pom.artifactId
            def version = pom.parent.version.toString()
            pom.parent.version = newVersion
            println "Updating the parents version of $id from $version to $newVersion."
        }
        Files.copy(Paths.get(xmlSource.getPath()),Paths.get(xmlBackup.getPath()), StandardCopyOption.REPLACE_EXISTING)
        // Try to recreate the formatting of Eclipse:
        def newPom = XmlUtil.serialize(pom)
        newPom = newPom.replaceAll(/(  )()/) { a -> "\t" }
        newPom = newPom.replaceAll(/()(?=<project)/) { a -> "\n" }
        newPom = newPom.replaceAll(/( )(?=xsi)/) { a -> "\n\t" }
        new File(xmlSource.getPath()).write(newPom)
    }
}
