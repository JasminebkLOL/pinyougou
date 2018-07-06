package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
//配置连接超时时间,推荐配置在 服务 端
@Service(timeout=5000)
public class ItemSerachServiceImpl implements ItemSearchService{
	
	@Autowired
	private SolrTemplate solrTemplate;
	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public Map<String, Object> search(Map searchMap) {
		Map<String,Object> resultMap = new HashMap<String,Object>();
		
		//1.按关键字查询（高亮显示）putAll是追加方法,在map后面直接追加
		resultMap.putAll(searchMap(searchMap));
		
		//2.根据关键字查询商品分类
		List<String> categoryList = searchCategoryList(searchMap);
		resultMap.put("categoryList",categoryList);
		
		//3.查询品牌和规格列表
		String category = (String)searchMap.get("category");//获取前台传入的查询体中的category数据
		if(!"".equals(category)){//如果查询列表的category不为"",则按照前端传入的category进行查询BrandAndSpecList
			Map categoryMap = searchBrandAndSpecList(category);
			resultMap.putAll(categoryMap);
		}else{
			//如果查询列表的category是"",则默认按照categoryList的第一个(category)数据查询BrandAndSpecList
			//如果查询到的商品分类数大于0,默认查询第一个商品分类的规格选项与品牌
			if(categoryList.size()>0){
				resultMap.putAll(searchBrandAndSpecList(categoryList.get(0)));
			}
		}
		
		return resultMap;
	}
	
	//根据 category商品分类 从redis中 查找品牌以及规格选项
	private Map searchBrandAndSpecList(String category){
		Map map = new HashMap();
		//这里可以直接用Long强转而不用判断null异常,因为RedisTemplate底层做了处理
		Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if(typeId!=null){
			//从redis中取数据.
			List<Map> brandList=(List<Map>)redisTemplate.boundHashOps("brandList").get(typeId);
			List<Map> specList=(List<Map>)redisTemplate.boundHashOps("specList").get(typeId);
			map.put("brandList", brandList);
			map.put("specList", specList);
		}
		return map;
	}

	
	//1.查询列表
	private Map searchMap(Map searchMap) {
		Map<String,Object> resultMap = new HashMap<String,Object>();
		//关键字  去掉空格
		String keywords = (String)searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));
		
		HighlightQuery query = new SimpleHighlightQuery();
		HighlightOptions highlightOptions = new HighlightOptions();
		highlightOptions.addField("item_title");//设置高亮域
		
		highlightOptions.setSimplePrefix("<em style='color:red'>");//添加前缀
		highlightOptions.setSimplePostfix("</em>");//添加后缀
		query.setHighlightOptions(highlightOptions);//为查询设置高亮选项
		
		//1.1关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//1.2按商品分类过滤
		if(!"".equals(searchMap.get("category"))){//如果用户选择了分类
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		//1.3按品牌分类过滤
		if(!"".equals(searchMap.get("brand"))){//如果用户选择了品牌
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		//1.4按规格过滤
		if(searchMap.get("spec")!=null  &&  !"{}".equals(searchMap.get("spec"))){
			 Map<String,String> specMap = (Map<String,String>)searchMap.get("spec");
			 //遍历map,过滤.
			 for(String key:specMap.keySet()){
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			 }
		}
		//1.5按照价格过滤
		if(!"".equals(searchMap.get("price"))){
			String[] price = ((String) searchMap.get("price")).split("-");
			if(!price[0].equals("0")){//如果区间起点不等于0
				Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);
				FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);				
			}		
			if(!price[1].equals("*")){//如果区间终点不等于*
				Criteria filterCriteria=new  Criteria("item_price").lessThanEqual(price[1]);
				FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);				
			}
		}
		//1.6分页查询,需要前台传入当前页pageNo,与每页记录数,pageSize..后台返回,总页数pageTotalPage,总记录数totalElements
		Integer pageNo = (Integer)searchMap.get("pageNo");
		if(pageNo==null){
			pageNo = 1;
		}
		Integer pageSize = (Integer)searchMap.get("pageSize");
		if(pageSize==null){//建议将这个变动的数字封装到配置文件中..
			pageSize = 30;//默认值每页30条
		}
		query.setOffset((pageNo-1)*pageSize);//设置起始页
		query.setRows(pageSize);//设置 每页记录数
		
		//1.7排序
		String sortValue= (String) searchMap.get("sort");//ASC  DESC  
		String sortField= (String) searchMap.get("sortField");//排序字段
		if(sortValue!=null && !sortValue.equals("")){  
			if(sortValue.equals("1")){//1代表默认升序
				Sort sort=new Sort(Sort.Direction.ASC, "item_"+sortField);
				query.addSort(sort);
			}
			if(sortValue.equals("0")){//0代表默认降序
				Sort sort=new Sort(Sort.Direction.DESC, "item_"+sortField);
				query.addSort(sort);
			}			
		}

		
		
		//*************  获取高亮显示结果集   ************
		//高亮显示页
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query , TbItem.class);
		List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
		//高亮的每一个选项,entry代表每一个手机框
		for (HighlightEntry<TbItem> entry : entryList) {
			//获取高亮列表,(高亮域的个数)
			List<Highlight> highlights = entry.getHighlights();
			//TbItem item相当于下面的list中的每一个TbItem实体,通过id关联到同一个内存地址
			TbItem item = entry.getEntity();
			if(highlights.size()>0 && highlights.get(0).getSnipplets().size()>0){
				//每一个实体可能有
				item.setTitle(highlights.get(0).getSnipplets().get(0));
			}
		}
		List<TbItem> list = page.getContent();
		resultMap.put("rows", list);
		resultMap.put("total", page.getTotalElements());//总记录数
		resultMap.put("totalPages", page.getTotalPages());//总页数
		
		return resultMap;
	}
	
	//分组列表
	private List<String> searchCategoryList(Map searchMap){
		List<String> list = new ArrayList<String>();
		
		Query query = new SimpleQuery("*:*");
		//根据关键字 查询
		Object keywords = searchMap.get("keywords");
		if(keywords!=null){
			Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
			query.addCriteria(criteria);
			//分组查询
			GroupOptions groupOptions = new GroupOptions();
			groupOptions.addGroupByField("item_category");
			query.setGroupOptions(groupOptions);
			//获取了分组页对象:
			GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
			
			/*//获取分组结果对象:addGroupByField("item_category")可能有多个...
			GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
			//获取分组入口页
			Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
			List<GroupEntry<TbItem>> entryList = groupEntries.getContent();*/
			
			//这样直接链式编程好理解....
			List<GroupEntry<TbItem>> entryList = page.getGroupResult("item_category").getGroupEntries().getContent();
			
			for (GroupEntry<TbItem> entry : entryList) {
				list.add(entry.getGroupValue());
			}
			
		}
		return list;
	}

	/**
	 * 导入数据 更solr索引
	 * @param list
	 */
	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);	
		solrTemplate.commit();
	}

	/**
	 * 删除商品ID的索引
	 */
	@Override
	public void deleteByGoodsIds(List goodsIds) {
		Query query = new SimpleQuery("*:*");
		Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
	}

}
