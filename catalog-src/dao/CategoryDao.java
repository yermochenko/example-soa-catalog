package dao;

import java.util.List;

import domain.Category;

public interface CategoryDao extends Dao<Category> {
	List<Category> readAll() throws DaoException;
}