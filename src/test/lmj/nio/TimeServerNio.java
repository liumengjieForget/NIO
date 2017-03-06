package test.lmj.nio;

/** 
 * REVIEW
 * @Description: 
 * @author mengjie.liu@baidao.com mengjie.liu
 * @date 2016年10月26日 下午5:32:51 
 *  
 */

public class TimeServerNio {

	public static void main(String[] args) {
		new Thread(new TimeServerHandleNio(8080), "TimeServerNio-001").start();
	}
}
