package jiraldapsyncer;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * ServiceLocator - locator interface to all spring application beans
 */
public class ServiceLocator {
	private ApplicationContext context;
    private static ServiceLocator instance  = null;

    protected ServiceLocator(){
        super();
        context = new ClassPathXmlApplicationContext("/serverAppContext.xml");
    }

    public static ServiceLocator getInstance(){
                if(instance == null){
                        instance = new ServiceLocator();
                }
                return instance;
        }

    public Object lookupService(String serviceBeanName){
                return context.getBean(serviceBeanName);
        }

}
