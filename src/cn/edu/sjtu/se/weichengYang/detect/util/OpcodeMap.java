package cn.edu.sjtu.se.weichengYang.detect.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.Constants;

public class OpcodeMap implements Constants {
	
	private static Map<Integer,String> code2value = new HashMap<Integer,String>();
	
	static{
		try{
			Class<Constants> cla = Constants.class;
			Field[] fields = cla.getDeclaredFields();
			int modifier = Modifier.PUBLIC+Modifier.STATIC+Modifier.FINAL;
			boolean start = false;
			for(Field field:fields){
				if(field.getType().getName().equals("short") && field.getModifiers()==modifier){
					String code = field.getName();
					short value = field.getShort(cla);
					if(code.equals("NOP")){
						start = true;
					}
					if(start){
						code2value.put((int)value, code);
						if(value==255){
							start = false;
							break;
						}
					}
				}
				
			}
		}catch(Exception e){
			
		}
	}
	
	public static String opcode2Instruction(int bytecode){
		return code2value.get(bytecode);
	}
}
