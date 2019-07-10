package com.personal.recommendation.utils;

import com.personal.recommendation.model.News;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class SolrUtil {

    //solr服务器所在的地址，core0为自己创建的文档库目录
    public static String SOLR_URL;

    /**
     * 获取客户端的连接
     *
     * @return HttpSolrClient
     */
    public HttpSolrClient createSolrServer() {
        return new HttpSolrClient.Builder(SOLR_URL).withConnectionTimeout(10000).withSocketTimeout(60000).build();
    }

    /**
     * 往索引库添加文档
     *
     * @throws SolrServerException e
     * @throws IOException e
     */
    public static void addDoc(HttpSolrClient solr, News news) throws SolrServerException, IOException {

        SolrInputDocument document = new SolrInputDocument();
        document.addField("id", news.getId());
        document.addField("title", news.getTitle());
        document.addField("module", news.getModule());
        document.addField("subModule", news.getSubModule());
        document.addField("newsTime", news.getNewsTime());
        document.addField("url", news.getUrl());
        document.addField("source", news.getSource());
        document.addField("imageUrl", news.getImageUrl());
        document.addField("tag", news.getTag());
        document.addField("classified", news.getClassified());

        solr.add(document);
        solr.commit();
    }

    /**
     * 根据ID从索引库删除文档
     *
     * @throws SolrServerException e
     * @throws IOException e
     */
    public void deleteDocumentById(HttpSolrClient solr) throws SolrServerException, IOException {
        solr.deleteById("6");
        solr.commit();
        solr.close();
    }

    /**
     * 根据设定的查询条件进行文档字段的查询
     * @throws Exception e
     */
    public static List<News> querySolrByTag(HttpSolrClient solr, String tag) throws Exception {

            SolrQuery query = new SolrQuery();

        // 下面设置solr查询参数

        // 参数fq, 给query增加过滤查询条件
        query.setQuery("tag:" + tag);

        // 设置分页参数
        query.setStart(0);
        query.setRows(10);

        // 设置高亮显示以及结果的样式
        query.setHighlight(true);
        query.addHighlightField("name");
        query.setHighlightSimplePre("<font color='red'>");
        query.setHighlightSimplePost("</font>");

        //执行查询
        QueryResponse response = solr.query(query);

        //获取返回结果
        SolrDocumentList resultList = response.getResults();

        //获取实体对象形式
        return toBeanList(resultList);
    }

    /**
     * 根据设定的查询条件进行文档字段的查询
     * @throws Exception e
     */
    public static List<News> querySolrByTitle(HttpSolrClient solr, String title, int num) throws Exception {

        SolrQuery query = new SolrQuery();

        // 下面设置solr查询参数

        // 参数fq, 给query增加过滤查询条件
        query.setQuery("title:" + title);

        // 设置分页参数
        query.setStart(0);
        query.setRows(num);

        // 设置高亮显示以及结果的样式
        query.setHighlight(true);
        query.addHighlightField("name");
        query.setHighlightSimplePre("<font color='red'>");
        query.setHighlightSimplePost("</font>");

        //执行查询
        QueryResponse response = solr.query(query);

        //获取返回结果
        SolrDocumentList resultList = response.getResults();

        //获取实体对象形式
        return toBeanList(resultList);
    }

    /**
     * 将SolrDocument转换成Bean
     * @param record SolrDocument
     * @return Object
     */
    private static News toBean(SolrDocument record){
        News obj = null;
        try {
            obj = ( News.class).newInstance();
        } catch (InstantiationException | IllegalAccessException e1) {
            e1.printStackTrace();
        }
        Field[] fields = News.class.getDeclaredFields();
        for(Field field:fields){
            try {
                String value = objectToString(record.get(field.getName()));
                if(StringUtils.isNotBlank(value)) {
                    if (field.getType() == String.class) {
                        BeanUtils.setProperty(obj, field.getName(), value);
                    }
                    if (field.getType() == Long.class) {
                        BeanUtils.setProperty(obj, field.getName(), Long.valueOf(value));
                    }
                    if (field.getType() == Integer.class) {
                        BeanUtils.setProperty(obj, field.getName(), Integer.parseInt(value));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    /**
     * Object to String
     * @param obj Object
     * @return String
     */
    private static String objectToString(Object obj) {
        if(obj == null){
            return null;
        }
        String value;
        if("java.util.ArrayList".equals(obj.getClass().getTypeName())){
            StringBuilder sb = new StringBuilder();
            ArrayList arrayValue = (ArrayList) obj;
            for(Object o : arrayValue){
                sb.append(o).append(",");
            }
            value = sb.substring(0, sb.toString().length()-1);
        }else{
            value = (String) obj;
        }

        return value;
    }

    /**
     * 将SolrDocumentList转换成BeanList
     * @param records SolrDocumentList
     * @return List
     */
    private static List<News> toBeanList(SolrDocumentList records){
        List<News> list = new ArrayList<>();
        for(SolrDocument record : records){
            list.add(toBean(record));
        }
        return list;
    }

    public static void main(String[] args) throws Exception {
        HttpSolrClient solr = new HttpSolrClient.Builder("http://localhost:8983/solr/demo_core")
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000).build();

        List<News> newsList = SolrUtil.querySolrByTitle(solr,"湖人被看扁！新赛季战绩预测不敌三队，快船第一，火箭垫底", 10);
        for(News news : newsList){
            System.out.println(news.getTitle());
        }
        solr.close();
    }
}
