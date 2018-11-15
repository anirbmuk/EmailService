package blog.anirbanm.mail.view;

import com.sun.faces.util.MessageFactory;

import java.io.IOException;

import java.text.MessageFormat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import oracle.adf.controller.ControllerContext;
import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.fragment.RichRegion;
import oracle.adf.view.rich.context.AdfFacesContext;

import org.apache.myfaces.trinidad.component.UIXCollection;
import org.apache.myfaces.trinidad.component.UIXCommand;
import org.apache.myfaces.trinidad.component.UIXEditableValue;
import org.apache.myfaces.trinidad.component.UIXForm;
import org.apache.myfaces.trinidad.component.UIXSubform;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;


public class JSFUtils {

    private static final String NO_RESOURCE_FOUND = "Missing resource: ";
    private static final ADFLogger LOGGER = ADFLogger.createADFLogger(JSFUtils.class);
    private static final String CREATE_MODES_KEY = "createModes";
    private static final String APPLICATION_FACTORY_KEY = "javax.faces.application.ApplicationFactory";

    /**
     * Method for taking a reference to a JSF binding expression and returning
     * the matching object (or creating it).
     * @param expression EL expression
     * @return Managed object
     */
    public static Object resolveExpression(String expression) {
        FacesContext facesContext = getFacesContext();
        Application app = facesContext.getApplication();
        ExpressionFactory elFactory = app.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        ValueExpression valueExp = elFactory.createValueExpression(elContext, expression, Object.class);
        return valueExp.getValue(elContext);
    }

    /**
     * @return
     */
    public static String resolveRemoteUser() {
        FacesContext facesContext = getFacesContext();
        ExternalContext ectx = facesContext.getExternalContext();
        return ectx.getRemoteUser();
    }

    /**
     * @return
     */
    public static String resolveUserPrincipal() {
        FacesContext facesContext = getFacesContext();
        ExternalContext ectx = facesContext.getExternalContext();
        HttpServletRequest request = (HttpServletRequest)ectx.getRequest();
        return request.getUserPrincipal().getName();
    }

    /**
     * @param expression
     * @param returnType
     * @param argTypes
     * @param argValues
     * @return
     */
    public static Object resolveMethodExpression(String expression, Class returnType, Class[] argTypes,
                                                 Object[] argValues) {
        FacesContext facesContext = getFacesContext();
        Application app = facesContext.getApplication();
        ExpressionFactory elFactory = app.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        MethodExpression methodExpression =
            elFactory.createMethodExpression(elContext, expression, returnType, argTypes);
        return methodExpression.invoke(elContext, argValues);
    }

    /**
     * Method for taking a reference to a JSF binding expression and returning
     * the matching Boolean.
     * @param expression EL expression
     * @return Managed object
     */
    public static Boolean resolveExpressionAsBoolean(String expression) {
        return (Boolean)resolveExpression(expression);
    }

    /**
     * Method for taking a reference to a JSF binding expression and returning
     * the matching String (or creating it).
     * @param expression EL expression
     * @return Managed object
     */
    public static String resolveExpressionAsString(String expression) {
        return (String)resolveExpression(expression);
    }

    /**
     * Convenience method for resolving a reference to a managed bean by name
     * rather than by expression.
     * @param beanName name of managed bean
     * @return Managed object
     */
    public static Object getManagedBeanValue(String beanName) {
        StringBuffer buff = new StringBuffer("#{");
        buff.append(beanName);
        buff.append("}");
        return resolveExpression(buff.toString());
    }

    /**
     * Method for setting a new object into a JSF managed bean
     * Note: will fail silently if the supplied object does
     * not match the type of the managed bean.
     * @param expression EL expression
     * @param newValue new value to set
     */
    public static void setExpressionValue(String expression, Object newValue) {
        FacesContext facesContext = getFacesContext();
        Application app = facesContext.getApplication();
        ExpressionFactory elFactory = app.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        ValueExpression valueExp = elFactory.createValueExpression(elContext, expression, Object.class);

        //Check that the input newValue can be cast to the property type
        //expected by the managed bean.
        //If the managed Bean expects a primitive we rely on Auto-Unboxing
        Class bindClass = valueExp.getType(elContext);
        if (bindClass.isPrimitive() || bindClass.isInstance(newValue)) {
            valueExp.setValue(elContext, newValue);
        }
    }

