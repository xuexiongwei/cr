package com.example.copyresult.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @Title: Restful.java
 * @Package Restful
 */
public class Restful {

	private static String module = Restful.class.getSimpleName();

	public static String GET = "GET";
	public static String POST = "POST";

	public static String restful(String urlPath, String cookie, String requestMethod, String json,
			Integer connectTimeout, Integer readTimeout)
			throws IOException, KeyManagementException, NoSuchAlgorithmException {

		String content = null;
		OutputStreamWriter out = null;
		HttpURLConnection conn = null;
		try {
			// Debug.info("请求：urlPath="+urlPath+" ,cookie="+cookie+"
			// ,requestMethod="+requestMethod+" ,json="+json,module);
			StringBuffer result = new StringBuffer();
//			HttpsURLConnection.setDefaultHostnameVerifier(new Restful().new NullHostNameVerifier());
//			SSLContext sc = SSLContext.getInstance("TLS");
//			CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
//			sc.init(null, trustAllCerts, new SecureRandom());
//			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			URL url = new URL(urlPath);
			// 打开restful链接
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(requestMethod);// POST GET PUT DELETE
			// 设置访问提交模式，表单提�?
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
			if (cookie != null && !"".equals(cookie)) {
				// Debug.info("请求Cookie:" + cookie, module);
				conn.setRequestProperty("Cookie", cookie);
			}
			if (null == connectTimeout)
				connectTimeout = 130000;
			if (null == readTimeout)
				readTimeout = 130000;
			conn.setConnectTimeout(connectTimeout);// 连接超时 单位毫秒
			conn.setReadTimeout(readTimeout);// 读取超时 单位毫秒
			if (POST.equals(requestMethod) && null != json) {// 如果为POST提交，则设置请求参数
				// 发�?POST请求必须设置如下两行
				conn.setDoOutput(true);
				conn.setDoInput(true);
				// 获取URLConnection对象对应的输出流
				out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8"); // 8859_1
				out.write(json); // post的关键所�?
				// flush输出流的缓冲
				out.flush();
			}
			// 读取请求返回
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result.append(line);
			}
			content = result.toString();
		} catch (Exception e) {
			e.printStackTrace();
			if (urlPath.indexOf("web/rest/cashlogin") != -1) {
				Cache.message = null;
			}
		} finally {
			if (null != out) {
				out.close();
			}
			if (null != conn) {
				conn.disconnect();
			}
		}
		return content;
	}

	static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			// TODO Auto-generated method stub
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			// TODO Auto-generated method stub
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			// TODO Auto-generated method stub
			return null;
		}
	} };

	public class NullHostNameVerifier implements HostnameVerifier {
		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String,
		 * javax.net.ssl.SSLSession)
		 */
		@Override
		public boolean verify(String arg0, SSLSession arg1) {
			// TODO Auto-generated method stub
			return true;
		}
	}
}