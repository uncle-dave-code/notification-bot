package com.uncledavecode.notificator.ui;

import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.annotation.PostConstruct;

import com.uncledavecode.notificator.bot.NotificatorBot;
import com.uncledavecode.notificator.model.AccessRequest;
import com.uncledavecode.notificator.model.UserAccount;
import com.uncledavecode.notificator.services.AccessRequestService;
import com.uncledavecode.notificator.services.UserAccountService;
import com.uncledavecode.notificator.ui.components.AlertEvent;
import com.uncledavecode.notificator.ui.components.UncleMessage;
import com.uncledavecode.notificator.ui.components.UncleTab;
import com.uncledavecode.notificator.utils.BotUtils;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;

@Route("")
@CssImport("./styles/app-style.css")
public class MainUI extends VerticalLayout {

    private final AccessRequestService accessRequestService;
    private final UserAccountService userAccountService;
    private final NotificatorBot notificatorBot;

    public MainUI(AccessRequestService accessRequestService, UserAccountService userAccountService,
            NotificatorBot notificatorBot) {
        this.accessRequestService = accessRequestService;
        this.userAccountService = userAccountService;
        this.notificatorBot = notificatorBot;
    }

    private Grid<AccessRequest> dgdAccessRequest;
    private Grid<UserAccount> dgdUserAccount;

    @PostConstruct
    private void init() {
        H2 title = new H2("Notificator Panel");

        UncleTab tabMain = new UncleTab();
        tabMain.addTab("Users List", this.getUserAccountPanel());
        tabMain.addTab("Access Requests", this.getRequestAccessPanel());

        this.add(title, tabMain);

        this.setSizeFull();

        this.refreshAccessRequestData();
        this.refreshUserAccountData();
    }

    private VerticalLayout getRequestAccessPanel() {
        Button btnRefresh = new Button(new Icon(VaadinIcon.REFRESH));
        btnRefresh.addThemeVariants(ButtonVariant.LUMO_LARGE, ButtonVariant.LUMO_TERTIARY);
        btnRefresh.addClassName("action-button");
        btnRefresh.addClickListener(event -> refreshAccessRequestData());
        Div spacer = new Div();

        HorizontalLayout pnlToolBar = new HorizontalLayout();
        pnlToolBar.setWidthFull();
        pnlToolBar.add(spacer, btnRefresh);
        pnlToolBar.expand(spacer);

        this.dgdAccessRequest = new Grid<>();
        this.dgdAccessRequest.setSizeFull();
        this.dgdAccessRequest.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS, GridVariant.LUMO_WRAP_CELL_CONTENT,
                GridVariant.LUMO_ROW_STRIPES);
        this.dgdAccessRequest.setSelectionMode(SelectionMode.NONE);
        this.dgdAccessRequest.addColumn(AccessRequest::getLogid).setHeader("Log Id");
        this.dgdAccessRequest.addColumn(AccessRequest::getEmail).setHeader("Email");
        this.dgdAccessRequest.addColumn(AccessRequest::getName).setHeader("Name");
        this.dgdAccessRequest.addColumn(AccessRequest::getLastname).setHeader("Last Name");
        this.dgdAccessRequest
                .addColumn(item -> DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm").format(item.getRequestDate()))
                .setHeader("Request Date");
        this.dgdAccessRequest.addComponentColumn(item -> getAccessRequestColumnRenderer(item)).setHeader("Actions")
                .setWidth("100px");
        VerticalLayout result = new VerticalLayout();
        result.add(pnlToolBar, dgdAccessRequest);
        result.setSizeFull();
        return result;
    }

    private VerticalLayout getUserAccountPanel() {

        Button btnRefresh = new Button(new Icon(VaadinIcon.REFRESH));
        btnRefresh.addThemeVariants(ButtonVariant.LUMO_LARGE, ButtonVariant.LUMO_TERTIARY);
        btnRefresh.addClassName("action-button");
        btnRefresh.addClickListener(event -> refreshUserAccountData());

        Button btnSend = new Button(new Icon(VaadinIcon.PAPERPLANE_O));
        btnSend.addThemeVariants(ButtonVariant.LUMO_LARGE, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
        btnSend.addClassName("action-button");
        btnSend.addClickListener(event -> sendMessage_handler(event));

        Div spacer = new Div();

        HorizontalLayout pnlToolBar = new HorizontalLayout();
        pnlToolBar.setWidthFull();
        pnlToolBar.add(btnSend, spacer, btnRefresh);
        pnlToolBar.expand(spacer);
        this.dgdUserAccount = new Grid<>();
        this.dgdUserAccount.setSizeFull();
        this.dgdUserAccount.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS, GridVariant.LUMO_WRAP_CELL_CONTENT,
                GridVariant.LUMO_ROW_STRIPES);
        this.dgdUserAccount.setSelectionMode(SelectionMode.MULTI);
        this.dgdUserAccount.addColumn(UserAccount::getEmail).setHeader("Email");
        this.dgdUserAccount.addColumn(UserAccount::getName).setHeader("Name");
        this.dgdUserAccount.addColumn(UserAccount::getLastname).setHeader("Last Name");

        this.dgdUserAccount.addComponentColumn(item -> getUserAccountColumnRenderer(item)).setHeader("Actions")
                .setWidth("100px");
        VerticalLayout result = new VerticalLayout();
        result.add(pnlToolBar, dgdUserAccount);
        result.setSizeFull();

        return result;
    }