    /**
     * Convenience method for setting the value of a managed bean by name
     * rather than by expression.
     * @param beanName name of managed bean
     * @param newValue new value to set
     */
    public static void setManagedBeanValue(String beanName, Object newValue) {
        StringBuffer buff = new StringBuffer("#{");
        buff.append(beanName);
        buff.append("}");
        setExpressionValue(buff.toString(), newValue);
    }


    /**
     * Convenience method for setting Session variables.
     * @param key object key
     * @param object value to store
     */
    public static void storeOnSession(String key, Object object) {
        FacesContext ctx = getFacesContext();
        Map sessionState = ctx.getExternalContext().getSessionMap();
        sessionState.put(key, object);
    }

    /**
     * Convenience method for setting pageflow variables.
     * @param key object key
     * @param object value to store
     */
    public static void storeOnView(String key, Object object) {
        Map viewState = AdfFacesContext.getCurrentInstance().getViewScope();
        viewState.put(key, object);
    }

    /**
     * Convenience method for setting pageflow variables.
     * @param key object key
     * @param object value to store
     */
    public static void storeOnPageflow(String key, Object object) {
        ADFContext ctx = ADFContext.getCurrent();
        Map pfState = ctx.getPageFlowScope();
        pfState.put(key, object);
    }

    /**
     * Convenience method for getting Session variables.
     * @param key object key
     * @return session object for key
     */
    public static Object getFromSession(String key) {
        FacesContext ctx = getFacesContext();
        Map sessionState = ctx.getExternalContext().getSessionMap();
        return sessionState.get(key);
    }

    /**
     * Convenience method for getting pageflow scope variables.
     * @param key object key
     * @return pageflow object for key
     */
    public static Object getFromPageflow(String key) {
        ADFContext ctx = ADFContext.getCurrent();
        Map pageflowState = ctx.getPageFlowScope();
        return pageflowState.get(key);
    }

    /**
     * Convenience method for getting view scope variables.
     * @param key object key
     * @return view object for key
     */
    public static Object getFromView(String key) {
        Map viewState = AdfFacesContext.getCurrentInstance().getViewScope();
        return viewState.get(key);
    }

    /**
     * @param key
     * @return
     */
    public static String getFromHeader(String key) {
        FacesContext ctx = getFacesContext();
        ExternalContext ectx = ctx.getExternalContext();
        return ectx.getRequestHeaderMap().get(key);
    }

    /**
     * Convenience method for getting Request variables.
     * @param key object key
     * @return session object for key
     */
    public static Object getFromRequest(String key) {
        FacesContext ctx = getFacesContext();
        Map sessionState = ctx.getExternalContext().getRequestMap();
        return sessionState.get(key);
    }

    /**
     * Pulls a String resource from the property bundle that
     * is defined under the application &lt;message-bundle&gt; element in
     * the faces config. Respects Locale
     * @param key string message key
     * @return Resource value or placeholder error String
     */
    public static String getStringFromBundle(String key) {
        ResourceBundle bundle = getBundle();
        return getStringSafely(bundle, key, null);
    }

    /**
     * Convenience method to construct a <code>FacesMesssage</code>
     * from a defined error key and severity
     * This assumes that the error keys follow the convention of
     * using <b>_detail</b> for the detailed part of the
     * message, otherwise the main message is returned for the
     * detail as well.
     * @param key for the error message in the resource bundle
     * @param severity severity of message
     * @return Faces Message object
     */
    public static FacesMessage getMessageFromBundle(String key, FacesMessage.Severity severity) {
        ResourceBundle bundle = getBundle();
        String summary = getStringSafely(bundle, key, null);
        String detail = getStringSafely(bundle, key + "_detail", summary);
        FacesMessage message = new FacesMessage(summary, detail);
        message.setSeverity(severity);
        return message;
    }

