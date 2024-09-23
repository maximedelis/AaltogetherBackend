package www.aaltogetherbackend.modules;

import org.springframework.core.io.Resource;

public interface DatabaseLoader {
    Resource getResource(Long id);
}
