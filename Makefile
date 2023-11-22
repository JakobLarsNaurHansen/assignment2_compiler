##### If you have not installed ANTLR in your classpath, you still need to copy antlr-4.13.0-complete.jar to this folder and set up the classpath option:

classpathoption = -cp 'antlr-4.13.0-complete.jar:.'  # if you are using mac/linux
#classpathoption = -cp 'antlr-4.13.0-complete.jar;.'  # if you are using windows

#classpathoption =   # nothing if already installed


antlr4 = java $(classpathoption) org.antlr.v4.Tool
grun   = java $(classpathoption) org.antlr.v4.gui.TestRig
SRCFILES  = main.java Environment.java AST.java
GENERATED = hwLexer.java hwParser.java hwBaseVisitor.java hwVisitor.java hwBaseListener.java hwListener.java

all:	
	make run

hwLexer.java:	hw.g4
	$(antlr4) -visitor hw.g4

main.class:	$(SRCFILES) $(GENERATED)
	javac $(classpathoption) $(SRCFILES) $(GENERATED) 

run:	main.class
	java $(classpathoption) main 01-hello-world.hw  > 01.html
	java $(classpathoption) main 02-trafiklys-minimal.hw  > 02.html
	java $(classpathoption) main 03-trafiklys.hw  > 03.html
	java $(classpathoption) main 04-von-Neumann.hw  > 04.html

error1: main.class
	java $(classpathoption) main 05-error1.hw

error2: main.class
	java $(classpathoption) main 06-error2.hw

error3: main.class
	java $(classpathoption) main 07-error3.hw

grun:	hwLexer.class hwParser.class 01-hello-world.hw
	$(grun) impl start -gui -tokens 01-hello-world.hw

clean:
	rm $(GENERATED) *.class hw.interp hwLexer.interp hwLexer.tokens *.html
