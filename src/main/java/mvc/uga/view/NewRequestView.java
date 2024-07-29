package mvc.uga.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import mvc.uga.constant.AppConstants;
import mvc.uga.entity.Equipment;
import mvc.uga.entity.Request;
import mvc.uga.entity.Type;
import mvc.uga.entity.User;
import mvc.uga.security.SecurityService;
import mvc.uga.service.UgaService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static mvc.uga.service.NotificationService.createNotification;

@Route(value = "new-request", layout = MainView.class)
@PageTitle("New Request")
@PermitAll
public class NewRequestView extends VerticalLayout implements BeforeEnterObserver {
    private final UgaService ugaService;
    private final SecurityService securityService;

    private Request request;
    private Binder<Request> binder;
    private String username;

    private TextArea description;
    private ComboBox<Equipment> equipment;
    private ComboBox<Type> category;
    private ComboBox<Type> option;
    private ComboBox<Type> scope;
    private ComboBox<Type> priority;

    private Button saveBtn;
    private Button cancelBtn;


    public NewRequestView(UgaService ugaService, SecurityService securityService) {
        this.ugaService = ugaService;
        this.securityService = securityService;

        setAlignItems(Alignment.CENTER);

        createVariables();
        createBinder();

        add(createTitle(), createRequestForm(), createButtons());
    }

    private Component createButtons() {
        configureSaveButton();
        configureCancelButton();

        HorizontalLayout buttonLayout = new HorizontalLayout(saveBtn, cancelBtn);
        buttonLayout.getStyle().set("margin-top", "30px");

        return buttonLayout;
    }

    private void configureCancelButton() {
        cancelBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.getPage().getHistory().back()));
    }

    private void configureSaveButton() {
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> {
            if (binder.validate().isOk()) {
                try {
                    binder.writeBean(request);
                    User user = ugaService.findUserByUsername(username);
                    request.setUser(user);
                    ugaService.saveRequest(request);
                    createNotification(AppConstants.REQUEST_SAVED_SUCCESSFULLY, NotificationVariant.LUMO_SUCCESS);
                    UI.getCurrent().navigate("");
                } catch (Exception exception) {
                    createNotification(AppConstants.ERROR_SAVING, NotificationVariant.LUMO_ERROR);
                }
            } else {
                createNotification(AppConstants.REQUIRED_FIELDS_NOTIFICATION, NotificationVariant.LUMO_ERROR);
            }
        });
    }

    private Component createRequestForm() {
        FormLayout requestForm = new FormLayout();
        requestForm.add(createLists(), createDescription(), createAttachmentSection());
        requestForm.setWidth("90%");

        HorizontalLayout centeredLayout = new HorizontalLayout(requestForm);
        centeredLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        return centeredLayout;
    }

    private Component createAttachmentSection() {
        MemoryBuffer buffer = new MemoryBuffer();
        Upload attachment = new Upload(buffer);

        attachment.setAcceptedFileTypes("image/*");
        attachment.setMaxFiles(1);
        attachment.setMaxFileSize(10485760);
        attachment.addSucceededListener(event -> {
            InputStream inputStream = buffer.getInputStream();
            try {
                byte[] attachmentBytes = inputStream.readAllBytes();
                request.setAttachment(attachmentBytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        attachment.addFileRejectedListener(event ->
                createNotification("File size limit exceeded.", NotificationVariant.LUMO_ERROR));

        H4 title = new H4("Upload image");

        Paragraph hint = new Paragraph(
                "File size must be less than or equal to 10 MB. Only image files are accepted.");

        VerticalLayout layout = new VerticalLayout(title, hint, attachment);
        layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        return layout;
    }

    private Component createDescription() {
        description.setPlaceholder("write a short description of your request...");
        description.setMaxLength(500);
        description.setHeight("210px");
        return description;
    }

    private Component createLists() {
        List<Type> categories = ugaService.findAllTypes(1);
        category.setItems(categories);
        category.setItemLabelGenerator(Type::getName);
        category.addValueChangeListener(event -> {
            boolean showOptionsAndEquipment = event.getValue() != null
                    && event.getValue().equals(categories.get(0));
            option.setVisible(showOptionsAndEquipment);
            equipment.setVisible(showOptionsAndEquipment);
        });

        List<Type> scopes = ugaService.findAllTypes(3);
        scope.setItems(scopes);
        scope.setItemLabelGenerator(Type::getName);

        List<Type> priorities = ugaService.findAllTypes(4);
        priority.setItems(priorities);
        priority.setItemLabelGenerator(Type::getName);

        List<Type> options = ugaService.findAllTypes(2);
        option.setItems(options);
        option.setItemLabelGenerator(Type::getName);

        List<Equipment> equipmentList = ugaService.findAllEquipmentByUsername(username);
        equipment.setItems(equipmentList);
        equipment.setItemLabelGenerator(Equipment::getName);

        option.setVisible(false);
        equipment.setVisible(false);

        FormLayout formLayout = new FormLayout();
        formLayout.add(category, scope, priority, option, equipment);

        formLayout.setColspan(category, 2);

        return formLayout;
    }

    private Component createTitle() {
        H3 title = new H3("New request");
        title.getElement().getStyle().set("font-weight", "bold");
        return title;
    }

    private void createBinder() {
        request = new Request();
        binder = new BeanValidationBinder<>(Request.class);
        binder.bindInstanceFields(this);

        binder.forField(option)
                .withValidator(value -> !option.isVisible() || value != null, AppConstants.REQUIRED_FIELD)
                .bind(Request::getOption, Request::setOption);

        binder.forField(equipment)
                .withValidator(value -> !equipment.isVisible() || value != null, AppConstants.REQUIRED_FIELD)
                .bind(Request::getEquipment, Request::setEquipment);
    }

    private void createVariables() {
        username = securityService.getAuthenticatedUser().getUsername();

        description = new TextArea("Description");

        equipment = new ComboBox<>("Equipment");
        equipment.setPlaceholder("Select equipment");

        category = new ComboBox<>("Category");
        category.setPlaceholder("Select category");

        option = new ComboBox<>("Option");
        option.setPlaceholder("Select option");

        scope = new ComboBox<>("Scope");
        scope.setPlaceholder("Select scope");

        priority = new ComboBox<>("Priority");
        priority.setPlaceholder("Select priority");

        saveBtn = new Button("Save");
        cancelBtn = new Button("Cancel");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        clearFormFields();
    }

    private void clearFormFields() {
        binder.readBean(new Request());
    }

}
