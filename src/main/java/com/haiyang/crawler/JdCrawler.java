package com.haiyang.crawler;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.haiyang.pojo.Item;
/**
 * 
 * @author lhy 2017-11-5
 *
 */
public class JdCrawler {
	
	public static final String BASE_URL ="https://list.jd.com/list.html?cat=9987,653,655&page={page}";
	
	public static final ObjectMapper MAPPER = new ObjectMapper();
	
	public void start() throws Exception{
		//入口页面地址
		String startUrl = StringUtils.replace(BASE_URL, "{page}", String.valueOf(1));
		//请求url
		String html = doGet(startUrl);
		//解析并获取总页数
		Document document = Jsoup.parse(html);
		String pageText = document.select("#J_topPage").text();
		String[] strs = pageText.split("\\D+");
		Integer totalPage = Integer.parseInt(strs[1]);
//		String str="1,2,3,4";
//		String[] strs = str.split("\\D+");
//		for(String str1 :strs){
//			System.out.println(str1);
//		}
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <=totalPage; i++) {
			String url = StringUtils.replace(BASE_URL, "{page}", String.valueOf(i));
			System.out.println(url);
			String content = doGet(url);
			//解析html
			Document root = Jsoup.parse(content);
			Elements lis = root.select("#plist li.gl-item");
			Map<Long,Item> items = new LinkedHashMap<Long,Item>();
			for(Element li : lis){
				Item item = new Item();
				Element div = li.child(0);
				Long id = Long.valueOf(div.attr("data-sku"));
				System.err.println(id);
				
				String image = li.select(".p-img img").attr("data-lazy-img");
				String title = li.select(".p-name").text();
//				System.out.println(price);
				item.setId(id);
				item.setImage(image);
				item.setTitle(title);
				items.put(id,item);
				
			}
			
			
			List<String> ids =new  ArrayList<String>(); 
			
			for(Long id:items.keySet()){
				ids.add("J_"+id);
			}
			//https://p.3.cn/prices/mgets?skuIds=J_4143422,J_11384983200
			//[{"op":"1399.00","m":"1600.00","id":"J_4143422","p":"1399.00"}]
			String priceUrl = "https://p.3.cn/prices/mgets?skuIds="+StringUtils.join(ids, ',');
			String jsonData = doGet(priceUrl);
			//System.out.println(jsonData);
			ArrayNode arrayNode = (ArrayNode)MAPPER.readTree(jsonData);
			for (JsonNode jsonNode : arrayNode) {
				Long id = Long.valueOf(StringUtils.substringAfter(jsonNode.get("id").asText(), "_"));
				items.get(id).setPrice(jsonNode.get("p").asLong());
			}
			
			ids.clear();
			for(Long id:items.keySet()){
				ids.add("AD_"+id);
			}
			String adUrl ="https://ad.3.cn/ads/mgets?skuids="+StringUtils.join(ids, ',');
			
			String jsonData1 = doGet(adUrl);
			//System.out.println(jsonData);
			ArrayNode arrayNode1 = (ArrayNode)MAPPER.readTree(jsonData1);
			for (JsonNode jsonNode : arrayNode1) {
				Long id = Long.valueOf(StringUtils.substringAfter(jsonNode.get("id").asText(), "_"));
				items.get(id).setSellPoint(jsonNode.get("ad").asText());
			}
			
			
			for (Item item : items.values()) {
				sb.append(item.toString()+"\n");
			}
			
			FileUtils.writeStringToFile(new File("D:\\2017\\item.txt"), sb.toString(), "UTF-8");
			//break;
		}
		
	}
	
	private String doGet(String url) throws Exception  {
		CloseableHttpClient httpClient = HttpClients.createDefault();

		// 创建http GET请求
		HttpGet httpGet = new HttpGet(url);

		CloseableHttpResponse response = null;
		try {
			// 执行请求
			response = httpClient.execute(httpGet);
			// 判断返回状态是否为200
			if (response.getStatusLine().getStatusCode() == 200) {
				String content = EntityUtils.toString(response.getEntity(), "UTF-8");
				return content;
			}
			
		} finally {
			if (response != null) {
				response.close();
			}
			// 此处不能关闭httpClient，如果关闭httpClient，连接池也会销毁
			httpClient.close();
		}
		return null;
	}
}