    /**
     * Add JSF info message.
     * @param msg info message string
     */
    public static void addFacesInformationMessage(String msg) {
        FacesContext ctx = getFacesContext();
        FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, "");
        ctx.addMessage(getRootViewComponentId(), fm);
    }
    
    /**
     * Add JSF warning message.
     * @param msg error message string
     */
    public static void addFacesWarningMessage(String msg) {
        FacesContext ctx = getFacesContext();
        FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, msg, "");
        ctx.addMessage(getRootViewComponentId(), fm);
    }

    /**
     * Add JSF error message.
     * @param msg error message string
     */
    public static void addFacesErrorMessage(String msg) {
        FacesContext ctx = getFacesContext();
        FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, "");
        ctx.addMessage(getRootViewComponentId(), fm);
    }

    /**
     * Add JSF error message for a specific attribute.
     * @param attrName name of attribute
     * @param msg error message string
     */
    public static void addFacesErrorMessage(String attrName, String msg) {
        FacesContext ctx = getFacesContext();
        FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, attrName, msg);
        ctx.addMessage(getRootViewComponentId(), fm);
    }
    
    public static void addTokenizedMessage(final String message, final Object[] args, FacesMessage.Severity severity) {
        if (severity == null) {
            severity = FacesMessage.SEVERITY_ERROR;
        }
        final String formattedMessage = MessageFormat.format(message, args);
        if (FacesMessage.SEVERITY_ERROR.equals(severity)) {
            addFacesErrorMessage(formattedMessage);
        } else if (FacesMessage.SEVERITY_WARN.equals(severity)) {
            addFacesWarningMessage(formattedMessage);
        } else if (FacesMessage.SEVERITY_INFO.equals(severity)) {
            addFacesInformationMessage(formattedMessage);
        }
    }

    /**
     * Get view id of the view root.
     * @return view id of the view root
     */
    public static String getRootViewId() {
        return getFacesContext().getViewRoot().getViewId();
    }

    /**
     * Get component id of the view root.
     * @return component id of the view root
     */
    public static String getRootViewComponentId() {
        return getFacesContext().getViewRoot().getId();
    }

    /**
     * Get FacesContext.
     * @return FacesContext
     */
    public static FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    private static ResourceBundle getBundle() {
        FacesContext ctx = getFacesContext();
        UIViewRoot uiRoot = ctx.getViewRoot();
        Locale locale = uiRoot.getLocale();
        ClassLoader ldr = Thread.currentThread().getContextClassLoader();
        return ResourceBundle.getBundle(ctx.getApplication().getMessageBundle(), locale, ldr);
    }

    /**
     * Get an HTTP Request attribute.
     * @param name attribute name
     * @return attribute value
     */
    public static Object getRequestAttribute(String name) {
        return getFacesContext().getExternalContext().getRequestMap().get(name);
    }

    /**
     * Set an HTTP Request attribute.
     * @param name attribute name
     * @param value attribute value
     */
    public static void setRequestAttribute(String name, Object value) {
        getFacesContext().getExternalContext().getRequestMap().put(name, value);
    }

    private static String getStringSafely(ResourceBundle bundle, String key, String defaultValue) {
        String resource = null;
        try {
            resource = bundle.getString(key);
        } catch (MissingResourceException mrex) {
            if (defaultValue != null) {
                resource = defaultValue;
            } else {
                resource = NO_RESOURCE_FOUND + key;
            }
        }
        return resource;
    }

    /**
     * Locate an UIComponent in view root with its component id. Use a recursive way to achieve this.
     * @param id UIComponent id
     * @return UIComponent object
     */
    public static UIComponent findComponentInRoot(String id) {
        UIComponent component = null;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            UIComponent root = facesContext.getViewRoot();
            component = findComponent(root, id);
        }
        return component;
    }

    /**
     * Locate an UIComponent from its root component.
     * Taken from http://www.jroller.com/page/mert?entry=how_to_find_a_uicomponent
     * @param base root Component (parent)
     * @param id UIComponent id
     * @return UIComponent object
     */
    //public static UIComponent findComponent(UIComponent base, String id) {
    public static UIComponent findComponent(UIComponent component, String name) {
        /*if (id.equals(base.getId()))
            return base;

        /*UIComponent children = null;
        UIComponent result = null;
        Iterator childrens = base.getFacetsAndChildren();
        while (childrens.hasNext() && (result == null)) {
            children = (UIComponent) childrens.next();
            if (id.equals(children.getId())) {
                result = children;
                break;
            }
            result = findComponent(children, id);
            if (result != null) {
                break;
            }
        }
        return result;*/
        if (name.equals(component.getId()))
            return component;
        /*if (component != null)
                    System.out.println(component.getId());  */

        List<UIComponent> items = component.getChildren();
        Iterator<UIComponent> facets = component.getFacetsAndChildren();

        if (items.size() > 0) {
            //System.out.println("got childern");
            for (UIComponent item : items) {
                UIComponent found = findComponent(item, name);
                if (found != null) {
                    return found;
                }
                if (item.getId().equalsIgnoreCase(name)) {
                    return item;
                }
            }
        } else if (facets.hasNext()) {
            //System.out.println("got facets");
            while (facets.hasNext()) {
                UIComponent facet = facets.next();
                UIComponent found = findComponent(facet, name);
                if (found != null) {
                    return found;
                }
                if (facet.getId().equalsIgnoreCase(name)) {
                    return facet;
                }
            }
        }
        return null;
    }

    /**
     * Method to create a redirect URL. The assumption is that the JSF servlet mapping is
     * "faces", which is the default
     *
     * @param view the JSP or JSPX page to redirect to
     * @return a URL to redirect to
     */
    public static String getPageURL(String view) {
        FacesContext facesContext = getFacesContext();
        ExternalContext externalContext = facesContext.getExternalContext();
        String url = ((HttpServletRequest)externalContext.getRequest()).getRequestURL().toString();
        StringBuffer newUrlBuffer = new StringBuffer();
        newUrlBuffer.append(url.substring(0, url.lastIndexOf("faces/")));
        newUrlBuffer.append("faces");
        String targetPageUrl = view.startsWith("/") ? view : "/" + view;
        newUrlBuffer.append(targetPageUrl);
        return newUrlBuffer.toString();
    }

    public void addMessage(String messageKey) {
        addMessage(messageKey, null);
    }

    public void addMessage(String messageKey, Object[] arguments) {
        addMessage(messageKey, arguments, FacesMessage.SEVERITY_INFO);
    }

    public void addWarning(String messageKey) {
        addWarning(messageKey, null);
    }

    public void addWarning(String messageKey, Object[] arguments) {
        addMessage(messageKey, arguments, FacesMessage.SEVERITY_WARN);
    }

    public void addError(String messageKey) {
        addError(messageKey, (Object[])null);
    }

    public void addError(String componentId, String messageKey) {
        addMessage(componentId, messageKey, null, FacesMessage.SEVERITY_ERROR);
    }

    public void addError(String componentId, String messageKey, Object[] arguments) {
        addMessage(componentId, messageKey, arguments, FacesMessage.SEVERITY_ERROR);
    }

    /**
     * This adds a Faces Message of type error with the message text as specfied
     * @param messageText
     */
    public void addFormattedError(String messageText) {
        addFormattedMessage(null, messageText, FacesMessage.SEVERITY_ERROR);
    }

    /**
     * This adds a Faces Message of type error with the message text as specfied
     * @param messageText
     */
    public void addFormattedError(String componentId, String messageText) {
        addFormattedMessage(componentId, messageText, FacesMessage.SEVERITY_ERROR);
    }

    /**
     * This adds a Faces Message of type warning with the message text as specfied
     * @param messageText
     */
    public void addFormattedWarning(String messageText) {
        addFormattedMessage(null, messageText, FacesMessage.SEVERITY_WARN);
    }

    /**
     * This adds a Faces Message of type info with the message text as specfied
     * @param messageText
     */
    public void addFormattedInfo(String messageText) {
        addFormattedMessage(null, messageText, FacesMessage.SEVERITY_INFO);
    }

    /**
     * This adds a Faces Message with the message text as specfied
     * @param componentId
     * @param messageText
     * @param severity
     */
    public void addFormattedMessage(String componentId, String messageText, FacesMessage.Severity severity) {
        FacesMessage message = new FacesMessage(messageText);
        message.setSeverity(severity);
        FacesContext.getCurrentInstance().addMessage(componentId, message);
    }

    /**
     * Returns true when maximum severity is error
     * @return
     */
    public boolean isHasErrors() {
        FacesMessage.Severity severity = FacesContext.getCurrentInstance().getMaximumSeverity();
        return severity == FacesMessage.SEVERITY_ERROR || severity == FacesMessage.SEVERITY_FATAL;
    }


    /**
     * Returns true when maximum severity is warning
     * @return
     */
    public boolean isHasWarnings() {
        FacesMessage.Severity severity = FacesContext.getCurrentInstance().getMaximumSeverity();
        return severity == FacesMessage.SEVERITY_WARN;
    }

    /**
     * Returns true when maximum severity is info
     * @return
     */
    public boolean isHasInfo() {
        FacesMessage.Severity severity = FacesContext.getCurrentInstance().getMaximumSeverity();
        return severity == FacesMessage.SEVERITY_INFO;
    }

    public void addError(String messageKey, Object[] arguments) {
        addMessage(messageKey, arguments, FacesMessage.SEVERITY_ERROR);
    }

    public void addFatalError(String messageKey) {
        addFatalError(messageKey, null);
    }

    public void addFatalError(String messageKey, Object[] arguments) {
        addMessage(messageKey, arguments, FacesMessage.SEVERITY_FATAL);
    }

    public void addMessage(String messageKey, Object[] arguments, FacesMessage.Severity severity) {
        addMessage(null, messageKey, arguments, severity);
    }

    public void addMessage(String componentId, String messageKey, Object[] arguments, FacesMessage.Severity severity) {
        FacesMessage message = MessageFactory.getMessage(messageKey, arguments);
        message.setSeverity(severity);
        FacesContext.getCurrentInstance().addMessage(componentId, message);
    }

    public Locale getLocale() {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
    }

    /**
     * Defined as static because it is used in getInstance()
     * @return
     */
    public static Application getApplication() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            return FacesContext.getCurrentInstance().getApplication();
        } else {
            ApplicationFactory afactory = (ApplicationFactory)FactoryFinder.getFactory(APPLICATION_FACTORY_KEY);
            return afactory.getApplication();
        }
    }

    /**
     * Evaluates JSF EL expression and returns the value. If the expression
     * does not start with "#{" it is assumed to be a literal value, and
     * the value returned will be the same as passed in.
     * <p>
     * Defined as static because it is used in getInstance()
     * @param jsfExpression
     * @return
     */
    @SuppressWarnings("oracle.jdeveloper.java.semantic-warning")
    public static Object getExpressionValue(String jsfExpression) {
        // when specifying EL expression in managed bean as "literal" value
        // so it can be evaluated later, the # is replaced with $, quite strange
        if (jsfExpression == null) {
            return jsfExpression;
        }
        if (jsfExpression.startsWith("${")) {
            jsfExpression = "#{" + jsfExpression.substring(2);
        }
        if (!jsfExpression.startsWith("#{")) {
            if (jsfExpression.equalsIgnoreCase("true")) {
                return Boolean.TRUE;
            } else if (jsfExpression.equalsIgnoreCase("false")) {
                return Boolean.FALSE;
            }
            // there can be literal text preceding the expression...
            else if (jsfExpression.indexOf("#{") < 0) {
                return jsfExpression;
            }
        }
        return getApplication().createValueBinding(jsfExpression).getValue(getFacesContext());
    }

    public void resetComponentTree() {
        UIComponent component = findComponentInRoot("dataForm");
        if (component != null) {
            LOGGER.fine("Executing resetComponentTree of dataForm");
            resetComponentTree(component);
        }
    }

    public void resetComponentTree(UIComponent component) {
        UIComponent form = getContainingForm(component);
        resetChildren(form);
    }

    public UIComponent getContainingForm(UIComponent component) {
        UIComponent previous = component;
        for (UIComponent parent = component.getParent(); parent != null; parent = parent.getParent()) {
            if ((parent instanceof UIForm) || (parent instanceof UIXForm) || (parent instanceof UIXSubform))
                return parent;
            previous = parent;
        }

        return previous;
    }

    public void setImmediateItemsValid(UIComponent comp) {
        UIComponent kid;
        for (Iterator kids = comp.getFacetsAndChildren(); kids.hasNext(); resetChildren(kid)) {
            kid = (UIComponent)kids.next();
            if (kid instanceof EditableValueHolder) {
                EditableValueHolder item = (EditableValueHolder)kid;
                if (item.isImmediate()) {
                    item.setValid(true);
                }
            }
        }
    }


    public void resetChildren(UIComponent comp) {
        UIComponent kid;
        for (Iterator kids = comp.getFacetsAndChildren(); kids.hasNext(); resetChildren(kid)) {
            kid = (UIComponent)kids.next();
            if (kid instanceof UIXEditableValue) {
                ((UIXEditableValue)kid).resetValue();
                continue;
            }
            if (kid instanceof EditableValueHolder) {
                resetEditableValueHolder((EditableValueHolder)kid);
                continue;
            }
            if (kid instanceof UIXCollection)
                ((UIXCollection)kid).resetStampState();
        }

    }

    public void resetEditableValueHolder(EditableValueHolder evh) {
        evh.setValue(null);
        evh.setSubmittedValue(null);
        evh.setLocalValueSet(false);
        evh.setValid(true);
    }

    public void resetComponent(UIComponent comp) {
        if (comp instanceof EditableValueHolder) {
            resetEditableValueHolder((EditableValueHolder)comp);
        }
    }


    public static UIViewRoot getViewRoot() {
        if (getFacesContext() != null) {
            return getFacesContext().getViewRoot();
        }
        return null;
    }

    /**
     * Convenience method for setting Request attributes.
     * @param key object key
     * @param object value to store
     */
    public static void storeOnRequest(String key, Object object) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        Map request = ctx.getExternalContext().getRequestMap();
        request.put(key, object);
    }

    /**
     * Get Http request from external context. Return null if request is not a HttpServletRequest
     * @return
     */
    public static HttpServletRequest getHTTPServletRequest() {
        Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();
        if (request instanceof HttpServletRequest) {
            return (HttpServletRequest)request;
        }
        return null;
    }

    /**
     * Get Http response from external context. Return null if response is not a HttpServletResponse
     * @return
     */
    public static HttpServletResponse getHttpServletResponse() {
        Object response = FacesContext.getCurrentInstance().getExternalContext().getResponse();
        if (response instanceof HttpServletResponse) {
            return (HttpServletResponse)response;
        }
        return null;
    }


    public boolean isPPRRequest() {
        return "true".equals(getFacesContext().getExternalContext().getRequestParameterMap().get("partial"));
    }

    /**
     * Does request contain parameter event with value equal to
     * the eventName argument?
     * @param eventName
     * @return
     */
    public boolean requestContainsEvent(String eventName) {
        return eventName.equals(getFacesContext().getExternalContext().getRequestParameterMap().get("event"));
    }

    /**
     * Does request contain parameter event with value equal to
     * the eventName argument, and a parameter named source with
     * value equal to source argument.
     * @param eventName
     * @return
     */
    public boolean requestContainsEventWithSource(String eventName, String source) {
        if (source == null) {
            return requestContainsEvent(eventName);
        }
        return requestContainsEvent(eventName) &&
            source.equals(getFacesContext().getExternalContext().getRequestParameterMap().get("source"));
    }


    public boolean isPostBack() {
        Boolean pb = (Boolean)getExpressionValue("#{adfFacesContext.postback}");
        if (pb != null) {
            return pb.booleanValue();
        }
        return false;
    }

    /**
     * Reverses boolean expression by adding ! in front of the expression
     * @param expression
     * @return
     */
    public static String reverseBooleanExpression(String expression) {
        if (expression == null) {
            return null;
        }
        String newExpr = "!(" + stripBracketsExpression(expression) + ")";
        return addBracketsExpression(newExpr);
    }

    public static String concatWithAndExpression(String expression1, String expression2) {
        return concatWithExpression(expression1, expression2, "and");
    }

    public static String concatWithOrExpression(String expression1, String expression2) {
        return concatWithExpression(expression1, expression2, "or");
    }

    public static String concatWithExpression(String expression1, String expression2, String operator) {
        if (expression1 == null && expression2 == null) {
            return null;
        } else if (expression1 == null) {
            return expression2;
        } else if (expression2 == null) {
            return expression1;
        }
        String newExpr =
            "(" + stripBracketsExpression(expression1) + ") " + operator + " (" + stripBracketsExpression(expression2) +
            ")";
        return addBracketsExpression(newExpr);
    }

    /**
     * Removes "#{" at start of expression, and "}" at end of expression.
     * @param expression
     * @return
     */
    public static String stripBracketsExpression(String expression) {
        if (expression == null) {
            return null;
        }
        expression = expression.replaceAll("\\$\\{", "");
        expression = expression.replaceAll("#\\{", "");
        expression = expression.replaceAll("\\}", "");
        return expression.trim();
    }

    public static String stripBracketsAndQuotesExpression(String expression) {
        return stripBracketsExpression(stripQuotesExpression(expression));
    }

    public static String stripQuotesExpression(String expression) {
        if (expression == null) {
            return null;
        }
        expression = expression.replaceAll("'", "");
        return expression.trim();
    }

    /**
     * Adds "#{" at start of expression, and "}" at end of expression.
     * @param expression
     * @return
     */
    public static String addBracketsExpression(String expression) {
        if (expression == null) {
            return null;
        }
        return "#{" + expression.trim() + "}";
    }

    public static String addBracketsAndQuotesExpression(String expression) {
        return addBracketsExpression(addQuotesExpression(expression));
    }

    public static String addQuotesExpression(String expression) {
        if (expression == null) {
            return null;
        }
        return "'" + expression.trim() + "'";
    }

    public static void redirect(String redirectUrl) {
        try {
            ExternalContext ext = FacesContext.getCurrentInstance().getExternalContext();
            ext.redirect(redirectUrl);
        } catch (IOException e) {
            LOGGER.severe("Error redirecting to " + redirectUrl + ": " + e.getMessage());
        }
    }

    public static void redirectToSelf() {
        FacesContext fctx = FacesContext.getCurrentInstance();
        ExternalContext ectx = fctx.getExternalContext();
        String viewId = fctx.getViewRoot().getViewId();
        ControllerContext controllerCtx = null;
        controllerCtx = ControllerContext.getInstance();
        String activityURL = controllerCtx.getGlobalViewActivityURL(viewId);
        try {
            ectx.redirect(activityURL);
            fctx.responseComplete();
        } catch (IOException e) {
            //Can't redirect
            LOGGER.severe("Error redirecting to self (" + activityURL + "): " + e.getMessage());
            fctx.renderResponse();
        }
    }

    public boolean isValidExpression(String expression) {
        boolean valid = true;
        try {
            getExpressionValue(expression);
        } catch (Exception ex) {
            valid = false;
        }
        return valid;
    }

    public PhaseId getLastPhaseId() {
        return (PhaseId)getFromRequest("oracle.adfinternal.view.faces.context.LAST_PHASE_ID");
    }

    public PhaseId getCurrentPhaseId() {
        return (PhaseId)getFromRequest("oracle.adfinternal.view.faces.context.CURRENT_PHASE_ID");
    }


    /**
     * Return the pageFlowScoped map that holds the createModes for a form page
     * @return
     */
    public Map getCreateModesMap() {
        Map createModes = (Map)AdfFacesContext.getCurrentInstance().getPageFlowScope().get(CREATE_MODES_KEY);
        if (createModes == null) {
            createModes = new HashMap();
            AdfFacesContext.getCurrentInstance().getPageFlowScope().put(CREATE_MODES_KEY, createModes);
        }
        return createModes;
    }

    /**
     * Programmatic invocation of a method that an EL evaluates
     * to. The method must not take any parameters.
     *
     * @param methodExpression EL of the method to invoke
     * @return Object that the method returns
     */
    public static Object invokeELMethod(String methodExpression) {
        return invokeELMethod(methodExpression, new Class[0], new Object[0]);
    }

    public static Object invokeMethodExpression(final String expression, Class returnType, Class argTypes,
                                                Object argValues) {
        return resolveMethodExpression(expression, returnType, new Class[] { argTypes }, new Object[] { argValues });
    }

    /**
     * Programmatic invocation of a method that an EL evaluates to.
     *
     * @param methodExpression EL of the method to invoke
     * @param paramTypes Array of Class defining the types of the
     * parameters
     * @param params Array of Object defining the values of the
     * parametrs
     * @return Object that the method returns
     */
    public static Object invokeELMethod(String methodExpression, Class[] paramTypes, Object[] params) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();
        MethodExpression exp = getMethodExpression(methodExpression, null, paramTypes);
        return exp.invoke(elContext, params);
    }

    public static MethodExpression getMethodExpression(String methodExpression, Class returnType, Class[] paramTypes) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();
        ExpressionFactory expressionFactory = facesContext.getApplication().getExpressionFactory();
        MethodExpression exp =
            expressionFactory.createMethodExpression(elContext, methodExpression, returnType, paramTypes);
        return exp;
    }


    public static UIComponent findComponentMatchingClientId(String clientCompId) {
        FacesContext context = FacesContext.getCurrentInstance();
        UIComponent root = context.getViewRoot();
        return findComponentMatchingClientId(root, clientCompId);
    }

    /**
     * Method to parse the active component clientId to identify the UIComponent
     * instance.
     * Code based on sample from Frank Nimphius on ADF Code Corner
     *
     * @param startComp The top-level component where we start finding.
     * @param clientCompId clientId or ClientLocatorId. A clientId contains all the
     *        naming comntainers between the document root and the UI component. A
     *        client locator also contains a row indes if the component is part of a
     *        table rendering
     * @return
     */
    public static UIComponent findComponentMatchingClientId(UIComponent startComp, String clientCompId) {

        String components[] = { };
        if (clientCompId != null) {
            components = clientCompId.split(":");
        }
        UIComponent component = null;

        //get the component

        if (components.length > 0) {
            String componentId = components[0];
            component = startComp.findComponent(componentId);

            if (component != null) {
                for (int i = 1; i < components.length; ++i) {
                    //if the component is in a table, then the clientId
                    //contains an integer vaue that indicates the row index
                    //to parse this out, we use a try/catch block
                    try {
                        Integer.parseInt(components[i]);
                    } catch (NumberFormatException nf) {
                        //the id is not a number, so lets try a get the component
                        if (component != null) {
                            component = findComponent(component, components[i]);
                        }
                    }
                }
            }
        }

        //if we are here then we have a component or null
        return component;
    }

    public static RichRegion findParentRegion(UIComponent component) {
        UIComponent parent = component.getParent();
        RichRegion region = null;
        while (parent != null) {
            if (parent instanceof RichRegion) {
                region = (RichRegion)parent;
                break;
            }
            parent = parent.getParent();
        }
        return region;
    }

    /**
     * If an action event is queued through the use of a function key, and the component of the event
     * has immediate propert set to true, we need to skip the Process Validations phase, and jump to render response right away.
     * Otherwise, we might get validation errors after the associated action(listener) has been executed.
     * The validation and model update phases are not skipped automatically because
     * the immediate property used to queue the event from the client, using AdfCustomEvent.queue only determines
     * the JSF phase, but does not skip phases like a server side event with immediate=true.
     * @param event
     */
    public static void jumpToRenderResponseIfNeeded(ActionEvent event) {
        if (event != null) {
            UIComponent comp = event.getComponent();
            if (comp instanceof UIXCommand && ((UIXCommand)comp).isImmediate()) {
                FacesContext.getCurrentInstance().renderResponse();
            }
        }
    }

    /**
     * Public convenience method to return ADF Faces Context
     * @return
     */
    public static AdfFacesContext getADFFacesContext() {
        AdfFacesContext ctx = AdfFacesContext.getCurrentInstance();
        return ctx;
    }

    /**
     * Public convenience method to return External Context
     * @return
     */
    public static ExternalContext getExternalContext() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return ctx.getExternalContext();
    }

    /**
     * Public convenience method to refresh an ADF UI component. The parameter can be any ADF Rich component
     * @param uicomponent
     */
    public static void addPartialTarget(UIComponent uicomponent) {
        if (uicomponent != null) {
            getADFFacesContext().addPartialTarget(uicomponent);
        }
    }

    public static Object getUIComponentAttribute(UIComponent uicomponent, String attributeName) {
        Object retValue = null;
        if (uicomponent != null) {
            Map attributeMap = uicomponent.getAttributes();
            if (attributeMap != null && attributeName != null) {
                retValue = attributeMap.get(attributeName);
            }
        }
        return retValue;
    }

    public static ValueExpression getValueExpression(final String expression) {
        final ELContext elContext = getFacesContext().getELContext();
        final Application app = getFacesContext().getApplication();
        final ExpressionFactory elFactory = app.getExpressionFactory();
        return elFactory.createValueExpression(elContext, expression, Object.class);
    }

    public static void writeJavaScriptToClient(String script) {
        final FacesContext fctx = getFacesContext();
        ExtendedRenderKitService erks = Service.getRenderKitService(fctx, ExtendedRenderKitService.class);
        erks.addScript(fctx, script);
    }

    /**
     *
     * @param errType
     * @param key
     * @param severity
     * @return
     */
    public static FacesMessage getMessageFromBundle(String errType, String key, FacesMessage.Severity severity) {
        ResourceBundle bundle = getBundle();
        String summary = getStringSafely(bundle, key + "_summary", null);
        String detail = getStringSafely(bundle, key + "_detail", summary);
        FacesMessage message;
        if (errType != null)
            message = new FacesMessage(summary + " " + errType, detail);
        else
            message = new FacesMessage(summary, detail);

        message.setSeverity(severity);
        return message;
    }

    /**
     * This public method returns the String value for the corresponding key
     * from the language bundle file.
     * @param key
     * @return String
     */
    public static String getDisplayMessageFromBundle(String key) {
        ResourceBundle bundle = getBundle();
        String displayMessage = getStringSafely(bundle, key, null);
        return displayMessage;
    }

    public static void updateModel(final ValueChangeEvent valueChangeEvent) {
        if (valueChangeEvent != null) {
            final UIComponent uiComponent = valueChangeEvent.getComponent();
            uiComponent.processUpdates(getFacesContext());
        }
    }

    /**
     * Convenience method for getting Request attributes.
     * @param key object key
     */
    public static Object getRequestParam(String key) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        Map request = ctx.getExternalContext().getRequestParameterMap();
        return request.get(key);
    }
}