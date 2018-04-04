/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.controller.communication.http;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Jan-Peter.Schmidt
 */
@Provider
public class HTTPContextFilter implements ContainerRequestFilter {

    public HTTPContextFilter() {
        
        super();
        
    }
    
    @Override
    public void filter(ContainerRequestContext crc) throws IOException {
        System.out.println("Called filter with " + crc.toString());        
    }
    
}
