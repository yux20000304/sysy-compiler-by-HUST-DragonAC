package ir;

import assembly.ContextAsm;
import util.Pair;

import java.util.*;

public class OptIR {

    public static void optimize_ir(List<IR> ir){
        Set<String> constexpr_function=find_constexpr_function(ir);
        for(int i=0;i<2;i++){
            local_common_subexpression_elimination(ir);
            local_common_constexpr_function(ir,constexpr_function);
            //dead_code_elimination(ir);
        }
    }

    public static void dead_code_elimination(List<IR> irs){
        ContextAsm ctx = new ContextAsm(irs,irs.listIterator());
        ListIterator<IR> it=irs.listIterator();
        while(it.hasNext()){
            ctx.set_ir_timestamp(it.next());
        }

        int index=irs.size();
        for(int i=index-1;i>0;i--){
            IR temp1= irs.get(i);
            ctx.set_var_latest_use_timestamp(temp1);
            Integer cur=ctx.ir_to_time.get(temp1);
            if(temp1.dest.type==OpName.Type.Var && temp1.dest.name.startsWith("%") &&
            temp1.op_code!=IR.OpCode.CALL && temp1.op_code!=IR.OpCode.PHI_MOV){
                if(!ctx.var_latest_use_timestamp.containsKey(temp1.dest.name) ||
                ctx.var_latest_use_timestamp.get(temp1.dest.name)<=cur){
                    irs.remove(i);
                }
            }
        }

        for(int i=0;i< irs.size();i++){
            ctx.set_var_define_timestamp(irs.get(i));
        }
    }

    public static void local_common_constexpr_function(List<IR> irs,Set<String> constexpr_function){
        HashMap<String,Vector<TreeMap<HashMap<Integer,OpName>,String>>> calls=null;

        for(int i=0;i<irs.size();i++){
            IR temp1=irs.get(i);
            if(temp1.op_code==IR.OpCode.LABEL ||
                temp1.op_code==IR.OpCode.FUNCTION_BEGIN){
                calls=null;
            }

            if(temp1.op_code==IR.OpCode.CALL){
                HashMap<Integer,OpName> args=null;
                String function_name=temp1.label;
                if(constexpr_function.contains(function_name)){
                    continue;
                }
                int j=i;
                IR temp2=irs.get(j);
                while(temp2.op_code==IR.OpCode.SET_ARG){
                    args.put(temp2.dest.value,temp2.op1);
                    j--;
                    temp2=irs.get(j);
                }
                boolean can_optimize=true;
                if(args!=null) {
                    for (Integer key : args.keySet()) {
                        if (args.get(key).type == OpName.Type.Var) {
                            if (!args.get(key).name.startsWith("%")) {
                                can_optimize = false;
                                break;
                            }
                            if (args.get(key).name.substring(0, 2).equals("%&")) {
                                can_optimize = false;
                                break;
                            }
                        }
                    }
                }
                if(!can_optimize)
                    continue;
                boolean has_same_call=false;
                String prev_call_result=null;
                if(calls!=null) {
                    for (TreeMap<HashMap<Integer, OpName>, String> prev_call : calls.get(function_name)) {
                        boolean same = true;
                        HashMap<Integer, OpName> prev_call_args = prev_call.firstKey();
                        prev_call_result = prev_call.get(prev_call_args);
                        for (Integer key : args.keySet()) {
                            if (prev_call_args.get(key) == null) {
                                same = false;
                                break;
                            }
                            if (!eq(args.get(key), prev_call_args.get(key))) {
                                same = false;
                                break;
                            }
                        }
                        if (same) {
                            has_same_call = true;
                            break;
                        }
                    }
                }
                if(!has_same_call){
                    if(temp1.dest.type==OpName.Type.Var){
                        TreeMap<HashMap<Integer,OpName>,String> tree1=new TreeMap<>();
                        if(args!=null)
                            tree1.put(args, temp1.dest.name);
                        if(calls!=null)
                            calls.get(function_name).add(tree1);
                    }
                }
                else{
                    if(temp1.dest.type==OpName.Type.Var){
                        temp1.op_code=IR.OpCode.MOV;
                        temp1.op1.type=OpName.Type.Var;
                        temp1.op1.name=prev_call_result;
                        temp1.op2.type=OpName.Type.Null;
                        temp1.op2.name=null;
                        temp1.label=null;
                        irs.set(i,temp1);
                    }
                }
            }
        }

    }

