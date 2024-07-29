package mvc.uga.view;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;
import mvc.uga.entity.Request;
import mvc.uga.security.SecurityService;
import mvc.uga.service.UgaService;

import java.io.ByteArrayInputStream;

@Route("attachment")
@PermitAll
public class AttachmentView extends VerticalLayout
        implements HasUrlParameter<String> {

    private final UgaService ugaService;
    private final SecurityService securityService;

    public AttachmentView(UgaService ugaService, SecurityService securityService) {
        this.ugaService = ugaService;
        this.securityService = securityService;
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        try {
            int requestId = Integer.parseInt(parameter);
            Request request = ugaService.findRequestById(requestId);

            if (request.getAttachment() != null && isUserOwner(request)) {
                Image attachmentImage = new Image(new StreamResource("attachment.png",
                        () -> new ByteArrayInputStream(request.getAttachment())), "Image");
                add(attachmentImage);
            } else {
                event.forwardTo(AccessDeniedView.class);
            }
        } catch (RuntimeException e) {
            add(new Text("Invalid request ID. Please provide a valid ID number."));
        }
    }

    private boolean isUserOwner(Request request) {
        String authenticatedUsername = securityService.getAuthenticatedUser().getUsername();
        return authenticatedUsername.equals(request.getUser().getUsername());
    }

}
