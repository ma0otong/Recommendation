package com.personal.recommendation.constants;

/**
 * 业务异常基类
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
