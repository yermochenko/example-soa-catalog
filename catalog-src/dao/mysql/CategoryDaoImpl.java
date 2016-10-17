package dao.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import dao.CategoryDao;
import dao.DaoException;
import domain.Category;

public class CategoryDaoImpl extends BaseDaoImpl implements CategoryDao {
	@Override
	public Integer create(Category category) throws DaoException {
		String sql = "INSERT INTO `category` (`name`, `parent_id`) VALUES(?, ?)";
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			statement.setString(1, category.getName());
			if(category.getParent() != null && category.getParent().getId() != null) {
				statement.setInt(2, category.getParent().getId());
			} else {
				statement.setNull(2, Types.INTEGER);
			}
			statement.execute();
			resultSet = statement.getGeneratedKeys();
			resultSet.next();
			return resultSet.getInt(1);
		} catch(SQLException e) {
			throw new DaoException(e);
		} finally {
			try { resultSet.close(); } catch(SQLException | NullPointerException e) {}
			try { statement.close(); } catch(SQLException | NullPointerException e) {}
		}
	}

	@Override
	public Category read(Integer id) throws DaoException {
		String sql = "SELECT `name`, `parent_id` FROM `category` WHERE `id` = ?";
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement(sql);
			statement.setInt(1, id);
			resultSet = statement.executeQuery();
			Category category = null;
			if(resultSet.next()) {
				category = new Category();
				category.setId(id);
				category.setName(resultSet.getString("name"));
				category.setParent(new Category());
				category.setChildren(new ArrayList<>());
				Integer parentId = resultSet.getInt("parent_id");
				if(!resultSet.wasNull()) {
					category.getParent().setId(parentId);
				}
			}
			return category;
		} catch(SQLException e) {
			throw new DaoException(e);
		} finally {
			try { resultSet.close(); } catch(SQLException | NullPointerException e) {}
			try { statement.close(); } catch(SQLException | NullPointerException e) {}
		}
	}

	@Override
	public List<Category> readAll() throws DaoException {
		String sql = "SELECT `id`, `name`, `parent_id` FROM `category`";
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().createStatement();
			resultSet = statement.executeQuery(sql);
			List<Category> categories = new ArrayList<>();
			while(resultSet.next()) {
				Category category = new Category();
				category.setId(resultSet.getInt("id"));
				category.setName(resultSet.getString("name"));
				category.setChildren(new ArrayList<>());
				Integer parentId = resultSet.getInt("parent_id");
				if(!resultSet.wasNull()) {
					category.setParent(new Category());
					category.getParent().setId(parentId);
				}
				categories.add(category);
			}
			return categories;
		} catch(SQLException e) {
			throw new DaoException(e);
		} finally {
			try { resultSet.close(); } catch(SQLException | NullPointerException e) {}
			try { statement.close(); } catch(SQLException | NullPointerException e) {}
		}
	}

	@Override
	public void update(Category category) throws DaoException {
		String sql = "UPDATE `category` SET `name` = ?, `parent_id` = ? WHERE `id` = ?";
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement(sql);
			statement.setString(1, category.getName());
			if(category.getParent() != null && category.getParent().getId() != null) {
				statement.setInt(2, category.getParent().getId());
			} else {
				statement.setNull(2, Types.INTEGER);
			}
			statement.setInt(3, category.getId());
			statement.execute();
		} catch(SQLException e) {
			throw new DaoException(e);
		} finally {
			try { statement.close(); } catch(SQLException | NullPointerException e) {}
		}
	}

	@Override
	public void delete(Integer id) throws DaoException {
		String sql = "DELETE FROM `category` WHERE `id` = ?";
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement(sql);
			statement.setInt(1, id);
			statement.execute();
		} catch(SQLException e) {
			throw new DaoException(e);
		} finally {
			try { statement.close(); } catch(SQLException | NullPointerException e) {}
		}
	}
}