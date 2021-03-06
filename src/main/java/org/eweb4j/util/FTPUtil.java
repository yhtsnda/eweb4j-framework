package org.eweb4j.util;

import java.io.File;
import java.io.UnsupportedEncodingException;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class FTPUtil implements ProtocolCommandListener{
	
	public FTPClient client = new FTPClient();
	private String host;
	private int port;
	private String user;
	private String pwd;
	private FTPListener listener;
	private boolean debug;
	
	public FTPUtil(String host, int port, String user, String pwd) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.pwd = pwd;
	}

	public void setListener(FTPListener listener){
		this.listener = listener;
	}
	
	public void setDebug(boolean debug){
		this.debug = debug;
	}
	
	public void connectAndLogin() throws Exception{
		client.addProtocolCommandListener(this);
		client.connect(host, port); 
		client.setControlEncoding("UTF-8"); 
		client.setFileType(FTPClient.BINARY_FILE_TYPE);
        if(!FTPReply.isPositiveCompletion(client.getReplyCode())){ 
        	client.disconnect();
        	throw new Exception("FTP is not PositiveCompletion");
        }
        
        if(!client.login(user, pwd)){
        	client.disconnect();
        	throw new Exception("FTP login fail");
        }
        
        if (listener != null)
        	listener.onInfo("FTP login success");
	}
	
	public void logoutAndDisconnect(){
		if (client != null) {
			try {
				client.logout();
				if (listener != null)
					listener.onInfo(" ftp logout success");
			} catch (Exception e){
				if (listener != null)
					listener.onError(" can not logout ftp", e);
			}
			
			try {
				client.disconnect();
				if (listener != null)
					listener.onInfo(" ftp disconnect success");
			} catch (Exception e){
				if (listener != null)
					listener.onError(" can not disconnect ftp", e);
			}
		}
	}
	
	private static String resolvePath(String path){
		File dir = new File(path);
		return dir.getPath().replace("\\", "/");
	}
	
	public boolean mkdir(String remoteDir) throws Exception{
		if (remoteDir == null || remoteDir.trim().length() == 0)
			throw new Exception("remoteDir -> " + remoteDir + " required !");
		String path = FTPUtil.resolvePath(remoteDir);
		// split by /
		String[] remoteDirs = path.split("/");
		StringBuilder builder = new StringBuilder("/");
		for (String dir : remoteDirs){
			if (builder.length() > 1)
				builder.append("/");
			
			builder.append(dir);
			boolean isDirOk = client.changeWorkingDirectory(builder.toString());
			if (!isDirOk)
				isDirOk = client.makeDirectory(builder.toString());
			
			if (!isDirOk)
				throw new Exception("ftp mkdir -> " + builder.toString() + " fail");
		}
		
		return true;
	}
	
	public static void main(String[] args) throws Exception{
		FTPUtil ftp = new FTPUtil("shoplay.com", 21, "weiwei", "123456");
		ftp.setListener(new FTPListener() {
			public void onInfo(String info) {
				System.out.println(info);
			}
			
			public void onError(String err, Exception e) {
				e.printStackTrace();
			}
		});
		
		ftp.setDebug(true);
		ftp.connectAndLogin();
		ftp.mkdir("/wwwroot/longyan-web/cache/bigpic/20121006/470/");
		ftp.logoutAndDisconnect();
	}
	
    public void protocolCommandSent(ProtocolCommandEvent ev) {
		try {
			String info  = new String(("sent->"+ev.getReplyCode()+"-"+"c->"+ev.getCommand()+"-"+ev.getMessage()).getBytes("iso8859-1"), "gbk");
			if (this.debug && listener != null)
				listener.onInfo(info);
		} catch (UnsupportedEncodingException e) {
			String info = e.toString();
			if (listener != null)
				listener.onError(info, e);
		}
	}

	public void protocolReplyReceived(ProtocolCommandEvent ev) {
		try {
			String info  = new String(("receive->"+ev.getReplyCode()+"-"+"c->"+ev.getCommand()+"-"+ev.getMessage()).getBytes("iso8859-1"), "gbk");
			if (this.debug && listener != null)
				listener.onInfo(info);
		} catch (UnsupportedEncodingException e) {
			String info = e.toString();
			if (listener != null)
				listener.onError(info, e);
		}
	}
}
