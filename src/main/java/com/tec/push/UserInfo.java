package com.tec.push;

import java.util.Date;

/**
 * 
 * 
 * <p>
 * 说明:用户登陆信息
 * </p>
 * <p>
 * 创建日期 2015年8月20日
 * </p>
 * 
 * @version 1.0
 * @author cody
 */
public class UserInfo {
	/**
	 * 登录用户名
	 */
	public String user;
	/**
	 * 登录密码
	 */
	public String pwd;
	/**
	 * 客户端ip
	 */
	public String clientIp;
	/**
	 * 客户端端口
	 */
	public int clientPort;
	/**
	 * 会话id，很重要。
	 */
	public long sessionId;
	/**
	 * 登录时间
	 */
	public Date loginDate;
	/**
	 * 手机号码
	 */
	public String msIsdn;
	/**
	 * 手机imei
	 */
	public String imei;

}
