package service;

public class ServiceLocatorFactory {
	private static Class<? extends ServiceLocator> locatorClass = null;

	public static ServiceLocator getLocator() throws ServiceException {
		try {
			return locatorClass.newInstance();
		} catch(InstantiationException | IllegalAccessException e) {
			throw new ServiceException(e);
		}
	}

	public static void registerLocator(Class<? extends ServiceLocator> locatorClass) {
		ServiceLocatorFactory.locatorClass = locatorClass;
	}
}