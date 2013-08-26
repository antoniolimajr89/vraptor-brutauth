package br.com.caelum.brutauth.interceptors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.vidageek.mirror.dsl.Mirror;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import br.com.caelum.brutauth.auth.annotations.CustomBrutauthRules;
import br.com.caelum.brutauth.auth.handlers.AccessNotAllowedHandler;
import br.com.caelum.brutauth.auth.handlers.HandlerSearcher;
import br.com.caelum.brutauth.auth.rules.CustomBrutauthRule;
import br.com.caelum.brutauth.interceptors.CustomBrutauthRuleInterceptorTest.TrueCustomRule;
import br.com.caelum.brutauth.reflection.MethodInvoker;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.core.MethodInfo;
import br.com.caelum.vraptor.ioc.Container;
import br.com.caelum.vraptor.resource.DefaultResourceClass;
import br.com.caelum.vraptor.resource.DefaultResourceMethod;
import br.com.caelum.vraptor.resource.ResourceMethod;
import br.com.caelum.vraptor.util.test.MockResult;

@RunWith(MockitoJUnitRunner.class)
public class CustomBrutauthRuleInterceptorTest {

	@Mock
	private Container container;
	@Mock
	private HandlerSearcher handlers;
	@Mock
	private InterceptorStack stack;
	@Mock
	private MethodInfo methodInfo;
	@Mock
	private MethodInvoker invoker;

	private MyController controller;
	private CustomBrutauthRuleInterceptor interceptor;
	private MyCustomRule customRule;
	private AnotherCustomRule anotherCustomRule;
	private AccessNotAllowedHandler handler;
	private ResourceMethod singleRuleControllerMethod;
	private ResourceMethod manyRulesControllerMethod;

	@Before
	public void setUp() throws Exception {
		controller = new MyController();
		singleRuleControllerMethod = method(MyController.class, "myCustomRuleMethod");
		manyRulesControllerMethod = method(MyController.class, "myManyCustomRulesMethod");
		interceptor = new CustomBrutauthRuleInterceptor(container, methodInfo, invoker, handlers);
		customRule = spy(new MyCustomRule());
		anotherCustomRule = spy(new AnotherCustomRule());
		handler = spy(new AccessNotAllowedHandler(new MockResult()));

		when(container.instanceFor(MyCustomRule.class)).thenReturn(customRule);
		when(container.instanceFor(AnotherCustomRule.class)).thenReturn(anotherCustomRule);
		when(handlers.getHandler(any(CustomBrutauthRule.class))).thenReturn(handler);
		when(invoker.invoke(any(CustomBrutauthRule.class), any(Object[].class))).then(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				Object rule = invocation.getArguments()[0];
				Object[] args = (Object[]) invocation.getArguments()[1];
				return (Boolean) new Mirror().on(rule).invoke().method("isAllowed").withArgs(args);
			}
		});
	}
	@Test
	public void should_stop_stack_if_rule_says_so() throws Exception {
		when(methodInfo.getParameters()).thenReturn(new Object[] { MyController.UNNACCEPTABLE_STRING });

		assertTrue("should accept myCustomRuleMethod", interceptor.accepts(singleRuleControllerMethod));
		interceptor.intercept(stack, singleRuleControllerMethod, controller);

		verify(stack, never()).next(singleRuleControllerMethod, controller);
	}
	@Test
	public void should_continue_stack_if_rule_allows_access() throws Exception {
		when(methodInfo.getParameters()).thenReturn(new Object[] { MyController.MY_STRING });

		assertTrue("should accept myCustomRuleMethod", interceptor.accepts(singleRuleControllerMethod));
		interceptor.intercept(stack, singleRuleControllerMethod, controller);

		verify(stack).next(singleRuleControllerMethod, controller);
	}
	@Test
	public void should_invoke_handler_if_not_allowed() throws Exception {
		when(methodInfo.getParameters()).thenReturn(new Object[] { MyController.UNNACCEPTABLE_STRING });

		assertTrue("should accept myCustomRuleMethod", interceptor.accepts(singleRuleControllerMethod));
		interceptor.intercept(stack, singleRuleControllerMethod, controller);

		verify(handler).handle();
	}
	@Test
	public void should_not_invoke_handler_if_allowed() throws Exception {
		when(methodInfo.getParameters()).thenReturn(new Object[] { MyController.MY_STRING });

		assertTrue("should accept myCustomRuleMethod", interceptor.accepts(singleRuleControllerMethod));
		interceptor.intercept(stack, singleRuleControllerMethod, controller);

		verify(handler, never()).handle();
	}
	@Test
	public void should_not_invoke_second_rule_if_first_fails() throws Exception {
		when(methodInfo.getParameters()).thenReturn(new Object[] { MyController.UNNACCEPTABLE_STRING });

		assertTrue("should accept myManyCustomRulesMethod", interceptor.accepts(manyRulesControllerMethod));
		interceptor.intercept(stack, manyRulesControllerMethod, controller);

		verify(anotherCustomRule, never()).isAllowed(anyString());
	}
	@Test
	public void should_invoke_second_rule_if_first_succeeds() throws Exception {
		when(methodInfo.getParameters()).thenReturn(new Object[] { MyController.MY_STRING });

		assertTrue("should accept myManyCustomRulesMethod", interceptor.accepts(manyRulesControllerMethod));
		interceptor.intercept(stack, manyRulesControllerMethod, controller);

		verify(anotherCustomRule).isAllowed(anyString());
	}
	@Test
	public void should_add_controllers_class_rules() throws Exception {
		ResourceMethod controllerWithRulesMethod = method(ControllerWithRules.class, "methodWithoutRules");
		
		when(methodInfo.getParameters()).thenReturn(new Object[] { MyController.MY_STRING });
		
		assertTrue("should accept ControllerWithRules", interceptor.accepts(controllerWithRulesMethod));
		interceptor.intercept(stack, controllerWithRulesMethod, controller);
		
		verify(customRule).isAllowed(anyString());
	}

	@Test
	public void should_add_controllers_class_and_method_rules() throws Exception {
		ResourceMethod controllerWithRulesMethod = method(ControllerWithRules.class, "methodWithRules");
		
		TrueCustomRule trueCustomRule = spy(new TrueCustomRule());
		when(container.instanceFor(TrueCustomRule.class)).thenReturn(trueCustomRule);
		when(methodInfo.getParameters()).thenReturn(new Object[] { MyController.MY_STRING });
		
		assertTrue("should accept ControllerWithRules", interceptor.accepts(controllerWithRulesMethod));
		interceptor.intercept(stack, controllerWithRulesMethod, controller);
		
		verify(customRule).isAllowed(anyString());
		verify(trueCustomRule).isAllowed(anyString());
	}

	private static ResourceMethod method(Class<?> clazz, String name) {
		return new DefaultResourceMethod(
				new DefaultResourceClass(clazz),
				new Mirror().on(clazz).reflect().method(name).withAnyArgs());
	}
	
	
	@CustomBrutauthRules({MyCustomRule.class})
	public class ControllerWithRules{
		
		public void methodWithoutRules(){
		}
		
		@CustomBrutauthRules(TrueCustomRule.class)
		public void methodWithRules(){
		}
	}
	
	
	public class TrueCustomRule implements CustomBrutauthRule{
		public boolean isAllowed(String string) {
			return true;
		}
	}
}
