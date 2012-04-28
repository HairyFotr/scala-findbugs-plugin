package cn.edu.sjtu.se.xiJia.bean;

import edu.umd.cs.findbugs.OpcodeStack.Item;

public class MethodOperate extends Operate {
	private String name;
	private String className;
	private String signature;
	private Integer argumentNum;
	private String returnType;
	private Item stackTop;

	public MethodOperate(Integer opCode, Integer stackOp, String name,
			String className, String signature, Integer argumentNum,
			Item stackTop) {
		super(opCode, stackOp);
		this.name = name;
		this.className = className;
		this.signature = signature;
		this.argumentNum = argumentNum;
		this.returnType = signature.substring(signature.indexOf(')') + 1);
		this.stackTop = stackTop;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public Integer getArgumentNum() {
		return argumentNum;
	}

	public void setArgumentNum(Integer argumentNum) {
		this.argumentNum = argumentNum;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public Item getStackTop() {
		return stackTop;
	}

	public void setStackTop(Item stackTop) {
		this.stackTop = stackTop;
	}

	@Override
	public String toString() {
		return super.toString() + " Method:" + name + " " + signature + " "
				+ returnType;
	}

}
