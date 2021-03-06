package org.pinguin.gf.gui.planning;

import java.math.BigDecimal;
import java.util.function.Function;

import org.pinguin.gf.facade.account.AccountTO;
import org.pinguin.gf.facade.planning.AccountPlanningTO;
import org.pinguin.gui.util.BindHelper;
import org.pinguin.gui.util.EditMode;
import org.pinguin.gui.util.PropertyAdapter;
import org.pinguin.gui.util.ValueConverter;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AccPlanningFormPresenter {

	private BindHelper<AccountPlanningTO> bindHelper = new BindHelper<>();

	private final ObservableList<AccountTO> accounts = FXCollections.observableArrayList();
	private final Property<AccountTO> accountProperty = new SimpleObjectProperty<>();
	private final Property<String> valueProperty = new SimpleStringProperty();

	private EditMode editMode = EditMode.CREATE;

	private AccountPlanningTO to;

	private Function<Void, Void> onSaveCommand;

	public ObservableList<AccountTO> getAccounts() {
		return accounts;
	}

	public Property<AccountTO> accountProperty() {
		return accountProperty;
	}

	public Property<String> valueProperty() {
		return valueProperty;
	}

	public EditMode getEditMode() {
		return editMode;
	}

	public void setEditMode(EditMode editMode) {
		this.editMode = editMode;
	}

	public AccountPlanningTO getTo() {
		return to;
	}

	public void setTo(AccountPlanningTO to) {
		this.to = to;

		if (to != null) {
			mapToPresenter(to);
		}
	}

	public Function<Void, Void> getOnSaveCommand() {
		return onSaveCommand;
	}

	public void setOnSaveCommand(Function<Void, Void> onSaveCommand) {
		this.onSaveCommand = onSaveCommand;
	}

	private void mapToPresenter(AccountPlanningTO to) {
		bindHelper.setTo(to);
		bindHelper.bind("account", accountProperty);
		bindHelper.bind("value",
				new PropertyAdapter<BigDecimal, String>(valueProperty, new ValueConverter<BigDecimal, String>() {

					@Override
					public BigDecimal convert1(String value) {
						try {
							return new BigDecimal(value);
						} catch (Exception e) {
							return BigDecimal.ZERO;
						}
					}

					@Override
					public String convert2(BigDecimal value) {
						if (value == null) {
							return "0.00";
						}
						return value.toString();
					}
				}));
	}

	public void save() {
		if (onSaveCommand != null) {
			onSaveCommand.apply(null);
		}
	}

	public void cancel() {
		// TODO Auto-generated method stub

	}

}