    private void sendMessage_handler(ClickEvent<Button> event) {
        if (dgdUserAccount.getSelectedItems() != null && !dgdUserAccount.getSelectedItems().isEmpty()) {
            UncleMessage message = new UncleMessage();
            message.initializeComponent();
            message.addAcceptListener(new ComponentEventListener<AlertEvent>() {
                @Override
                public void onComponentEvent(AlertEvent event) {
                    Long[] chatIds = dgdUserAccount.getSelectedItems().stream().filter(user ->
                    user.getActive()==true).map(p -> p.getChatId()).toArray(Long[]::new);

                    BotUtils.sendMessage(notificatorBot, event.getMessage(), chatIds);
                }
            });

            message.open();
        }

    }

    private HorizontalLayout getAccessRequestColumnRenderer(AccessRequest request) {
        HorizontalLayout result = new HorizontalLayout();

        Button btnDelete = new Button(new Icon(VaadinIcon.TRASH));
        btnDelete.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        btnDelete.addClassName("action-button");
        btnDelete.addClickListener(event -> removeAccessRequest_handler(event, request));
        result.add(btnDelete);

        return result;
    }

    private void removeAccessRequest_handler(ClickEvent<Button> event, AccessRequest request) {
        Dialog dialog = new Dialog();
        dialog.add(getDialogLayout("Remove Access Request?", dialog, new Command() {
            @Override
            public void execute() {
                accessRequestService.deleteByChatId(request.getChatId());
                dialog.close();
                refreshAccessRequestData();
            }

        }));
        dialog.open();
    }

    private HorizontalLayout getUserAccountColumnRenderer(UserAccount userAccount) {
        HorizontalLayout result = new HorizontalLayout();

        if (!userAccount.getActive()) {
            Button btnAccept = new Button(new Icon(VaadinIcon.CHECK));
            btnAccept.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SUCCESS,
                    ButtonVariant.LUMO_TERTIARY);
            btnAccept.addClassName("action-button");
            btnAccept.addClickListener(event -> acceptUserAccount_handler(event, userAccount));

            Button btnReject = new Button(new Icon(VaadinIcon.CLOSE));
            btnReject.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            btnReject.addClassName("action-button");
            btnReject.addClickListener(event -> rejectUserAccount_handler(event, userAccount));

            result.add(btnAccept, btnReject);
        } else {
            Button btnDelete = new Button(new Icon(VaadinIcon.TRASH));
            btnDelete.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            btnDelete.addClassName("action-button");
            btnDelete.addClickListener(event -> deleteUserAccount_handler(event, userAccount));

            result.add(btnDelete);
        }

        return result;
    }

    private void acceptUserAccount_handler(ClickEvent<Button> event, UserAccount userAccount) {
        userAccount.setActive(true);
        userAccountService.updateUserAccount(userAccount);
        this.refreshUserAccountData();
    }

    private void rejectUserAccount_handler(ClickEvent<Button> event, UserAccount userAccount) {
        Dialog dialog = new Dialog();
        dialog.add(getDialogLayout("Reject User Account?", dialog, new Command() {
            @Override
            public void execute() {
                userAccountService.deleteByChatId(userAccount.getChatId());
                dialog.close();
                refreshUserAccountData();
            }
        }));
        dialog.open();
    }

    private void deleteUserAccount_handler(ClickEvent<Button> event, UserAccount userAccount) {
        Dialog dialog = new Dialog();
        dialog.add(getDialogLayout("Delete User Account?", dialog, new Command() {
            @Override
            public void execute() {
                userAccountService.deleteByChatId(userAccount.getChatId());
                dialog.close();
                refreshUserAccountData();
            }
        }));
        dialog.open();
    }

    private void refreshAccessRequestData() {
        List<AccessRequest> lstAccessRequest = this.accessRequestService.getAllAccessRequests();
        dgdAccessRequest.setItems(lstAccessRequest);
    }

    private void refreshUserAccountData() {
        List<UserAccount> lstUserAccount = this.userAccountService.getAllUserAccounts();
        dgdUserAccount.setItems(lstUserAccount);
    }

    private VerticalLayout getDialogLayout(String message, Dialog dialog, Command command) {
        H3 headline = new H3(message);
        headline.getStyle().set("margin", "var(--lumo-space-m) 0 0 0")
                .set("font-size", "1.5em").set("font-weight", "bold");

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button saveButton = new Button("Yes", e -> command.execute());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Div spacer = new Div();

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, spacer, saveButton);
        buttonLayout.setWidthFull();
        buttonLayout.expand(spacer);
        VerticalLayout dialogLayout = new VerticalLayout(headline, buttonLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setAlignItems(Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "300px").set("max-width", "100%");

        return dialogLayout;
    }
}
