package ir;

import assembly.ContextAsm;
import util.Pair;

import java.util.*;

public class OptIR {

    public static void optimize_ir(List<IR> ir){
        Set<String> constexpr_function=find_constexpr_function(ir);
        for(int i=0;i<2;i++){
            local_common_subexpression_elimination(ir);
        }
    }

    public static void local_common_subexpression_elimination(List<IR> ir){
        TreeMap<IR,Integer> maybe_opt=new TreeMap<>();
        TreeSet<String> mutability_var = new TreeSet<>();

        ListIterator<IR> ir_begin= ir.listIterator();
        ContextAsm ctx=new ContextAsm(ir,ir_begin);
        ir_begin=ir.listIterator();
        while(ir_begin.hasNext()){
            ctx.set_ir_timestamp(ir_begin.next());
        }
        ir_begin=ir.listIterator();
        while(ir_begin.hasNext()){
            IR it=ir.get(ir_begin.nextIndex());
            if(it.op_code!=IR.OpCode.PHI_MOV)
                if(ir.get(ir_begin.nextIndex()).dest.name!=null)
                mutability_var.add(ir.get(ir_begin.nextIndex()).dest.name);
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
                if(maybe_opt.containsKey(it) && !it.equals(maybe_opt.lastKey())){
                    IR opt_ir = it;
                    Integer time=maybe_opt.get(it);
                    if(mutability_var.last().equals(opt_ir.dest.name)){
                        //相距太远
                        if(ctx.ir_to_time.get(it) - time <20){
                            IR temp1=new IR(IR.OpCode.MOV,it.dest,opt_ir.dest);
                            ir_begin.remove();
                            ir_begin.add(temp1);
                        }
                    }
                }

            }
            maybe_opt.put(ir.get(ir_begin.nextIndex()),ctx.ir_to_time.get(ir_begin.next()));
        }

    }

    static boolean is_constexpr_function(List<IR> irs, ListIterator<IR> begin, ListIterator<IR> end){
        int begin_index = begin.nextIndex();
        int end_index = end.nextIndex();
        for(int i=begin_index;i!=end_index;i++){
            IR ir=irs.get(i);

            if(ir.op1.type==OpName.Type.Var){
                if( !ir.op1.name.startsWith("%") && !ir.op1.name.substring(0,4).equals("$arg")){
                    return false;
                }
            }

            if(ir.op2.type==OpName.Type.Var){
                if( !ir.op2.name.startsWith("%") && !ir.op2.name.substring(0,4).equals("$arg")){
                    return false;
                }
            }

            if(ir.op3.type==OpName.Type.Var){
                if( !ir.op3.name.startsWith("%") && !ir.op3.name.substring(0,4).equals("$arg")){
                    return false;
                }
            }

            if(ir.dest.type==OpName.Type.Var){
                if( !ir.dest.name.startsWith("%") && !ir.dest.name.substring(0,4).equals("$arg")){
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
}
