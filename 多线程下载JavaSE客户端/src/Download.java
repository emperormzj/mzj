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
						System.out.println("��Ӧ�ɹ�");
						int length = conn.getContentLength();
						//����һ��������ͼƬ��Сһ���ĸ����������Ǹ��տǣ�Ϊ֮����߳�������׼��
						File file = new File("C:/Programs/tomcat.gif");
						//���ģʽΪֻ�� r�����ᴴ���ļ�����ȥ��ȡһ���Ѵ����ļ���������ļ������ڣ��������쳣��
						//���ģʽΪ��д rw���������ļ������ڣ����Զ���������������򲻻Ḳ�Ǹ��ļ���
						raf = new RandomAccessFile(file, MODE);
						raf.setLength(length);
						//ƽ��ÿ���߳����صĴ�С
						int blockSize = length / ThreadCount;
						//���ʵ��ÿ���߳�Ҫ���صĴ�С
						for(int i=0; i<ThreadCount; i++) {
							int begin = i * blockSize;
							int end = i * blockSize + (blockSize - 1);
							if(i == 2)
								end = length;
							new ChildDown(begin, end, path).start();
						}
					} else {
						System.out.println("����������");
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
	 * ���߳�������
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
				//�������ط�Χ������ͷ
				conn.setRequestProperty("range", "bytes="+begin+"-"+end);
				
				int code = conn.getResponseCode();
				if(code == 206) {		//������Ӧ���ݵ�һ���֣����ҳɹ�������Ӧ����206
					System.out.println("���߳̿�ʼ����");
					File file = new File("C:/Programs/tomcat.gif");
					raf = new RandomAccessFile(file, MODE);
					is = conn.getInputStream();
					//ָ�����ݴ�ʲô�ط���ʼд��
					raf.seek(begin);
					byte[] buf = new byte[1024];
					int len = -1;
					while((len = is.read(buf)) != -1) {
						raf.write(buf, 0, len);
					}
					
					synchronized (Download.class) {
						count --;
						if(count == 0)
							System.out.println("�������");
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
