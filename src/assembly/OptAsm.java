package assembly;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class OptAsm {


    public String opt_asm1(List<String> line1){
        ArrayList<String> temp1=new ArrayList<>();
        String tar_line;
        String token;
        // ADD R0,R1,#0  => MOV R0,R1
        if(line1.contains("ADD")&& line1.contains("#0")){

        }
        return null;
    }


    public void optimize_asm(List<String> in,List<String> out){
        String line1=null;
        String line1_orin=null;
        String line0=null;
        String line_target=null;
        boolean isfirst=true;

        int length = 0;
        for(int i=0;i<in.size();i++) {
            ArrayList<String> temp=new ArrayList<>();
            //读取一行信息
            while(!in.get(i).endsWith("\n")){
                temp.add(in.get(i));
                i++;
            }
            temp.add("\n");
            //如果是注释，跳过
            if(temp.get(0).startsWith("#")){
                for(String index1:temp)
                    out.add(index1);
                continue;
            }
            else{
                out.add(opt_asm1(temp));
            }

        }

    }
}
