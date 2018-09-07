package com.sherlock.imService.utils;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: gl
 * Date: 14-3-20 下午12:43
 */
public class HttpClient {

    private static Logger logger = LoggerFactory.getLogger(HttpClient.class);
    private static final String DEFAULT_CHARSET = "UTF-8";
    private  static final Byte[] LOCKS = new Byte[0];  
    
//    private static HttpClient INSTANCE1 = null;
    private volatile static HttpClient INSTANCE = null;
    
    private DefaultHttpClientConfig httpClientConfig = null;
    private CloseableHttpClient httpClient;
    private PoolingHttpClientConnectionManager connectionManager;

    private Lock lock = new ReentrantLock();
    private static ConcurrentHashMap<Integer, RequestConfig> requstConfigCache = new ConcurrentHashMap<Integer, RequestConfig>(10);
    
    private HttpClient(DefaultHttpClientConfig httpClientConfig) {
    	this.httpClientConfig =httpClientConfig;
    }

   /* public static HttpClient getHttpClient() {
    	if(INSTANCE1==null){
    		synchronized (LOCKS) {
    			if(INSTANCE1==null){
    				INSTANCE1 = new HttpClient(new DefaultHttpClientConfig());
    			}
			}
    	}
        return INSTANCE1;
    }*/
    
    public static HttpClient getHttpClient(DefaultHttpClientConfig httpClientConfig) {
    	if(INSTANCE==null){
    		synchronized (LOCKS) {
    			if(INSTANCE==null){
    				if(httpClientConfig==null){
    					httpClientConfig=new DefaultHttpClientConfig();
    				}
    				INSTANCE = new HttpClient(httpClientConfig);
    			}
			}
    	}
        return INSTANCE;
    }

    /*
     * timeout:socket超时时间，毫秒;不设置请求超时时间，则默认为2000；
     */
    public RequestConfig getRequestConfig(int timeout) 
    {
    	if(timeout==0)
    	{
    		timeout = 3000;
    	}
    	RequestConfig requestConfig = requstConfigCache.get(timeout);
    	if (requestConfig == null) {
            lock.lock();
            try {
                requestConfig = requstConfigCache.get(timeout);
                if (requestConfig == null) {
                    requestConfig = RequestConfig.custom()
                            .setConnectionRequestTimeout(httpClientConfig.getConnectionRequestTimeout())//该值就是连接不够用的时候等待超时时间，一定要设置，而且不能太大
                            .setConnectTimeout(httpClientConfig.getConnectionTimeout())//连接一个url的连接等待时间（连接超时）
                            .setSocketTimeout(timeout).build();//连接上一个url，获取response的返回等待时间(请求超时)
                    requstConfigCache.put(timeout, requestConfig);
                }
            } finally {
                lock.unlock();
            }
        }
    	return requestConfig;
    }
    public RequestConfig getRequestConfig() 
	{
    	return getRequestConfig(httpClientConfig.getDefaultSocketTimeout());
	}
    
    private void init() {
        if (httpClient == null) {
            lock.lock();
            try {
                if (httpClient == null) {              	 
                    connectionManager = new PoolingHttpClientConnectionManager();
                    connectionManager.setMaxTotal(httpClientConfig.getMaxTotal());//设置整个连接池最大连接数
                    /**
                     * 根据连接到的主机对MaxTotal的一个细分；比如：
						MaxtTotal=400 DefaultMaxPerRoute=200
					         只连接到http://sishuok.com时，到这个主机的并发最多只有200；而不是400；
						当连接到http://sishuok.com 和 http://qq.com时，
						到每个主机的并发最多只有200；即加起来是400（但不能超过400）；
						所以起作用的设置是DefaultMaxPerRoute。
                     */
                    connectionManager.setDefaultMaxPerRoute(httpClientConfig.getDefaultMaxPerRoute());
                    connectionManager.closeExpiredConnections();
                    httpClient = HttpClientBuilder.create()
                            .setConnectionManager(connectionManager)
                            .setDefaultRequestConfig(getRequestConfig())
                            .disableCookieManagement()
                            .disableAutomaticRetries()
                            .build();
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public PoolStats getConnectionStatus(URI uri) {
    	init();
        if (connectionManager != null) {
            HttpHost target = URIUtils.extractHost(uri);
            if (target.getPort() <= 0) {
                try {
                    target = new HttpHost(
                            target.getHostName(),
                            DefaultSchemePortResolver.INSTANCE.resolve(target),
                            target.getSchemeName());
                } catch (Throwable ignore) {
                    target = null;
                }
            }
            if (target != null)
            {
            	return connectionManager.getStats(new HttpRoute(target));
            }
        }
        return null;
    }

    public String execute(HttpRequestBase request) throws Exception {
        byte[] bytes = executeAndReturnByte(request);
        if (bytes == null || bytes.length == 0) return null;
        return new String(bytes, DEFAULT_CHARSET);
    }
    
    public byte[] executeAndReturnByte(HttpRequestBase request) throws Exception {
        init();
        HttpEntity entity = null;
        CloseableHttpResponse resp = null;
        byte[] rtn = new byte[0];
        if (request == null) return rtn;
        try {
            resp = httpClient.execute(request);
            entity = resp.getEntity();
            if (resp.getStatusLine().getStatusCode() == 200) {
                String encoding = ("" + resp.getFirstHeader("Content-Encoding")).toLowerCase();
                if (encoding.indexOf("gzip") > 0) {
                    entity = new GzipDecompressingEntity(entity);
                }
                rtn = EntityUtils.toByteArray(entity);
            } else {
                logger.warn(request.getURI().toString()+"return error {}");
            }
        } catch (Exception e) {
            logger.error(request.getURI().toString() + " " + e.getMessage());
            throw e;
        } finally {
            EntityUtils.consumeQuietly(entity);
            if (resp != null) {
                try {
                    resp.close();
                } catch (Exception ignore) {
                }
            }
        }
        return rtn;
    }

}

