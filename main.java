import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/*
# Task 3

1. Each signal is exactly one of the following:
    – an input signal (i.e., declared in the .inputs section)
    – the output of a latch (i.e., occurs as ... -> signal in the .latches section)
    – the output of an update (i.e., as signal = ... in the .updates section)

It is thus an error if a signal is neither of these nor more than one of these.

2. The updates must not be cyclic, for example sig= !sig is not allowed because would mean connecting
 a signal with its own negation. To make the detection of cyclic specifications easier we can
make the following simpler requirement: the expression in every update may only use a signal if
it is output of a previous update, an input signal, or output of a latch. Question: Why does this
requirement prevent cyclic update specifications?

3. For every input signal, the .simulation section specifies a sequence of Booleans and the sequence
is of the same length for all input signals, and not of length 0.

*/

public class main {
    public static void main(String[] args) throws IOException {

        // we expect exactly one argument: the name of the input file
        if (args.length != 1) {
            System.err.println("\n");
            System.err.println("Hardware Simulator\n");
            System.err.println("==================\n\n");
            System.err.println("Please give as input argument a filename\n");
            System.exit(-1);
        }
        String filename = args[0];

        // open the input file
        CharStream input = CharStreams.fromFileName(filename);
        //new ANTLRFileStream (filename); // depricated

        // create a lexer/scanner
        hwLexer lex = new hwLexer(input);

        // get the stream of tokens from the scanner
        CommonTokenStream tokens = new CommonTokenStream(lex);

        // create a parser
        hwParser parser = new hwParser(tokens);

        // and parse anything from the grammar for "start"
        ParseTree parseTree = parser.start();

        // The JaxMaker is a visitor that produces html/jax output as a string
        String result = new JaxMaker().visit(parseTree) + "\n";

	/* The AstMaker generates the abstract syntax to be used for
	   the second assignment, where for the start symbol of the
	   ANTLR grammar, it generates an object of class Circuit (see
	   AST.java). */
        Circuit p = (Circuit) new AstMaker().visit(parseTree);



	/* For the second assignment you need to extend the classes of
	    AST.java with some methods that correspond to running a
	    simulation of the given hardware for given simulation
	    inputs. The method for starting the simulation should be
	    called here for the Circuit p. */

        // Create an Environment object
        Environment env = new Environment();

        p.validateSimulationSection();

        // Run the simulation
        result += p.runSimulator(env) + "\n";

        result += "\n</body></html>\n";
        System.out.println(result);


    }
}

// The visitor for producing html/jax -- solution for assignment 1, task 3:

class JaxMaker extends AbstractParseTreeVisitor<String> implements hwVisitor<String> {

    public String visitStart(hwParser.StartContext ctx) {
        // add css here if you want.
        String css = "body{max-width:600px; margin:auto;}";
        StringBuilder result = new StringBuilder("<!DOCTYPE html>\n" + "<html><head><title> " + ctx.name.getText() + "</title>\n<style>" + css + "</style>" + "<script src=\"https://polyfill.io/v3/polyfill.min.js?features=es6\"></script>\n" + "<script type=\"text/javascript\" id=\"MathJax-script\" async src=\"https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js\">\n" + "</script></head><body>\n");
        result.append("<h1>").append(ctx.name.getText()).append("</h1>\n").append("<h2>Inputs</h2>\n");

        for (Token t : ctx.ins) {
            result.append(t.getText()).append(" ");
        }

        result.append("\n<h2>Outputs</h2>\n");
        for (Token t : ctx.outs) {
            result.append(t.getText()).append(" ");
        }

        result.append("\n<h2>Latches</h2>\n");

        for (hwParser.LatchdeclContext t : ctx.ls) {
            result.append(visit(t));
        }

        result.append("\n<h2>Updates</h2>\n");

        for (hwParser.UpdatedeclContext t : ctx.up) {
            result.append(visit(t));
        }

        result.append("\n<h2>Simulation inputs</h2>\n");
        for (hwParser.SimInpContext t : ctx.simin)
            result.append(visit(t));

        return result.toString();
    }

