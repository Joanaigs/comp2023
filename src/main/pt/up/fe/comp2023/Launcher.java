package pt.up.fe.comp2023;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp2023.jasmin.JasminGenerator;
import pt.up.fe.comp2023.ollir.Optimization;
import pt.up.fe.comp2023.semantic_analysis.SemanticAnalysis;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

public class Launcher {

    public static void main(String[] args) {
        // Setups console logging and other things
        SpecsSystem.programStandardInit();

        // Parse arguments as a map with predefined options
        var config = parseArgs(args);

        // Get input file
        File inputFile = new File(config.get("inputFile"));

        // Check if file exists
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + inputFile + "'.");
        }

        // Read contents of input file
        String code = SpecsIo.read(inputFile);

        // Instantiate JmmParser
        SimpleParser parser = new SimpleParser();

        // Parse stage
        JmmParserResult parserResult = parser.parse(code, config);

        // Check if there are parsing errors
        TestUtils.noErrors(parserResult.getReports());

        System.out.println(parserResult.getRootNode().toTree());


        SemanticAnalysis semanticAnalysis= new SemanticAnalysis();
        JmmSemanticsResult jmmSemanticsResult= semanticAnalysis.semanticAnalysis(parserResult);
        System.out.println(jmmSemanticsResult.getSymbolTable().print());

        // Check if there are semantic errors
        TestUtils.noErrors(jmmSemanticsResult.getReports());



        //ollir
        Optimization optimizer = new Optimization();

        if(config.get("optimize").equals("true")){
            optimizer.optimize(jmmSemanticsResult);
        }

        OllirResult ollir = optimizer.toOllir(jmmSemanticsResult);
        System.out.println(ollir.getOllirCode());
        // ... add remaining stages

        //jasmin
        JasminGenerator jasminGenerator = new JasminGenerator();
        JasminResult jasmin = jasminGenerator.toJasmin(ollir);
        System.out.println(jasmin.getJasminCode());


    }

    private static Map<String, String> parseArgs(String[] args) {
        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // Check if there is at least one argument
        if (args.length != 1) {
            throw new RuntimeException("Expected a single argument, a path to an existing input file.");
        }

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", args[0]);
        config.put("optimize", "true");
        config.put("registerAllocation", "-1");
        config.put("debug", "false");

        return config;
    }

}
