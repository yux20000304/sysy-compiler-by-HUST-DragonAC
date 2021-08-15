package assembly;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class OptAsm {


    public String opt_asm1(List<String> line1,List<String> line2){
        ArrayList<String> temp1=new ArrayList<>();
        String tar_line;
        String token;
        // 连续的两行


        return null;
    }


    public static void optimize_asm(List<String> in, List<String> out){


        for(int i=0;i<in.size();i++) {
            ArrayList<String> line1=new ArrayList<>();
            ArrayList<String> line2=new ArrayList<>();
            if(in.get(i).endsWith("\n")) {
                line1.add(in.get(i));
                System.out.print(in.get(i));
            }

//            System.out.println(in.get(i));
        }
    }
}
