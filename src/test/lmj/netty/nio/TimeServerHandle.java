package test.lmj.netty.nio;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;

/** 
 * REVIEW
 * @Description: 
 * @author mengjie.liu@baidao.com mengjie.liu
 * @date 2016年10月27日 下午5:07:24 
 *  
 */

public class TimeServerHandle extends ChannelInboundHandlerAdapter {
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = (ByteBuf) msg;
		byte[] req = new byte[buf.readableBytes()];
		buf.readBytes(req);
		String param = new String(req, "UTF-8");
		String time = null;
		System.out.println("client param : " + param);
		if ("query time".equalsIgnoreCase(param.trim())) {
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			time = sf.format(new Date());

		} else {
			time = "unkown request!";
		}
		ByteBuf rep = Unpooled.copiedBuffer(time.getBytes());
		ctx.write(rep);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}

}
