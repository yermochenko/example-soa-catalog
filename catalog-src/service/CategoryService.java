package service;

import java.util.List;

import domain.Category;

// TODO: add method canDelete(Integer id), проверяющий, нет ли с категорией связанных объектов
public interface CategoryService {
	List<Category> findAll() throws ServiceException;

	Category findById(Integer id) throws ServiceException;

	List<Category> findPossibleParents(Integer id) throws ServiceException;

	void save(Category category) throws ServiceException;

	void delete(Integer id) throws ServiceException;
}