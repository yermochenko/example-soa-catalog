package service;

import java.util.List;

import dao.CategoryDao;
import dao.DaoException;
import dao.ProductDao;
import domain.Category;
import domain.Product;

public class ProductServiceImpl implements ProductService {
	private CategoryDao categoryDao;
	private ProductDao productDao;

	public void setCategoryDao(CategoryDao categoryDao) {
		this.categoryDao = categoryDao;
	}

	public void setProductDao(ProductDao productDao) {
		this.productDao = productDao;
	}

	@Override
	public List<Product> findByCategory(Integer categoryId) throws ServiceException {
		try {
			Category category = categoryDao.read(categoryId);
			List<Product> products = productDao.readByCategoryId(categoryId);
			for(Product product : products) {
				product.setCategory(category);
			}
			return products;
		} catch(DaoException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public Product findById(Integer id) throws ServiceException {
		try {
			Product product = productDao.read(id);
			Category category = product.getCategory();
			category = categoryDao.read(category.getId());
			product.setCategory(category);
			return product;
		} catch(DaoException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void save(Product product) throws ServiceException {
		try {
			if(product.getId() != null) {
				productDao.update(product);
			} else {
				Integer id = productDao.create(product);
				product.setId(id);
			}
		} catch(DaoException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void delete(Integer id) throws ServiceException {
		try {
			productDao.delete(id);
		} catch(DaoException e) {
			throw new ServiceException(e);
		}
	}
}