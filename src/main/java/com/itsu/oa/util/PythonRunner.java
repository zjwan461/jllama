package com.itsu.oa.util;

import org.python.core.PyFunction;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

public class PythonRunner {

    private static PythonInterpreter interpreter = new PythonInterpreter();

    public static void runPython(String script) {
        interpreter.execfile(script);
    }

    public static void runPython(String script, String function) {
        interpreter.execfile(script);
        PyFunction pyFunction = interpreter.get(function, PyFunction.class);
        pyFunction.__call__(new PyString("unsloth/DeepSeek-R1-Distill-Qwen-1.5B-GGUF"), new PyString("./models"), new PyString("DeepSeek-R1-Distill-Qwen-1.5B-GGUF"));
    }

    public static void main(String[] args) {
        init();
        runPython("D:\\workspaces\\java\\jllama\\scripts\\download.py", "download");
    }

    private static void init() {
        interpreter.exec("setup.py install modelscope");
    }
}
