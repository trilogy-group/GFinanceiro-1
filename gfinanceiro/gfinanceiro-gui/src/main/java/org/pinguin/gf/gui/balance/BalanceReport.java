package org.pinguin.gf.gui.balance;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.pinguin.gf.facade.account.AccountTO;
import org.pinguin.gf.facade.journalentry.BalanceTO;
import org.pinguin.gf.gui.accstatement.OpenAccStatementCommand;
import org.pinguin.gf.gui.accstatement.OpenAccStatementParam;
import org.pinguin.gf.gui.util.AccountStringConverter;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import jfxtras.scene.control.CalendarTextField;

public class BalanceReport extends AnchorPane {

	@Inject
	private BalancePresenter presenter;
	@Inject
	private OpenAccStatementCommand openAccStatement;

	@FXML
	private CalendarTextField startDateText;
	@FXML
	private CalendarTextField endDateText;
	@FXML
	private TreeTableView<BalanceTO> balanceTree;
	@FXML
	private TreeTableColumn<BalanceTO, String> balanceTColumn;

	public BalanceReport() {
		loadFxml();
		balanceTree.setShowRoot(false);
	}

	@Inject
	public void init() {
		startDateText.calendarProperty().bindBidirectional(presenter.startDateProperty());
		endDateText.calendarProperty().bindBidirectional(presenter.endDateProperty());

		presenter.getBalanceList().addListener(new ListChangeListener<BalanceTO>() {

			@Override
			public void onChanged(Change<? extends BalanceTO> c) {
				ObservableList<? extends BalanceTO> list = c.getList();
				// Transformar e preencher
				TreeItem<BalanceTO> root = new TreeItem<BalanceTO>(null);
				transform(list, root);
				balanceTree.setRoot(root);
			}

			private void transform(ObservableList<? extends BalanceTO> list, TreeItem<BalanceTO> root) {
				// 1. Criar TreeItem e guardar no map, por ID da Conta
				Map<Long, TreeItem<BalanceTO>> map = new HashMap<>();
				for (BalanceTO item : list) {
					TreeItem<BalanceTO> treeItem = new TreeItem<BalanceTO>(item);
					map.put(item.getAccount().getId(), treeItem);
				}
				// 2. Montar a hierarquia e guardar os roots
				for (TreeItem<BalanceTO> item : map.values()) {
					if (item.getValue().getAccount().getParent() != null) {
						TreeItem<BalanceTO> parent = map.get(item.getValue().getAccount().getParent().getId());
						parent.getChildren().add(item);
					} else {
						root.getChildren().add(item);
					}
				}
			}
		});

		final StringConverter<AccountTO> accStrConverter = new AccountStringConverter();
		balanceTColumn
				.setCellValueFactory(new SimpleTVCellValueFactory<BalanceTO, AccountTO>("account", accStrConverter));

		balanceTree.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (event.getClickCount() == 3) {
					BalanceTO selected = balanceTree.getSelectionModel().getSelectedItem().getValue();
					openAccStatement.apply(new OpenAccStatementParam(null, selected.getAccount(),
							presenter.startDateProperty().getValue(), presenter.endDateProperty().getValue()));
				}
			}
		});
	}

	private void loadFxml() {
		final FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/META-INF/fxml/BalanceReport.xml"));
		loader.setRoot(this);
		loader.setController(this);
		try {
			loader.load();
		} catch (final IOException exception) {
			throw new IllegalStateException(exception);
		}
	}

	@FXML
	public void retrieve(ActionEvent evt) {
		presenter.retrieve();
	}

	public static class SimpleTVCellValueFactory<T, O>
			implements Callback<CellDataFeatures<T, String>, ObservableValue<String>> {

		private final String fieldName;
		private final StringConverter<O> strConverter;

		public SimpleTVCellValueFactory(String fieldName, StringConverter<O> strConverter) {
			this.fieldName = fieldName;
			this.strConverter = strConverter;
		}

		@Override
		public ObservableValue<String> call(CellDataFeatures<T, String> cellDataFeatures) {

			T bean = cellDataFeatures.getValue().getValue();

			if (bean == null) {
				return null;
			}

			try {
				Field field = bean.getClass().getDeclaredField(fieldName);
				field.setAccessible(true);
				@SuppressWarnings("unchecked")
				O fieldValue = (O) field.get(bean);
				return new ReadOnlyObjectWrapper<String>(strConverter.toString(fieldValue));
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				return new ReadOnlyObjectWrapper<String>("Error");
			}
		}
	}

}
