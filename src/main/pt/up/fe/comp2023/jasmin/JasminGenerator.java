package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.ClassUnit;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.Collections;

public class JasminGenerator implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {

        String jasminCode = new OllirToJasmin(ollirResult.getOllirClass()).getCode();
        var jasminResult = new JasminResult(jasminCode);

        return jasminResult;
    }

}