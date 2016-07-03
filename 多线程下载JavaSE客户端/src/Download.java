import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;


public class Download {
	
	private static final String MODE = "rw";
	private int ThreadCount = 5;
	private String path ="http://192.168.1.110:8080/day11_web_fileupload_filedownload/WEB-INF/upload/girl_1.png";

	public void down() {
		new Thread() {
			public void run() {
				HttpURLConnection conn = null;
				RandomAccessFile raf = null;
				try {
					URL url = new URL(path);
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(3000);
					
					int code = conn.getResponseCode();
					if(code == 200) {
						System.out.println("响应成功");
						int length = conn.getContentLength();
						//创建一个跟下载图片大小一样的副本，但还是个空壳，为之后多线程下载作准备
						File file = new File("C:/Programs/tomcat.gif");
						//如果模式为只读 r，不会创建文件。会去读取一个已存在文件，如果该文件不存在，则会出现异常。
						//如果模式为读写 rw，操作的文件不存在，会自动创建，如果存在则不会覆盖该文件。
						raf = new RandomAccessFile(file, MODE);
						raf.setLength(length);
						//平均每个线程下载的大小
						int blockSize = length / ThreadCount;
						//算出实际每个线程要下载的大小
						for(int i=0; i<ThreadCount; i++) {
							int begin = i * blockSize;
							int end = i * blockSize + (blockSize - 1);
							if(i == 2)
								end = length;
							new ChildDown(begin, end, path).start();
						}
					} else {
						System.out.println("服务器故障");
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if(raf != null) {
						try {
							raf.close();
							raf = null;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if(conn != null) {
						conn.disconnect();
						conn = null;
					}
				}
			}
		}.start();
	}
	
	private int count = ThreadCount;
	/**
	 * 子线程下载类
	 * @author emperormzj
	 *
	 */
	private class ChildDown extends Thread {
		private int begin;
		private int end;
		private String path;
		
		public ChildDown(int begin, int end, String path) {
			this.begin = begin;
			this.end = end;
			this.path = path;
		}
		
		public void run() {
			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			InputStream is = null;
			try {
				URL url = new URL(path);
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(3000);
				//设置下载范围的请求头
				conn.setRequestProperty("range", "bytes="+begin+"-"+end);
				
				int code = conn.getResponseCode();
				if(code == 206) {		//返回响应内容的一部分，并且成功，那响应码是206
					System.out.println("子线程开始下载");
					File file = new File("C:/Programs/tomcat.gif");
					raf = new RandomAccessFile(file, MODE);
					is = conn.getInputStream();
					//指定数据从什么地方开始写入
					raf.seek(begin);
					byte[] buf = new byte[1024];
					int len = -1;
					while((len = is.read(buf)) != -1) {
						raf.write(buf, 0, len);
					}
					
					synchronized (Download.class) {
						count --;
						if(count == 0)
							System.out.println("下载完成");
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}  finally {
				if(is != null) {
					try {
						is.close();
						is = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(raf != null) {
					try {
						raf.close();
						raf = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(conn != null) {
					conn.disconnect();
					conn = null;
				}
			}
		}
	}
	
	public static void main(String[] args) {
		new Download().down();
	}
}
