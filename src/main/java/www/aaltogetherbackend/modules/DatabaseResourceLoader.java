package www.aaltogetherbackend.modules;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class DatabaseResourceLoader implements ResourceLoader {

    private static final String DB_URL_PREFIX = "db:";
    private final ApplicationContext applicationContext;
    private final ResourceLoader delegate;

    public DatabaseResourceLoader(
            ApplicationContext applicationContext,
            ResourceLoader delegate) {
        this.applicationContext = applicationContext;
        this.delegate = delegate;
    }

    @Override
    public Resource getResource(String location) {
        if (location.startsWith(DB_URL_PREFIX)) {
            DatabaseLoader databaseLoader =
                    this.applicationContext.getBean(DatabaseLoader.class);
            String id = StringUtils.removeStart(location, DB_URL_PREFIX);
            return databaseLoader.getResource(Long.valueOf(id));
        }
        return this.delegate.getResource(location);
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.delegate.getClassLoader();
    }
}
