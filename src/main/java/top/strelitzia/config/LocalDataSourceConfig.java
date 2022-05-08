package top.strelitzia.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.web.client.RestTemplate;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author strelitzia
 * @Date 2022/04/03
 * 注入DataSource相关配置Bean
 **/
@Configuration
@MapperScan(basePackages = "top.strelitzia.dao",
        sqlSessionTemplateRef = "localSqlSessionTemplate")
public class LocalDataSourceConfig {

    @Bean(name = "localDataSource")
    public DataSource getLocalDataSource() {
        File dir = new File("runFile");
        if (!dir.exists()) {
            boolean mkdir = dir.mkdir();
        }
        File file = new File("runFile/angelina.db");
        if (!file.exists()) {
            try (InputStream is = new ClassPathResource("/database/angelina.db").getInputStream(); FileOutputStream fs = new FileOutputStream(file)) {
                boolean newFile = file.createNewFile();
                byte[] b = new byte[1024];
                while (is.read(b) != -1) {
                    fs.write(b);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return DataSourceBuilder.create()
                .driverClassName("org.sqlite.JDBC")
                .url("jdbc:sqlite:" + file.getAbsolutePath())
                .type(SQLiteDataSource.class)
                .build();
    }

    @Bean("localSqlSessionFactory")
    public SqlSessionFactory localSqlSessionFactory(
            @Qualifier("localDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:mapping/*.xml"));
        bean.setDataSource(dataSource);
        return bean.getObject();
    }

    @Bean("localTransactionManger")
    public DataSourceTransactionManager localTransactionManger(
            @Qualifier("localDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean("localSqlSessionTemplate")
    public SqlSessionTemplate localSqlSessionTemplate(
            @Qualifier("localSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
