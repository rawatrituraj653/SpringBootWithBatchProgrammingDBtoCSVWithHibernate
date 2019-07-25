package com.st.config;


import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.Session;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.HibernateCursorItemReader;
import org.springframework.batch.item.database.orm.HibernateNativeQueryProvider;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.st.listener.JobEXListener;
import com.st.model.Travel;

@Configuration
@EnableBatchProcessing
@EnableTransactionManagement
public class BatchConfig {

	
	@Autowired
	private JobBuilderFactory jf;
	@Autowired
	private StepBuilderFactory sf;
	
	@Bean
	public Step step() {
		return sf.get("step")
				.<Travel,Travel>chunk(4)
				.reader(read())
				.processor(process())
				.writer(write())
				.build();
	}
	@Bean
	public Job job() {
	
		return jf.get("job")
				.incrementer(new RunIdIncrementer())
				.listener(listener())
				.start(step())
				.build();
	}
	
	
	
	@Bean
	public ItemReader<Travel> read(){
			HibernateCursorItemReader<Travel> htc=new HibernateCursorItemReader<>();
			htc.setSessionFactory(sessionFactory().getObject());
			htc.setQueryProvider(new HibernateNativeQueryProvider<Travel>() {{
				Session session=sessionFactory().getObject().openSession();
				setEntityClass(Travel.class);
				setSession(session);	
				setSqlQuery("select * from travelagency");
				createQuery();
			}});
			return htc;	
	}	
	@Bean
	public ItemProcessor<Travel,Travel> process(){
		return travel->{
		
			System.out.println(travel);
			return travel;
		};
	}
	@Bean
	public ItemWriter<Travel> write(){
		System.out.println("item Writer");
		FlatFileItemWriter<Travel> writer=new FlatFileItemWriter<>();
		writer.setResource(new FileSystemResource("target/Mydata.csv"));
		writer.setAppendAllowed(true);
		writer.setLineAggregator(new  DelimitedLineAggregator<Travel>() {{
			setDelimiter(",");
		setFieldExtractor(new BeanWrapperFieldExtractor<Travel>() {{
			setNames(new String[]{"flightId","flightName","pilotName","agentId","ticketCost","discount","gst","finalAmount"} );
			}});
		}});
		return writer;
	}
	@Bean
	public LocalSessionFactoryBean sessionFactory() {
		LocalSessionFactoryBean bean=new LocalSessionFactoryBean();
		bean.setDataSource(ds());
		bean.setAnnotatedClasses(Travel.class);
		bean.setHibernateProperties(prop());
		
	return bean;	
	}
	@Bean
	public Properties prop() {
		
		Properties p=new Properties();
		p.put("hibernate.dialect", "org.hibernate.dialect.MySQL55Dialect");
		p.put("hibernate.hbm2ddl.auto", "update");
		p.put("hibernate.show_sql", true);
	
		return p;
	}
	
	@Bean
	public DataSource ds() {
		DriverManagerDataSource dataSource=new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3306/batch");
		dataSource.setUsername("root");
		dataSource.setPassword("root");
		
		return dataSource;
	}
	
	@Bean
	public JobExecutionListener listener() {
		
		return new JobEXListener();
	}
	
}
