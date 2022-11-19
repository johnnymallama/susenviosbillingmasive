package co.com.surenvios.billingmasive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.autoconfigure.domain.*;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@ComponentScan({ "co.com.surenvios.billingmasive" })
@EntityScan("co.com.surenvios.librarycommon.database")
@Configuration
@EnableAutoConfiguration
@EnableScheduling
@EnableAsync
@EnableCaching
public class BillingMasiveApplication implements ApplicationRunner {
	
	private static final Logger logger = LogManager.getLogger(BillingMasiveApplication.class);
	
	@Value("${count.thread}")
	private Integer countThread;

	public static void main(String[] args) {
		SpringApplication.run(BillingMasiveApplication.class, args);
	}

	@Bean(name = "threadTaskExecutor")
	public ThreadPoolTaskExecutor threadTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(this.countThread);
		executor.setMaxPoolSize(this.countThread);
		executor.setQueueCapacity(0);
		executor.setThreadNamePrefix("MASIVE-");
		executor.initialize();
		return executor;
	}

	@Bean(name = "cacheProcess")
	public CacheManager cacheManager(){
		String[] cachables = {"token", "emisor"};
		return new ConcurrentMapCacheManager(cachables);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		logger.info("BillingMasiveApplication_::run");
	}

}
