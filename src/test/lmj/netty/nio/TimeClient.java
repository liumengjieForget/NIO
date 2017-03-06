package test.lmj.netty.nio;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/** 
 * REVIEW
 * @Description: 
 * @author mengjie.liu@baidao.com mengjie.liu
 * @date 2016年10月27日 下午5:37:13 
 *  
 */

public class TimeClient {

	public void connect(String host, int port) throws Exception {
		//配置客户端nio线程组
		EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new TimeClientHandle());
				}
			});
			//发起异步连接
			ChannelFuture channelFuture = bootstrap.connect(host, port);
			channelFuture.channel().closeFuture().sync();
		} finally {
			eventLoopGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) {
		int port = 8080;
		try {
			new TimeClient().connect("127.0.0.1", port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
