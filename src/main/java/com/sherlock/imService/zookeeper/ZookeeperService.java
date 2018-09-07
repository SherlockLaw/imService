package com.sherlock.imService.zookeeper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.sherlock.imService.configure.ZookeeperConfigure;
import com.sherlock.imService.redis.RedisService;

@Component
public class ZookeeperService implements Watcher{
	@Autowired
	private ZookeeperConfigure zookeeperConfigure;
	
	@Autowired
	private RedisService redisService;
	
	private CountDownLatch latch = new CountDownLatch(1);
	
	private Map<String,String> innerIpPortMap = new HashMap<>();
	private Map<String,String> outIpPortMap = new HashMap<>();
	private ZooKeeper zk;
	
	private String serviceName = null;
	
	@PostConstruct
	public void init(){
		try {
			zk = new ZooKeeper(zookeeperConfigure.getConnectingString(), zookeeperConfigure.getSESSION_TIMEOUT(), this);
			latch.await();
			// 创建父节点
			if (zk.exists(zookeeperConfigure.getBASE_PATH(), true) == null) {
				zk.create(zookeeperConfigure.getBASE_PATH(), null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			// 在父节点下面创建临时顺序节点
			String nodeName = zk.create(zookeeperConfigure.getBASE_PATH()+"/"+zookeeperConfigure.getSERVICE(),
//					null,
					getJson(zookeeperConfigure.getImAddress_out(),zookeeperConfigure.getImAddress_inner())
						.toJSONString().getBytes(),
					ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			serviceName = nodeName.substring(nodeName.lastIndexOf("/")+1);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (KeeperException e) {
			e.printStackTrace();
		}
	}
	
	private JSONObject getJson(String ImAddress_out,String ImAddress_inner){
		JSONObject job = new JSONObject();
		job.put("ImAddress_out", ImAddress_out);
		job.put("ImAddress_inner", ImAddress_inner);
		return job;
	}
	
	@Override
	public void process(WatchedEvent event) {
		boolean connected = false;
		if (event.getState() == Event.KeeperState.SyncConnected && null == event.getPath()) {
			// 连接成功
			connected = true;
			latch.countDown();
		} else if (event.getState() == Event.KeeperState.Disconnected && null == event.getPath()) {
			//连接断开
			
		}
		if (connected || event.getType() == Event.EventType.NodeChildrenChanged) {
			refreshCache();
		}
	}
	// 刷新本地缓存
	private void refreshCache(){
		try {
			Map<String,String> innerIpPortMap = new HashMap<>();
			Map<String,String> outIpPortMap = new HashMap<>();
			
			List<String> nodeList = zk.getChildren(zookeeperConfigure.getBASE_PATH(),true);
			for (String node : nodeList) {
				byte[] bytes = zk.getData(zookeeperConfigure.getBASE_PATH() + "/" + node, false, null);
				String str = new String(bytes);
				JSONObject job = JSONObject.parseObject(str);
				outIpPortMap.put(node, job.getString("ImAddress_out"));
				innerIpPortMap.put(node, job.getString("ImAddress_inner"));
			}
			this.outIpPortMap = outIpPortMap;
			this.innerIpPortMap = innerIpPortMap;
			// 删除redis中不在线的服务
			redisService.clearDeadService(nodeList);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (KeeperException e) {
			e.printStackTrace();
		}
	}

	public String getServiceName() {
		return serviceName;
	}
	
	/**
	 * 获取内部调用的ip:port
	 * @param serviceName
	 * @return
	 */
	public String getInnerIpPort(String serviceName) {
		return innerIpPortMap.get(serviceName);
	}
	public String getLocalInnerIpPort() {
		return innerIpPortMap.get(serviceName);
	}
	/**
	 * 找到压力最小的服务，返回给客户端用以连接
	 * @return
	 */
	public String getConnectServer(){
		Map<String,Integer> serviceMap = redisService.getAllService();
		//找出压力最小的服务
		String ipPort = null;
		int minCount = Integer.MAX_VALUE;
		Set<Map.Entry<String,String>> entrySet = outIpPortMap.entrySet();
		for (Map.Entry<String,String> entry : entrySet) {
			Integer count = serviceMap.get(entry.getKey());
			if (count==null) {
				count = 0;
			}
			if (count < minCount) {
				ipPort = entry.getValue();
				minCount = count;
			}
		}
		return ipPort;
	}
}