    public String visitSimInp(hwParser.SimInpContext ctx) {
        return ctx.str.getText() + " <b>" + ctx.in.getText() + "</b><br>\n";
    }

    public String visitLatchdecl(hwParser.LatchdeclContext ctx) {
        return ctx.in.getText() + " &rarr; " + ctx.out.getText() + "<br>\n";
    }

    public String visitUpdatedecl(hwParser.UpdatedeclContext ctx) {
        return ctx.write.getText() + " &larr; \\(" + visit(ctx.e) + "\\)<br>\n";
    }

    public String visitSignal(hwParser.SignalContext ctx) {
        return "\\mathrm{" + ctx.x.getText() + "}";
    }

    public String visitConjunction(hwParser.ConjunctionContext ctx) {
        return "(" + visit(ctx.e1) + "\\wedge" + visit(ctx.e2) + ")";
    }

    public String visitDisjunction(hwParser.DisjunctionContext ctx) {
        return "(" + visit(ctx.e1) + "\\vee" + visit(ctx.e2) + ")";
    }

    public String visitNegation(hwParser.NegationContext ctx) {
        return "\\neg(" + visit(ctx.e) + ")";
    }

    public String visitParenthesis(hwParser.ParenthesisContext ctx) {
        return visit(ctx.e);
    }

}

// The visitor for producing the Abstract Syntax (see AST.java).

class AstMaker extends AbstractParseTreeVisitor<AST> implements hwVisitor<AST> {

    public AST visitStart(hwParser.StartContext ctx) {
        List<String> ins = new ArrayList<>();
        for (Token t : ctx.ins) {
            ins.add(t.getText());
        }
        List<String> outs = new ArrayList<>();
        for (Token t : ctx.outs) {
            outs.add(t.getText());
        }
        List<Latch> latches = new ArrayList<>();
        for (hwParser.LatchdeclContext t : ctx.ls) {
            latches.add((Latch) visit(t));
        }
        List<Update> updates = new ArrayList<>();
        for (hwParser.UpdatedeclContext t : ctx.up) {
            updates.add((Update) visit(t));
        }
        List<Trace> siminp = new ArrayList<>();
        for (hwParser.SimInpContext t : ctx.simin)
            siminp.add((Trace) visit(t));
        return new Circuit(ctx.name.getText(), ins, outs, latches, updates, siminp);
    }

    public AST visitSimInp(hwParser.SimInpContext ctx) {
        String s = ctx.str.getText();
        boolean isValid = s.matches("[01]+");
        if (!isValid) {
            System.err.println("Input must be only 0 or 1 of length > 0");
            System.exit(-1);
        }
                // s is a string consisting of characters '0' and '1' (not numbers!)
        Boolean[] tr = new Boolean[s.length()];
        // for the simulation it is more convenient to work with
        // Booleans, so converting the string s to an array of
        // Booleans here:
        for (int i = 0; i < s.length(); i++)
            tr[i] = (s.charAt(i) == '1');
        return new Trace(ctx.in.getText(), tr);
    }

    public AST visitLatchdecl(hwParser.LatchdeclContext ctx) {
        return new Latch(ctx.in.getText(), ctx.out.getText());
    }

    public AST visitUpdatedecl(hwParser.UpdatedeclContext ctx) {
        return new Update(ctx.write.getText(), (Expr) visit(ctx.e));
    }


    public AST visitSignal(hwParser.SignalContext ctx) {
        return new Signal(ctx.x.getText());
    }

    public AST visitConjunction(hwParser.ConjunctionContext ctx) {
        return new Conjunction((Expr) visit(ctx.e1), (Expr) visit(ctx.e2));
    }

    public AST visitDisjunction(hwParser.DisjunctionContext ctx) {
        return new Disjunction((Expr) visit(ctx.e1), (Expr) visit(ctx.e2));
    }

    public AST visitNegation(hwParser.NegationContext ctx) {
        return new Negation((Expr) visit(ctx.e));
    }

    public AST visitParenthesis(hwParser.ParenthesisContext ctx) {
        return visit(ctx.e);
    }

}

