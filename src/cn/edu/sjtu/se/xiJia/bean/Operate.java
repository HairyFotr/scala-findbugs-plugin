package cn.edu.sjtu.se.xiJia.bean;

public abstract class Operate {
	private Integer opCode;
	private Integer stackOp;

	public Operate(Integer opCode, Integer stackOp) {
		super();
		this.opCode = opCode;
		this.stackOp = stackOp;
	}

	public Integer getOpCode() {
		return opCode;
	}

	public void setOpCode(Integer opCode) {
		this.opCode = opCode;
	}

	public Integer getStackOp() {
		return stackOp;
	}

	public void setStackOp(Integer stackOp) {
		this.stackOp = stackOp;
	}

	@Override
	public String toString() {
		return opCode + ": " + stackOp;
	}

}