    public static boolean eq(OpName a, OpName b){
        if(a.type!=b.type)
            return false;
        if(a.type==OpName.Type.Imm){
            return a.value==b.value;
        }
        if(a.type==OpName.Type.Var){
            return a.name.equals(b.name);
        }
        return true;
    }

    public static void local_common_subexpression_elimination(List<IR> ir){
        TreeMap<IR,Integer> maybe_opt=new TreeMap<>();
        Set<String> mutability_var = new TreeSet<>();

        ListIterator<IR> ir_begin= ir.listIterator();
        ContextAsm ctx=new ContextAsm(ir,ir_begin);
        ir_begin=ir.listIterator();
        while(ir_begin.hasNext()){
            ctx.set_ir_timestamp(ir_begin.next());
        }
        for(int i=0;i<ir.size();i++){
            IR it=ir.get(i);
            if(it.op_code!=IR.OpCode.PHI_MOV)
                if(it.dest.name!=null)
                    mutability_var.add(it.dest.name);
            if(it.op_code!=IR.OpCode.LABEL ||
                it.op_code!=IR.OpCode.FUNCTION_BEGIN){
                maybe_opt.clear();
            }
            if(it.dest.type==OpName.Type.Var && it.dest.name.startsWith("%") &&
            it.op1.type!=OpName.Type.Null &&
            it.op2.type!=OpName.Type.Null &&
            it.op_code!=IR.OpCode.MOVEQ && it.op_code!=IR.OpCode.MOVNE &&
            it.op_code!=IR.OpCode.MOVGT && it.op_code!=IR.OpCode.MOVGE &&
            it.op_code!=IR.OpCode.MOVLT && it.op_code!=IR.OpCode.MOVLE &&
            it.op_code!=IR.OpCode.MALLOC_IN_STACK &&
            it.op_code!=IR.OpCode.LOAD && it.op_code!=IR.OpCode.MOV &&
            it.op_code!=IR.OpCode.PHI_MOV){
                if(maybe_opt.containsKey(it)&&maybe_opt.containsValue(0)){
                    IR opt_ir = it;
                    Integer time=maybe_opt.get(it);
                    if(!mutability_var.contains(opt_ir.dest.name)){
                        //相距太远
                        if(ctx.ir_to_time.get(it) - time <100){
                            IR temp1=new IR(IR.OpCode.MOV,it.dest,opt_ir.dest);
                            ir.set(i,temp1);
                        }
                    }
                }
                maybe_opt.put(it,ctx.ir_to_time.get(it));
            }
        }

    }

    static boolean is_constexpr_function(List<IR> irs, ListIterator<IR> begin, ListIterator<IR> end){
        int begin_index = begin.nextIndex();
        int end_index = end.nextIndex();
        for(int i=begin_index;i!=end_index;i++){
            IR ir=irs.get(i);

            if(ir.op1.type==OpName.Type.Var){
                if( !ir.op1.name.startsWith("%") && !ir.op1.name.substring(0,2).equals("$a")){
                    return false;
                }
            }

            if(ir.op2.type==OpName.Type.Var){
                if( !ir.op2.name.startsWith("%") && !ir.op2.name.substring(0,2).equals("$a")){
                    return false;
                }
            }

            if(ir.op3.type==OpName.Type.Var){
                if( !ir.op3.name.startsWith("%") && !ir.op3.name.substring(0,2).equals("$a")){
                    return false;
                }
            }

            if(ir.dest.type==OpName.Type.Var){
                if( !ir.dest.name.startsWith("%") && !ir.dest.name.substring(0,2).equals("$a")){
                    return false;
                }
            }

            if(ir.op_code==IR.OpCode.INFO && ir.label.equals("NOT CONSTEXPR")){
                return false;
            }
        }
        return true;
    }

