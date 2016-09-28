package dao;

import java.util.List;

import domain.Product;

public interface ProductDao extends Dao<Product> {
	List<Product> readByCategoryId(Integer id) throws DaoException;
}