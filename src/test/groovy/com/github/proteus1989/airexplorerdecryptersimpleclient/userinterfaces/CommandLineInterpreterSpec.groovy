package com.github.proteus1989.airexplorerdecryptersimpleclient.userinterfaces


import picocli.CommandLine
import spock.lang.Specification

class CommandLineInterpreterSpec extends Specification {

    def 'should process program options properly'() {
        given: 'a commandInterpreterImpl'
        CommandLineInterpreter impl = new CommandLineInterpreter()

        and: 'a command line interpreter'
        CommandLine cmd = new CommandLine(impl).setTrimQuotes(true)

        when: 'options are processed'
        cmd.execute(args)

        then: 'the options are present'
        impl.getPassword() == pass
        impl.getOutput() == output
        impl.isGui() == gui
        impl.isHelp() == help

        where:
        args                                           || pass      | output             | gui   | help
        new String[0]                                  || null      | null               | false | false
        new String[]{'-p=my_pass'}                     || 'my_pass' | null               | false | false
        new String[]{'-p="my_pass"'}                   || 'my_pass' | null               | false | false
        new String[]{'--password=my_pass'}             || 'my_pass' | null               | false | false
        new String[]{'--password="my_pass"'}           || 'my_pass' | null               | false | false
        new String[]{'-o=../../'}                      || null      | new File('../../') | false | false
        new String[]{'--output="../../"'}              || null      | new File('../../') | false | false
        new String[]{'--output=../../'}                || null      | new File('../../') | false | false
        new String[]{'-g'}                             || null      | null               | true  | false
        new String[]{'-h'}                             || null      | null               | false | true
        new String[]{'-p="pass"', '-o="../../"', '-g'} || 'pass'    | new File('../../') | true  | false
    }

    def 'should parse arguments properly'() {
        given: 'a commandInterpreterImpl'
        CommandLineInterpreter impl = new CommandLineInterpreter()

        and: 'a command line interpreter'
        CommandLine cmd = new CommandLine(impl).setTrimQuotes(true)

        when: 'options are processed'
        int exitCode = cmd.setTrimQuotes(true).execute(args)

        then: 'the options are present'
        exitCode == code

        where:
        args                                                                                                     || code
        new String[]{CommandLineInterpreterSpec.class.getSimpleName()}                                           || 2
        new String[]{'invalidPath'}                                                                              || 2
        new String[]{CommandLineInterpreterSpec.class.getSimpleName(), 'invalidPath'}                            || 2
        new String[]{'-p=pass'}                                                                                  || 2
        new String[]{'src/test/resources/4aad7bBqwQGCr7kbatwHRClR2lPF8sSQQAXgJ2YR1wk=.cloudencoded2'}            || 2
        new String[]{'-p=pass', 'src/test/resources/4aad7bBqwQGCr7kbatwHRClR2lPF8sSQQAXgJ2YR1wk=.cloudencoded2'} || 0
    }


}