    public static Set<String> find_constexpr_function(List<IR> irs){
        Set<String> ret=new HashSet<>();
        ListIterator<IR> function_begin_it = irs.listIterator();
        ListIterator<IR> outter_it= irs.listIterator();
        while(outter_it.hasNext()){
            IR ir=outter_it.next();
            if(ir.op_code== IR.OpCode.FUNCTION_BEGIN){
                function_begin_it = irs.listIterator(outter_it.nextIndex());
            }
            else if(ir.op_code== IR.OpCode.FUNCTION_END){
                if(is_constexpr_function(irs,function_begin_it,outter_it)){
                    ret.add(irs.get(function_begin_it.nextIndex()).label);
                }
            }
        }
        return ret;
    }

    public static boolean loop_invariant_code_motion(List<IR> ir_before,List<IR> ir_cond,
                                                     List<IR> ir_jmp, List<IR> ir_do,
                                                     List<IR> ir_continue){

        HashSet<String> never_write_var=new HashSet<>();
        boolean do_optimize=false;
        Vector<List<IR>> temp=new Vector<>();
        temp.add(ir_cond);
        temp.add(ir_jmp);
        temp.add(ir_do);
        temp.add(ir_continue);
        for(List<IR> irs:temp){
            for(IR ir:irs){
                if(ir.op_code==IR.OpCode.LOAD || ir.op_code==IR.OpCode.CALL){
                    continue;
                }

                if(ir.op1.type==OpName.Type.Var){
                    never_write_var.add(ir.op1.name);
                }

                if(ir.op2.type==OpName.Type.Var){
                    never_write_var.add(ir.op2.name);
                }

                if(ir.op3.type==OpName.Type.Var){
                    never_write_var.add(ir.op3.name);
                }
            }
        }
        for(List<IR> irs:temp){
            for(IR ir:irs) {
                if(ir.dest.type==OpName.Type.Var){
                    never_write_var.remove(ir.dest.name);
                }
            }
        }

        for(List<IR> irs:temp) {
            for (IR ir : irs) {
                boolean can_optimize=true;
                if(ir.op_code==IR.OpCode.LOAD || ir.op_code==IR.OpCode.CALL ||
                ir.op_code==IR.OpCode.PHI_MOV ||
                ir.op_code==IR.OpCode.LABEL || ir.op_code==IR.OpCode.CMP ||
                ir.op_code==IR.OpCode.MOVEQ || ir.op_code==IR.OpCode.MOVNE ||
                ir.op_code==IR.OpCode.MOVGT || ir.op_code==IR.OpCode.MOVGE ||
                ir.op_code==IR.OpCode.MOVLT || ir.op_code==IR.OpCode.MOVLE ||
                ir.op_code==IR.OpCode.NOOP){
                    can_optimize=false;
                }

                if(ir.op1.type==OpName.Type.Var &&
                    !never_write_var.contains(ir.op1.name)){
                    can_optimize=false;
                }

                if(ir.op2.type==OpName.Type.Var &&
                        !never_write_var.contains(ir.op2.name)){
                    can_optimize=false;
                }

                if(ir.op3.type==OpName.Type.Var &&
                        !never_write_var.contains(ir.op3.name)){
                    can_optimize=false;
                }

                if(ir.dest.type!=OpName.Type.Var){
                    can_optimize=false;
                }

                if(can_optimize){
                    ir_before.add(ir);
                    ir.op_code=IR.OpCode.NOOP;
                    ir.op1.type=OpName.Type.Null;
                    ir.op2.type=OpName.Type.Null;
                    ir.op3.type=OpName.Type.Null;
                    ir.dest.type=OpName.Type.Null;
                    do_optimize=true;
                }
            }
        }
        return do_optimize;
    }

    public static void optimize_loop_ir(List<IR> ir_before,List<IR> ir_cond,List<IR> ir_jmp,
                                        List<IR> ir_do,List<IR> ir_continue){
        while(loop_invariant_code_motion(ir_before,ir_cond,ir_jmp,ir_do,ir_continue));
    }
}
