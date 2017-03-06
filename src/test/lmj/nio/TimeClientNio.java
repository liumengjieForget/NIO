package test.lmj.nio;

/** 
 * REVIEW
 * @Description: 
 * @author mengjie.liu@baidao.com mengjie.liu
 * @date 2016年10月26日 下午5:32:51 
 *  
 */

public class TimeClientNio {

	public static void main(String[] args) {
		new Thread(new TimeClientHandleNio("localhost", 8080), "TimeClientNio-001").start();
	}
}
