package www.aaltogetherbackend.modules;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.Ordered;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class ResourceLoaderBeanPostProcessor implements BeanPostProcessor, BeanFactoryPostProcessor, Ordered,
        ResourceLoaderAware, ApplicationContextAware {

    private ResourceLoader resourceLoader;
    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(
            Object bean, String beanName) {
        if (bean instanceof ResourceLoaderAware) {
            ((ResourceLoaderAware)bean).
                    setResourceLoader(this.resourceLoader);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(
            Object bean,
            String beanName) {
        return bean;
    }

    @Override
    public void postProcessBeanFactory(
            ConfigurableListableBeanFactory beanFactory) {
        this.resourceLoader =
                new DatabaseResourceLoader(
                        this.applicationContext,
                        this.resourceLoader);
        beanFactory.registerResolvableDependency(
                ResourceLoader.class, this.resourceLoader);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void setResourceLoader(
            ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setApplicationContext(
            ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
