package com.okta.developer.blog.config;

import com.github.cloudyrock.spring.v5.EnableMongock;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tech.jhipster.config.JHipsterConstants;
import tech.jhipster.domain.util.JSR310DateConverters.DateToZonedDateTimeConverter;
import tech.jhipster.domain.util.JSR310DateConverters.ZonedDateTimeToDateConverter;

@Configuration
@EnableMongock
@EnableReactiveElasticsearchRepositories("com.okta.developer.blog.repository.search")
@EnableReactiveMongoRepositories(
    basePackages = "com.okta.developer.blog.repository",
    includeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, value = ReactiveMongoRepository.class)
)
@Profile("!" + JHipsterConstants.SPRING_PROFILE_CLOUD)
@Import(value = { MongoAutoConfiguration.class, MongoReactiveAutoConfiguration.class })
public class DatabaseConfiguration {

    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener() {
        return new ValidatingMongoEventListener(validator());
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(DateToZonedDateTimeConverter.INSTANCE);
        converters.add(ZonedDateTimeToDateConverter.INSTANCE);
        return new MongoCustomConversions(converters);
    }
}
