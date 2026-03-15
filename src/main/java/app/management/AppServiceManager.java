package app.management;

import java.util.HashMap;
import java.util.Map;

public class AppServiceManager {
	
	AppServiceManager() {
		this(new PropertieService());
	}
	
	AppServiceManager(PropertieService propertieService) {
		attach(PropertieService.class, propertieService);
	}
	
	Map<Class<? extends ApplicationService>, ApplicationService> services = new HashMap<>();
	
	public <T extends ApplicationService> void attach(Class<? extends T> serviceClass, T service) {
		if (services.containsKey(service.getClass())) {
			throw new IllegalStateException("Service already attached");
		}
		if (service.appManager != null) {
			throw new IllegalStateException("Service already initialized");
		}
		service.appManager = this;
		if (!(service instanceof PropertieService)) {			
			getService(PropertieService.class).loadProperties(service);
		}
		service.init();
		services.put(service.getClass(), service);
	}
	
	public void attach(ApplicationService service) {
		attach(service.getClass(), service);
	}
	
	public <T extends ApplicationService> boolean hasService(Class<T> clazz) {
		return services.containsKey(clazz);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ApplicationService> T getService(Class<T> clazz) {
		if (!hasService(clazz)) {
			throw new IllegalArgumentException("No Service of class " + clazz.getName());
		}
		return (T) services.get(clazz);
	}
}
