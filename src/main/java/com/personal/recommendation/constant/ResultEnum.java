package com.personal.recommendation.constant;

/**
 * 业务异常基类，所有业务异常都必须继承于此异常 定义异常时，需要先确定异常所属模块。 例如：无效用户可以定义为 [10010001]
 * 前四位数为系统模块编号，后4位为错误代码 ,唯一。
 */
@SuppressWarnings("unused")
public enum ResultEnum {

    // 通用
    SUCCESS(200,"OK"),
    FAILURE(100, "FAILURE"),
    PARA_ERR(99, "参数错误"),
    IP_NOT_IN_WHITE_LIST(98, "ip无访问权限"),

    FAIL_AND_TRY_AGAIN(88, "操作失败， 请重试"),
    UPDATE_NEW_VERSION(2000, "版本过低，请升级到最新版本");

    private int state;

	private String msg;

	ResultEnum(int state, String msg) {
		this.state = state;
		this.msg = msg;
	}

	public int getState() {
		return state;
	}

	public String getMsg() {
		return msg;
	}

}
