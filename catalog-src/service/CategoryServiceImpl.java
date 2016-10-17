package service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dao.CategoryDao;
import dao.DaoException;
import domain.Category;

public class CategoryServiceImpl implements CategoryService {
	private CategoryDao categoryDao;

	public void setCategoryDao(CategoryDao categoryDao) {
		this.categoryDao = categoryDao;
	}

	@Override
	public List<Category> findAll() throws ServiceException {
		return filterTopLevel(readAll());
	}

	@Override
	public Category findById(Integer id) throws ServiceException {
		return readAll().get(id);
	}

	@Override
	public List<Category> findPossibleParents(Integer id) throws ServiceException {
		Map<Integer, Category> categories = readAll();
		removeChildren(categories, id, true);
		return filterTopLevel(categories);
	}

	@Override
	public void save(Category category) throws ServiceException {
		try {
			if(category.getId() != null) {
				Category oldCategory = readAll().get(category.getId());
				if(oldCategory != null) {
					if(canCategoryBeParentOfCategory(category.getParent(), oldCategory)) {
						categoryDao.update(category);
					} else {
						throw new CategoriesCycleLinkServiceException();
					}
				}
			} else {
				Integer id = categoryDao.create(category);
				category.setId(id);
			}
		} catch(DaoException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void delete(Integer id) throws ServiceException {
		try {
			categoryDao.delete(id);
		} catch(DaoException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Метод читает из базы данных список всех категорий (с помощью CategoryDao).
	 * Затем между категориями восстанавливаются связи "предок-потомок". Например, если имеется
	 * следующая иерархия категорий:
	 * aaa
	 * ├─ bbb
	 * │  ├─ ccc
	 * │  │  ├─ ddd
	 * │  │  └─ eee
	 * │  └─ fff
	 * │     └─ ggg
	 * ├─ hhh
	 * │  ├─ iii
	 * │  └─ jjj
	 * └─ kkk
	 * 
	 * То в базе данных эти категорий могут храниться следующим образом:
	 * ╔══╤════╤═════════╗
	 * ║id│name│parent_id║
	 * ╠══╪════╪═════════╣
	 * ║1 │aaa │  NULL   ║
	 * ╟──┼────┼─────────╢
	 * ║2 │bbb │    1    ║
	 * ╟──┼────┼─────────╢
	 * ║3 │ccc │    2    ║
	 * ╟──┼────┼─────────╢
	 * ║4 │ddd │    3    ║
	 * ╟──┼────┼─────────╢
	 * ║5 │eee │    3    ║
	 * ╟──┼────┼─────────╢
	 * ║6 │fff │    2    ║
	 * ╟──┼────┼─────────╢
	 * ║7 │ggg │    6    ║
	 * ╟──┼────┼─────────╢
	 * ║8 │hhh │    1    ║
	 * ╟──┼────┼─────────╢
	 * ║9 │iii │    8    ║
	 * ╟──┼────┼─────────╢
	 * ║10│jjj │    8    ║
	 * ╟──┼────┼─────────╢
	 * ║11│kkk │    1    ║
	 * ╚══╧════╧═════════╝
	 * 
	 * Используя CategoryDao мы получаем список объектов со следующей структурой
	 * (вместо ссылок parent и children используются линии, соединяющие объекты)
	 * ╔═════╤══════════╤═══════════╗
	 * ║id=1 │name="aaa"│parent=null║
	 * ╚═════╧══════════╧═══════════╝
	 * ╔═════╤══════════╤═══════════╗   ╔════╤═════════╤═══════════╗
	 * ║id=2 │name="bbb"│parent ◯───╫──▷║id:1│name=null│parent=null║
	 * ╚═════╧══════════╧═══════════╝   ╚════╧═════════╧═══════════╝
	 * ╔═════╤══════════╤═══════════╗   ╔════╤═════════╤═══════════╗
	 * ║id=3 │name="ccc"│parent ◯───╫──▷║id:2│name=null│parent=null║
	 * ╚═════╧══════════╧═══════════╝   ╚════╧═════════╧═══════════╝
	 * ╔═════╤══════════╤═══════════╗   ╔════╤═════════╤═══════════╗
	 * ║id=4 │name="ddd"│parent ◯───╫──▷║id:3│name=null│parent=null║
	 * ╚═════╧══════════╧═══════════╝   ╚════╧═════════╧═══════════╝
	 * ╔═════╤══════════╤═══════════╗   ╔════╤═════════╤═══════════╗
	 * ║id=5 │name="eee"│parent ◯───╫──▷║id:3│name=null│parent=null║
	 * ╚═════╧══════════╧═══════════╝   ╚════╧═════════╧═══════════╝
	 * ╔═════╤══════════╤═══════════╗   ╔════╤═════════╤═══════════╗
	 * ║id=6 │name="fff"│parent ◯───╫──▷║id:2│name=null│parent=null║
	 * ╚═════╧══════════╧═══════════╝   ╚════╧═════════╧═══════════╝
	 * ╔═════╤══════════╤═══════════╗   ╔════╤═════════╤═══════════╗
	 * ║id=7 │name="ggg"│parent ◯───╫──▷║id:6│name=null│parent=null║
	 * ╚═════╧══════════╧═══════════╝   ╚════╧═════════╧═══════════╝
	 * ╔═════╤══════════╤═══════════╗   ╔════╤═════════╤═══════════╗
	 * ║id=8 │name="hhh"│parent ◯───╫──▷║id:1│name=null│parent=null║
	 * ╚═════╧══════════╧═══════════╝   ╚════╧═════════╧═══════════╝
	 * ╔═════╤══════════╤═══════════╗   ╔════╤═════════╤═══════════╗
	 * ║id=9 │name="iii"│parent ◯───╫──▷║id:8│name=null│parent=null║
	 * ╚═════╧══════════╧═══════════╝   ╚════╧═════════╧═══════════╝
	 * ╔═════╤══════════╤═══════════╗   ╔════╤═════════╤═══════════╗
	 * ║id=10│name="jjj"│parent ◯───╫──▷║id:8│name=null│parent=null║
	 * ╚═════╧══════════╧═══════════╝   ╚════╧═════════╧═══════════╝
	 * ╔═════╤══════════╤═══════════╗   ╔════╤═════════╤═══════════╗
	 * ║id=11│name="kkk"│parent ◯───╫──▷║id:1│name=null│parent=null║
	 * ╚═════╧══════════╧═══════════╝   ╚════╧═════════╧═══════════╝
	 *
	 * После восстановления связей предок потомок сслыки между объектами
	 * должны выглядеть следующим образом
	 *              ╔═════╤══════════╤═══════════╗
	 *              ║id:1 │name="aaa"│parent=null║
	 *              ╚═════╧══════════╧═══════════╝
	 *         ╔═════╤══════════╤═══════════╗ △△△
	 *         ║id:2 │name="bbb"│parent ◯───╫─┘││
	 *         ╚═════╧══════════╧═══════════╝  ││
	 *     ╔═════╤══════════╤═══════════╗ △△   ││
	 *     ║id:3 │name="ccc"│parent ◯───╫─┘│   ││
	 *     ╚═════╧══════════╧═══════════╝  │   ││
	 * ╔═════╤══════════╤═══════════╗ △△   │   ││
	 * ║id:4 │name="ddd"│parent ◯───╫─┘│   │   ││
	 * ╚═════╧══════════╧═══════════╝  │   │   ││
	 * ╔═════╤══════════╤═══════════╗  │   │   ││
	 * ║id:5 │name="eee"│parent ◯───╫──┘   │   ││
	 * ╚═════╧══════════╧═══════════╝      │   ││
	 *     ╔═════╤══════════╤═══════════╗  │   ││
	 *     ║id:6 │name="fff"│parent ◯───╫──┘   ││
	 *     ╚═════╧══════════╧═══════════╝      ││
	 * ╔═════╤══════════╤═══════════╗ △        ││
	 * ║id:7 │name="ggg"│parent ◯───╫─┘        ││
	 * ╚═════╧══════════╧═══════════╝          ││
	 *         ╔═════╤══════════╤═══════════╗  ││
	 *         ║id:8 │name="hhh"│parent ◯───╫──┘│
	 *         ╚═════╧══════════╧═══════════╝   │
	 *     ╔═════╤══════════╤═══════════╗ △△    │
	 *     ║id:9 │name="iii"│parent ◯───╫─┘│    │
	 *     ╚═════╧══════════╧═══════════╝  │    │
	 *     ╔═════╤══════════╤═══════════╗  │    │
	 *     ║id:10│name="jjj"│parent ◯───╫──┘    │
	 *     ╚═════╧══════════╧═══════════╝       │
	 *         ╔═════╤══════════╤═══════════╗   │
	 *         ║id:11│name="kkk"│parent ◯───╫───┘
	 *         ╚═════╧══════════╧═══════════╝
	 *
	 * @return карта отображения с ключами - идентификаторами категорий (id),
	 * значениями - самими категориями (для удобвста поиска категорий по id)
	 * @throws ServiceException в случае невозможности прочитать данные через CategoryDao
	 */
	private Map<Integer, Category> readAll() throws ServiceException {
		try {
			List<Category> categories = categoryDao.readAll();
			Map<Integer, Category> result = new LinkedHashMap<>();
			/* копируем ссылки на категории в карту отображений для ускорения поиска по id */
			for(Category category : categories) {
				result.put(category.getId(), category);
			}
			/* восстанавливаем между объектами связи "предок-потомок" */
			for(Category category : result.values()) {
				Category parent = category.getParent();
				if(parent != null) {
					parent = result.get(parent.getId());
					parent.getChildren().add(category);
					category.setParent(parent);
				}
			}
			return result;
		} catch(DaoException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Рекурсивный метод, проверяющий, может ли одна категория быть родительской для другой категории.
	 * Этот метод проверяет, если идентификатор <code>category</code> или идентификатор любой её
	 * дочерней категории совпадает с идентификатором <code>possibleParent</code>, то использование
	 * <code>possibleParent</code> в качестве родительской категории невозможно, так как это та же
	 * категория, или её дочерняя категория (на произвольном уровне вложения).
	 * @param possibleParent &mdash; категория, выступающая в роли возможного родительской категории
	 * @param category &mdash; категория, выступаящая в роли возможной дочерней категории
	 * @return <code>true</code>, если использование <code>possibleParent</code> в качестве родительской
	 * для <code>category</code> возможно, и <code>false</code> в противном случае
	 */
	private boolean canCategoryBeParentOfCategory(Category possibleParent, Category category) {
		if(possibleParent != null && possibleParent.getId() != null) {
			if(category.getId().equals(possibleParent.getId())) {
				return false;
			}
			for(Category child : category.getChildren()) {
				if(canCategoryBeParentOfCategory(possibleParent, child)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Метод удаляет из карты отображения категорию с заданным идентификатором и все её
	 * дочерние подкатегории
	 * @param categories &mdash; карта отображения категорий, в которой ключи &mdash; это
	 * идентификаторы категорий, а значения &mdash; сами объекты-категории
	 * @param id &mdash; идентификатор удаляемой категори
	 * @param needParentLinkRemove &mdash; нужно ли у родительской категории удалаять из
	 * списка дочерних категорий ссылку на удаляемую категорию
	 */
	private void removeChildren(Map<Integer, Category> categories, Integer id, boolean needParentLinkRemove) {
		Category category = categories.remove(id);
		if(category != null) {
			if(needParentLinkRemove) {
				Category parent = category.getParent();
				if(parent != null) {
					parent.getChildren().remove(category);
				}
			}
			for(Category child : category.getChildren()) {
				removeChildren(categories, child.getId(), false);
			}
		}
	}

	/**
	 * Метод выбирает из карты отображения категории, не имеющие родительской категории,
	 * и формирует из них список категорий
	 * @param categories &mdash; карта отображения категорий, в которой ключи &mdash; это
	 * идентификаторы категорий, а значения &mdash; сами объекты-категории
	 * @return список категорий &laquo;верхнего уровня&raquo;
	 */
	private List<Category> filterTopLevel(Map<Integer, Category> categories) {
		List<Category> result = new ArrayList<>();
		for(Integer id : categories.keySet()) {
			Category category = categories.get(id);
			if(category.getParent() == null) {
				result.add(category);
			}
		}
		return result;
	}
}