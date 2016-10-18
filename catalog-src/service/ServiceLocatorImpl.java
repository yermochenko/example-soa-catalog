package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import dao.mysql.CategoryDaoImpl;
import dao.mysql.ProductDaoImpl;
import utils.pool.ConnectionPool;
import utils.pool.ConnectionPoolException;

public class ServiceLocatorImpl implements ServiceLocator {
	private Connection connection;
	private Map<Class<?>, Object> services = new HashMap<>();

	public ServiceLocatorImpl() throws ServiceException {
		try {
			connection = ConnectionPool.getInstance().getConnection();
			CategoryDaoImpl categoryDao = new CategoryDaoImpl();
			categoryDao.setConnection(connection);
			ProductDaoImpl productDao = new ProductDaoImpl();
			productDao.setConnection(connection);
			CategoryServiceImpl categoryService = new CategoryServiceImpl();
			categoryService.setCategoryDao(categoryDao);
			ProductServiceImpl productService = new ProductServiceImpl();
			productService.setCategoryDao(categoryDao);
			productService.setProductDao(productDao);
			services.put(CategoryService.class, categoryService);
			services.put(ProductService.class, productService);
		} catch(ConnectionPoolException e) {
			throw new ServiceException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T getService(Class<T> key) {
		return (T)services.get(key);
	}

	public void close() throws ServiceException {
		try {
			connection.close();
		} catch(SQLException e) {
			throw new ServiceException(e);
		}
	}
}