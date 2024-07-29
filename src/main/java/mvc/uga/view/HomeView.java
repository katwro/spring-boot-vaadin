package mvc.uga.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import mvc.uga.entity.Equipment;
import mvc.uga.entity.Request;
import mvc.uga.entity.Type;
import mvc.uga.security.SecurityService;
import mvc.uga.service.UgaService;

@Route(value = "", layout = MainView.class)
@PageTitle("Home")
@PermitAll
public class HomeView extends VerticalLayout {

    private final UgaService ugaService;
    private final SecurityService securityService;

    private Grid<Request> requestGrid;
    private Grid<Equipment> equipmentGrid;
    private TabSheet tabSheet;
    private String username;

    public HomeView(UgaService ugaService, SecurityService securityService) {
        this.ugaService = ugaService;
        this.securityService = securityService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        createVariables();
        configureRequestGrid();
        configureEquipmentGrid();

        add(createTitle(), createTabSheet());
    }

    private Component createTabSheet() {
        tabSheet.setWidth("100%");
        tabSheet.setHeight("100%");
        tabSheet.addThemeVariants(TabSheetVariant.LUMO_TABS_EQUAL_WIDTH_TABS);
        tabSheet.add("My Requests", requestGrid);
        tabSheet.add("My Equipment", equipmentGrid);

        return  tabSheet;
    }

    private Component createTitle() {
        return new H1("UserGear Application");
    }

    private void configureEquipmentGrid() {
        equipmentGrid.setColumns("id", "name", "serialNumber", "inventoryNumber");
        equipmentGrid.setItems(ugaService.findAllEquipmentByUsername(username));
        equipmentGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        equipmentGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        equipmentGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        equipmentGrid.setHeight("100%");
    }

    private void configureRequestGrid() {
        requestGrid.setColumns("id");
        requestGrid.addComponentColumn(this::createDescriptionColumn).setHeader("Description");
        requestGrid.addColumn(r -> r.getCategory().getName()).setHeader("Category");
        requestGrid.addColumn(this::getOptionName).setHeader("Option");
        requestGrid.addColumn(r -> r.getScope().getName()).setHeader("Scope");
        requestGrid.addColumn(r -> r.getPriority().getName()).setHeader("Priority");
        requestGrid.addColumn(this::getEquipmentName).setHeader("Equipment");
        requestGrid.addComponentColumn(this::createAttachmentColumn).setHeader("Attachment");
        requestGrid.setItems(ugaService.findAllRequestsByUsername(username));

        requestGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        requestGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        requestGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        requestGrid.setHeight("100%");
    }

    private Component createAttachmentColumn(Request r) {
        if (r.getAttachment() != null) {
            Button openAttachmentBtn = new Button(VaadinIcon.FILE_PICTURE.create());
            openAttachmentBtn.getElement().setAttribute("title", "View");
            openAttachmentBtn.addClickListener(e -> UI.getCurrent().getPage().open("attachment/" + r.getId(), "_blank"));
            return openAttachmentBtn;
        } else {
            return new Text("-");
        }
    }

    private String getEquipmentName(Request request) {
        Equipment equipment = request.getEquipment();
        return equipment != null ? equipment.getName() : "-";
    }

    private String getOptionName(Request request) {
        Type options = request.getOption();
        return options != null ? options.getName() : "-";
    }

    private HorizontalLayout createDescriptionColumn(Request r) {
        HorizontalLayout layout = new HorizontalLayout();
        String fullDescription = r.getDescription();
        int maxLength = 10;
        String truncatedDescription = fullDescription.length() > maxLength ? fullDescription.substring(0, maxLength) + "..." : fullDescription;
        Span descriptionSpan = new Span(truncatedDescription);

        Button fullDescriptionBtn = new Button(VaadinIcon.EXPAND_FULL.create());
        fullDescriptionBtn.getElement().setAttribute("title", "View full description");
        fullDescriptionBtn.addClickListener(e -> openDescriptionDialog(fullDescription));

        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        layout.add(descriptionSpan, fullDescriptionBtn);
        layout.expand(descriptionSpan);

        return layout;
    }

    private void openDescriptionDialog(String fullDescription) {
        Dialog dialog = new Dialog();
        dialog.add(new Text(fullDescription));
        dialog.setMaxWidth("60%");
        dialog.open();
    }

    private void createVariables() {
        tabSheet = new TabSheet();
        requestGrid = new Grid<>(Request.class);
        equipmentGrid = new Grid<>(Equipment.class);
        username = securityService.getAuthenticatedUser().getUsername();
    }

}
