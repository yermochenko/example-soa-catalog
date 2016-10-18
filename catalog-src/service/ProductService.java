package service;

import java.util.List;

import domain.Product;

public interface ProductService {
	List<Product> findByCategory(Integer categoryId) throws ServiceException;

	Product findById(Integer id) throws ServiceException;

	void save(Product product) throws ServiceException;

	void delete(Integer id) throws ServiceException;
}