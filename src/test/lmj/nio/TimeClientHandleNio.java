package test.lmj.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/** 
 * REVIEW
 * @Description: 
 * @author mengjie.liu@baidao.com mengjie.liu
 * @date 2016年10月27日 下午2:42:19 
 *  
 */

public class TimeClientHandleNio implements Runnable {

	private SocketChannel socketChannel;
	private Selector selector;

	private volatile boolean stop;

	public TimeClientHandleNio(String host, int port) {
		try {
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			selector = Selector.open();

			socketChannel.connect(new InetSocketAddress(host, port));

			selector = Selector.open();
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void run() {

		if (socketChannel.isConnectionPending()) {
			try {
				socketChannel.finishConnect();
				socketChannel.register(selector, SelectionKey.OP_WRITE);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
		}
		System.out.println("输入任意键可继续!");
		Scanner scanner = new Scanner(System.in);

		while (scanner.hasNextLine()) {
			scanner.nextLine();
			stop = false;
			while (!stop) {
				try {
					int len = selector.select();
					if (len < 0) {
						continue;
					}
					Set<SelectionKey> selectionKeys = selector.selectedKeys();
					Iterator<SelectionKey> it = selectionKeys.iterator();
					SelectionKey selectionKey = null;
					while (it.hasNext()) {
						selectionKey = it.next();
						it.remove();
						try {
							handle(selectionKey);
						} catch (IOException e) {
							if (selectionKey != null) {
								selectionKey.cancel();
								if (selectionKey.channel() != null) {
									selectionKey.channel().close();
								}
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handle(SelectionKey selectionKey) throws IOException {
		if (selectionKey.isValid()) {//连接是否成功
			if (selectionKey.isWritable()) {
				doWrite(socketChannel);
				socketChannel.register(selector, SelectionKey.OP_READ);
			}
			if (selectionKey.isReadable()) {
				ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
				int readBytes = socketChannel.read(byteBuffer);
				if (readBytes > 0) {
					byteBuffer.flip();
					byte[] bytes = new byte[byteBuffer.remaining()];
					byteBuffer.get(bytes);
					String response = new String(bytes, "UTF-8");
					System.out.println("server response time : " + response);
					socketChannel.register(selector, SelectionKey.OP_WRITE);
					this.stop = true;
				} else if (readBytes < 0) {
					selectionKey.cancel();
					socketChannel.close();
				} else {
					//忽略
				}
			}
		}
	}

	private void doWrite(SocketChannel socketChannel) throws IOException {
		byte[] param = "query time".getBytes();
		ByteBuffer byteBuffer = ByteBuffer.allocate(param.length);
		byteBuffer.put(param);
		byteBuffer.flip();
		socketChannel.write(byteBuffer);
		System.out.println("client send param success");
	}
}
