import java.util.List;

public abstract class AST{
    public void error(String msg){
	System.err.println(msg);
	System.exit(-1);
    }
};

/* Expressions are similar to arithmetic expressions in the impl
   language: the atomic expressions are just Signal (similar to
   variables in expressions) and they can be composed to larger
   expressions with And (Conjunction), Or (Disjunction), and
   Not (Negation) */

abstract class Expr extends AST{
    abstract public Boolean eval(Environment env);
}

class Conjunction extends Expr{
    Expr e1,e2;
    Conjunction(Expr e1,Expr e2){this.e1=e1; this.e2=e2;}

    public Boolean eval(Environment env) {
    return e1.eval(env) && e2.eval(env);
    }
}

class Disjunction extends Expr{
    Expr e1,e2;
    Disjunction(Expr e1,Expr e2){this.e1=e1; this.e2=e2;}
    public Boolean eval(Environment env) {
        return e1.eval(env) || e2.eval(env);
    }
}

class Negation extends Expr{
    Expr e;
    Negation(Expr e){this.e=e;}
    public Boolean eval(Environment env) {
        return !e.eval(env);
    }
}

class Signal extends Expr{
    String varname; // a signal is just identified by a name 
    Signal(String varname){this.varname=varname;}

    public Boolean eval(Environment env) {
        return env.getVariable(varname);
    }
}

// Latches have an input and output signal

class Latch extends AST{
    String inputname;
    String outputname;
    Latch(String inputname, String outputname){
	this.inputname=inputname;
	this.outputname=outputname;
    }
    public void initialize(Environment env){
        env.setVariable(outputname, false);
    }
    public void nextCycle(Environment env){
        Boolean inputValue = env.getVariable(inputname);
        if (inputValue != null){
            env.setVariable(outputname, inputValue);
        } else {
            error("Variable not defined: "+inputname);
        }
    }
}

// An Update is any of the lines " signal = expression "
// in the .update section

class Update extends AST{
    String name;
    Expr e;
    Update(String name, Expr e){this.e=e; this.name=name;}

    public void eval(Environment env){
        if (env.hasVariable(name)) {
            env.setVariable(name, e.eval(env));
        } else {
            error("Variable not defined: "+name);
        }
    }
}

/* A Trace is a signal and an array of Booleans, for instance each
   line of the .simulate section that specifies the traces for the
   input signals of the circuit. It is suggested to use this class
   also for the output signals of the circuit in the second
   assignment.
*/

class Trace extends AST{
    String signal;
    Boolean[] values;
    Trace(String signal, Boolean[] values){
	this.signal=signal;
	this.values=values;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (values != null) {
            for (Boolean value : values) {
                sb.append(value ? "1" : "0").append(" ");
            }
        } else {
            sb.append("No values");
        }
        return sb.toString();
    }
}

/* The main data structure of this simulator: the entire circuit with
   its inputs, outputs, latches, and updates. Additionally for each
   input signal, it has a Trace as simulation input. 
   
   There are two variables that are not part of the abstract syntax
   and thus not initialized by the constructor (so far): simoutputs
   and simlength. It is suggested to use them for assignment 2 to
   implement the interpreter:
 
   1. to have simlength as the length of the traces in siminputs. (The
   simulator should check they have all the same length and stop with
   an error otherwise.) Now simlength is the number of simulation
   cycles the interpreter should run.

   2. to store in simoutputs the value of the output signals in each
   simulation cycle, so they can be displayed at the end. These traces
   should also finally have the length simlength.
*/

class Circuit extends AST{
    String name; 
    List<String> inputs;
    List<String> outputs;
    List<Latch>  latches;
    List<Update> updates;
    List<Trace>  siminputs;
    List<Trace>  simoutputs;
    int simlength;
    Circuit(String name,
	    List<String> inputs,
	    List<String> outputs,
	    List<Latch>  latches,
	    List<Update> updates,
	    List<Trace>  siminputs){
	this.name=name;
	this.inputs=inputs;
	this.outputs=outputs;
	this.latches=latches;
	this.updates=updates;
	this.siminputs=siminputs;
    }
    public void initialize(Environment env) {
        // Initialize input signals
        for (Trace input : siminputs) {
            if (input.values == null || input.values.length == 0) {
                error("Siminput for '" + input.signal + "' is not defined or empty.");
            } else {
                env.setVariable(input.signal, input.values[0]);
            }
        }

        // Initialize all latches
        for (Latch latch : latches) {
            latch.initialize(env);
        }

        // Evaluate all updates
        for (Update update : updates) {
            update.eval(env);
        }

        // Print the environment
        System.out.println(env.toString());
    }
    public void nextCycle(Environment env, int cycle) {
        // Check for each input signal at the current cycle
        for (Trace input : siminputs) {
            if (cycle >= input.values.length) {
                error("No input value for '" + input.signal + "' at cycle " + cycle);
            } else {
                env.setVariable(input.signal, input.values[cycle]);
            }
        }

        // Update latches for the next cycle
        for (Latch latch : latches) {
            latch.nextCycle(env);
        }

        // Evaluate all updates for the new cycle
        for (Update update : updates) {
            update.eval(env);
        }

        // Print the environment
        System.out.println(env.toString());
    }

    public void runSimulator(Environment env) {
        initialize(env);

        int n = siminputs.isEmpty() ? 0 : siminputs.get(0).values.length;

        for (int i = 1; i < n; i++) {
            nextCycle(env, i);
        }
    }

}