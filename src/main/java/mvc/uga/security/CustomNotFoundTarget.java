package mvc.uga.security;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.RouteNotFoundError;
import jakarta.servlet.http.HttpServletResponse;
import mvc.uga.view.AccessDeniedView;

public class CustomNotFoundTarget extends RouteNotFoundError {

    @Override
    public int setErrorParameter(final BeforeEnterEvent event, final ErrorParameter<NotFoundException> parameter) {
        event.forwardTo(AccessDeniedView.class);
        return HttpServletResponse.SC_NOT_FOUND;
    }

}
