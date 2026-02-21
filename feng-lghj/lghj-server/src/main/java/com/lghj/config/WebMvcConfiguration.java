package com.lghj.config;

import com.lghj.interceptor.JwtTokenAdminInterceptor;
import com.lghj.interceptor.JwtTokenUserInterceptor;
import com.lghj.json.JacksonObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    private final JwtTokenAdminInterceptor jwtTokenAdminInterceptor;
    private final JwtTokenUserInterceptor jwtTokenUserInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/login")
                .excludePathPatterns("/api/admin/stock/import");

        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/api/user/**")
                .excludePathPatterns("/api/login")
                .excludePathPatterns("/api/user/blog/comments/list") // 查询博客评论列表（带二级评论，树形结构）
                .excludePathPatterns("/api/user/realtime/news") // 获取股票实时资讯
                .excludePathPatterns("/api/user/realtime/minute") // 获取股票分时数据
                .excludePathPatterns("/api/user/blog/query/hot") // 根据点赞数量（热度）展示博客
                .excludePathPatterns("/api/user/blog/query/of/user") // 查看指定用户发的博客
                .excludePathPatterns("/api/user/stock/search"); // 搜索股票（支持代码前缀和名称模糊搜索）
    }

    /**
     * 设置静态资源映射
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }


    /**
     * 扩展Spring MVC框架的消息转换器 -- 用于日期格式化
     *
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        // 创建一个消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        // 需要为消息转换器设置一个对象转换器，对象转换器可以将Java对象序列化为json数据
        converter.setObjectMapper(new JacksonObjectMapper());
        // 将自己的消息转换器加入容器中【0表示我们自己的消息转换器排在第一位优先使用】
        converters.add(0, converter);
    }
}
