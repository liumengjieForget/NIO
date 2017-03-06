package test.lmj.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/** 
 * REVIEW
 * @Description: 
 * @author mengjie.liu@baidao.com mengjie.liu
 * @date 2016年10月27日 上午9:56:59 
 *  
 */

public class TimeServerHandleNio implements Runnable {

	private ServerSocketChannel serverSocketChannel;

	private Selector selector;

	private volatile boolean stop;

	String param;

	public TimeServerHandleNio(int port) {
		try {
			//打开
			serverSocketChannel = ServerSocketChannel.open();
			//设置为非阻塞
			serverSocketChannel.configureBlocking(false);
			//backlog 1024 等待队列长度
			serverSocketChannel.socket().bind(new InetSocketAddress(port), 1024);

			selector = Selector.open();
			//将serverSocketChannel注册到selector,会返回一个SelectionKey
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			/**
			SelectionKey.OP_ACCEPT —— 接收连接继续事件，表示服务器监听到了客户连接，服务器可以接收这个连接了
			SelectionKey.OP_CONNECT —— 连接就绪事件，表示客户与服务器的连接已经建立成功
			SelectionKey.OP_READ —— 读就绪事件，表示通道中已经有了可读的数据，可以执行读操作了（通道目前有数据，可以进行读操作了）
			SelectionKey.OP_WRITE —— 写就绪事件，表示已经可以向通道写数据了（通道目前可以用于写操作）
			**/
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void stop() {
		this.stop = true;
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				int len = selector.select();
				if (len < 0) {
					continue;
				}
				//已经就绪的通道key
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				//迭代这些key
				Iterator<SelectionKey> it = selectionKeys.iterator();
				SelectionKey selectionKey = null;
				while (it.hasNext()) {
					selectionKey = it.next();
					it.remove();
					handle(selectionKey);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			//			catch (ClosedSelectorException e1) {
			//				System.out.println("等待超时 了！重新来!");
			//			}
		}
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void handle(SelectionKey selectionKey) throws IOException {
		/** 告知此键是否有效**/
		if (selectionKey.isValid()) {
			/**测试此键的通道是否已准备好接受新的套接字连接**/
			if (selectionKey.isAcceptable()) {//处理新接入的请求
				/**返回为之创建此键的通道**/
				//ServerSocketChannel _serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
				SocketChannel socketChannel = serverSocketChannel.accept();
				socketChannel.configureBlocking(false);
				socketChannel.register(selector, SelectionKey.OP_READ);
			}
			/**测试此键的通道是否已准备好进行读取**/
			if (selectionKey.isReadable()) {
				SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
				//创建一个容量1024的byteBuffer
				ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
				int readBytes = socketChannel.read(byteBuffer);
				if (readBytes > 0) {
					//设置读取时的位置和可读数据长度
					byteBuffer.flip();
					//可读数的长度limit - position，创建byte数组
					byte[] bytes = new byte[byteBuffer.remaining()];
					//数据读入byte数组
					byteBuffer.get(bytes);
					String param = new String(bytes, "UTF-8");
					this.param = param;
					System.out.println("client pram : " + param);
				} else if (readBytes < 0) {
					selectionKey.cancel();
					socketChannel.close();
				} else {
					//读到0字节  忽略
				}
				socketChannel.register(selector, SelectionKey.OP_WRITE);
			} else if (selectionKey.isWritable()) {
				SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
				String time = "";
				if ("query time".equalsIgnoreCase(param.trim())) {
					SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					time = sf.format(new Date());
				} else {
					time = "unkown request!";
				}
				doWrite(socketChannel, time);
				socketChannel.register(selector, SelectionKey.OP_READ);
			}
		}
	}

	private void doWrite(SocketChannel socketChannel, String time) throws IOException {
		if (socketChannel != null) {
			byte[] bytes = time.getBytes();
			ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
			byteBuffer.put(bytes);
			byteBuffer.flip();
			socketChannel.write(byteBuffer);
		}
	}
}
