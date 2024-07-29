package mvc.uga.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("access-denied")
@PageTitle("Access denied")
@PermitAll
public class AccessDeniedView extends VerticalLayout {

    public AccessDeniedView() {

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        H2 header = new H2("You are not authorized to access this resource or this resource does not exist.");

        Button backBtn = new Button("Back", VaadinIcon.HOME.create());
        backBtn.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("")));

        add(header, backBtn);
    }

}
