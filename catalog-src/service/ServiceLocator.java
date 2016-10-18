package service;

public interface ServiceLocator {
	<T> T getService(Class<T> key);

	void close() throws ServiceException;
}