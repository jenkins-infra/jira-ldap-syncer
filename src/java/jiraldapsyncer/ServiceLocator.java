package jiraldapsyncer;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * ServiceLocator - locator interface to all spring application beans
 */
public class ServiceLocator {
	private ApplicationContext context;

    public ServiceLocator(){
        context = new ClassPathXmlApplicationContext("/serverAppContext.xml");
    }

    public Object lookupService(String serviceBeanName) {
        return context.getBean(serviceBeanName);
    }
}
