package com.sherlock.imService.configure;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

//@Configuration
public class MybatisConfigure {

//	@Bean
//    @ConfigurationProperties(prefix = "spring.datasource.db_medical")
//    @Primary //这个注解是说明此是默认数据源，必须设定默认数据源。（即主数据源）
//    public DataSource dataSource() {
//        return DataSourceBuilder.create().build();
//    }
	
	@Bean
	public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) 
			throws Exception{
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        //xml路径
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:com/yilife/mapper/db_medical/*.xml"));
        return bean.getObject();
	}
	
	@Bean
	public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory){
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}
