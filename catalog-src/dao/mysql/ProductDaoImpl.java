package dao.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dao.DaoException;
import dao.ProductDao;
import domain.Category;
import domain.Product;

public class ProductDaoImpl extends BaseDaoImpl implements ProductDao {
	@Override
	public Integer create(Product product) throws DaoException {
		String sql = "INSERT INTO `product` (`name`, `description`, `price`, `category_id`) VALUES(?, ?, ?, ?)";
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			statement.setString(1, product.getName());
			statement.setString(2, product.getDescription());
			statement.setInt(3, product.getPrice());
			statement.setInt(4, product.getCategory().getId());
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
	public Product read(Integer id) throws DaoException {
		String sql = "SELECT `name`, `description`, `price`, `category_id` FROM `product` WHERE `id` = ?";
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement(sql);
			statement.setInt(1, id);
			resultSet = statement.executeQuery();
			Product product = null;
			if(resultSet.next()) {
				product = new Product();
				product.setId(id);
				product.setName(resultSet.getString("name"));
				product.setDescription(resultSet.getString("description"));
				product.setPrice(resultSet.getInt("price"));
				product.setCategory(new Category());
				product.getCategory().setId(resultSet.getInt("category_id"));
			}
			return product;
		} catch(SQLException e) {
			throw new DaoException(e);
		} finally {
			try { resultSet.close(); } catch(SQLException | NullPointerException e) {}
			try { statement.close(); } catch(SQLException | NullPointerException e) {}
		}
	}

	@Override
	public List<Product> readByCategoryId(Integer id) throws DaoException {
		String sql = "SELECT `id`, `name`, `description`, `price` FROM `product` WHERE `category_id` = ?";
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement(sql);
			statement.setInt(1, id);
			resultSet = statement.executeQuery();
			List<Product> products = new ArrayList<>();
			while(resultSet.next()) {
				Product product = new Product();
				product.setId(resultSet.getInt("id"));
				product.setName(resultSet.getString("name"));
				product.setDescription(resultSet.getString("description"));
				product.setPrice(resultSet.getInt("price"));
				product.setCategory(new Category());
				product.getCategory().setId(id);
				products.add(product);
			}
			return products;
		} catch(SQLException e) {
			throw new DaoException(e);
		} finally {
			try { resultSet.close(); } catch(SQLException | NullPointerException e) {}
			try { statement.close(); } catch(SQLException | NullPointerException e) {}
		}
	}

	@Override
	public void update(Product product) throws DaoException {
		String sql = "UPDATE `product` SET `name` = ?, `description` = ?, `price` = ?, `category_id` = ? WHERE `id` = ?";
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement(sql);
			statement.setString(1, product.getName());
			statement.setString(2, product.getDescription());
			statement.setInt(3, product.getPrice());
			statement.setInt(4, product.getCategory().getId());
			statement.setInt(5, product.getId());
			statement.execute();
		} catch(SQLException e) {
			throw new DaoException(e);
		} finally {
			try { statement.close(); } catch(SQLException | NullPointerException e) {}
		}
	}

	@Override
	public void delete(Integer id) throws DaoException {
		String sql = "DELETE FROM `product` WHERE `id` = ?";
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