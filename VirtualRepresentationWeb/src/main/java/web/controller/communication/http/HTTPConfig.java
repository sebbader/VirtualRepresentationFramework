package web.controller.communication.http;

import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

/**
 * This class enables the multipart feature and defines the HTTP-Endpoint
 * under /representations.
 * @author Jan-Peter.Schmidt
 */
@ApplicationPath("representations")
public class HTTPConfig extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
        resources.add(MultiPartFeature.class);
        addRestResourceClasses(resources);
        return resources;
    }

    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(org.glassfish.jersey.server.wadl.internal.WadlResource.class);
        resources.add(web.controller.communication.http.HTTPCommunication.class);
        resources.add(web.controller.communication.http.HTTPContextFilter.class);
    }  
}
