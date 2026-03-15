package app.management;

public class ApplicationService {

	protected AppServiceManager appManager;
	
	public void attach(ApplicationService service) {
		appManager.attach(service);
	}
	
	public <T extends ApplicationService> boolean hasService(Class<T> clazz) {
		return appManager.hasService(clazz);
	}
	
	public <T extends ApplicationService> T getService(Class<T> clazz) {
		return appManager.getService(clazz);
	}
	
	public String getProperty(String key) {
		return appManager.getService(PropertieService.class).getProperty(key);
	}
	
	public void init() {
		
	}
	
	
}
