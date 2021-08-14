import java.io.*;
import java.util.*;

import assembly.Asm;
import node.*;
import parser.*;
import ir.*;

public class Compiler {

    static public void main(String argv[]) throws Exception {
        String out = null;
        PrintStream file0=null;
        PrintStream file1=null;
        PrintStream file2=null;
        PrintStream file3=null;
        ArrayList<String> code=new ArrayList<>();
        FileOutputStream outfile = null;
        String from = argv[3];
        if (argv.length > 1) {
            out = argv[2];
            outfile = new FileOutputStream(out, false);
            file0 = new PrintStream(outfile);
        }

        parser p = new parser(new Lexer(new FileReader(from)));
        Object result = p.parse().value;
        NRoot root = p.root;
//           root.print(0,false,System.out);
        List<IR> ir = new ArrayList<>();
        ContextIR ctx = new ContextIR();
        root.generate_ir(ctx, ir);
        Asm.generate_asm(ir, code);
        for(String s:code) {
            file0.print(s);
            System.out.print(s);
        }


    }

}